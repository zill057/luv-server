package com.hiwangzi.luv.storage.account;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class AccountAccountStorageServiceImpl implements AccountStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAccountStorageServiceImpl.class);
    private final AsyncSQLClient asyncSQLClient;

    AccountAccountStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<AccountStorageService>> readyHandler) {
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

    public AccountStorageService getAccountByToken(String token, Handler<AsyncResult<List<JsonObject>>> resultHandler) {

        final String sql = "SELECT accid, token, ws_token, text_handler_id, binary_handler_id FROM account WHERE token = ?";
        JsonArray params = new JsonArray().add(token);

        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(ar.result().getRows()));
            } else {
                LOGGER.error("Database query error", ar.cause());
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Override
    public AccountStorageService updateWsTokenByToken(String token, String wsToken, Handler<AsyncResult<Void>> resultHandler) {
        final String sql = "UPDATE account SET ws_token = ? WHERE token = ?";
        JsonArray params = new JsonArray().add(wsToken).add(token);

        asyncSQLClient.updateWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Database query error", ar.cause());
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Override
    public AccountStorageService updateWsHandlerIdByWsTokenRetuningAccount(String wsToken, String textHandlerId, String binaryHandlerId,
                                                                           Handler<AsyncResult<List<JsonObject>>> resultHandler) {

        final String sql = "UPDATE account SET " +
                "               text_handler_id = ? ," +
                "               binary_handler_id = ? " +
                "           WHERE ws_token = ?" +
                "           RETURNING accid, token, ws_token, text_handler_id, binary_handler_id";
        JsonArray params = new JsonArray().add(textHandlerId).add(binaryHandlerId).add(wsToken);

        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(ar.result().getRows()));
            } else {
                LOGGER.error("Database query error", ar.cause());
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    @Override
    public AccountStorageService getAccountByWsToken(String wsToken, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        final String sql = "SELECT accid, token, ws_token, text_handler_id, binary_handler_id FROM account WHERE ws_token = ?";
        JsonArray params = new JsonArray().add(wsToken);

        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(ar.result().getRows()));
            } else {
                LOGGER.error("Database query error", ar.cause());
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }
}
