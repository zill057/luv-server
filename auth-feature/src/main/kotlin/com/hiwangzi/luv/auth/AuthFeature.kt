package com.hiwangzi.luv.auth

import com.hiwangzi.luv.auth.impl.AuthFeatureImpl
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.resource.Authorization
import com.hiwangzi.luv.model.resource.Device
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.web.handler.AuthenticationHandler

fun authFeatureOf(vertx: Vertx, securityConfig: JsonObject): AuthFeature {
  return AuthFeatureImpl(vertx, securityConfig)
}

interface AuthFeature {

  val stompAuthenticationProvider: AuthenticationProvider
  val httpAuthenticationHandler: AuthenticationHandler

  fun authenticate(accessToken: String): Future<io.vertx.ext.auth.User>

  fun authenticateRefreshToken(refreshToken: String): Future<io.vertx.ext.auth.User>

  /**
   * Generate a new token.
   *
   * @param platformId Platform client's ID
   * @param device User's device
   * @param identityType Identify type
   * @param identifier Identifier, e.g. username, phone
   * @param credential Credential for identifier, e.g. password
   *
   * @see com.hiwangzi.luv.model.resource.Authorization
   */
  fun generateAuthorization(
    platformId: String,
    device: Device,
    identityType: UserIdentityType,
    identifier: String,
    credential: String
  ): Future<Authorization>

  fun refreshAuthorization(
    platformId: String,
    device: Device,
    refreshToken: String
  ): Future<Authorization>

  fun revokeAuthorization(platformId: String, userId: String, authorizationId: String): Future<Void>
}
