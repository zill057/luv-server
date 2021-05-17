package com.hiwangzi.luv.model.resource

import com.hiwangzi.luv.model.enumeration.message.ContentType
import com.hiwangzi.luv.model.enumeration.message.MessageType
import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
data class IMMessage(
  val id: String,
  val senderId: String?,
  val messageType: MessageType,
  val contentType: ContentType,
  val content: String,
  val createdAt: Long
) {
  constructor(jsonObject: JsonObject) : this(
    id = jsonObject.getString("id"),
    senderId = jsonObject.getString("senderId"),
    messageType = MessageType.forValue(jsonObject.getString("messageType")),
    contentType = ContentType.forValue(jsonObject.getString("contentType")),
    content = jsonObject.getString("content"),
    createdAt = jsonObject.getLong("createdAt"),
  )

  fun toJson(): JsonObject {
    return JsonObject.mapFrom(this)
  }
}
