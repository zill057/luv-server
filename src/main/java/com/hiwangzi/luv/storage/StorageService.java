package com.hiwangzi.luv.storage;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

import java.util.List;

@ProxyGen
public interface StorageService {

    @Fluent
    StorageService saveMessageSync(String fromAccid, String toChannel, JsonObject message, long messageCreateTime,
                                   long createTime, String toAccid, Handler<AsyncResult<Void>> handler);

    /* ↓ by token */
    @Fluent
    StorageService getAccountByToken(String token, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    StorageService updateWsTokenByToken(String token, String wsToken, Handler<AsyncResult<Void>> handler);
    /* ↑ by token */


    /* ↓ by wsToken */
    @Fluent
    StorageService updateWsHandlerIdByWsTokenRetuningAccount(String wsToken, String textHandlerId, String binaryHandlerId,
                                                             Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    StorageService clearWsTokenAndHandlerIdByWsToken(String wsToken, Handler<AsyncResult<Void>> handler);
    /* ↑ by wsToken */

    @Fluent
    StorageService getAccidAndHandlerIdListByChannelId(String channelId, Handler<AsyncResult<List<JsonObject>>> handler);


    static StorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<StorageService>> readyHandler) {
        return new StorageServiceImpl(asyncSQLClient, readyHandler);
    }

    static StorageService createProxy(Vertx vertx, String address) {
        return new StorageServiceVertxEBProxy(vertx, address);
    }

}
