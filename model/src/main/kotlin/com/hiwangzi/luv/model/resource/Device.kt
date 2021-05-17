package com.hiwangzi.luv.model.resource

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class Device(
  val name: String,
  val os: String,
  val ip: String
) {
  constructor(jsonObject: JsonObject) : this(
    name = jsonObject.getString("name"),
    os = jsonObject.getString("os"),
    ip = jsonObject.getString("ip"),
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }

}
