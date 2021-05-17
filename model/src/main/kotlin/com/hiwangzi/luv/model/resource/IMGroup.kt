package com.hiwangzi.luv.model.resource

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class IMGroup(
  val id: String,
  val name: String,
  val profilePhoto: String,
  var creator: User? = null,
  var latestMessage: IMMessage? = null
) {

  constructor(jsonObject: JsonObject) : this(
    id = jsonObject.getString("id"),
    name = jsonObject.getString("name"),
    profilePhoto = jsonObject.getString("profilePhoto"),
    creator = User(jsonObject.getJsonObject("creator")),
    latestMessage = IMMessage(jsonObject.getJsonObject("latestMessage"))
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }
}
