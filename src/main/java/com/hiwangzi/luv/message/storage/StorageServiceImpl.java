package com.hiwangzi.luv.message.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StorageServiceImpl implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceImpl.class);
    private final AsyncSQLClient asyncSQLClient;

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

    public StorageService saveMessage(String from, String to, JsonObject payload, Handler<AsyncResult<Void>> resultHandler) {

        final String sql = "INSERT INTO message(from_accid,to_accid,payload,create_time) VALUES(?,?,?,?)";
        JsonArray params = new JsonArray().add(from).add(to).add(payload.encode()).add(System.currentTimeMillis());

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
}
