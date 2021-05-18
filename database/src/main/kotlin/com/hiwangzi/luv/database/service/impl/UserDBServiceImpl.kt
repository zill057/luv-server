package com.hiwangzi.luv.database.service.impl

import com.hiwangzi.luv.database.service.UserDBService
import com.hiwangzi.luv.model.enumeration.UserIdentityType
import com.hiwangzi.luv.model.exception.SystemException
import com.hiwangzi.luv.model.resource.*
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Tuple
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class UserDBServiceImpl(private val pgPool: PgPool) : UserDBService {

  override fun findUserInformation(
    platformId: String, identityType: UserIdentityType, identifier: String,
    resultHandler: Handler<AsyncResult<UserInformation?>>
  ) {
    val sql = """
      SELECT t_identities.user_id as user_id,
             t_user.name as user_name,
             t_user.profile_photo,
             t_user.email,
             t_user.phone,
             t_dept.id as dept_id,
             t_dept.name as dept_name,
             t_org.id as org_id,
             t_org.name as org_name,
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
            val user = User(
              id = row.getUUID("user_id").toString(),
              name = row.getString("user_name"),
              profilePhoto = row.getString("profile_photo"),
              email = row.getString("email"),
              phone = row.getString("email"),
              department = Department(row.getUUID("dept_id").toString(), row.getString("dept_name")),
              organization = Organization(row.getUUID("org_id").toString(), row.getString("org_name"))
            )
            val identity = Identity(
              id = row.getUUID("identity_id").toString(),
              hashedCredential = row.getString("credential")
            )
            UserInformation(user, identity)
          }
        )
      }.onComplete { resultHandler.handle(it) }
  }

  override fun findUserById(platformId: String, userId: String, resultHandler: Handler<AsyncResult<User?>>) {
    com.hiwangzi.luv.database.service.common.user
      .findUserById(pgPool, platformId, userId)
      .onComplete { resultHandler.handle(it) }
  }

  override fun saveUserAuthorization(
    userId: String,
    device: Device,
    accessToken: String,
    refreshToken: String,
    accessTokenExpiredAt: Long,
    refreshTokenExpiredAt: Long,
    issuedAt: Long,
    resultHandler: Handler<AsyncResult<String>>
  ) {
    val sql = """
      INSERT INTO luv_user.user_authorizations
      (user_id, device, access_token, refresh_token, access_expired_at, refresh_expired_at, issued_at)
      VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id
    """.trimIndent()
    pgPool.preparedQuery(sql)
      .execute(
        Tuple.of(
          userId, device.toJson(), accessToken, refreshToken,
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(accessTokenExpiredAt), ZoneId.of("Asia/Shanghai")),
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(refreshTokenExpiredAt), ZoneId.of("Asia/Shanghai")),
          OffsetDateTime.ofInstant(Instant.ofEpochMilli(issuedAt), ZoneId.of("Asia/Shanghai"))
        )
      )
      .compose { rowSet ->
        val authorizationId = rowSet.firstOrNull()?.getUUID("id")
        if (authorizationId != null) {
          Future.succeededFuture(authorizationId.toString())
        } else {
          Future.failedFuture(SystemException("No returning authorization id"))
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
