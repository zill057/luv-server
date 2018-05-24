package com.hiwangzi.luv.push;

import com.hiwangzi.luv.constant.Topic;
import com.hiwangzi.luv.storage.StorageVerticle;
import com.hiwangzi.luv.storage.wstoken.WsTokenStorageService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PushServiceImpl implements PushService {

    private Vertx vertx;
    private WsTokenStorageService wsTokenStorageService;


    PushServiceImpl(Vertx vertx, Handler<AsyncResult<PushService>> readyHandler) {
        this.vertx = vertx;
        this.wsTokenStorageService = WsTokenStorageService.createProxy(vertx, StorageVerticle.STORAGE_WSTOKEN);
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void createPush(String syncAccid, JsonObject push, Handler<AsyncResult<Void>> handler) {
        final EventBus eventBus = vertx.eventBus();
        getWsTextHandlerIdJsonArray(syncAccid, textHandlerIdList -> {
            if (textHandlerIdList.succeeded()) {
                for (String eventBusAddress : textHandlerIdList.result()) {
                    JsonObject eventBusMessage = new JsonObject()
                            .put("header",
                                    new JsonObject().put("topic", Topic.PUSH_POST).put("id", UUID.randomUUID().toString()))
                            .put("payload",
                                    new JsonObject().put("push", push));
                    eventBus.send(eventBusAddress, eventBusMessage.encode());
                }
                // TODO 需要检查推送是否成功
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(textHandlerIdList.cause()));
            }
        });
    }

    private void getWsTextHandlerIdJsonArray(String accid, Handler<AsyncResult<List<String>>> handler) {
        wsTokenStorageService.retrieveWsHandlerIdByAccid(accid, ar -> {
            if (ar.succeeded()) {
                List<String> textHandlerIdList = new ArrayList<>();
                for (Object handlerId : ar.result()) {
                    String textHandlerId = ((JsonObject) handlerId).getString("text_handler_id");
                    if (textHandlerId != null) {
                        textHandlerIdList.add(textHandlerId);
                    }
                }
                handler.handle(Future.succeededFuture(textHandlerIdList));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }
}
