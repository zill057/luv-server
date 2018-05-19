package com.hiwangzi.luv.storage.message;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

@ProxyGen
public interface MessageStorageService {

    static MessageStorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<MessageStorageService>> handler) {
        return new MessageStorageServiceImpl(asyncSQLClient, handler);
    }

    static MessageStorageService createProxy(Vertx vertx, String address) {
        return new MessageStorageServiceVertxEBProxy(vertx, address);
    }

    void retrieveMessageList(String channelId, long messageId, boolean after, int limit,
                             Handler<AsyncResult<JsonArray>> handler);

    void updateMessage(long messageId, JsonObject messageBody, Handler<AsyncResult<Boolean>> handler);

    void createMessage(String fromAccid, String toChannel, long createTime, JsonObject messageBody,
                       Handler<AsyncResult<Long>> handler);

    void deleteMessage(long messageId, Handler<AsyncResult<Boolean>> handler);
}
