package com.hiwangzi.luv.data;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ServiceBinder;

public class DataProcessingVerticle extends AbstractVerticle {

    public static final String CONFIG_DATA_PROCESSING_QUEUE = "data-processing.queue";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
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
