package com.hiwangzi.luv.model.resource

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class User(
  val id: String,
  val name: String,
  val profilePhoto: String,
  val email: String,
  val phone: String,
  val department: Department,
  val organization: Organization
) {

  constructor(jsonObject: JsonObject) : this(
    id = jsonObject.getString("id"),
    name = jsonObject.getString("name"),
    profilePhoto = jsonObject.getString("profilePhoto"),
    email = jsonObject.getString("email"),
    phone = jsonObject.getString("phone"),
    department = Department(jsonObject.getJsonObject("department")),
    organization = Organization(jsonObject.getJsonObject("organization")),
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }

}
