package com.hiwangzi.luv.model.exception

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

class ParamException(
  message: String = "参数错误",
  private val field: String,
  private val value: String? = null,
  private val issue: String,
  private val location: String, // url, query, body
  cause: Throwable? = null
) : LuvGeneralException(400, "PARAM_ERROR", message, cause) {

  override fun toResponseBodyJson(): JsonObject {
    return super.toResponseBodyJson().also {
      val details = jsonObjectOf(
        Pair("field", field), Pair("value", value),
        Pair("issue", issue), Pair("location", location)
      )
      it.put("details", details)
    }
  }
}
