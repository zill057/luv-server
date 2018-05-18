package com.hiwangzi.luv.connection;

import com.hiwangzi.luv.processing.DataProcessingService;
import com.hiwangzi.luv.processing.DataProcessingVerticle;
import com.hiwangzi.luv.storage.StorageService;
import com.hiwangzi.luv.storage.StorageVerticle;
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

/**
 * 用于管理连接请求（HTTP、WebSocket），并给予响应
 */
public class ConnectionListeningVerticle extends AbstractVerticle {

    private StorageService storageService;
    private DataProcessingService dataProcessingService;

    @Override
    public void start(Future<Void> startFuture) {

        storageService = StorageService.createProxy(vertx, StorageVerticle.CONFIG_DB_QUEUE);
        dataProcessingService = DataProcessingService.createProxy(vertx, DataProcessingVerticle.CONFIG_DATA_PROCESSING_QUEUE);

        Handler<HttpServerRequest> httpRequestHandler = httpRequest -> {
            Router router = Router.router(vertx);
            router.route().handler(routingContext -> {
                // TODO 后续可以考虑使用 JWT
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

            // HTTP GET /websocket
            // Header token
            // Response JSON {"webSocket":"ws://127.0.0.1/?086729a9-8bc3-44a7-8aef-df23d753b1d5"}
            router.get("/websocket").handler(routingContext -> {
                String wsToken = UUID.randomUUID().toString();
                storageService.updateWsTokenByToken(
                        routingContext.<JsonObject>get("account").getString("token"),
                        wsToken,
                        ar -> {
                            if (ar.succeeded()) {
                                JsonObject response = new JsonObject().put("webSocket", "ws://127.0.0.1/?" + wsToken);
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
                storageService.updateWsHandlerIdByWsTokenRetuningAccount(
                        wsToken, textHandlerID, binaryHandlerID,
                        accountsRes -> {

                            // 根据 ws_token 更新 handler_id 失败，服务端错误
                            if (accountsRes.failed()) {
                                ws.reject(500);
                                return;
                            }

                            // TODO 1.0 版本暂时不考虑多终端同步问题
                            // 不存在对应的 ws_token
                            List<JsonObject> accountList = accountsRes.result();
                            if (accountList.size() == 0) {
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
                                            .put("S-from", accountList.get(0).getString("accid"));
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
                                storageService.clearWsTokenAndHandlerIdByWsToken(wsToken, nothing -> {
                                });
                            });
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
