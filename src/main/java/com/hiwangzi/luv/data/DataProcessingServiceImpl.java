package com.hiwangzi.luv.data;

import com.hiwangzi.luv.constant.Channel;
import com.hiwangzi.luv.constant.Header;
import com.hiwangzi.luv.constant.Payload;
import com.hiwangzi.luv.constant.Server;
import com.hiwangzi.luv.push.PushService;
import com.hiwangzi.luv.push.PushVerticle;
import com.hiwangzi.luv.storage.StorageService;
import com.hiwangzi.luv.storage.StorageVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

public class DataProcessingServiceImpl implements DataProcessingService {

    private Vertx vertx;
    private StorageService storageService;
    private PushService pushService;

    @Override
    public DataProcessingService process(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        switch (data.getJsonObject(Header.SELF).getString(Header.TOPIC, "")) {
            case Header.CHAT_SEND:
                processMessage(
                        data.getString(Server.S_FROM),
                        data.getJsonObject(Payload.SELF),
                        resultHandler);
                break;
            case Header.CHAT_INIT:
                initChat(
                        data.getString(Server.S_FROM),
                        data.getJsonObject(Payload.SELF),
                        resultHandler);
            default:
                break;
        }
        return this;
    }

    private void processMessage(String fromAccid, JsonObject payload, Handler<AsyncResult<JsonObject>> handler) {

        if (StringUtils.isBlank(fromAccid)) {
            handler.handle(Future.failedFuture("bad fromAccid"));
            return;
        }
        String toChannel = payload.getString(Payload.TO_CHANNEL);
        JsonObject message = payload.getJsonObject("message");
        long createTime = System.currentTimeMillis();

        // 消息持久化
        storageService.saveMessage(fromAccid, toChannel, message, createTime, ar -> {
            if (ar.succeeded()) {

                pushService.push(fromAccid, toChannel, message, createTime, doNothing -> {
                    // TODO
                    System.out.println(doNothing.result());
                });

                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });

    }


    private void initChat(String fromAccid, JsonObject payload, Handler<AsyncResult<JsonObject>> handler) {

        if (StringUtils.isBlank(fromAccid)) {
            handler.handle(Future.failedFuture("bad fromAccid"));
            return;
        }
        Channel type = Channel.PRIVATE; // TODO 临时
        String name = payload.getString(Payload.CHANNEL_NAME);
        JsonArray accids = payload.getJsonArray(Payload.ACCIDS);
        long createTime = System.currentTimeMillis();

        storageService.initChannel(fromAccid, type, name, accids, createTime, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(new JsonObject().put("channelId", ar.result())));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });

    }

    DataProcessingServiceImpl(Vertx vertx, Handler<AsyncResult<DataProcessingService>> readyHandler) {
        // TODO
        this.vertx = vertx;
        this.storageService = StorageService.createProxy(this.vertx, StorageVerticle.CONFIG_DB_QUEUE);
        this.pushService = PushService.createProxy(this.vertx, PushVerticle.CONFIG_PUSH_QUEUE);
        readyHandler.handle(Future.succeededFuture(this));
    }
}
