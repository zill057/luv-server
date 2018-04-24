package com.hiwangzi.luv.storage.message;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageStorageServiceImpl implements MessageStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageStorageServiceImpl.class);
    private final AsyncSQLClient asyncSQLClient;


    @Override
    public MessageStorageService saveMessage(String fromAccid, String toChannel, JsonObject message, long createTime,
                                             Handler<AsyncResult<Void>> resultHandler) {

        final String sql = "INSERT INTO message(from_accid,to_channel,message,create_time) VALUES(?,?,?,?)";
        JsonArray params = new JsonArray().add(fromAccid).add(toChannel).add(message.encode()).add(createTime);

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

    MessageStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<MessageStorageService>> readyHandler) {
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
