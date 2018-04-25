package com.hiwangzi.luv.storage.push;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

import java.util.List;

@ProxyGen
public interface PushStorageService {

    @Fluent
    PushStorageService producePush(String fromAccid, String channelId, JsonObject pushContent,
                                   Handler<AsyncResult<List<Integer>>> resultHandler);

    static PushStorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<PushStorageService>> readyHandler) {
        return new PushStorageServiceImpl(asyncSQLClient, readyHandler);
    }

    static PushStorageService createProxy(Vertx vertx, String address) {
        return new PushStorageServiceVertxEBProxy(vertx, address);
    }
}
