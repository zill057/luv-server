package com.hiwangzi.luv.connection;

import com.hiwangzi.luv.data.DataProcessingService;
import com.hiwangzi.luv.data.DataProcessingVerticle;
import com.hiwangzi.luv.storage.StorageVerticle;
import com.hiwangzi.luv.storage.account.AccountStorageService;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

public class ConnectionListeningVerticle extends AbstractVerticle {

    private AccountStorageService accountStorageService;
    private DataProcessingService dataProcessingService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        accountStorageService = AccountStorageService.createProxy(vertx, StorageVerticle.CONFIG_ACCOUNT_DB_QUEUE);
        dataProcessingService = DataProcessingService.createProxy(vertx, DataProcessingVerticle.CONFIG_DATA_PROCESSING_QUEUE);

        Handler<HttpServerRequest> httpRequestHandler = httpRequest -> {
            Router router = Router.router(vertx);
            router.route().handler(routingContext -> {
                String token = routingContext.request().getHeader("token");
                if (StringUtil.isNullOrEmpty(token)) {
                    routingContext.fail(401);
                } else {
                    accountStorageService.getAccountByToken(
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
                accountStorageService.updateWsTokenByToken(
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
            if (StringUtils.isBlank(wsToken)) {
                ws.reject(401);
            } else {
                accountStorageService.updateWsHandlerIdByWsTokenRetuningAccount(
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
                                        JsonObject dataJson;
                                        try {
                                            dataJson = data.toJsonObject()
                                                    .put("fromAccid", accountList.get(0).getString("accid"));
                                            dataProcessingService.process(dataJson, ar -> {
                                                if (ar.succeeded()) {
                                                    JsonObject response = new JsonObject().put("code", 200);
                                                    if (ar.result() != null) {
                                                        response.put("data", ar.result());
                                                    }
                                                    vertx.eventBus().send(textHandlerID, response.encode());
                                                } else {
                                                    vertx.eventBus().send(textHandlerID, new JsonObject()
                                                            .put("code", 500).put("message", ar.cause().getMessage()).encode()
                                                    );
                                                }
                                            });
                                        } catch (Exception e) {
                                            vertx.eventBus().send(textHandlerID, new JsonObject()
                                                    .put("code", 400).put("message", e.getMessage())
                                            );
                                        }
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
