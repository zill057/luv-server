package com.hiwangzi.luv.model.exception

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

open class LuvGeneralException(
  val status: Int,
  private val code: String,
  override val message: String,
  override val cause: Throwable?
) : RuntimeException() {
  companion object {
    val NOT_FOUNT_RESPONSE_BODY = jsonObjectOf(Pair("code", "NOT_FOUND"), Pair("message", "请求的资源不存在")).toString()
    val SYSTEM_ERROR_RESPONSE_BODY = jsonObjectOf(Pair("code", "SYSTEM_ERROR"), Pair("message", "系统错误")).toString()
    val REQUEST_ENTITY_TOO_LARGE =
      jsonObjectOf(Pair("code", "REQUEST_ENTITY_TOO_LARGE"), Pair("message", "上传文件过大")).toString()
  }

  open fun toResponseBodyJson(): JsonObject {
    return jsonObjectOf(Pair("code", code), Pair("message", message))
  }
}
