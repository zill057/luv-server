package com.hiwangzi.luv.connection;

import com.hiwangzi.luv.processing.DataProcessingService;
import com.hiwangzi.luv.processing.DataProcessingVerticle;
import com.hiwangzi.luv.storage.StorageVerticle;
import com.hiwangzi.luv.storage.account.AccountStorageService;
import com.hiwangzi.luv.storage.wstoken.WsTokenStorageService;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * 用于管理连接请求（HTTP、WebSocket），并给予响应
 */
public class ConnectionListeningVerticle extends AbstractVerticle {

    private AccountStorageService accountStorageService;
    private WsTokenStorageService wsTokenStorageService;
    private DataProcessingService dataProcessingService;

    @Override
    public void start(Future<Void> startFuture) {

        this.accountStorageService = AccountStorageService.createProxy(vertx, StorageVerticle.STORAGE_ACCOUNT);
        this.wsTokenStorageService = WsTokenStorageService.createProxy(vertx, StorageVerticle.STORAGE_WSTOKEN);
        this.dataProcessingService = DataProcessingService.createProxy(vertx, DataProcessingVerticle.CONFIG_DATA_PROCESSING_QUEUE);

        Handler<HttpServerRequest> httpRequestHandler = httpRequest -> {
            Router router = Router.router(vertx);
            router.route().handler(routingContext -> {
                // TODO 后续可以考虑使用 JWT
                String token = routingContext.request().getHeader("token");
                if (StringUtil.isNullOrEmpty(token)) {
                    routingContext.fail(401);
                } else {
                    accountStorageService.retrieveAccount(token, account -> {
                        if (account.succeeded()) {
                            if (account.result() != null) {
                                routingContext.put("account", account.result());
                                routingContext.next();
                            } else {
                                routingContext.fail(401);
                            }
                        } else {
                            routingContext.fail(account.cause());
                        }
                    });
                }
            });

            // HTTP GET /websocket
            // Header token
            // Response JSON {"webSocket":"ws://127.0.0.1/?086729a9-8bc3-44a7-8aef-df23d753b1d5"}
            router.get("/websocket").handler(routingContext -> {
                String wsToken = UUID.randomUUID().toString();
                String accid = routingContext.<JsonObject>get("account").getString("accid");
                wsTokenStorageService.createWsToken(wsToken, accid, ar -> {
                    if (ar.succeeded()) {
                        JsonObject response = new JsonObject().put("webSocket", "ws://127.0.0.1/?" + wsToken);
                        routingContext.request().response()
                                .putHeader("Content-Type", "application/json;charset=utf-8")
                                .end(response.encode());
                    } else {
                        routingContext.fail(ar.cause());
                    }
                });

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
                wsTokenStorageService.retrieveWsHandlerId(wsToken, existedWsHandlerId -> {
                    if (existedWsHandlerId.succeeded()) {
                        if (existedWsHandlerId.result() == null) {
                            ws.close((short) 401, "Unauthorized");
                            return;
                        }
                        if (existedWsHandlerId.result().getJsonObject("text_handler_id") != null) {
                            ws.close((short) 403, "WsToken has been used.");
                            return;
                        }
                        // TODO rxJava
                        wsTokenStorageService.updateWsHandlerIdHandleAccid(wsToken, textHandlerID, binaryHandlerID,
                                updated -> {
                                    // 根据 ws_token 更新 handler_id 失败，服务端错误
                                    if (updated.failed()) {
                                        ws.close((short) 500, "Failed to handle websocket handler.");
                                        return;
                                    }
                                    // 不存在对应的 ws_token
                                    if (updated.result() == null) {
                                        ws.close((short) 401, "Unauthorized");
                                        return;
                                    }

                                    ws.handler(data -> {
                                        JsonObject dataJson;
                                        try {
                                            // TODO 使用 JSON-Schema 检测格式
                                            dataJson = data.toJsonObject();

                                            // 后续处理统一参数形式（服务端额外追加数据，请求header，请求payload）
                                            // 回调结果即为响应payload
                                            JsonObject serverAppendage = new JsonObject()
                                                    .put("S-from", updated.result().getString("accid"));
                                            JsonObject requestHeader = dataJson.getJsonObject("header");
                                            JsonObject requestPayload = dataJson.getJsonObject("payload");

                                            dataProcessingService.process(serverAppendage, requestHeader, requestPayload, ar -> {
                                                if (ar.succeeded()) {
                                                    JsonObject response = new JsonObject()
                                                            .put("header", requestHeader.put("code", 200))
                                                            .put("payload", ar.result());
                                                    vertx.eventBus().send(textHandlerID, response.encode());
                                                } else {
                                                    JsonObject responsePayload = new JsonObject().put("cause", ar.cause().getMessage());
                                                    JsonObject response = new JsonObject()
                                                            .put("header", requestHeader.put("code", 500))
                                                            .put("payload", responsePayload);
                                                    vertx.eventBus().send(textHandlerID, response.encode());
                                                }
                                            });
                                        } catch (Exception e) {
                                            // TODO 此处应当统一response格式
                                            vertx.eventBus().send(textHandlerID, e.getMessage());
                                        }
                                    });

                                    ws.closeHandler(event -> {
                                        wsTokenStorageService.deleteWsToken(wsToken, doNothing -> {
                                        });
                                    });
                                });
                    } else {
                        ws.close((short) 500, "Failed to check websocket handler.");
                    }
                });
            }
        };

        vertx.createHttpServer()
                .requestHandler(httpRequestHandler)
                .websocketHandler(webSocketHandler)
                .listen(80);

        startFuture.complete();
    }

}
