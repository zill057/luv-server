package com.hiwangzi.luv.database.service

import com.hiwangzi.luv.database.DatabaseVerticle
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.resource.Device
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.deploymentOptionsOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory

@ExtendWith(VertxExtension::class)
class UserDBServiceTest {

  companion object {

    private val LOGGER = LoggerFactory.getLogger(UserDBServiceTest::class.java)

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
  fun testFindUserInformation(vertx: Vertx, testContext: VertxTestContext) {
    UserDBServiceFactory.createProxy(vertx)
      .findUserInformation(
        "4381600b-4c08-4707-a054-ecd009933b92",
        UserIdentityType.PHONE_AND_PASSWORD,
        "18812345678"
      ) { ar ->
        if (ar.succeeded()) {
          LOGGER.debug(ar.result()?.encodePrettily())
          Assertions.assertNotNull(ar.result())
          testContext.completeNow()
        } else {
          testContext.failNow(ar.cause())
        }
      }
  }

  @Test
  fun testFindUserById(vertx: Vertx, testContext: VertxTestContext) {
    UserDBServiceFactory.createProxy(vertx)
      .findUserById(
        "4381600b-4c08-4707-a054-ecd009933b92",
        "6228df95-4458-4d5b-9d2f-48fe8f19ba35"
      ) { ar ->
        if (ar.succeeded()) {
          LOGGER.debug(ar.result()?.encodePrettily())
          val user = ar.result()
          Assertions.assertNotNull(user)
          Assertions.assertNotNull(user?.getString("id"))
          Assertions.assertNotNull(user?.getString("name"))
          Assertions.assertNotNull(user?.getString("profilePhoto"))
          Assertions.assertNotNull(user?.getJsonObject("department"))
          Assertions.assertNotNull(user?.getJsonObject("organization"))
          testContext.completeNow()
        } else {
          testContext.failNow(ar.cause())
        }
      }
  }

  @Test
  fun testSaveUserAuthorization(vertx: Vertx, testContext: VertxTestContext) {
    UserDBServiceFactory.createProxy(vertx)
      .saveUserAuthorization(
        userId = "6228df95-4458-4d5b-9d2f-48fe8f19ba35",
        token = "test token",
        device = Device(name = "(database) iPhone 12 Pro Max", os = "iOS 14.5.1", ip = "223.166.93.68"),
        issuedAt = System.currentTimeMillis(),
        expiredAt = System.currentTimeMillis()
      ) { ar ->
        if (ar.succeeded()) {
          LOGGER.debug(ar.result())
          Assertions.assertNotNull(ar.result())
          testContext.completeNow()
        } else {
          testContext.failNow(ar.cause())
        }
      }
  }

  @Test
  fun testRevokeUserAuthorization(vertx: Vertx, testContext: VertxTestContext) {
    UserDBServiceFactory.createProxy(vertx)
      .revokeUserAuthorization(
        userId = "6228df95-4458-4d5b-9d2f-48fe8f19ba35",
        authorizationId = "ffa0a134-26e2-4dd2-bbdf-e6bf93eaafee",
      ) { ar ->
        if (ar.succeeded()) {
          testContext.completeNow()
        } else {
          testContext.failNow(ar.cause())
        }
      }
  }
}
