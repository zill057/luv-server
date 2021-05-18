package com.hiwangzi.luv.feature.impl

import com.hiwangzi.luv.database.service.UserDBServiceFactory
import com.hiwangzi.luv.feature.UserFeature
import com.hiwangzi.luv.model.exception.NotFoundException
import com.hiwangzi.luv.model.resource.User
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx

class UserFeatureImpl(vertx: Vertx) : UserFeature {

  private val userDBService = UserDBServiceFactory.createProxy(vertx)

  override fun findUser(platformId: String, userId: String): Future<User> {
    val promise = Promise.promise<User>()
    userDBService.findUserById(platformId, userId) { userR ->
      if (userR.succeeded()) {
        val user = userR.result()
        if (user == null) {
          promise.fail(NotFoundException("未找到用户"))
        } else {
          promise.complete(user)
        }
      } else {
        promise.fail(userR.cause())
      }
    }
    return promise.future()
  }
}
