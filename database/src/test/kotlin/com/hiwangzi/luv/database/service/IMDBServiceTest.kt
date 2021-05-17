package com.hiwangzi.luv.database.service

import com.hiwangzi.luv.database.DatabaseVerticle
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
class IMDBServiceTest {

  companion object {

    private val LOGGER = LoggerFactory.getLogger(IMDBServiceTest::class.java)

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
  fun listMemberIdsByGroupId(vertx: Vertx, testContext: VertxTestContext) {
    val groupId = "35dd6615-7708-4961-97d4-da59bfe03538"
    IMDBServiceFactory.createProxy(vertx)
      .listMemberIdsByGroupId(groupId) { membersR ->
        if (membersR.succeeded()) {
          val members = membersR.result()
          Assertions.assertEquals(3, members.size)
          testContext.completeNow()
        } else {
          testContext.failNow(membersR.cause())
        }
      }
  }

  @Test
  fun listMembersByGroupId(vertx: Vertx, testContext: VertxTestContext) {
    val groupId = "35dd6615-7708-4961-97d4-da59bfe03538"
    IMDBServiceFactory.createProxy(vertx)
      .listMembersByGroupId(groupId) { membersR ->
        if (membersR.succeeded()) {
          val members = membersR.result()
          Assertions.assertEquals(3, members.size)
          testContext.completeNow()
        } else {
          testContext.failNow(membersR.cause())
        }
      }
  }

  @Test
  fun addIMGroup(vertx: Vertx, testContext: VertxTestContext) {
    val platformId = "4381600b-4c08-4707-a054-ecd009933b92"
    val name = "群聊哈哈哈"
    val profilePhoto = ""
    val creatorId = "6228df95-4458-4d5b-9d2f-48fe8f19ba35"
    val inviteeId = "30830316-e129-431a-b5f9-d1c7ffede658"
    IMDBServiceFactory.createProxy(vertx)
      .addIMGroup(platformId, name, profilePhoto, creatorId, inviteeId) { groupR ->
        if (groupR.succeeded()) {
          LOGGER.debug(groupR.result().toJson().encodePrettily())
          testContext.completeNow()
        } else {
          testContext.failNow(groupR.cause())
        }
      }
  }
}
