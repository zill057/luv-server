package com.hiwangzi.luv.storage.wstoken;

import com.hiwangzi.luv.storage.BaseDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

public class WsTokenStorageServiceImpl implements WsTokenStorageService {

    private final AsyncSQLClient asyncSQLClient;

    public WsTokenStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<WsTokenStorageService>> readyHandler) {
        this.asyncSQLClient = asyncSQLClient;
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void retrieveWsHandlerId(String wsToken, Handler<AsyncResult<JsonObject>> handler) {
        final String sql = "SELECT text_handler_id, binary_handler_id FROM wstoken WHERE ws_token = ?";
        JsonArray params = new JsonArray().add(wsToken);
        BaseDao.queryWithParamsHandleJsonObject(asyncSQLClient, sql, params, handler);
    }

    @Override
    public void updateWsHandlerIdHandleAccid(String wsToken, String textHandlerId, String binaryHandlerId,
                                             Handler<AsyncResult<JsonObject>> handler) {

        final String sql = "UPDATE wstoken SET text_handler_id = ?, binary_handler_id = ? WHERE ws_token = ? RETURNING accid";
        JsonArray params = new JsonArray().add(textHandlerId).add(binaryHandlerId).add(wsToken);
        BaseDao.queryWithParamsHandleJsonObject(asyncSQLClient, sql, params, handler);
    }

    @Override
    public void createWsToken(String wsToken, String accid, Handler<AsyncResult<Boolean>> handler) {
        final String sql = "INSERT INTO wstoken(ws_token, accid) VALUES (?, ?)";
        JsonArray params = new JsonArray().add(wsToken).add(accid);
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql, params, handler);
    }

    @Override
    public void deleteWsToken(String wsToken, Handler<AsyncResult<Boolean>> handler) {
        final String sql = "DELETE FROM wstoken WHERE ws_token = ?";
        JsonArray params = new JsonArray().add(wsToken);
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql, params, handler);
    }
}
