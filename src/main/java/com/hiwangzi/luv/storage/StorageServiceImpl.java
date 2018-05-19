package com.hiwangzi.luv.storage;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StorageServiceImpl implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceImpl.class);
    private final AsyncSQLClient asyncSQLClient;


    public StorageService saveMessage(String fromAccid, String toChannel, JsonObject message, long createTime,
                                      Handler<AsyncResult<Void>> handler) {

        final String sql = "INSERT INTO message_storage" +
                "(from_accid,to_channel,message,create_time) " +
                "VALUES(?,?,?,?)";
        JsonArray params = new JsonArray().add(fromAccid).add(toChannel).add(message.encode()).add(createTime);

        asyncSQLClient.updateWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", ar.cause());
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }


    @Override
    public StorageService saveMessageSync(String fromAccid, String toChannel, JsonObject message, long messageCreateTime,
                                          long createTime, String toAccid, Handler<AsyncResult<Void>> handler) {
        final String sql = "INSERT INTO message_sync" +
                "(from_accid,to_channel,message,message_create_time,create_time,to_accid) " +
                "VALUES(?,?,?,?,?,?)";
        JsonArray params = new JsonArray().add(fromAccid).add(toChannel).add(message.encode()).add(messageCreateTime)
                .add(createTime).add(toAccid);

        asyncSQLClient.updateWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", ar.cause());
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Fluent
    public StorageService getAccountByToken(String token, Handler<AsyncResult<List<JsonObject>>> handler) {

        final String sql = "SELECT accid, token, ws_token, text_handler_id, binary_handler_id " +
                "FROM account WHERE token = ?";
        JsonArray params = new JsonArray().add(token);

        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(ar.result().getRows()));
            } else {
                LOGGER.error("Database query error", ar.cause());
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Override
    public StorageService updateWsTokenByToken(String token, String wsToken, Handler<AsyncResult<Void>> handler) {

        final String sql = "UPDATE account SET ws_token = ? WHERE token = ?";
        JsonArray params = new JsonArray().add(wsToken).add(token);

        asyncSQLClient.updateWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", ar.cause());
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Override
    public StorageService updateWsHandlerIdByWsTokenRetuningAccount(String wsToken, String textHandlerId, String binaryHandlerId,
                                                                    Handler<AsyncResult<List<JsonObject>>> handler) {

        final String sql = "UPDATE account SET " +
                "text_handler_id = ? ," +
                "binary_handler_id = ? " +
                "WHERE ws_token = ?" +
                "RETURNING accid, token, ws_token, text_handler_id, binary_handler_id";
        JsonArray params = new JsonArray().add(textHandlerId).add(binaryHandlerId).add(wsToken);

        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(ar.result().getRows()));
            } else {
                LOGGER.error("Database query error", ar.cause());
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Override
    public StorageService clearWsTokenAndHandlerIdByWsToken(String wsToken, Handler<AsyncResult<Void>> handler) {

        final String sql = "UPDATE account SET ws_token = NULL, " +
                "text_handler_id = NULL, " +
                "binary_handler_id = NULL " +
                "WHERE ws_token = ?";
        JsonArray params = new JsonArray().add(wsToken);

        asyncSQLClient.updateWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", ar.cause());
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Override
    public StorageService getAccidAndHandlerIdListByChannelId(String channelId, Handler<AsyncResult<List<JsonObject>>> handler) {
        final String sql = "SELECT " +
                "  accid_t.accid, " +
                "  name, " +
                "  text_handler_id, " +
                "  binary_handler_id " +
                "FROM ( " +
                "       SELECT jsonb_array_elements_text(accounts) AS accid " +
                "       FROM channel " +
                "       WHERE channel_id = ? " +
                "     ) AS accid_t " +
                "  LEFT JOIN account ON accid_t.accid = account.accid";
        JsonArray params = new JsonArray().add(channelId);

        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(ar.result().getRows()));
            } else {
                LOGGER.error("Database query error", ar.cause());
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    StorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<StorageService>> readyHandler) {
        this.asyncSQLClient = asyncSQLClient;
        asyncSQLClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                readyHandler.handle(Future.failedFuture(ar.cause()));
            } else {
                readyHandler.handle(Future.succeededFuture(this));
            }
        });
    }
}
