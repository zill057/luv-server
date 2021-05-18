package com.hiwangzi.luv.database.service.impl

import com.hiwangzi.luv.database.service.IMDBService
import com.hiwangzi.luv.database.service.common.group.insertIMGroup
import com.hiwangzi.luv.database.service.common.group.insertUserGroupRelationships
import com.hiwangzi.luv.database.service.common.insertMessage
import com.hiwangzi.luv.database.service.common.user.findUserById
import com.hiwangzi.luv.model.enumeration.message.ContentType
import com.hiwangzi.luv.model.enumeration.message.MessageType
import com.hiwangzi.luv.model.resource.*
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple

class IMDBServiceImpl(private val pgPool: PgPool) : IMDBService {

  override fun listMemberIdsByGroupId(groupId: String, resultHandler: Handler<AsyncResult<List<String>>>) {
    val sql = "SELECT user_id AS id FROM luv_im.user_group_relationships WHERE group_id = $1"
    pgPool.preparedQuery(sql)
      .execute(Tuple.of(groupId))
      .onSuccess { rowSet ->
        val members = rowSet.map { row -> row.getUUID("id").toString() }
        resultHandler.handle(Future.succeededFuture(members))
      }
      .onFailure { resultHandler.handle(Future.failedFuture(it)) }
  }

  override fun listMembersByGroupId(
    groupId: String,
    resultHandler: Handler<AsyncResult<List<User>>>
  ) {
    val sql = """
      SELECT t_user.id as user_id,
             t_user.name as user_name,
             t_user.profile_photo,
             t_user.email,
             t_user.phone,
             t_dept.id as dept_id,
             t_dept.name as dept_name,
             t_org.id as org_id,
             t_org.name as org_name
      FROM luv_im.user_group_relationships as t_u_g_rs
         LEFT JOIN luv_user.users as t_user ON t_u_g_rs.user_id = t_user.id
         LEFT JOIN luv_user.departments as t_dept ON t_user.department_id = t_dept.id
         LEFT JOIN luv_user.organizations as t_org ON t_dept.organization_id = t_org.id
      WHERE t_u_g_rs.group_id = $1
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(Tuple.of(groupId))
      .onSuccess { rowSet ->
        val members = rowSet.map { row ->
          User(
            id = row.getUUID("user_id").toString(),
            name = row.getString("user_name"),
            profilePhoto = row.getString("profile_photo"),
            email = row.getString("email"),
            phone = row.getString("phone"),
            department = Department(row.getUUID("dept_id").toString(), row.getString("dept_name")),
            organization = Organization(row.getUUID("org_id").toString(), row.getString("org_name"))
          )
        }
        resultHandler.handle(Future.succeededFuture(members))
      }
      .onFailure { resultHandler.handle(Future.failedFuture(it)) }
  }

  override fun listGroupsByUserId(
    userId: String,
    resultHandler: Handler<AsyncResult<List<IMGroup>>>
  ) {
    val sql = """
      SELECT * FROM (
          SELECT DISTINCT ON (t_msg.group_id)
                 t_group.id,
                 t_group.name,
                 t_group.profile_photo,
                 t_msg.id as msg_id,
                 t_msg.from_user,
                 t_msg.message_type,
                 t_msg.content_type,
                 t_msg.content,
                 t_msg.created_at
          FROM luv_im.user_group_relationships as t_u_g_rs
              LEFT JOIN luv_im.groups as t_group ON t_u_g_rs.group_id = t_group.id
              LEFT JOIN luv_im.messages as t_msg ON t_group.id = t_msg.group_id
          WHERE t_u_g_rs.user_id = $1
          ORDER BY t_msg.group_id, t_msg.created_at DESC
      ) t
      ORDER BY created_at
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(Tuple.of(userId))
      .onSuccess { rowSet ->
        val members = rowSet.map { row ->
          IMGroup(
            id = row.getUUID("id").toString(),
            name = row.getString("name"),
            profilePhoto = row.getString("profile_photo"),
            latestMessage = IMMessage(
              id = row.getUUID("msg_id").toString(),
              senderId = row.getUUID("from_user").toString(),
              messageType = MessageType.fromCode(row.getInteger("message_type")),
              contentType = ContentType.fromCode(row.getInteger("content_type")),
              content = row.getString("content"),
              createdAt = row.getOffsetDateTime("created_at").toInstant().toEpochMilli()
            )
          )
        }
        resultHandler.handle(Future.succeededFuture(members))
      }
      .onFailure { resultHandler.handle(Future.failedFuture(it)) }
  }

