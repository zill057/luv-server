package com.hiwangzi.luv.message.listening;

import com.hiwangzi.luv.message.storage.StorageService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class MessageListeningVerticle extends AbstractVerticle {

    private StorageService storageService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        storageService = StorageService
                .createProxy(vertx, "message.db.queue");

        vertx.createHttpServer().websocketHandler(ws -> ws.handler(buffer -> {
                    JsonObject message = buffer.toJsonObject();
                    String from = message.getString("from");
                    String to = message.getString("to");
                    JsonObject payload = message.getJsonObject("payload");
                    storageService.saveMessage(from, to, payload, nothing -> {

                    });
                })
        ).listen(9999);

        startFuture.complete();
    }

}
