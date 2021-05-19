package com.hiwangzi.luv.stomp.handler

import com.hiwangzi.luv.auth.authFeatureOf
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.stomp.impl.DefaultStompHandler
import org.slf4j.LoggerFactory

class LuvStompHandler(vertx: Vertx, securityConfig: JsonObject) : DefaultStompHandler(vertx) {

  private val logger = LoggerFactory.getLogger(LuvStompHandler::class.java)
  private val authFeature = authFeatureOf(vertx, securityConfig)

  init {
    // for debug
    super.receivedFrameHandler { sf ->
      logger.debug("Received data: ${sf.frame().toJson()}")
      logger.debug("Current subscriptions: ${this.destinations.map { it.destination() }}")
    }

    super.authProvider(authFeature.stompAuthenticationProvider)
    super.sendHandler(LuvSendHandler(vertx, this))
  }
}
