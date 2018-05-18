package com.hiwangzi.luv.processing;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface DataProcessingService {

    @Fluent
    DataProcessingService process(JsonObject serverAppendage, JsonObject header, JsonObject payload,
                                  Handler<AsyncResult<JsonObject>> handler);

    static DataProcessingService create(Vertx vertx, Handler<AsyncResult<DataProcessingService>> readyHandler) {
        return new DataProcessingServiceImpl(vertx, readyHandler);
    }

    static DataProcessingService createProxy(Vertx vertx, String address) {
        return new DataProcessingServiceVertxEBProxy(vertx, address);
    }
}
