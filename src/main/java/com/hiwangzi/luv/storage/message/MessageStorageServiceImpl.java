package com.hiwangzi.luv.storage.message;

import com.hiwangzi.luv.storage.BaseDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

public class MessageStorageServiceImpl implements MessageStorageService {

    private final AsyncSQLClient asyncSQLClient;

    public MessageStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<MessageStorageService>> readyHandler) {
        this.asyncSQLClient = asyncSQLClient;
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void retrieveMessageList(String channelId, long messageId, boolean after, int limit,
                                    Handler<AsyncResult<JsonArray>> handler) {

        StringBuilder sql = new StringBuilder("SELECT id AS message_id, from_accid, create_time, message_body FROM message ")
                .append(" WHERE to_channel = ? AND is_del = 0 ");
        if (after) {
            sql.append(" AND id > ?");
        } else {
            sql.append(" AND id < ?");
        }
        sql.append(" ORDER BY id LIMIT ?");
        BaseDao.queryWithParamsHandleJsonArray(asyncSQLClient, sql.toString(),
                new JsonArray().add(channelId).add(messageId).add(limit), handler);
    }

    @Override
    public void updateMessage(long messageId, JsonObject messageBody, Handler<AsyncResult<Boolean>> handler) {

        // TODO JSON-scheme 校验格式
        final String sql = "UPDATE message SET message_body = ? WHERE id = ?";
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient,
                sql, new JsonArray().add(messageBody.encode()).add(messageId), handler);
    }

    @Override
    public void createMessage(String fromAccid, String toChannel, long createTime, JsonObject messageBody,
                              Handler<AsyncResult<Long>> handler) {

        final String sql = "INSERT INTO message(from_accid, to_channel, create_time, message_body, is_del)" +
                " VALUES(?, ?, ?, ?, 0) RETURNING id AS message_id";
        JsonArray params = new JsonArray()
                .add(fromAccid)
                .add(toChannel)
                .add(createTime)
                .add(messageBody.encode());
        BaseDao.queryWithParamsHandleJsonObject(asyncSQLClient, sql, params, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(ar.result().getLong("message_id")));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void deleteMessage(long messageId, Handler<AsyncResult<Boolean>> handler) {

        final String sql = "UPDATE message SET is_del = 1 WHERE id = ?";
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql, new JsonArray().add(messageId), handler);
    }
}
