package com.hiwangzi.luv.stomp.handler

import com.hiwangzi.luv.feature.imFeatureOf
import com.hiwangzi.luv.model.enumeration.message.MessageType
import com.hiwangzi.luv.model.resource.IMMessage
import com.hiwangzi.luv.stomp.service.BroadcastBizServiceFactory
import io.vertx.core.Vertx
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.stomp.DefaultSendHandler
import io.vertx.ext.stomp.ServerFrame
import io.vertx.ext.stomp.StompServerHandler
import org.slf4j.LoggerFactory
import java.util.*

class LuvSendHandler(vertx: Vertx, private val stompHandler: StompServerHandler) : DefaultSendHandler() {

  private val logger = LoggerFactory.getLogger(LuvSendHandler::class.java)
  private val broadcastBizService = BroadcastBizServiceFactory.createProxy(vertx)
  private val imFeature = imFeatureOf(vertx)

  override fun handle(sf: ServerFrame) {
    super.handle(sf)
    try {
      val message = sf.frame().body.toJsonObject()
      this.onMessageReceived(sf, message)
    } catch (e: DecodeException) {
      logger.warn("Decode data failed, data = ${sf.frame().body}）", e)
    } catch (e: Exception) {
      logger.error("Handle data failed, data = ${sf.frame().body}）", e)
    }
  }

  private fun onMessageReceived(sf: ServerFrame, message: JsonObject) {
    // override create time, TODO: should check is client time a few seconds ago
    message.put("createdAt", System.currentTimeMillis())

    val groupId = this.parseGroupId(sf.frame().destination)
    imFeature.listMemberIdsByGroupId(groupId)
      .onFailure {
        logger.error("Broadcast message failed, failed to list group members, message: $message", it)
      }
      .onSuccess {
        val user = stompHandler.getUserBySession(sf.connection().session())
        // append some field
        message.put("id", UUID.randomUUID().toString())
        message.put("groupId", groupId)
        message.put("senderId", user.get("sub")) // get user id from jwt token
        message.put("messageType", MessageType.USER_MESSAGE) // get user id from jwt token
        logger.info("Broadcast message in group: $groupId")
        broadcastBizService.broadcastMessage(it, message)
        imFeature.saveMessage(groupId, IMMessage(message))
      }
  }

  private fun parseGroupId(destination: String): String {
    return destination.split("/groups/").last()
  }
}
