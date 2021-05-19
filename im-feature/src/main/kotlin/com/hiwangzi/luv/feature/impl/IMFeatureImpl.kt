package com.hiwangzi.luv.feature.impl

import com.hiwangzi.luv.database.service.IMDBServiceFactory
import com.hiwangzi.luv.feature.IMFeature
import com.hiwangzi.luv.model.resource.IMGroup
import com.hiwangzi.luv.model.resource.IMMessage
import com.hiwangzi.luv.model.resource.User
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx

class IMFeatureImpl(vertx: Vertx) : IMFeature {

  private val imDBService = IMDBServiceFactory.createProxy(vertx)

  override fun listMemberIdsByGroupId(groupId: String): Future<List<String>> {
    val promise = Promise.promise<List<String>>()
    imDBService.listMemberIdsByGroupId(groupId) { membersR ->
      if (membersR.succeeded()) {
        promise.complete(membersR.result())
      } else {
        promise.fail(membersR.cause())
      }
    }
    return promise.future()
  }

  override fun listMembersByGroupId(groupId: String): Future<List<User>> {
    val promise = Promise.promise<List<User>>()
    imDBService.listMembersByGroupId(groupId) { membersR ->
      if (membersR.succeeded()) {
        promise.complete(membersR.result())
      } else {
        promise.fail(membersR.cause())
      }
    }
    return promise.future()
  }

  override fun listGroupsByUserId(userId: String): Future<List<IMGroup>> {
    val promise = Promise.promise<List<IMGroup>>()
    imDBService.listGroupsByUserId(userId) { groupsR ->
      if (groupsR.succeeded()) {
        promise.complete(groupsR.result())
      } else {
        promise.fail(groupsR.cause())
      }
    }
    return promise.future()
  }

  override fun listMessagesByGroupId(groupId: String, before: Long, perPage: Int): Future<List<IMMessage>> {
    val promise = Promise.promise<List<IMMessage>>()
    val now = System.currentTimeMillis()
    val createdBefore = if (before < now) before else now
    val limit = if (perPage in 1..100) perPage else 100
    imDBService.listMessagesByGroupId(groupId, createdBefore, limit) { messagesR ->
      if (messagesR.succeeded()) {
        promise.complete(messagesR.result().reversed())
      } else {
        promise.fail(messagesR.cause())
      }
    }
    return promise.future()
  }

  override fun saveMessage(groupId: String, message: IMMessage): Future<IMMessage> {
    val promise = Promise.promise<IMMessage>()
    imDBService.saveMessage(
      groupId,
      message.messageType,
      message.contentType,
      message.content,
      message.senderId
    ) { messageR ->
      if (messageR.succeeded()) {
        promise.complete(messageR.result())
      } else {
        promise.fail(messageR.cause())
      }
    }
    return promise.future()
  }

  override fun createNewGroup(platformId: String, creatorId: String, inviteeId: String): Future<IMGroup> {
    TODO("Not yet implemented")
  }
}
