package com.hiwangzi.luv

import com.hiwangzi.luv.database.DatabaseVerticle
import com.hiwangzi.luv.stomp.ServerVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.core.*
import io.vertx.kotlin.core.deploymentOptionsOf

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    ConfigRetriever.create(vertx).config
      .compose {
        val serverDeploymentOptions = deploymentOptionsOf(instances = 4, config = it.getJsonObject("server"))
        val dbDeploymentOptions = deploymentOptionsOf(instances = 4, config = it.getJsonObject("database"))
        CompositeFuture.all(
          vertx.deployVerticle(ServerVerticle::class.java, serverDeploymentOptions),
          vertx.deployVerticle(DatabaseVerticle::class.java, dbDeploymentOptions)
        )
      }
      .onSuccess { startPromise.complete() }
      .onFailure { startPromise.fail(it) }
  }
}

// only for ide debug
fun main() {
  val vertx: Vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle::class.java, DeploymentOptions().setInstances(1))
    .onFailure { it.printStackTrace() }
}
