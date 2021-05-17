package com.hiwangzi.luv.model.enumeration.message

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MessageType(val code: Int, val displayName: String) {
  SYSTEM_MESSAGE(0, "system"),
  USER_MESSAGE(1, "user");

  companion object {
    fun fromCode(code: Int): MessageType {
      return values().firstOrNull { it.code == code } ?: USER_MESSAGE
    }

    @JsonCreator
    fun forValue(value: String): MessageType {
      return values().firstOrNull { it.displayName == value } ?: USER_MESSAGE
    }
  }

  @JsonValue
  fun toValue(): String {
    return this.displayName
  }
}
