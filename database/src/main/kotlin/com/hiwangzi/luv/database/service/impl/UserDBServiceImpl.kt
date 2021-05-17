package com.hiwangzi.luv.database.service.impl

import com.hiwangzi.luv.database.service.UserDBService
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.exception.SystemError
import com.hiwangzi.luv.model.resource.Device
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class UserDBServiceImpl(private val pgPool: PgPool) : UserDBService {

  override fun findUserInformation(
    platformId: String,
    identityType: UserIdentityType,
    identifier: String,
    resultHandler: Handler<AsyncResult<JsonObject?>>
  ) {
    val sql = """
      SELECT t_identities.user_id as user_id,
             t_user.name as user_name,
             t_user.profile_photo,
             t_user.email,
             t_user.phone,
             t_dept.id as department_id,
             t_dept.name as department_name,
             t_org.id as organization_id,
             t_org.name as organization_name,
             t_identities.id as identity_id,
             t_identities.credential
      FROM luv_user.user_identities as t_identities
               JOIN luv_user.users as t_user ON t_identities.user_id = t_user.id
               LEFT JOIN luv_user.departments as t_dept ON t_user.department_id = t_dept.id
               LEFT JOIN luv_user.organizations as t_org ON t_dept.organization_id = t_org.id
      WHERE t_user.platform_id = $1
        AND identity_type = $2
        AND identifier = $3
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(Tuple.of(platformId, identityType.code, identifier))
      .compose { rowSet ->
        Future.succeededFuture(
          rowSet.firstOrNull()?.let { row ->
            val user = jsonObjectOf(
              Pair("id", row.getUUID("user_id").toString()),
              Pair("name", row.getString("user_name")),
              Pair("profilePhoto", row.getString("profile_photo")),
              Pair("email", row.getString("email")),
              Pair("phone", row.getString("email")),
              Pair(
                "department",
                jsonObjectOf(
                  Pair("id", row.getUUID("department_id").toString()),
                  Pair("name", row.getString("department_name")),
                )
              ),
              Pair(
                "organization",
                jsonObjectOf(
                  Pair("id", row.getUUID("organization_id").toString()),
                  Pair("name", row.getString("organization_name")),
                )
              )
            )
            val identity = jsonObjectOf(
              Pair("id", row.getUUID("identity_id").toString()),
              Pair("credential", row.getString("credential"))
            )
            jsonObjectOf(Pair("user", user), Pair("identity", identity))
          }
        )
      }.onComplete { resultHandler.handle(it) }
  }

  override fun findUserById(
    platformId: String,
    userId: String, resultHandler:
    Handler<AsyncResult<JsonObject?>>
  ) {
    val sql = """
      SELECT t_user.id as user_id,
             t_user.name as user_name,
             t_user.profile_photo,
             t_user.email,
             t_user.phone,
             t_dept.id as department_id,
             t_dept.name as department_name,
             t_org.id as organization_id,
             t_org.name as organization_name
      FROM luv_user.users as t_user
               LEFT JOIN luv_user.departments as t_dept ON t_user.department_id = t_dept.id
               LEFT JOIN luv_user.organizations as t_org ON t_dept.organization_id = t_org.id
      WHERE t_user.platform_id = $1
        AND t_user.id = $2
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(Tuple.of(platformId, userId))
      .compose { rowSet ->
        Future.succeededFuture(
          rowSet.firstOrNull()?.let { row ->
            jsonObjectOf(
              Pair("id", row.getUUID("user_id").toString()),
              Pair("name", row.getString("user_name")),
              Pair("profilePhoto", row.getString("profile_photo")),
              Pair("email", row.getString("email")),
              Pair("phone", row.getString("phone")),
              Pair(
                "department",
                jsonObjectOf(
                  Pair("id", row.getUUID("department_id").toString()),
                  Pair("name", row.getString("department_name")),
                )
              ),
              Pair(
                "organization",
                jsonObjectOf(
                  Pair("id", row.getUUID("organization_id").toString()),
                  Pair("name", row.getString("organization_name")),
                )
              )
            )
          }
        )
      }.onComplete { resultHandler.handle(it) }
  }

  override fun saveUserAuthorization(
    userId: String,
    token: String,
    device: Device,
    issuedAt: Long,
    expiredAt: Long,
    resultHandler: Handler<AsyncResult<String>>
  ) {
    val sql = """
      INSERT INTO luv_user.user_authorizations
      (user_id, token, device, issued_at, expired_at)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING id
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(
        Tuple.of(
          userId, token, device.toJson(),
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(issuedAt), ZoneId.of("Asia/Shanghai")),
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(expiredAt), ZoneId.of("Asia/Shanghai"))
        )
      )
      .compose { rowSet ->
        val authorizationId = rowSet.firstOrNull()?.getUUID("id")
        if (authorizationId != null) {
          Future.succeededFuture(authorizationId.toString())
        } else {
          Future.failedFuture(SystemError("No returning authorization id"))
        }
      }
      .onComplete { resultHandler.handle(it) }
  }

  override fun revokeUserAuthorization(
    userId: String,
    authorizationId: String,
    resultHandler: Handler<AsyncResult<Void>>
  ) {
    val sql = """
      UPDATE luv_user.user_authorizations SET revoked_at = $1, updated_at = $1
      WHERE user_id = $2 AND id = $3 AND revoked_at IS NULL
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(
        Tuple.of(
          OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("Asia/Shanghai")),
          userId, authorizationId
        )
      )
      .onSuccess { resultHandler.handle(Future.succeededFuture()) }
      .onFailure { resultHandler.handle(Future.failedFuture(it)) }
  }

}