  override fun listMessagesByGroupId(
    groupId: String,
    resultHandler: Handler<AsyncResult<List<IMMessage>>>
  ) {
    val sql = """
      SELECT t_msg.id,
             t_msg.from_user,
             t_msg.message_type,
             t_msg.content_type,
             t_msg.content,
             t_msg.created_at
      FROM luv_im.messages as t_msg
      WHERE t_msg.group_id = $1
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(Tuple.of(groupId))
      .onSuccess { rowSet ->
        val members = rowSet.map { row ->
          IMMessage(
            id = row.getUUID("id").toString(),
            senderId = row.getUUID("from_user").toString(),
            messageType = MessageType.fromCode(row.getInteger("message_type")),
            contentType = ContentType.fromCode(row.getInteger("content_type")),
            content = row.getString("content"),
            createdAt = row.getOffsetDateTime("created_at").toInstant().toEpochMilli()
          )
        }
        resultHandler.handle(Future.succeededFuture(members))
      }
      .onFailure { resultHandler.handle(Future.failedFuture(it)) }
  }

  override fun saveMessage(
    groupId: String,
    messageType: MessageType,
    contentType: ContentType,
    content: String,
    fromUserId: String?,
    resultHandler: Handler<AsyncResult<IMMessage>>
  ) {
    val sql = """
      INSERT INTO luv_im.messages(message_type, content_type, content, from_user, group_id)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING id, created_at
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(Tuple.of(messageType.code, contentType.code, content, fromUserId, groupId))
      .onSuccess { rowSet ->
        val row = rowSet.first()
        val message = IMMessage(
          id = row.getUUID("id").toString(),
          senderId = fromUserId,
          messageType = messageType,
          contentType = contentType,
          content = content,
          createdAt = row.getOffsetDateTime("created_at").toInstant().toEpochMilli()
        )
        resultHandler.handle(Future.succeededFuture(message))
      }
      .onFailure { resultHandler.handle(Future.failedFuture(it)) }
  }

  override fun addIMGroup(
    platformId: String,
    name: String,
    profilePhoto: String,
    creatorId: String,
    inviteeId: String,
    resultHandler: Handler<AsyncResult<IMGroup>>
  ) {
    // 1. find group creator
    pgPool.withTransaction { client ->
      findUserById(sqlClient = client, platformId = platformId, userId = creatorId)
        // 2. create group
        .compose { creator ->
          insertIMGroup(sqlClient = client, name = name, profilePhoto = profilePhoto)
            .compose { group -> Future.succeededFuture(group.also { it.creator = creator }) }
        }
        // 3. invite user into group
        .compose { group ->
          insertUserGroupRelationships(
            sqlClient = client,
            groupId = group.id,
            userIds = arrayOf(creatorId, inviteeId)
          )
            .compose { Future.succeededFuture(group) }
        }
        // 4. create first message in group
        .compose { group ->
          val messageType = MessageType.SYSTEM_MESSAGE
          val contentType = ContentType.TEXT_PLAIN
          val content = "【${group.creator!!.name}】创建了群聊"
          insertMessage(
            sqlClient = client,
            groupId = group.id,
            messageType = messageType,
            contentType = contentType,
            content = content
          ).compose { message -> Future.succeededFuture(group.also { it.latestMessage = message }) }
        }
    }.onComplete(resultHandler)
  }
}

