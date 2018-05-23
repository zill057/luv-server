package com.hiwangzi.luv.storage.account;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

@ProxyGen
public interface AccountStorageService {

    static AccountStorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<AccountStorageService>> handler) {
        return new AccountStorageServiceImpl(asyncSQLClient, handler);
    }

    static AccountStorageService createProxy(Vertx vertx, String address) {
        return new AccountStorageServiceVertxEBProxy(vertx, address);
    }

    void retrieveAccount(String token, Handler<AsyncResult<JsonObject>> handler);

}
