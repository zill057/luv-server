package com.hiwangzi.luv.connection;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectionListeningVerticle extends AbstractVerticle {

    // TODO just for test
    private static final Map<String, JsonObject> tokenAccount = new HashMap<>();

    static {
        tokenAccount.put("wangzitoken", new JsonObject().put("accid", "wangzi"));
        tokenAccount.put("xiaomingtoken", new JsonObject().put("accid", "xiaoming"));
    }

    private Router router = Router.router(vertx);

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        HttpServer httpServer = vertx.createHttpServer();
        HttpServer webSocketServer = vertx.createHttpServer();

        httpServer.requestHandler(router::accept);
        router.get("/websocket").handler(routingContext -> {
            JsonObject account = tokenAccount.<JsonObject>get(routingContext.request().headers().get("token"));

            if (account != null) {
                String wsToken = UUID.randomUUID().toString();
                account.put("wsToken", wsToken);
                JsonObject response = new JsonObject().put("WebSocket", "ws://127.0.0.1:8090/?" + wsToken);
                routingContext.request().response()
                        .putHeader("Content-Type", "application/json;charset=utf-8")
                        .end(response.encode());
            } else {
                routingContext.fail(400);
            }
        });

        webSocketServer.websocketHandler(ws -> {
            final String textHandlerID = ws.textHandlerID();
            String wsToken = ws.query();
            JsonObject account = tokenAccount.<JsonObject>get(wsToken);
            if (account == null) {
                // 未经验证的请求
                ws.reject(400);
            } else {
                ws.handler(data -> {
                    account.put("wsTextHandlerId", textHandlerID);
                });
                ws.closeHandler(event -> account.remove("wsTextHandlerId"));
            }
        });

        httpServer.listen(80);
        webSocketServer.listen(8090);

        startFuture.complete();
    }

}
