package com.hiwangzi.luv.auth.impl

import at.favre.lib.crypto.bcrypt.BCrypt
import com.hiwangzi.luv.auth.AuthFeature
import com.hiwangzi.luv.auth.http.LuvHTTPAuthenticationHandler
import com.hiwangzi.luv.auth.jwt.LuvJWTAuth
import com.hiwangzi.luv.database.service.UserDBServiceFactory
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.exception.ExpiredRefreshTokenException
import com.hiwangzi.luv.model.exception.InvalidCredentialExceptionLuv
import com.hiwangzi.luv.model.exception.SystemException
import com.hiwangzi.luv.model.resource.Authorization
import com.hiwangzi.luv.model.resource.Device
import com.hiwangzi.luv.model.resource.User
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import org.slf4j.LoggerFactory
import java.time.Instant

class AuthFeatureImpl(vertx: Vertx, securityConfig: JsonObject) : AuthFeature {

  private val logger = LoggerFactory.getLogger(AuthFeatureImpl::class.java)
  private val accessTokenExpiresInSeconds = securityConfig.getInteger("tokenExpiresInMinutes", 30) * 60
  private val refreshTokenExpiresInSeconds = securityConfig.getInteger("refreshTokenExpiresInMinutes", 10080) * 60
  private val userDBService = UserDBServiceFactory.createProxy(vertx)
  private val luvJwtAuth = LuvJWTAuth(vertx, securityConfig)

  override val stompAuthenticationProvider = luvJwtAuth
  override val httpAuthenticationHandler = LuvHTTPAuthenticationHandler(luvJwtAuth)

  override fun authenticate(accessToken: String): Future<io.vertx.ext.auth.User> {
    val promise = Promise.promise<io.vertx.ext.auth.User>()
    luvJwtAuth.authenticate(jsonObjectOf(Pair("token", accessToken)), promise::handle)
    return promise.future()
  }

  override fun authenticateRefreshToken(refreshToken: String): Future<io.vertx.ext.auth.User> {
    val promise = Promise.promise<io.vertx.ext.auth.User>()
    luvJwtAuth.authenticate(jsonObjectOf(Pair("token", refreshToken))) { userR ->
      if (userR.succeeded()) {
        val user = userR.result()
        if (user.get("refresh")) {
          promise.complete(user)
        } else {
          promise.fail(ExpiredRefreshTokenException(message = "Invalid refresh token"))
        }
      } else {
        val cause = userR.cause()
        promise.fail(ExpiredRefreshTokenException(message = cause.message ?: "", cause = cause))
      }
    }
    return promise.future()
  }

  override fun generateAuthorization(
    platformId: String,
    device: Device,
    identityType: UserIdentityType,
    identifier: String,
    credential: String
  ): Future<Authorization> {
    val promise = Promise.promise<Authorization>()
    val trimmedPlatformId = platformId.trim()
    val trimmedIdentifier = identifier.trim()
    val trimmedCredential = credential.trim()
    userDBService.findUserInformation(trimmedPlatformId, identityType, trimmedIdentifier) { userInfoR ->
      // system error
      if (userInfoR.failed()) {
        promise.fail(SystemException(cause = userInfoR.cause()))
        logger.error("Unexpected error happens at generateAuthorization", userInfoR.cause())
        return@findUserInformation
      }
      // user not found
      val userInformation = userInfoR.result()
      if (userInformation == null) {
        promise.fail(InvalidCredentialExceptionLuv())
        logger.warn(
          "User not found at generateAuthorization, platformId: $trimmedPlatformId, " +
            "identityType: $identityType, _identifier: $trimmedIdentifier"
        )
        return@findUserInformation
      }
      // wrong password
      val user = userInformation.user
      val identity = userInformation.identity
      val notPassed = !this.verifyPassword(
        plainCredential = trimmedCredential, hashedCredential = identity.hashedCredential
      )
      if (notPassed) {
        promise.fail(InvalidCredentialExceptionLuv())
        logger.warn(
          "Password not match at generateAuthorization, platformId: $trimmedPlatformId, " +
            "identityType: $identityType, _identifier: $trimmedIdentifier"
        )
        return@findUserInformation
      }
      // right password
      this.generateToken(user, device).onComplete(promise::handle)
    }
    return promise.future()
  }

  override fun refreshAuthorization(
    platformId: String,
    device: Device,
    refreshToken: String
  ): Future<Authorization> {
    val promise = Promise.promise<Authorization>()
    val trimmedPlatformId = platformId.trim()
    this.authenticateRefreshToken(refreshToken)
      .onFailure(promise::fail)
      .onSuccess { vertxUser ->
        val userId: String = vertxUser.get("sub")
        userDBService.findUserById(trimmedPlatformId, userId) { userR ->
          // system error
          if (userR.failed()) {
            promise.fail(SystemException(cause = userR.cause()))
            logger.error("Unexpected error happens at refreshAuthorization", userR.cause())
            return@findUserById
          }
          // user not found
          val user = userR.result()
          if (user == null) {
            promise.fail(InvalidCredentialExceptionLuv())
            logger.warn("User not found at refreshAuthorization, platformId: $trimmedPlatformId, userId: $userId")
            return@findUserById
          }
          // generate a new token now
          this.generateToken(user, device).onComplete(promise::handle)
        }
      }
    return promise.future()
  }

  override fun revokeAuthorization(platformId: String, userId: String, authorizationId: String): Future<Void> {
    val promise = Promise.promise<Void>()
    userDBService.revokeUserAuthorization(userId, authorizationId, promise::handle)
    return promise.future()
  }

  private fun generateToken(user: User, device: Device): Future<Authorization> {
    val promise = Promise.promise<Authorization>()
    val issuedAt = Instant.now().epochSecond
    val accessTokenExpiredAt = issuedAt + accessTokenExpiresInSeconds
    val refreshTokenExpiredAt = issuedAt + refreshTokenExpiresInSeconds
    val accessToken = luvJwtAuth.generateToken(
      jsonObjectOf(
        Pair("iss", "luv-server"), Pair("sub", user.id), Pair("nbf", issuedAt), Pair("iat", issuedAt),
        Pair("exp", accessTokenExpiredAt),
      )
    )
    val refreshToken = luvJwtAuth.generateToken(
      jsonObjectOf(
        Pair("iss", "luv-server"), Pair("sub", user.id), Pair("nbf", issuedAt), Pair("iat", issuedAt),
        Pair("exp", refreshTokenExpiredAt), Pair("refresh", true)
      )
    )
    userDBService.saveUserAuthorization(
      userId = user.id,
      accessToken = accessToken,
      refreshToken = refreshToken,
      device = device,
      issuedAt = issuedAt * 1000,
      accessTokenExpiredAt = accessTokenExpiredAt * 1000,
      refreshTokenExpiredAt = refreshTokenExpiredAt * 1000,
    ) { authIdR ->
      if (authIdR.failed()) {
        promise.fail(SystemException())
        logger.error("Unexpected error happens at generateToken", authIdR.cause())
      } else {
        promise.complete(Authorization(authIdR.result(), accessToken, refreshToken, user))
      }
    }
    return promise.future()
  }

  private fun verifyPassword(plainCredential: String, hashedCredential: String): Boolean {
    return BCrypt.verifyer(BCrypt.Version.VERSION_2A)
      .verify(plainCredential.toByteArray(), hashedCredential.toByteArray())
      .verified
  }
}
