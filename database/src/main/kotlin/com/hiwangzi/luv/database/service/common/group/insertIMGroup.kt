package com.hiwangzi.luv.database.service.common.group

import com.hiwangzi.luv.model.resource.IMGroup
import io.vertx.core.Future
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

fun insertIMGroup(sqlClient: SqlClient, name: String, profilePhoto: String): Future<IMGroup> {
  val sql = "INSERT INTO luv_im.groups(name, profile_photo) VALUES ($1, $2) RETURNING id"
  return sqlClient.preparedQuery(sql)
    .execute(Tuple.of(name, profilePhoto))
    .compose { rowSet ->
      Future.succeededFuture(
        IMGroup(
          id = rowSet.first().getUUID("id").toString(), name = name, profilePhoto = profilePhoto,
          creator = null, latestMessage = null
        )
      )
    }
}
