package com.hiwangzi.luv.connection;

import com.hiwangzi.luv.storage.StorageService;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.List;
import java.util.UUID;

public class ConnectionListeningVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        StorageService storageService = StorageService.createProxy(vertx, "db.queue");

        Handler<HttpServerRequest> httpRequestHandler = httpRequest -> {
            Router router = Router.router(vertx);
            router.route().handler(routingContext -> {
                String token = routingContext.request().getHeader("token");
                if (StringUtil.isNullOrEmpty(token)) {
                    routingContext.fail(401);
                } else {
                    storageService.getAccountByToken(
                            token,
                            accountsRes -> {
                                if (accountsRes.succeeded()) {
                                    // 目前 ws_token 同样存储在数据库，因此可能会查询出多个 account
                                    // TODO 但 1.0 版本暂时不考虑多终端同步问题
                                    List<JsonObject> accountList = accountsRes.result();
                                    if (accountList.size() == 0) {
                                        routingContext.fail(401);
                                    } else {
                                        routingContext.put("account", accountList.get(0));
                                        routingContext.next();
                                    }
                                } else {
                                    routingContext.fail(accountsRes.cause());
                                }
                            }
                    );
                }
            });
            router.get("/websocket").handler(routingContext -> {
                String wsToken = UUID.randomUUID().toString();
                storageService.updateWsTokenByToken(
                        routingContext.<JsonObject>get("account").getString("token"),
                        wsToken,
                        ar -> {
                            if (ar.succeeded()) {
                                JsonObject response = new JsonObject().put("WebSocket", "ws://127.0.0.1/?" + wsToken);
                                routingContext.request().response()
                                        .putHeader("Content-Type", "application/json;charset=utf-8")
                                        .end(response.encode());
                            } else {
                                routingContext.fail(ar.cause());
                            }
                        }
                );
            });
            router.accept(httpRequest);
        };

        Handler<ServerWebSocket> webSocketHandler = ws -> {

            String wsToken = ws.query();
            final String textHandlerID = ws.textHandlerID();
            final String binaryHandlerID = ws.binaryHandlerID();
            if (StringUtil.isNullOrEmpty(wsToken)) {
                ws.reject(401);
            } else {
                storageService.updateWsHandlerIdByWsTokenRetuningAccount(
                        wsToken, textHandlerID, binaryHandlerID,
                        accountsRes -> {
                            if (accountsRes.succeeded()) {
                                // 目前 ws_token 同样存储在数据库，因此可能会查询出多个 account
                                // TODO 但 1.0 版本暂时不考虑多终端同步问题
                                List<JsonObject> accountList = accountsRes.result();
                                if (accountList.size() == 0) {
                                    ws.reject(401);
                                } else {
                                    ws.handler(data -> {
                                        // TODO
                                    });
                                    ws.closeHandler(event -> {
                                        // TODO
                                    });
                                }
                            } else {
                                ws.reject(500);
                            }
                        }
                );
            }
        };

        vertx.createHttpServer()
                .requestHandler(httpRequestHandler)
                .websocketHandler(webSocketHandler)
                .listen(80);

        startFuture.complete();
    }

}
