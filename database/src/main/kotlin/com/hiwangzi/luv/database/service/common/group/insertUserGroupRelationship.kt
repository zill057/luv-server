package com.hiwangzi.luv.database.service.common.group

import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

fun insertUserGroupRelationships(sqlClient: SqlClient, groupId: String, vararg userIds: String): Future<Void> {
  val sql = "INSERT INTO luv_im.user_group_relationships(group_id, user_id) VALUES ($1, $2), ($1, $3)"
  return sqlClient.preparedQuery(sql)
    .execute(Tuple.of(groupId, *userIds))
    .compose { Future.succeededFuture() }
}
