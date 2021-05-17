package com.hiwangzi.luv.database.service.common

import com.hiwangzi.luv.model.enumeration.message.ContentType
import com.hiwangzi.luv.model.enumeration.message.MessageType
import com.hiwangzi.luv.model.resource.IMMessage
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

fun insertMessage(
  sqlClient: SqlClient,
  groupId: String,
  senderId: String? = null,
  messageType: MessageType,
  contentType: ContentType,
  content: String
): Future<IMMessage> {
  val sql = """
    INSERT INTO luv_im.messages(group_id, from_user, message_type, content_type, content)
    VALUES ($1, $2, $3, $4, $5)
    RETURNING id, created_at
    """.trimIndent()
  return sqlClient.preparedQuery(sql)
    .execute(Tuple.of(groupId, senderId, messageType.code, contentType.code, content))
    .compose { rowSet ->
      val row = rowSet.first()
      Future.succeededFuture(
        IMMessage(
          id = row.getUUID("id").toString(),
          senderId = senderId,
          messageType = messageType,
          contentType = contentType,
          content = content,
          createdAt = row.getOffsetDateTime("created_at").toInstant().toEpochMilli()
        )
      )
    }
}
