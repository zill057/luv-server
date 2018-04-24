package com.hiwangzi.luv.storage.account;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

import java.util.List;

@ProxyGen
public interface AccountStorageService {

    @Fluent
    AccountStorageService getAccountByToken(String token, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    AccountStorageService updateWsTokenByToken(String token, String wsToken, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    AccountStorageService getAccountByWsToken(String wsToken, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    AccountStorageService updateWsHandlerIdByWsTokenRetuningAccount(String wsToken, String textHandlerId, String binaryHandlerId,
                                                                    Handler<AsyncResult<List<JsonObject>>> resultHandler);

    static AccountStorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<AccountStorageService>> readyHandler) {
        return new AccountAccountStorageServiceImpl(asyncSQLClient, readyHandler);
    }

    static AccountStorageService createProxy(Vertx vertx, String address) {
        return new AccountStorageServiceVertxEBProxy(vertx, address);
    }

}
