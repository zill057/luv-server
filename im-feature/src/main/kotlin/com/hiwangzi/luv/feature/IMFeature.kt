package com.hiwangzi.luv.feature

import com.hiwangzi.luv.feature.impl.IMFeatureImpl
import com.hiwangzi.luv.model.resource.IMGroup
import com.hiwangzi.luv.model.resource.IMMessage
import com.hiwangzi.luv.model.resource.User
import io.vertx.core.Future
import io.vertx.core.Vertx

fun imFeatureOf(vertx: Vertx): IMFeature {
  return IMFeatureImpl(vertx)
}

interface IMFeature {

  fun listMemberIdsByGroupId(groupId: String): Future<List<String>>

  fun listMembersByGroupId(groupId: String): Future<List<User>>

  fun listGroupsByUserId(userId: String): Future<List<IMGroup>>

  fun listMessagesByGroupId(groupId: String, before: Long, perPage: Int): Future<List<IMMessage>>

  fun saveMessage(groupId: String, message: IMMessage): Future<IMMessage>

  fun createNewGroup(platformId: String, creatorId: String, inviteeId: String): Future<IMGroup>
}
