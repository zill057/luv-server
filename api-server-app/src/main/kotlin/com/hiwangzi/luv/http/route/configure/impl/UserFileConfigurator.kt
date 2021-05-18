package com.hiwangzi.luv.http.route.configure.impl

import com.hiwangzi.luv.auth.AuthFeature
import com.hiwangzi.luv.http.route.configure.RouteConfigurator
import com.hiwangzi.luv.model.exception.ParamException
import com.hiwangzi.luv.util.endJsonObject
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.jsonObjectOf

class UserFileConfigurator(
  private val vertx: Vertx,
  private val authFeature: AuthFeature,
  private val userFilesHost: String,
  private val processedDirectory: String,
) : RouteConfigurator() {

  override fun configure(router: Router) {

    /**
     * @api {post} /users/:id/files 上传文件 🎯
     * @apiName PostUserFile
     * @apiGroup File
     * @apiVersion 1.0.0
     *
     * @apiParam (Header参数) {UUID} X-PLATFORM-ID 平台ID
     * @apiParam (路径参数) {UUID} id 用户id
     * @apiParam (body参数) {File} file 文件
     * @apiParamExample 请求示例 (URL)
     *     /users/110ca843-24d8-4a26-acea-56c741998de1/files
     * @apiParamExample 请求示例（multipart/form-data）
     *     ----WebKitFormBoundary7MA4YWxkTrZu0gW
     *     Content-Disposition: form-data; name="file"; filename="bea8fe1c-f01f-4209-a523-16847cdd290e.jpg"
     *     Content-Type: image/jpeg
     *
     *     (data)
     *     ----WebKitFormBoundary7MA4YWxkTrZu0gW
     *
     * @apiSuccess (返回参数) {String} url 文件可访问url
     * @apiSuccessExample 返回示例
     *     {
     *       "url": "https://user-images.hiwangzi.com/110ca843-24d8-4a26-acea-56c741998de1/a5f4138f-786f-4bbf-b4fb-bf56473c21a7.jpeg"
     *     }
     */
    router.route(HttpMethod.POST, "/users/:id/files")
      .handler(authFeature.httpAuthenticationHandler)
      .handler { ctx ->
        val userId = ctx.user().get<String>("sub")
        val files = ctx.fileUploads()
        if (files.isNullOrEmpty()) {
          ctx.fail(ParamException(field = "file", value = "", issue = "File is null or empty", location = "body"))
          return@handler
        }
        val file = files.first()
        val name = file.uploadedFileName().split("/").last()
        val extension = file.fileName().split(".").last()
        val fs = vertx.fileSystem()
        fs.exists("$processedDirectory/$userId")
          .compose { existed ->
            if (existed) Future.succeededFuture()
            else fs.mkdirs("$processedDirectory/$userId")
          }
          .compose {
            fs.move(file.uploadedFileName(), "$processedDirectory/$userId/$name.$extension")
          }
          .onSuccess {
            ctx.response().endJsonObject(jsonObjectOf(Pair("url", "$userFilesHost/$userId/$name.$extension")))
          }
          .onFailure { ctx.fail(it) }
      }
  }
}
