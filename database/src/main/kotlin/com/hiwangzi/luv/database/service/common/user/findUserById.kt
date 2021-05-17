package com.hiwangzi.luv.database.service.common.user

import com.hiwangzi.luv.model.resource.Department
import com.hiwangzi.luv.model.resource.Organization
import com.hiwangzi.luv.model.resource.User
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

fun findUserById(sqlClient: SqlClient, platformId: String, userId: String): Future<User?> {
  val sql = """
      SELECT t_user.id as user_id,
             t_user.name as user_name,
             t_user.profile_photo,
             t_user.email,
             t_user.phone,
             t_dept.id as dept_id,
             t_dept.name as dept_name,
             t_org.id as org_id,
             t_org.name as org_name
      FROM luv_user.users as t_user
               LEFT JOIN luv_user.departments as t_dept ON t_user.department_id = t_dept.id
               LEFT JOIN luv_user.organizations as t_org ON t_dept.organization_id = t_org.id
      WHERE t_user.platform_id = $1
        AND t_user.id = $2
    """.trimIndent()
  return sqlClient.preparedQuery(sql)
    .execute(Tuple.of(platformId, userId))
    .compose { rowSet ->
      Future.succeededFuture(
        rowSet.firstOrNull()?.let { row ->
          User(
            id = row.getUUID("user_id").toString(),
            name = row.getString("user_name"),
            profilePhoto = row.getString("profile_photo"),
            email = row.getString("email"),
            phone = row.getString("phone"),
            department = Department(row.getUUID("dept_id").toString(), row.getString("dept_name")),
            organization = Organization(row.getUUID("org_id").toString(), row.getString("org_name"))
          )
        }
      )
    }
}
