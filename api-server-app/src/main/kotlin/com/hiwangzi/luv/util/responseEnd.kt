package com.hiwangzi.luv.util

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun HttpServerResponse.endJsonObject(responseBody: JsonObject) {
  this.putHeader("Content-Type", "application/json")
  this.end(responseBody.encode())
}

fun HttpServerResponse.endJsonArray(responseBody: JsonArray) {
  this.putHeader("Content-Type", "application/json")
  this.end(responseBody.encode())
}
