package com.hiwangzi.luv.http.route.configure.impl

import com.hiwangzi.luv.auth.AuthFeature
import com.hiwangzi.luv.http.route.configure.RouteConfigurator
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.resource.Device
import com.hiwangzi.luv.util.endJsonObject
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class AuthorizationRouteConfigurator(private val authFeature: AuthFeature) : RouteConfigurator() {

  override fun configure(router: Router) {
    /**
     * @api {post} /authorizations/by/password 1. 获取token（登录） 🎯
     * @apiName PostAuthorizationByPassword
     * @apiGroup Authorization
     * @apiVersion 1.0.0
     *
     * @apiParam (Header参数) {UUID} X-PLATFORM-ID 平台ID
     * @apiParam (body参数) {String} username 用户名
     * @apiParam (body参数) {String} password 密码
     * @apiParamExample 请求示例 (URL)
     *     /authorizations/by/password
     * @apiParamExample 请求示例 (JSON)
     *     {
     *       "username": "18812345678",
     *       "password": "It's your password"
     *     }
     *
     * @apiSuccess (返回参数) {UUID} id 授权id
     * @apiSuccess (返回参数) {String} accessToken 访问凭据
     * @apiSuccess (返回参数) {String} refreshToken 刷新凭据
     * @apiSuccess (返回参数) {JsonObject} user 用户信息
     * @apiSuccessExample 返回示例
     *     {
     *       "id": "7b84b492-43e4-466a-9eed-ee1962d5ffa8",
     *       "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJsdXYtc2VydmVyIiwic3ViIjoiNjIyOGRmOTUtNDQ1OC00ZDViLTlkMmYtNDhmZThmMTliYTM1IiwibmJmIjoxNjIxMzI0NTY3LCJpYXQiOjE2MjEzMjQ1NjcsImV4cCI6MTYyMTMyNjM2N30.0ysrvC6ad5YbQOBdHnjUy1e0A8eVrOy-u8zsDVcWgXk",
     *       "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJsdXYtc2VydmVyIiwic3ViIjoiNjIyOGRmOTUtNDQ1OC00ZDViLTlkMmYtNDhmZThmMTliYTM1IiwibmJmIjoxNjIxMzI0NTY3LCJpYXQiOjE2MjEzMjQ1NjcsImV4cCI6MTYyMTkyOTM2NywicmVmcmVzaCI6dHJ1ZX0.adPGl7C5pUshkAZ_fFeuKhCY5jI6cQZVT6yQ5p6_4wg",
     *       "user": {
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
     *     }
     *
     * @apiError (错误码) INVALID_CREDENTIAL 用户名或密码错误
     */
    router.route(HttpMethod.POST, "/authorizations/by/password").handler { postAuthorizationByPassword(it) }
    /**
     * @api {post} /authorizations/by/refresh-token 2. 刷新token 🎯
     * @apiName PostAuthorizationByRefreshToken
     * @apiGroup Authorization
     * @apiVersion 1.0.0
     *
     * @apiParam (Header参数) {UUID} X-PLATFORM-ID 平台ID
     * @apiParamExample 请求示例 (URL)
     *     /authorizations/by/refresh-token
     * @apiParamExample 请求示例 (JSON)
     *     {
     *       "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJsdXYtc2VydmVyIiwic3ViIjoiNjIyOGRmOTUtNDQ1OC00ZDViLTlkMmYtNDhmZThmMTliYTM1IiwibmJmIjoxNjIxMzI0NTY3LCJpYXQiOjE2MjEzMjQ1NjcsImV4cCI6MTYyMTkyOTM2NywicmVmcmVzaCI6dHJ1ZX0.adPGl7C5pUshkAZ_fFeuKhCY5jI6cQZVT6yQ5p6_4wg"
     *     }
     *
     * @apiSuccess (返回参数) {UUID} id 授权id
     * @apiSuccess (返回参数) {String} accessToken 访问凭据
     * @apiSuccess (返回参数) {String} refreshToken 刷新凭据
     * @apiSuccess (返回参数) {JsonObject} user 用户信息
     * @apiSuccessExample 返回示例
     *     {
     *       "id": "7b84b492-43e4-466a-9eed-ee1962d5ffa8",
     *       "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJsdXYtc2VydmVyIiwic3ViIjoiNjIyOGRmOTUtNDQ1OC00ZDViLTlkMmYtNDhmZThmMTliYTM1IiwibmJmIjoxNjIxMzI0NTY3LCJpYXQiOjE2MjEzMjQ1NjcsImV4cCI6MTYyMTMyNjM2N30.0ysrvC6ad5YbQOBdHnjUy1e0A8eVrOy-u8zsDVcWgXk",
     *       "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJsdXYtc2VydmVyIiwic3ViIjoiNjIyOGRmOTUtNDQ1OC00ZDViLTlkMmYtNDhmZThmMTliYTM1IiwibmJmIjoxNjIxMzI0NTY3LCJpYXQiOjE2MjEzMjQ1NjcsImV4cCI6MTYyMTkyOTM2NywicmVmcmVzaCI6dHJ1ZX0.adPGl7C5pUshkAZ_fFeuKhCY5jI6cQZVT6yQ5p6_4wg",
     *       "user": {
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
     *     }
     *
     * @apiError (错误码) EXPIRED_REFRESH_TOKEN 刷新凭据已经过期
     */
    router.route(HttpMethod.POST, "/authorizations/by/refresh-token").handler { postAuthorizationByRefreshToken(it) }
    /**
     * @api {delete} /authorizations/:id 3. 作废token（退出） 🎯
     * @apiName DeleteAuthorization
     * @apiGroup Authorization
     * @apiVersion 1.0.0
     *
     * @apiParam (Header参数) {UUID} X-PLATFORM-ID 平台ID
     * @apiParam (路径参数) {UUID} id 授权id
     * @apiParamExample 请求示例 (URL)
     *     /authorizations/d211abe0-c1af-42a2-9aba-784130ca92fa
     */
    router.route(HttpMethod.DELETE, "/authorizations/:id")
      .handler(authFeature.httpAuthenticationHandler)
      .handler { deleteAuthorization(it) }
  }

  private fun postAuthorizationByPassword(ctx: RoutingContext) {
    val request = ctx.request()
    val platformId = getHeaderParam(request, "X-PLATFORM-ID", UUID_REGEX)
    val device = Device(
      name = request.getHeader("User-Agent") ?: "",
      os = "",
      ip = request.remoteAddress().hostAddress()
    )
    val identifier = getBodyParam(ctx, "username", Regex("^1[0-9]{10}$"))
    val credential = getBodyParam(ctx, "password")

    authFeature.generateAuthorization(platformId, device, UserIdentityType.PHONE_AND_PASSWORD, identifier, credential)
      .onSuccess { ctx.response().endJsonObject(it.toJson()) }
      .onFailure { ctx.fail(it) }
  }

  private fun postAuthorizationByRefreshToken(ctx: RoutingContext) {
    val request = ctx.request()
    val platformId = getHeaderParam(request, "X-PLATFORM-ID", UUID_REGEX)
    val device = Device(
      name = request.getHeader("User-Agent") ?: "",
      os = "",
      ip = request.remoteAddress().hostAddress()
    )
    val refreshToken = getBodyParam(ctx, "refreshToken")

    authFeature.refreshAuthorization(platformId, device, refreshToken)
      .onSuccess { ctx.response().endJsonObject(it.toJson()) }
      .onFailure { ctx.fail(it) }
  }

  private fun deleteAuthorization(ctx: RoutingContext) {
    val request = ctx.request()
    val platformId = getHeaderParam(request, "X-PLATFORM-ID", UUID_REGEX)
    val authorizationId = getQueryParam(request, "id", "path", UUID_REGEX)

    authFeature.revokeAuthorization(platformId, ctx.user().get("sub"), authorizationId)
      .onSuccess { ctx.end() }
      .onFailure { ctx.fail(it) }
  }
}
