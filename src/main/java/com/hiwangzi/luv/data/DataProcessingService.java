package com.hiwangzi.luv.data;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface DataProcessingService {

    @Fluent
    DataProcessingService process(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler);

    static DataProcessingService create(Vertx vertx, Handler<AsyncResult<DataProcessingService>> readyHandler) {
        return new DataProcessingServiceImpl(vertx, readyHandler);
    }

    static DataProcessingService createProxy(Vertx vertx, String address) {
        return new DataProcessingServiceVertxEBProxy(vertx, address);
    }
}
