package com.hiwangzi.luv.model.resource

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class Organization(
  val id: String,
  val name: String,
) {

  constructor(jsonObject: JsonObject) : this(
    id = jsonObject.getString("id"),
    name = jsonObject.getString("name"),
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }

}
