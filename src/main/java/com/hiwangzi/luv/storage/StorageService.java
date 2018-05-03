package com.hiwangzi.luv.storage;

import com.hiwangzi.luv.constant.Channel;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

import java.util.List;

@ProxyGen
public interface StorageService {

    @Fluent
    StorageService saveMessage(String fromAccid, String toChannel, JsonObject message, long createTime,
                               Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    StorageService initChannel(String fromAccid, Channel type, String name, JsonArray accids, long createTime,
                               Handler<AsyncResult<String>> resultHandler);

    @Fluent
    StorageService saveMessageSync(String fromAccid, String toChannel, JsonObject message, long messageCreateTime,
                                   long createTime, String toAccid, Handler<AsyncResult<Void>> resultHandler);

    /* ↓ by token */
    @Fluent
    StorageService getAccountByToken(String token, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    StorageService updateWsTokenByToken(String token, String wsToken, Handler<AsyncResult<Void>> resultHandler);
    /* ↑ by token */


    /* ↓ by wsToken */
    @Fluent
    StorageService updateWsHandlerIdByWsTokenRetuningAccount(String wsToken, String textHandlerId, String binaryHandlerId,
                                                             Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    StorageService clearWsTokenAndHandlerIdByWsToken(String wsToken, Handler<AsyncResult<Void>> resultHandler);
    /* ↑ by wsToken */

    @Fluent
    StorageService getAccidAndHandlerIdListByChannelId(String channelId, Handler<AsyncResult<List<JsonObject>>> resultHandler);


    static StorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<StorageService>> readyHandler) {
        return new StorageServiceImpl(asyncSQLClient, readyHandler);
    }

    static StorageService createProxy(Vertx vertx, String address) {
        return new StorageServiceVertxEBProxy(vertx, address);
    }

}
