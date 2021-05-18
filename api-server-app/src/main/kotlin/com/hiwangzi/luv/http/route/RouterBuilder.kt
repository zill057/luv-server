package com.hiwangzi.luv.http.route

import com.hiwangzi.luv.auth.authFeatureOf
import com.hiwangzi.luv.http.route.configure.impl.AuthorizationRouteConfigurator
import com.hiwangzi.luv.http.route.configure.impl.UserFileConfigurator
import com.hiwangzi.luv.http.route.configure.impl.UserRouteConfigurator
import com.hiwangzi.luv.model.exception.LuvGeneralException
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import org.slf4j.LoggerFactory

class RouterBuilder(
  private val vertx: Vertx,
  securityConfig: JsonObject,
  uploadsConfig: JsonObject
) {
  private val logger = LoggerFactory.getLogger(RouterBuilder::class.java)
  private val authFeature = authFeatureOf(vertx, securityConfig)
  private val requestBodyLimit = securityConfig.getLong("requestBodyLimitInBytes", 2097152) // 2MB as default
  private val userHome = System.getProperty("user.home")
  private val uploadsDirectory = uploadsConfig.getString("uploadsDirectory", "$userHome/file-uploads")
  private val userFilesHost = uploadsConfig.getString("userFilesHost", "http://localhost")
  private val processedDirectory = uploadsConfig.getString("processedDirectory", "$userHome/file-uploads")

  fun build(): Router {
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create().setBodyLimit(requestBodyLimit).setUploadsDirectory(uploadsDirectory))
    addCorsHandler(router)
    addLuvRoute(router)
    addGeneralErrorsHandler(router)
    addOtherErrorsHandler(router)
    return router
  }

  private fun addCorsHandler(router: Router) {
    val corsHandler = CorsHandler.create()
      .addOrigin("*")
      .allowedMethods(setOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE))
    router.route().handler(corsHandler)
  }

  private fun addLuvRoute(router: Router) {
    AuthorizationRouteConfigurator(authFeature).configure(router)
    UserFileConfigurator(vertx, authFeature, userFilesHost, processedDirectory).configure(router)
    UserRouteConfigurator(vertx, authFeature).configure(router)
  }

  private fun addGeneralErrorsHandler(router: Router) {
    router.route().failureHandler { ctx ->
      val response = ctx.response().putHeader("Content-Type", "application/json")
      val cause = ctx.failure()
      val parentCause = cause?.cause

      // custom exceptions
      if (cause is LuvGeneralException) {
        response.setStatusCode(cause.status).end(cause.toResponseBodyJson().toString())
      } else if (cause != null && parentCause != null && parentCause is LuvGeneralException) {
        response.setStatusCode(parentCause.status).end(parentCause.toResponseBodyJson().toString())
      }
      // unrecognized cause
      else if (cause != null) {
        response.setStatusCode(ctx.statusCode()).end(LuvGeneralException.SYSTEM_ERROR_RESPONSE_BODY)
        logger.error("Catch failure with unrecognized cause: ${ctx.request()}", cause)
      }
      // without cause
      else {
        if (413 == ctx.statusCode()) {
          response.setStatusCode(ctx.statusCode()).end(LuvGeneralException.REQUEST_ENTITY_TOO_LARGE)
        } else {
          response.setStatusCode(ctx.statusCode()).end(LuvGeneralException.SYSTEM_ERROR_RESPONSE_BODY)
          logger.error("Catch failure without cause: ${ctx.request()}")
        }
      }
    }
  }

  private fun addOtherErrorsHandler(router: Router) {
    router.errorHandler(404) { ctx ->
      val response = ctx.response().putHeader("Content-Type", "application/json")
      response.setStatusCode(404).end(LuvGeneralException.NOT_FOUNT_RESPONSE_BODY)
    }
  }
}
