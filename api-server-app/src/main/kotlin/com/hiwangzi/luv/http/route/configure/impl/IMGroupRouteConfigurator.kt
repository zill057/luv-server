package com.hiwangzi.luv.http.route.configure.impl

import com.hiwangzi.luv.auth.AuthFeature
import com.hiwangzi.luv.feature.imFeatureOf
import com.hiwangzi.luv.http.route.configure.RouteConfigurator
import com.hiwangzi.luv.util.endJsonArray
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router

class IMGroupRouteConfigurator(vertx: Vertx, private val authFeature: AuthFeature) : RouteConfigurator() {

  private val imFeature = imFeatureOf(vertx)

  override fun configure(router: Router) {

    /**
     * @api {get} /users/:id/im-groups 拉取用户所有IM群组 🎯
     * @apiName ListUserIMGroups
     * @apiGroup IMGroup
     * @apiVersion 1.0.0
     *
     * @apiParam (路径参数) {UUID} id 用户id
     * @apiParamExample 请求示例 (URL)
     *     /users/391394b0-6871-40fb-916c-31819f358593/im-groups
     *
     * @apiSuccess (返回参数) {UUID} id IM群组id
     * @apiSuccess (返回参数) {String} name IM群组名称
     * @apiSuccess (返回参数) {String} profilePhoto IM群组头像
     * @apiSuccess (返回参数) {JsonObject} latestMessage 群内最后一条消息
     * @apiSuccessExample 返回示例
     *     [
     *       {
     *         "id": "1824312a-87ee-4dd6-a457-bd6aac677459",
     *         "name": "群组名称",
     *         "profilePhoto": "https://user-images.hiwangzi.com/110ca843-24d8-4a26-acea-56c741998de1/a5f4138f-786f-4bbf-b4fb-bf56473c21a7.jpeg",
     *         "creator": {
     *           "id": "6228df95-4458-4d5b-9d2f-48fe8f19ba35",
     *           "name": "王子",
     *           "profilePhoto": "http://user-files.hiwangzi.com/default-avatars/7.jpg",
     *           "email": "meetzwang@gmail.com",
     *           "phone": "18812345678",
     *           "department": {
     *             "id": "787762b4-ecbf-4114-a7fd-a8b2a7ae6ac7",
     *             "name": "默认部门"
     *           },
     *           "organization": {
     *             "id": "6126b674-aa1e-4854-a974-1f28253c8c96",
     *             "name": "默认公司"
     *           }
     *         },
     *         "latestMessage": {
     *             "id": "daba361b-6774-4192-a1a8-2bc894e70f76",
     *             "senderId": "cedce3ef-87ca-41d8-bb14-52d8d05f4942", // 可能为`null`(消息类型为`system`时)
     *             "messageType": "user", // 消息类型: user, system
     *             "contentType": "text/plain", // 内容类型: text/plain, text/image-url, text/html
     *             "content": "这是消息内容",
     *             "createdAt": 1592898512000
     *         }
     *       }
     *     ]
     */
    router.route(HttpMethod.GET, "/users/:id/im-groups")
      .handler(authFeature.httpAuthenticationHandler)
      .handler { ctx ->
        val request = ctx.request()
        val userId = getQueryParam(request, "id", "path", UUID_REGEX)
        imFeature.listGroupsByUserId(userId)
          .onSuccess { ctx.response().endJsonArray(JsonArray(it)) }
          .onFailure { ctx.fail(it) }
      }
  }
}
