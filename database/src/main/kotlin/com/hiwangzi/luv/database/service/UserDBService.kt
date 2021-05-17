package com.hiwangzi.luv.database.service

import com.hiwangzi.luv.database.service.impl.UserDBServiceImpl
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.resource.Device
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.pgclient.PgPool

@ProxyGen
interface UserDBService {

  /**
   * user information
   * <pre>
   * {
   *   "user": {
   *     "id": "",
   *     "name": "",
   *     "profilePhoto": "",
   *     "department": {
   *       "id": "b555b982-66e5-40c7-9658-670e826e94cd",
   *       "name": "天才吧"
   *     },
   *     "organization": {
   *       "id": "38a21c11-90a4-430b-bd99-e18253337ea9",
   *       "name": "苹果电子产品商贸（北京）有限公司"
   *     }
   *   },
   *   "identity": {
   *     "id": "",
   *     "credential": ""
   *   }
   * }
   * </pre>
   *
   *
   * Find user's info by user's identifier (e.g username, phone)
   *
   * @param platformId    Platform client's ID
   * @param identityType  Identify type
   * @param identifier    Identifier, e.g username, phone
   * @param resultHandler handle result `user information`, `null` if not fount
   */
  fun findUserInformation(
    platformId: String, identityType: UserIdentityType, identifier: String,
    resultHandler: Handler<AsyncResult<JsonObject?>>
  )

  /**
   * user
   * <pre>
   * {
   *   "id": "",
   *   "name": "",
   *   "profilePhoto": "",
   *   "department": {
   *     "id": "b555b982-66e5-40c7-9658-670e826e94cd",
   *     "name": "天才吧"
   *   },
   *   "organization": {
   *     "id": "38a21c11-90a4-430b-bd99-e18253337ea9",
   *     "name": "苹果电子产品商贸（北京）有限公司"
   *   }
   * }
   * </pre>
   *
   *
   * Find user by user id
   *
   * @param platformId    Platform client's ID
   * @param userId    user id
   * @param resultHandler handle result `user`, <strong><code>null</code> if not fount</strong>
   */
  fun findUserById(platformId: String, userId: String, resultHandler: Handler<AsyncResult<JsonObject?>>)

  /**
   * Save generated user's authorization
   *
   * @param userId User's id
   * @param token Generated token
   * @param device User device name
   * @param issuedAt Token issued time, milliseconds
   * @param expiredAt Token expired time, milliseconds
   * @param resultHandler handle result `authorization id`
   */
  fun saveUserAuthorization(
    userId: String,
    token: String,
    device: Device,
    issuedAt: Long,
    expiredAt: Long,
    resultHandler: Handler<AsyncResult<String>>
  )

  fun revokeUserAuthorization(
    userId: String,
    authorizationId: String,
    resultHandler: Handler<AsyncResult<Void>>
  )

}

object UserDBServiceFactory {
  const val ADDRESS = "user-db-service-address"

  fun create(pgPool: PgPool): UserDBService {
    return UserDBServiceImpl(pgPool)
  }

  fun createProxy(vertx: Vertx): UserDBService {
    return UserDBServiceVertxEBProxy(vertx, ADDRESS)
  }
}
