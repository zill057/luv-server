package com.hiwangzi.luv.storage.account;

import com.hiwangzi.luv.storage.BaseDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

public class AccountStorageServiceImpl implements AccountStorageService {

    private final AsyncSQLClient asyncSQLClient;

    public AccountStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<AccountStorageService>> readyHandler) {
        this.asyncSQLClient = asyncSQLClient;
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void retrieveAccount(String token, Handler<AsyncResult<JsonObject>> handler) {
        final String sql = "SELECT accid FROM account WHERE token = ?";
        JsonArray params = new JsonArray().add(token);
        BaseDao.queryWithParamsHandleJsonObject(asyncSQLClient, sql, params, handler);
    }
}
