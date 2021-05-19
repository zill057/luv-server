package com.hiwangzi.luv.stomp.service

import com.hiwangzi.luv.stomp.service.impl.BroadcastBizServiceImpl
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.stomp.StompServerHandler

@ProxyGen
interface BroadcastBizService {
  /**
   * Broadcast message in the group
   * @param groupMembers users id in the group, `user` example:
   * @param message      message broadcast in the group
   */
  fun broadcastMessage(groupMembers: List<String>, message: JsonObject)
}

object BroadcastBizServiceFactory {
  const val ADDRESS = "broadcast-biz-service-address"

  fun create(stompServerHandler: StompServerHandler): BroadcastBizService {
    return BroadcastBizServiceImpl(stompServerHandler)
  }

  fun createProxy(vertx: Vertx): BroadcastBizService {
    return BroadcastBizServiceVertxEBProxy(vertx, ADDRESS)
  }

}
