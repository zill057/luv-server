package com.hiwangzi.luv.model.enumeration.message

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ContentType(val code: Int, private val displayName: String) {
  TEXT_PLAIN(0, "text/plain"),
  TEXT_IMAGE_URL(1, "text/image-url");

  companion object {
    fun fromCode(code: Int): ContentType {
      return values().firstOrNull() { it.code == code } ?: TEXT_PLAIN
    }

    @JsonCreator
    fun forValue(value: String): ContentType {
      return values().firstOrNull { it.displayName == value } ?: TEXT_PLAIN
    }
  }

  @JsonValue
  fun toValue(): String {
    return this.displayName
  }

}
