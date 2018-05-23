package com.hiwangzi.luv.storage.channel;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;

@ProxyGen
public interface ChannelStorageService {

    static ChannelStorageService create(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<ChannelStorageService>> handler) {
        return new ChannelStorageServiceImpl(asyncSQLClient, handler);
    }

    static ChannelStorageService createProxy(Vertx vertx, String address) {
        return new ChannelStorageServiceVertxEBProxy(vertx, address);
    }

    void retrieveChannelByChannel(JsonObject channel, Handler<AsyncResult<JsonObject>> handler);

    void retrieveChannel(String channelId, Handler<AsyncResult<JsonObject>> handler);

    void updateChannel(String channelId, JsonObject change, Handler<AsyncResult<Boolean>> handler);

    void createChannel(JsonObject channel, Handler<AsyncResult<Boolean>> handler);

    void deleteChannel(String channelId, Handler<AsyncResult<Boolean>> handler);
}
