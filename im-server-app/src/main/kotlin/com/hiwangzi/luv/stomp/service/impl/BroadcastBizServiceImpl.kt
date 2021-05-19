package com.hiwangzi.luv.stomp.service.impl

import com.hiwangzi.luv.stomp.service.BroadcastBizService
import io.vertx.core.json.JsonObject
import io.vertx.ext.stomp.Command
import io.vertx.ext.stomp.Frame
import io.vertx.ext.stomp.StompServerHandler
import org.slf4j.LoggerFactory

class BroadcastBizServiceImpl(
  private val stompServerHandler: StompServerHandler
) : BroadcastBizService {

  private val logger = LoggerFactory.getLogger(BroadcastBizServiceImpl::class.java)

  override fun broadcastMessage(groupMembers: List<String>, message: JsonObject) {
    groupMembers
      .map { "/users/$it" }
      .forEach { address -> this.sendMessage(address, message) }
  }

  private fun sendMessage(address: String, message: JsonObject) {
    val messageFrame = this.createFrame(address, message)
    val destination = stompServerHandler.getDestination(address)
    if (destination != null) {
      logger.debug("Sending data to: ${destination.destination()}, data: $message")
      destination.dispatch(null, messageFrame)
      logger.debug("Send data to: ${destination.destination()}, data: $message")
    }
  }

  private fun createFrame(destination: String, body: JsonObject): Frame {
    val bodyBuffer = body.toBuffer()
    return Frame()
      .setCommand(Command.MESSAGE)
      .setDestination(destination)
      .setBody(bodyBuffer)
      .addHeader(Frame.SUBSCRIPTION, "luv")
      .addHeader(Frame.CONTENT_LENGTH, bodyBuffer.length().toString())
      .addHeader(Frame.CONTENT_TYPE, "application/json")
  }
}
