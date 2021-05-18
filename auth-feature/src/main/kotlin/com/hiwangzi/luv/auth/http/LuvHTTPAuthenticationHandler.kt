package com.hiwangzi.luv.auth.http

import com.hiwangzi.luv.model.exception.ExpiredTokenError
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.auth.authentication.AuthenticationProvider
import io.vertx.ext.auth.authentication.Credentials
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.AuthenticationHandlerImpl

class LuvHTTPAuthenticationHandler<T : AuthenticationProvider>(authProvider: T) :
  AuthenticationHandlerImpl<T>(authProvider) {

  override fun parseCredentials(context: RoutingContext, handler: Handler<AsyncResult<Credentials>>) {

    val request = context.request()
    val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)

    if (authorization == null) {
      handler.handle(Future.failedFuture(ExpiredTokenError(message = "Empty authorization header")))
      return
    }

    try {
      val idx = authorization.indexOf(' ')
      if (idx <= 0) {
        handler.handle(Future.failedFuture(ExpiredTokenError(message = "Invalid authorization header")))
        return
      }
      if ("Bearer" != authorization.substring(0, idx)) {
        handler.handle(Future.failedFuture(ExpiredTokenError(message = "Invalid format for authorization header")))
        return
      }
      handler.handle(Future.succeededFuture(TokenCredentials(authorization.substring(idx + 1))))
    } catch (e: RuntimeException) {
      handler.handle(Future.failedFuture(e))
    }
  }
}
