package com.hiwangzi.luv.http.route.configure.impl

import com.hiwangzi.luv.auth.AuthFeature
import com.hiwangzi.luv.feature.imFeatureOf
import com.hiwangzi.luv.http.route.configure.RouteConfigurator
import com.hiwangzi.luv.util.endJsonArray
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router

class MessageRouteConfigurator(vertx: Vertx, private val authFeature: AuthFeature) : RouteConfigurator() {

  private val imFeature = imFeatureOf(vertx)

  override fun configure(router: Router) {

    /**
     * @api {get} /im-groups/:id/messages 拉取IM群组消息记录 🎯
     * @apiDescription 通过`before`参数**分页**
     * @apiName ListMessages
     * @apiGroup Message
     * @apiVersion 1.0.0
     *
     * @apiParam (路径参数) {UUID} id IM群组id
     * @apiParam (查询参数) {Timestamp} before 消息创建时间，只有早于该时间的消息才会返回
     * @apiParam (查询参数) {Int} perPage 每页结果数量（最大100）
     * @apiParamExample 请求示例 (URL)
     *     /im-groups/4ef141d5-5360-43b5-8b3b-2de935071831/messages?before=1619088533000&perPage=10
     *
     * @apiSuccess (返回参数) {UUID} id 消息id
     * @apiSuccess (返回参数) {UUID} senderId 消息发送用户id
     * @apiSuccess (返回参数) {Enum} contentType 消息类型
     * @apiSuccess (返回参数) {String} content 消息内容
     * @apiSuccess (返回参数) {Timestamp} createdAt 消息创建时间
     * @apiSuccessExample 返回示例
     *     [
     *       {
     *         "id": "daba361b-6774-4192-a1a8-2bc894e70f76",
     *         "senderId": "cedce3ef-87ca-41d8-bb14-52d8d05f4942",
     *         "messageType": "user",
     *         "contentType": "text/plain",
     *         "content": "这是消息内容",
     *         "createdAt": 1592898512000
     *       }
     *     ]
     */
    router.route(HttpMethod.GET, "/im-groups/:id/messages")
      .handler(authFeature.httpAuthenticationHandler)
      .handler { ctx ->
        val request = ctx.request()
        val groupId = getQueryParam(request, "id", "path", UUID_REGEX)
        val before = request.getParam("before")?.toLongOrNull() ?: System.currentTimeMillis()
        val perPage = request.getParam("perPage")?.toIntOrNull() ?: 10
        imFeature.listMessagesByGroupId(groupId, before, perPage)
          .onSuccess { ctx.response().endJsonArray(JsonArray(it)) }
          .onFailure { ctx.fail(it) }
      }
  }
}
