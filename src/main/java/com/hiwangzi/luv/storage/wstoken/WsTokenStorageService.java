package com.hiwangzi.luv.storage.wstoken;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

@ProxyGen
public interface WsTokenStorageService {

    static WsTokenStorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<WsTokenStorageService>> handler) {
        return new WsTokenStorageServiceImpl(asyncSQLClient, handler);
    }

    static WsTokenStorageService createProxy(Vertx vertx, String address) {
        return new WsTokenStorageServiceVertxEBProxy(vertx, address);
    }

    void retrieveWsHandlerId(String wsToken, Handler<AsyncResult<JsonObject>> handler);

    void updateWsHandlerIdHandleAccid(String wsToken, String textHandlerId, String binaryHandlerId,
                                      Handler<AsyncResult<JsonObject>> handler);

    void createWsToken(String wsToken, String accid, Handler<AsyncResult<Boolean>> handler);

    void deleteWsToken(String wsToken, Handler<AsyncResult<Boolean>> handler);

}
