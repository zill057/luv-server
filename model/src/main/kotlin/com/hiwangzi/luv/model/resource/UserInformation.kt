package com.hiwangzi.luv.model.resource

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class UserInformation(
  val user: User,
  val identity: Identity
) {

  constructor(jsonObject: JsonObject) : this(
    user = User(jsonObject.getJsonObject("user")),
    identity = Identity(jsonObject.getJsonObject("identity"))
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }

}
