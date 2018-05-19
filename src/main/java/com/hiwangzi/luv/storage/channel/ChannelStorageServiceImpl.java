package com.hiwangzi.luv.storage.channel;

import com.hiwangzi.luv.storage.BaseDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

public class ChannelStorageServiceImpl implements ChannelStorageService {

    private final AsyncSQLClient asyncSQLClient;

    public ChannelStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<ChannelStorageService>> readyHandler) {
        this.asyncSQLClient = asyncSQLClient;
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void retrieveChannel(String channelId, Handler<AsyncResult<JsonObject>> handler) {

        final String sql = "SELECT channel_type, channel_id, channel_name, admins, accids FROM channel " +
                " WHERE channel_id = ? AND is_del = 0";
        BaseDao.queryWithParamsHandleJsonObject(asyncSQLClient, sql, new JsonArray().add(channelId), handler);
    }

    @Override
    public void updateChannel(String channelId, JsonObject change, Handler<AsyncResult<Boolean>> handler) {

        String[] fieldChangesPermitted = new String[]{"channel_name", "accids"};
        boolean changePermitted = false;
        for (String fieldChange : fieldChangesPermitted) {
            if (change.containsKey(fieldChange)) {
                changePermitted = true;
            }
        }
        if (!changePermitted) {
            handler.handle(Future.succeededFuture(Boolean.FALSE));
            return;
        }

        final StringBuilder sql = new StringBuilder("UPDATE channel SET ");
        JsonArray params = new JsonArray();
        if (change.getString("channel_name") != null) {
            sql.append("channel_name = ?,");
            params.add(change.getString("channel_name"));
        }
        if (change.getJsonObject("accids") != null) {
            sql.append("accids = ?,");
            params.add(change.getJsonObject("accids").encode());
        }
        sql.deleteCharAt(sql.length() - 1).append("WHERE channel_id = ?");
        params.add(channelId);

        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql.toString(), params, handler);
    }

    @Override
    public void createChannel(JsonObject channel, Handler<AsyncResult<Boolean>> handler) {

        final String sql = "INSERT INTO channel(channel_id, channel_type, channel_name, admins, creator, latest_active, accids, is_del)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, 0)";
        JsonArray params = new JsonArray()
                .add(channel.getString("channel_id"))
                .add(channel.getInteger("channel_type"))
                .add(channel.getString("channel_name"))
                .add(channel.getJsonObject("admins").encode())
                .add(channel.getJsonObject("creator").encode())
                .add(channel.getLong("latest_active"))
                .add(channel.getJsonObject("accids").encode());
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql, params, handler);
    }

    @Override
    public void deleteChannel(String channelId, Handler<AsyncResult<Boolean>> handler) {

        final String sql = "UPDATE channel SET is_del = 1 WHERE channel_id = ? ";
        BaseDao.updateWithParamsHandleAffecting1Row(asyncSQLClient, sql, new JsonArray().add(channelId), handler);
    }
}
