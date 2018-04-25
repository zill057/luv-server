package com.hiwangzi.luv.data;

import com.hiwangzi.luv.constant.Constant;
import com.hiwangzi.luv.constant.Payload;
import com.hiwangzi.luv.constant.Topic;
import com.hiwangzi.luv.storage.StorageVerticle;
import com.hiwangzi.luv.storage.message.MessageStorageService;
import com.hiwangzi.luv.storage.push.PushStorageService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

public class DataProcessingServiceImpl implements DataProcessingService {

    private Vertx vertx;
    private MessageStorageService messageStorageService;
    private PushStorageService pushStorageService;

    @Override
    public DataProcessingService process(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        switch (data.getString(Constant.TOPIC, "")) {
            case Topic.CHAT_SEND:
                processMessage(
                        data.getString(Payload.FROM_ACCID),
                        data.getJsonObject(Constant.PAYLOAD),
                        resultHandler);
                break;
            default:
                break;
        }
        return this;
    }

    private void processMessage(String fromAccid, JsonObject payload, Handler<AsyncResult<JsonObject>> resultHandler) {

        if (StringUtils.isBlank(fromAccid)) {
            resultHandler.handle(Future.failedFuture("bad fromAccid"));
            return;
        }
        String toChannel = payload.getString(Payload.TO_CHANNEL);
        JsonObject message = payload.getJsonObject("message");
        long createTime = System.currentTimeMillis();

        // 消息持久化
        messageStorageService.saveMessage(fromAccid, toChannel, message, createTime, ar -> {
            if (ar.succeeded()) {

                JsonObject pushContent = new JsonObject()
                        .put("header", new JsonObject()
                                .put(Constant.TOPIC, Topic.CHAT_PUSH)
                                .put("createTime", System.currentTimeMillis())
                                .put(Payload.FROM_ACCID, fromAccid))
                        .put("payload", new JsonObject()
                                .put("message", message));
                pushStorageService.producePush(fromAccid, toChannel, pushContent, doNothing -> {
                    // TODO
                });

                resultHandler.handle(Future.succeededFuture());
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });

    }

    DataProcessingServiceImpl(Vertx vertx, Handler<AsyncResult<DataProcessingService>> readyHandler) {
        // TODO
        this.vertx = vertx;
        this.messageStorageService = MessageStorageService.createProxy(this.vertx, StorageVerticle.CONFIG_MESSAGE_DB_QUEUE);
        this.pushStorageService = PushStorageService.createProxy(this.vertx, StorageVerticle.CONFIG_PUSH_DB_QUEUE);
        readyHandler.handle(Future.succeededFuture(this));
    }
}
