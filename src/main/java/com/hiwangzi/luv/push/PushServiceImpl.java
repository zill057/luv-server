package com.hiwangzi.luv.push;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class PushServiceImpl implements PushService {

    private Vertx vertx;

    PushServiceImpl(Vertx vertx, Handler<AsyncResult<PushService>> readyHandler) {
        this.vertx = vertx;
        readyHandler.handle(Future.succeededFuture(this));
    }

}
