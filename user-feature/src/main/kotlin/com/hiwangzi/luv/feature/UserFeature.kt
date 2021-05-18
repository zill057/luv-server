package com.hiwangzi.luv.feature

import com.hiwangzi.luv.feature.impl.UserFeatureImpl
import com.hiwangzi.luv.model.resource.User
import io.vertx.core.Future
import io.vertx.core.Vertx

fun userFeatureOf(vertx: Vertx): UserFeature {
  return UserFeatureImpl(vertx)
}

interface UserFeature {
  fun findUser(platformId: String, userId: String): Future<User>
}
