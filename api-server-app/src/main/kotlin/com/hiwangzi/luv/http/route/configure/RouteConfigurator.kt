package com.hiwangzi.luv.http.route.configure

import com.hiwangzi.luv.model.exception.ParamException
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.DecodeException
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

abstract class RouteConfigurator {

  companion object {
    internal val UUID_REGEX = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
  }

  abstract fun configure(router: Router)

  internal fun getHeaderParam(
    request: HttpServerRequest,
    paramName: String,
    regex: Regex? = null
  ): String {
    val value = request.getHeader(paramName)?.trim()
    if (value == null)
      throw ParamException(field = paramName, value = value, issue = "Empty $paramName", location = "header")
    if (regex != null && !regex.matches(value))
      throw ParamException(field = paramName, value = value, issue = "Illegal $paramName", location = "header")
    return value
  }

  /**
   * url or query param
   */
  internal fun getQueryParam(
    request: HttpServerRequest,
    paramName: String,
    paramLocation: String,
    regex: Regex? = null
  ): String {
    val value = request.getParam(paramName)?.trim()
    if (value == null)
      throw ParamException(field = paramName, value = value, issue = "Empty $paramName", location = paramLocation)
    if (regex != null && !regex.matches(value))
      throw ParamException(field = paramName, value = value, issue = "Illegal $paramName", location = paramLocation)
    return value
  }

  internal fun getBodyParam(
    ctx: RoutingContext,
    paramName: String,
    regex: Regex? = null
  ): String {
    try {
      val body = ctx.bodyAsJson ?: throw ParamException(field = paramName, issue = "Body is empty", location = "body")
      val value = body.getString(paramName)?.trim()
      if (value == null)
        throw ParamException(field = paramName, value = value, issue = "Empty $paramName", location = "body")
      if (regex != null && !regex.matches(value))
        throw ParamException(field = paramName, value = value, issue = "Illegal $paramName", location = "body")
      return value
    } catch (e: DecodeException) {
      throw ParamException(field = paramName, issue = "Body format should be json", location = "body")
    }
  }
}
