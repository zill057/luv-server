package com.hiwangzi.luv.storage.cmsync;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

@ProxyGen
public interface ChannelMessageSyncStorageService {

    static ChannelMessageSyncStorageService create(AsyncSQLClient asyncSQLClient,
                                                   Handler<AsyncResult<ChannelMessageSyncStorageService>> handler) {
        return new ChannelMessageSyncStorageServiceImpl(asyncSQLClient, handler);
    }

    static ChannelMessageSyncStorageService createProxy(Vertx vertx, String address) {
        return new ChannelMessageSyncStorageServiceVertxEBProxy(vertx, address);
    }

    void retrieveBatchCMS(String syncAccid, long messageId, boolean after, int limit, Handler<AsyncResult<JsonArray>> handler);

    void updateCMS(long messageId, JsonObject messageBody, Handler<AsyncResult<Boolean>> handler);

    void createCMS(long messageId, String fromAccid, String toChannel, long createTime, JsonObject messageBody, String syncAccid,
                   Handler<AsyncResult<Boolean>> handler);

    void deleteCMS(long messageId, Handler<AsyncResult<Boolean>> handler);

    void deleteBatchCMS(String syncAccid, String channelId, Handler<AsyncResult<Void>> handler);
}
