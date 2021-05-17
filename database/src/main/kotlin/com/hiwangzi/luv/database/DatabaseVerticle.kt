package com.hiwangzi.luv.database

import com.hiwangzi.luv.database.service.IMDBService
import com.hiwangzi.luv.database.service.IMDBServiceFactory
import com.hiwangzi.luv.database.service.UserDBService
import com.hiwangzi.luv.database.service.UserDBServiceFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.kotlin.pgclient.pgConnectOptionsOf
import io.vertx.kotlin.sqlclient.poolOptionsOf
import io.vertx.pgclient.PgPool
import io.vertx.serviceproxy.ServiceBinder

class DatabaseVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    val dbConfig = config()
    val port = dbConfig.getInteger("port", 5432)
    val host = dbConfig.getString("host", "localhost")
    val database = dbConfig.getString("database", "postgres")
    val user = dbConfig.getString("user", "postgres")
    val password = dbConfig.getString("password", "")

    val connectOptions = pgConnectOptionsOf(
      port = port, host = host, database = database,
      user = user, password = password
    )
    val pgPool = PgPool.pool(vertx, connectOptions, poolOptionsOf(maxSize = 5))
    val serviceBinder = ServiceBinder(vertx)
    serviceBinder.setAddress(UserDBServiceFactory.ADDRESS)
      .register(UserDBService::class.java, UserDBServiceFactory.create(pgPool))
    serviceBinder.setAddress(IMDBServiceFactory.ADDRESS)
      .register(IMDBService::class.java, IMDBServiceFactory.create(pgPool))
    startPromise.complete()
  }
}
