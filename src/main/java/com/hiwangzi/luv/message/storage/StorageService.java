package com.hiwangzi.luv.message.storage;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

@ProxyGen
public interface StorageService {

    @Fluent
    StorageService saveMessage(String from, String to, JsonObject payload, Handler<AsyncResult<Void>> resultHandler);

    static StorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<StorageService>> readyHandler) {
        return new StorageServiceImpl(asyncSQLClient, readyHandler);
    }

    static StorageService createProxy(Vertx vertx, String address) {
        return new StorageServiceVertxEBProxy(vertx, address);
    }

}
