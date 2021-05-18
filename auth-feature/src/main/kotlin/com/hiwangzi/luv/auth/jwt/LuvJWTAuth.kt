package com.hiwangzi.luv.auth.jwt

import com.hiwangzi.luv.model.exception.ExpiredTokenException
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import java.util.*

class LuvJWTAuth(vertx: Vertx, securityConfig: JsonObject) : JWTAuth {

  private val secretKey = securityConfig.getString("secret", UUID.randomUUID().toString())

  private val jwtAuth = JWTAuth.create(
    vertx,
    jwtAuthOptionsOf(pubSecKeys = listOf(pubSecKeyOptionsOf(algorithm = "HS256", buffer = Buffer.buffer(secretKey))))
  )

  override fun authenticate(credentials: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
    if (credentials.getString("token") == null) {
      credentials.put("token", credentials.getString("password"))
    }
    jwtAuth.authenticate(credentials) { ar ->
      if (ar.succeeded()) {
        // TODO should check token whether in block list
        resultHandler.handle(Future.succeededFuture(ar.result()))
      } else {
        val cause = ar.cause()
        val wrappedCause = ExpiredTokenException(message = cause.message ?: "", cause = cause)
        resultHandler.handle(Future.failedFuture(wrappedCause))
      }
    }
  }

  override fun generateToken(claims: JsonObject): String {
    return this.generateToken(claims, JWTOptions())
  }

  override fun generateToken(claims: JsonObject, options: JWTOptions): String {
    claims.getString("iss") ?: claims.put("iss", "luv-server")
    return jwtAuth.generateToken(claims, options)
  }
}
