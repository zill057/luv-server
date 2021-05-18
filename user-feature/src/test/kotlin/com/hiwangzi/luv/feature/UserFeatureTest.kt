package com.hiwangzi.luv.feature

import com.hiwangzi.luv.database.DatabaseVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.deploymentOptionsOf
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory

@ExtendWith(VertxExtension::class)
class UserFeatureTest {

  private val logger = LoggerFactory.getLogger(UserFeatureTest::class.java)

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
  fun testFindUser(vertx: Vertx, testContext: VertxTestContext) {
    val userFeature = userFeatureOf(vertx)
    val platformId = "4381600b-4c08-4707-a054-ecd009933b92"
    val userId = "6228df95-4458-4d5b-9d2f-48fe8f19ba35"
    userFeature.findUser(platformId, userId)
      .onSuccess {
        logger.debug(it.toJson().encodePrettily())
        testContext.completeNow()
      }
      .onFailure {
        testContext.failNow(it)
      }
  }
}
