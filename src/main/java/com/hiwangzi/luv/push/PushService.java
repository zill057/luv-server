package com.hiwangzi.luv.push;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface PushService {

    static PushService create(Vertx vertx, Handler<AsyncResult<PushService>> readyHandler) {
        return new PushServiceImpl(vertx, readyHandler);
    }

    static PushService createProxy(Vertx vertx, String address) {
        return new PushServiceVertxEBProxy(vertx, address);
    }

    void createPush(String syncAccid, JsonObject push, Handler<AsyncResult<Void>> handler);
}
