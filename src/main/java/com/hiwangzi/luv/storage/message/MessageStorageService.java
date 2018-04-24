package com.hiwangzi.luv.storage.message;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

@ProxyGen
public interface MessageStorageService {

    @Fluent
    MessageStorageService saveMessage(String fromAccid, String toChannel, JsonObject message, long createTime,
                                      Handler<AsyncResult<Void>> resultHandler);

    static MessageStorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<MessageStorageService>> readyHandler) {
        return new MessageStorageServiceImpl(asyncSQLClient, readyHandler);
    }

    static MessageStorageService createProxy(Vertx vertx, String address) {
        return new MessageStorageServiceVertxEBProxy(vertx, address);
    }

}
