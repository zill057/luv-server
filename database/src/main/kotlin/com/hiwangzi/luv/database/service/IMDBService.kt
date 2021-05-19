package com.hiwangzi.luv.database.service

import com.hiwangzi.luv.database.service.impl.IMDBServiceImpl
import com.hiwangzi.luv.model.enumeration.message.ContentType
import com.hiwangzi.luv.model.enumeration.message.MessageType
import com.hiwangzi.luv.model.resource.IMGroup
import com.hiwangzi.luv.model.resource.IMMessage
import com.hiwangzi.luv.model.resource.User
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.pgclient.PgPool

@ProxyGen
interface IMDBService {

  fun listMemberIdsByGroupId(groupId: String, resultHandler: Handler<AsyncResult<List<String>>>)

  /**
   * List members by group id
   *
   * @param groupId group id
   * @param resultHandler handle result `group members`
   */
  fun listMembersByGroupId(groupId: String, resultHandler: Handler<AsyncResult<List<User>>>)

  /**
   * List groups by user id
   *
   * @param userId group id
   * @param resultHandler handle result `group`
   */
  fun listGroupsByUserId(userId: String, resultHandler: Handler<AsyncResult<List<IMGroup>>>)

  /**
   * List messages by group id
   *
   * @param groupId group id
   * @param createdBefore message created before
   * @param limit count limit
   * @param resultHandler handle result `message`
   */
  fun listMessagesByGroupId(
    groupId: String, createdBefore: Long, limit: Int,
    resultHandler: Handler<AsyncResult<List<IMMessage>>>
  )

  fun saveMessage(
    groupId: String,
    messageType: MessageType,
    contentType: ContentType,
    content: String,
    fromUserId: String?,
    resultHandler: Handler<AsyncResult<IMMessage>>
  )

  fun addIMGroup(
    platformId: String,
    name: String,
    profilePhoto: String,
    creatorId: String,
    inviteeId: String,
    resultHandler: Handler<AsyncResult<IMGroup>>
  )
}

object IMDBServiceFactory {
  const val ADDRESS = "im-db-service-address"

  fun create(pgPool: PgPool): IMDBService {
    return IMDBServiceImpl(pgPool)
  }

  fun createProxy(vertx: Vertx): IMDBService {
    return IMDBServiceVertxEBProxy(vertx, ADDRESS)
  }
}
