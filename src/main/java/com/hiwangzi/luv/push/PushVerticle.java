package com.hiwangzi.luv.push;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ServiceBinder;

public class PushVerticle extends AbstractVerticle {

    public static final String CONFIG_PUSH_QUEUE = "push.queue";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        PushService.create(vertx, ready -> {
            if (ready.succeeded()) {
                new ServiceBinder(vertx)
                        .setAddress(CONFIG_PUSH_QUEUE)
                        .register(PushService.class, ready.result());
                startFuture.complete();
            } else {
                startFuture.fail(ready.cause());
            }
        });

    }
}
