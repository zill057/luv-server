package com.hiwangzi.luv.http.route.configure.impl

import com.hiwangzi.luv.auth.AuthFeature
import com.hiwangzi.luv.feature.imFeatureOf
import com.hiwangzi.luv.feature.userFeatureOf
import com.hiwangzi.luv.http.route.configure.RouteConfigurator
import com.hiwangzi.luv.util.endJsonArray
import com.hiwangzi.luv.util.endJsonObject
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router

class UserRouteConfigurator(vertx: Vertx, private val authFeature: AuthFeature) : RouteConfigurator() {

  private val userFeature = userFeatureOf(vertx)
  private val imFeature = imFeatureOf(vertx)

  override fun configure(router: Router) {

    /**
     * @api {get} /im-groups/:id/members 1. 拉取IM群组内成员 🎯
     * @apiName ListUsers
     * @apiGroup User
     * @apiVersion 1.0.0
     *
     * @apiParam (路径参数) {UUID} id IM群组id
     * @apiParamExample 请求示例 (URL)
     *     /im-groups/4ef141d5-5360-43b5-8b3b-2de935071831/users
     *
     * @apiSuccess (返回参数) {UUID} id 用户id
     * @apiSuccess (返回参数) {String} name 用户名称
     * @apiSuccess (返回参数) {String} profilePhoto 用户头像
     * @apiSuccessExample 返回示例
     *     [
     *       {
     *         "id": "6228df95-4458-4d5b-9d2f-48fe8f19ba35",
     *         "name": "王子",
     *         "profilePhoto": "http://user-files.hiwangzi.com/default-avatars/7.jpg",
     *         "email": "meetzwang@gmail.com",
     *         "phone": "meetzwang@gmail.com",
     *         "department": {
     *           "id": "787762b4-ecbf-4114-a7fd-a8b2a7ae6ac7",
     *           "name": "默认部门"
     *         },
     *         "organization": {
     *           "id": "6126b674-aa1e-4854-a974-1f28253c8c96",
     *           "name": "默认公司"
     *         }
     *       }
     *     ]
     */
    router.route(HttpMethod.GET, "/im-groups/:id/members")
      .handler(authFeature.httpAuthenticationHandler)
      .handler { ctx ->
        val request = ctx.request()
        val imGroupId = getQueryParam(request, "id", "path", UUID_REGEX)
        imFeature.listMembersByGroupId(imGroupId)
          .onSuccess {
            ctx.response().endJsonArray(JsonArray(it))
          }
          .onFailure {
            ctx.fail(it)
          }
      }

    /**
     * @api {get} /users/:id 2. 获取用户资料 🎯
     * @apiName GetUser
     * @apiGroup User
     * @apiVersion 1.0.0
     *
     * @apiParam (路径参数) {UUID} id 用户id
     * @apiParamExample 请求示例 (URL)
     *     /users/110ca843-24d8-4a26-acea-56c741998de1
     *
     * @apiSuccess (返回参数) {UUID} id 用户id
     * @apiSuccess (返回参数) {String} name 用户名称
     * @apiSuccess (返回参数) {String} profilePhoto 用户头像
     * @apiSuccessExample 返回示例
     *     {
     *       "id": "6228df95-4458-4d5b-9d2f-48fe8f19ba35",
     *       "name": "王子",
     *       "profilePhoto": "http://user-files.hiwangzi.com/default-avatars/7.jpg",
     *       "email": "meetzwang@gmail.com",
     *       "phone": "meetzwang@gmail.com",
     *       "department": {
     *         "id": "787762b4-ecbf-4114-a7fd-a8b2a7ae6ac7",
     *         "name": "默认部门"
     *       },
     *       "organization": {
     *         "id": "6126b674-aa1e-4854-a974-1f28253c8c96",
     *         "name": "默认公司"
     *       }
     *     }
     */
    router.route(HttpMethod.GET, "/users/:id")
      .handler(authFeature.httpAuthenticationHandler)
      .handler { ctx ->
        val request = ctx.request()
        val platformId = getHeaderParam(request, "X-PLATFORM-ID", UUID_REGEX)
        val userId = getQueryParam(request, "id", "path", UUID_REGEX)
        userFeature.findUser(platformId, userId)
          .onSuccess {
            ctx.response().endJsonObject(it.toJson())
          }
          .onFailure {
            ctx.fail(it)
          }
      }
  }
}
