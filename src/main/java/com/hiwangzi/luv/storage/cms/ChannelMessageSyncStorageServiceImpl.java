package com.hiwangzi.luv.storage.cms;

import com.hiwangzi.luv.storage.BaseDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

public class ChannelMessageSyncStorageServiceImpl implements ChannelMessageSyncStorageService {

    private final AsyncSQLClient asyncSQLClient;

    public ChannelMessageSyncStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<ChannelMessageSyncStorageService>> readyHandler) {
        this.asyncSQLClient = asyncSQLClient;
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void retrieveBatchCMS(String syncAccid, long messageId, boolean after, int limit, Handler<AsyncResult<JsonArray>> handler) {

        StringBuilder sql = new StringBuilder("SELECT message_id, from_accid, to_channel, create_time, message_body FROM channel_message_sync ")
                .append(" WHERE sync_accid = ? AND is_del = 0 ");
        if (after) {
            sql.append(" AND message_id > ?");
        } else {
            sql.append(" AND message_id < ?");
        }
        sql.append(" ORDER BY id LIMIT ?");
        BaseDao.queryWithParamsHandleJsonArray(asyncSQLClient, sql.toString(),
                new JsonArray().add(syncAccid).add(messageId).add(limit), handler);
    }

    @Override
    public void updateCMS(long messageId, JsonObject messageBody, Handler<AsyncResult<Boolean>> handler) {

        // TODO JSON-scheme 校验格式
        final String sql = "UPDATE channel_message_sync SET message_body = ? WHERE message_id = ?";
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient,
                sql, new JsonArray().add(messageBody.encode()).add(messageId), handler);
    }

    @Override
    public void createCMS(long messageId, String fromAccid, String toChannel, long createTime, JsonObject messageBody, String syncAccid,
                          Handler<AsyncResult<Boolean>> handler) {

        final String sql = "INSERT INTO channel_message_sync(message_id, from_accid, to_channel, create_time, message_body, sync_accid, is_del)" +
                " VALUES(?, ?, ?, ?, ?, ?, 0)";
        JsonArray params = new JsonArray()
                .add(messageId)
                .add(fromAccid)
                .add(toChannel)
                .add(createTime)
                .add(messageBody.encode())
                .add(syncAccid);
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql, params, handler);
    }

    @Override
    public void deleteCMS(long messageId, Handler<AsyncResult<Boolean>> handler) {

        final String sql = "UPDATE channel_message_sync SET is_del = 1 WHERE message_id = ?";
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql, new JsonArray().add(messageId), handler);
    }

    @Override
    public void deleteBatchCMS(String syncAccid, String channelId, Handler<AsyncResult<Void>> handler) {

        final String sql = "UPDATE channel_message_sync SET is_del = 1 WHERE sync_accid = ? AND to_channel = ?";
        asyncSQLClient.updateWithParams(sql, new JsonArray().add(syncAccid).add(channelId), ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }
}
