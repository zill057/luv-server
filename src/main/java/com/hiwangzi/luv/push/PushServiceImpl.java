package com.hiwangzi.luv.push;

import com.hiwangzi.luv.storage.StorageService;
import com.hiwangzi.luv.storage.StorageVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class PushServiceImpl implements PushService {

    private Vertx vertx;
    private StorageService storageService;

    // TODO 需要重构
    @Override
    public PushService push(String fromAccid, String toChannel, JsonObject message, long messageCreateTime,
                            Handler<AsyncResult<Void>> resultHandler) {
        storageService.getAccidAndHandlerIdListByChannelId(toChannel, accountListRes -> {
            if (accountListRes.succeeded()) {
                // 过滤掉发送者
                List<JsonObject> accountList = accountListRes.result().parallelStream()
                        .filter(j -> !j.getString("accid").equals(fromAccid))
                        .collect(Collectors.toList());
                for (JsonObject account : accountList) {
                    String address = account.getString("text_handler_id");
                    if (StringUtils.isNotBlank(address)) {
                        // 直接推送
                        JsonObject pushContent = new JsonObject()
                                .put("header", new JsonObject()
                                        .put("topic", "chat.push")
                                        .put("createTime", System.currentTimeMillis()))
                                .put("payload", new JsonObject()
                                        .put("message", message));
                        vertx.eventBus().send(address, pushContent.encode());
                    } else {
                        // 暂存同步库
                        long createTime = System.currentTimeMillis(); // 推送创建时间
                        storageService.saveMessageSync(fromAccid, toChannel, message, messageCreateTime,
                                createTime, account.getString("accid"), nothing -> {
                                }
                        );
                    }
                }
            } else {
                resultHandler.handle(Future.failedFuture(accountListRes.cause()));
            }
        });
        return this;
    }

    PushServiceImpl(Vertx vertx, Handler<AsyncResult<PushService>> readyHandler) {
        this.vertx = vertx;
        this.storageService = StorageService.createProxy(this.vertx, StorageVerticle.CONFIG_DB_QUEUE);
        readyHandler.handle(Future.succeededFuture(this));
    }

}
