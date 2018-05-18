package com.hiwangzi.luv.processing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * 用于注册 Service 到 EventBus
 */
public class DataProcessingVerticle extends AbstractVerticle {

    public static final String CONFIG_DATA_PROCESSING_QUEUE = "processing.queue";

    @Override
    public void start(Future<Void> startFuture) {
        DataProcessingService.create(vertx, ready -> {
            if (ready.succeeded()) {
                new ServiceBinder(vertx)
                        .setAddress(CONFIG_DATA_PROCESSING_QUEUE)
                        .register(DataProcessingService.class, ready.result());
                startFuture.complete();
            } else {
                startFuture.fail(ready.cause());
            }
        });
    }
}
