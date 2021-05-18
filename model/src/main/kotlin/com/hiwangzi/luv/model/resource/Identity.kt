package com.hiwangzi.luv.model.resource

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class Identity(
  val id: String,
  val hashedCredential: String
) {

  constructor(jsonObject: JsonObject) : this(
    id = jsonObject.getString("id"),
    hashedCredential = jsonObject.getString("hashedCredential"),
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }

}
