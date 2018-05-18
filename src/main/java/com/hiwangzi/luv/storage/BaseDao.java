package com.hiwangzi.luv.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BaseDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);

    public static void updateWithParamsHandleAffecting1Row(AsyncSQLClient asyncSQLClient,
                                                           String sql, JsonArray params,
                                                           Handler<AsyncResult<Boolean>> handler) {
        asyncSQLClient.updateWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(ar.result().getUpdated() == 1));
            } else {
                handler.handle(Future.failedFuture("SQL query failed."));
                LOGGER.error("sql = {}, params = {}, cause = {}",
                        sql, params.toString(), ar.cause());
            }
        });
    }

    public static void queryWithParamsHandleJsonObject(AsyncSQLClient asyncSQLClient,
                                                       String sql, JsonArray params,
                                                       Handler<AsyncResult<JsonObject>> handler) {
        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                List<JsonObject> resultSet = ar.result().getRows();
                if (resultSet.size() == 1) {
                    handler.handle(Future.succeededFuture(resultSet.get(0)));
                } else {
                    handler.handle(Future.failedFuture("Error count of result set."));
                    LOGGER.error("sql = {}, params = {}, result set = {}",
                            sql, params.toString(), resultSet);
                }
            } else {
                handler.handle(Future.failedFuture("SQL query failed."));
                LOGGER.error("sql = {}, params = {}, cause = {}",
                        sql, params.toString(), ar.cause());
            }
        });
    }

    public static void queryWithParamsHandleList(AsyncSQLClient asyncSQLClient,
                                                 String sql, JsonArray params,
                                                 Handler<AsyncResult<List<JsonObject>>> handler) {
        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(ar.result().getRows()));
            } else {
                handler.handle(Future.failedFuture("SQL query failed."));
                LOGGER.error("sql = {}, params = {}, cause = {}",
                        sql, params.toString(), ar.cause());
            }
        });
    }

}
