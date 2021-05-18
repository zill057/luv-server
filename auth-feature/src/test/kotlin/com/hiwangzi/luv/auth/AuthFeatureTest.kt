package com.hiwangzi.luv.auth

import com.hiwangzi.luv.database.DatabaseVerticle
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.resource.Device
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.json.jsonObjectOf
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory

@ExtendWith(VertxExtension::class)
class AuthFeatureTest {

  private val logger = LoggerFactory.getLogger(AuthFeatureTest::class.java)

  companion object {
    @JvmStatic
    @BeforeAll
    fun deployVerticle(vertx: Vertx, testContext: VertxTestContext) {
      ConfigRetriever.create(vertx).config
        .compose {
          val dbDeploymentOptions = deploymentOptionsOf(instances = 4, config = it.getJsonObject("database"))
          vertx.deployVerticle(DatabaseVerticle::class.java, dbDeploymentOptions)
        }
        .onSuccess { testContext.completeNow() }
        .onFailure { testContext.failNow(it) }
    }
  }

  @Test
  fun generateAuthorization(vertx: Vertx, testContext: VertxTestContext) {
    val authFeature = authFeatureOf(vertx, jsonObjectOf())
    authFeature.generateAuthorization(
      platformId = "4381600b-4c08-4707-a054-ecd009933b92",
      device = Device(name = AuthFeatureTest::class.java.canonicalName, os = "macOS 11.3.1", ip = "127.0.0.1"),
      identityType = UserIdentityType.PHONE_AND_PASSWORD,
      identifier = "18812345678",
      credential = "test"
    )
      .onSuccess {
        logger.debug(it.toJson().encodePrettily())
        testContext.completeNow()
      }
      .onFailure { testContext.failNow(it) }
  }

  @Test
  fun refreshAuthorization(vertx: Vertx, testContext: VertxTestContext) {
    val authFeature = authFeatureOf(vertx, jsonObjectOf())
    authFeature.generateAuthorization(
      platformId = "4381600b-4c08-4707-a054-ecd009933b92",
      device = Device(name = AuthFeatureTest::class.java.canonicalName, os = "macOS 11.3.1", ip = "127.0.0.1"),
      identityType = UserIdentityType.PHONE_AND_PASSWORD,
      identifier = "18812345678",
      credential = "test"
    )
      .compose { auth ->
        logger.debug("generated access token: ${auth.accessToken}")
        authFeature.refreshAuthorization(
          platformId = "4381600b-4c08-4707-a054-ecd009933b92",
          device = Device(name = AuthFeatureTest::class.java.canonicalName, os = "macOS 11.3.1", ip = "127.0.0.1"),
          refreshToken = auth.refreshToken
        )
      }
      .onSuccess { testContext.completeNow() }
      .onFailure { testContext.failNow(it) }
  }

  @Test
  fun generateAuthorizationThenAuthenticate(vertx: Vertx, testContext: VertxTestContext) {
    val authFeature =
      authFeatureOf(vertx, jsonObjectOf(Pair("secret", "The Show"), Pair("tokenExpiresInMinutes", 1440)))
    authFeature.generateAuthorization(
      platformId = "4381600b-4c08-4707-a054-ecd009933b92",
      device = Device(name = AuthFeatureTest::class.java.canonicalName, os = "macOS 11.3.1", ip = "127.0.0.1"),
      identityType = UserIdentityType.PHONE_AND_PASSWORD,
      identifier = "18812345678",
      credential = "test"
    )
      .compose { auth ->
        logger.debug("generated access token: ${auth.accessToken}")
        authFeature.authenticate(auth.accessToken)
        authFeature.authenticateRefreshToken(auth.refreshToken)
      }
      .onSuccess { testContext.completeNow() }
      .onFailure { testContext.failNow(it) }
  }
}
