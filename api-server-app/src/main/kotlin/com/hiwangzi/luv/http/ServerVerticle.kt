package com.hiwangzi.luv.http

import com.hiwangzi.luv.http.route.RouterBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.kotlin.core.http.httpServerOptionsOf
import io.vertx.kotlin.core.json.jsonObjectOf
import org.slf4j.LoggerFactory

class ServerVerticle : AbstractVerticle() {

  private val logger = LoggerFactory.getLogger(ServerVerticle::class.java)

  override fun start(startPromise: Promise<Void>) {
    val serverConfig = config()
    val port = serverConfig.getInteger("port", 80)
    val httpServerOptions = httpServerOptionsOf(port = port, logActivity = true)
    val securityConfig = serverConfig.getJsonObject("security", jsonObjectOf())
    val uploadsConfig = serverConfig.getJsonObject("uploads", jsonObjectOf())
    vertx.createHttpServer(httpServerOptions)
      .requestHandler(RouterBuilder(vertx, securityConfig, uploadsConfig).build())
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
