package com.hiwangzi.luv.stomp

import com.hiwangzi.luv.stomp.handler.LuvStompHandler
import com.hiwangzi.luv.stomp.service.BroadcastBizService
import com.hiwangzi.luv.stomp.service.BroadcastBizServiceFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.stomp.StompServer
import io.vertx.kotlin.core.http.httpServerOptionsOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.ext.stomp.stompServerOptionsOf
import io.vertx.serviceproxy.ServiceBinder
import org.slf4j.LoggerFactory

class ServerVerticle : AbstractVerticle() {

  private val logger = LoggerFactory.getLogger(ServerVerticle::class.java)

  override fun start(startPromise: Promise<Void>) {
    val serverConfig = config()
    val port = serverConfig.getInteger("port", 1234)
    val stompServerOptions =
      stompServerOptionsOf(port = -1, secured = true, websocketBridge = true, websocketPath = "/luv")
    val stompServer = StompServer.create(vertx, stompServerOptions)
      .handler(LuvStompHandler(vertx, serverConfig.getJsonObject("security", jsonObjectOf())))
    // register service
    ServiceBinder(vertx).setAddress(BroadcastBizServiceFactory.ADDRESS)
      .register(BroadcastBizService::class.java, BroadcastBizServiceFactory.create(stompServer.stompHandler()))

    val httpServerOptions =
      httpServerOptionsOf(port = port, webSocketSubProtocols = listOf("v10.stomp, v11.stomp"), logActivity = true)
    vertx.createHttpServer(httpServerOptions)
      .webSocketHandler(stompServer.webSocketHandler())
      .listen { ar ->
        if (ar.succeeded()) {
          logger.info("🚀 HTTP Server has been launched at port: $port")
          startPromise.complete()
        } else {
          logger.error("⚠️ Failing to start the HTTP server : " + ar.cause().message)
          startPromise.fail(ar.cause())
        }
      }
  }
}
