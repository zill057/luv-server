package com.hiwangzi.luv.data;

import com.hiwangzi.luv.storage.StorageVerticle;
import com.hiwangzi.luv.storage.message.MessageStorageService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

public class DataProcessingServiceImpl implements DataProcessingService {

    private Vertx vertx;
    private MessageStorageService messageStorageService;

    @Override
    public DataProcessingService process(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        switch (data.getInteger("type", -1)) {
            case 1:
                processMessage(data.getString("fromAccid"), data.getJsonObject("payload"), resultHandler);
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
        String toChannel = payload.getString("toChannel");
        JsonObject message = payload.getJsonObject("message");
        long createTime = System.currentTimeMillis();

        messageStorageService.saveMessage(fromAccid, toChannel, message, createTime, ar -> {
            if (ar.succeeded()) {
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
        readyHandler.handle(Future.succeededFuture(this));
    }
}
