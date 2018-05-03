package com.hiwangzi.luv.push;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface PushService {

    @Fluent
    PushService push(String fromAccid, String toChannel, JsonObject message, long messageCreateTime,
                     Handler<AsyncResult<Void>> resultHandler);

    static PushService create(Vertx vertx, Handler<AsyncResult<PushService>> readyHandler) {
        return new PushServiceImpl(vertx, readyHandler);
    }

    static PushService createProxy(Vertx vertx, String address) {
        return new PushServiceVertxEBProxy(vertx, address);
    }

}
