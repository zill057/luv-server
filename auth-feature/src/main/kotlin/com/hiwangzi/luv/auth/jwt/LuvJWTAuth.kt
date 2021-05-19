package com.hiwangzi.luv.auth.jwt

import com.hiwangzi.luv.model.exception.ExpiredRefreshTokenException
import com.hiwangzi.luv.model.exception.ExpiredTokenException
import io.vertx.core.*
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import java.util.*

class LuvJWTAuth(vertx: Vertx, securityConfig: JsonObject) : JWTAuth {

  private val secretKey = securityConfig.getString("secret", UUID.randomUUID().toString())

  private val jwtAuth = JWTAuth.create(
    vertx,
    jwtAuthOptionsOf(pubSecKeys = listOf(pubSecKeyOptionsOf(algorithm = "HS256", buffer = Buffer.buffer(secretKey))))
  )

  override fun authenticate(credentials: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
    val accessToken = credentials.getString("token") ?: credentials.getString("password") ?: ""
    val promise = Promise.promise<User>()
    this.authenticateJWT(accessToken)
      .onSuccess { user ->
        if ("access" == user.get("type")) {
          promise.complete(user)
        } else {
          promise.fail(ExpiredTokenException(message = "Invalid access token"))
        }
      }
      .onFailure {
        promise.fail(ExpiredTokenException(message = it.message ?: "", cause = it))
      }
    promise.future().onComplete(resultHandler)
  }

  fun authenticateRefreshToken(refreshToken: String): Future<User> {
    val promise = Promise.promise<User>()
    this.authenticateJWT(refreshToken)
      .onSuccess { user ->
        if ("refresh" == user.get("type")) {
          promise.complete(user)
        } else {
          promise.fail(ExpiredRefreshTokenException(message = "Invalid refresh token"))
        }
      }
      .onFailure {
        promise.fail(ExpiredRefreshTokenException(message = it.message ?: "", cause = it))
      }
    return promise.future()
  }

  private fun authenticateJWT(jwt: String): Future<User> {
    val promise = Promise.promise<User>()
    jwtAuth.authenticate(jsonObjectOf(Pair("token", jwt))) { userR ->
      if (userR.succeeded()) {
        // TODO should check token whether in block list
        promise.complete(userR.result())
      } else {
        promise.fail(userR.cause())
      }
    }
    return promise.future()
  }

  override fun generateToken(claims: JsonObject): String {
    return this.generateToken(claims, JWTOptions())
  }

  override fun generateToken(claims: JsonObject, options: JWTOptions): String {
    claims.getString("iss") ?: claims.put("iss", "luv-server")
    return jwtAuth.generateToken(claims, options)
  }
}
