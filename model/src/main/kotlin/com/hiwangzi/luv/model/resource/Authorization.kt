package com.hiwangzi.luv.model.resource

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class Authorization(
  val id: String,
  val accessToken: String,
  val refreshToken: String,
  val user: User
) {

  constructor(jsonObject: JsonObject) : this(
    id = jsonObject.getString("id"),
    accessToken = jsonObject.getString("accessToken"),
    refreshToken = jsonObject.getString("refreshToken"),
    user = User(jsonObject.getJsonObject("id"))
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }

}
