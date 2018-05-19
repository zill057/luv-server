package com.hiwangzi.luv.processing;

import com.hiwangzi.luv.constant.Topic;
import com.hiwangzi.luv.storage.StorageVerticle;
import com.hiwangzi.luv.storage.channel.ChannelStorageService;
import com.hiwangzi.luv.storage.message.MessageStorageService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataProcessingServiceImpl implements DataProcessingService {

    private ChannelStorageService channelStorageService;
    private MessageStorageService messageStorageService;

    @Override
    public DataProcessingService process(JsonObject serverAppendage, JsonObject header, JsonObject payload,
                                         Handler<AsyncResult<JsonObject>> handler) {

        String fromAccid = serverAppendage.getString("S-from");
        switch (header.getString("topic", "")) {
            case Topic.CHANNEL_GET:
                getChannel(fromAccid, payload.getString("channelId"), handler);
                break;
            case Topic.CHANNEL_PATCH:
                patchChannel(fromAccid, payload.getString("channelId"), payload.getJsonObject("patch"), handler);
                break;
            case Topic.CHANNEL_POST:
                postChannel(fromAccid, payload.getJsonObject("channel"), handler);
                break;
            case Topic.CHANNEL_DELETE:
                deleteChannel(fromAccid, payload.getString("channelId"), handler);
                break;
            case Topic.MESSAGE_GET:
                getMessage(fromAccid, payload.getString("channelId"),
                        payload.getInteger("after", payload.getInteger("before")),
                        payload.getInteger("after") != null, handler);
                break;
            case Topic.MESSAGE_PUT:
                putMessage(fromAccid, payload.getString("channelId"),
                        payload.getLong("messageId"), payload.getJsonObject("messageBody"), handler);
                break;
            case Topic.MESSAGE_POST:
                postMessage(fromAccid, payload.getString("channelId"),
                        payload.getJsonObject("messageBody"), handler);
                break;
            case Topic.MESSAGE_DELETE:
                deleteMessage(fromAccid, payload.getString("channelId"),
                        payload.getLong("messageId"), handler);
                break;
            default:
                handler.handle(Future.failedFuture("Invalid topic in header."));
                break;
        }
        return this;
    }

    private void getChannel(String fromAccid, String channelId, Handler<AsyncResult<JsonObject>> handler) {
        channelStorageService.retrieveChannel(channelId, ar -> {
            if (ar.succeeded()) {
                JsonObject channel = new JsonObject();
                channel.put("channelType", ar.result().getInteger("channel_type"));
                channel.put("channelId", ar.result().getString("channel_id"));
                channel.put("channelName", ar.result().getString("channel_name"));
                channel.put("admins", new JsonArray(new JsonObject(ar.result().getString("admins"))
                        .stream().map(Map.Entry::getKey).collect(Collectors.toList()))
                );
                channel.put("accids", new JsonArray(new JsonObject(ar.result().getString("accids"))
                        .stream().map(Map.Entry::getKey).collect(Collectors.toList()))
                );
                handler.handle(Future.succeededFuture(new JsonObject().put("channel", channel)));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void patchChannel(String fromAccid, String channelId, JsonObject patch,
                              Handler<AsyncResult<JsonObject>> handler) {
        JsonObject change = new JsonObject();
        if (patch.getString("channelName") != null) {
            change.put("channel_name", patch.getString("channelName"));
        }

        if (change.size() == 0) {
            handler.handle(Future.failedFuture("Invalid patch."));
            return;
        }

        channelStorageService.updateChannel(channelId, change, ar -> {
            if (ar.succeeded()) {
                if (ar.result()) {
                    handler.handle(Future.succeededFuture(new JsonObject()));
                } else {
                    handler.handle(Future.failedFuture("Failed to execute update."));
                }
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });

    }

    private void postChannel(String fromAccid, JsonObject channel, Handler<AsyncResult<JsonObject>> handler) {

        Integer channelType = channel.getInteger("channelType");
        String channelName = channel.getString("channelName");
        JsonArray accids = channel.getJsonArray("accids");

        if (channelType == null || channelName == null || accids == null) {
            handler.handle(Future.failedFuture("Invalid channel."));
            return;
        }

        final String channelId = UUID.randomUUID().toString();
        final Long currentTimeMillis = System.currentTimeMillis();
        final JsonObject accidsJO = new JsonObject();
        for (Object accid : accids) {
            accidsJO.put(accid.toString(), new JsonObject().put("createTime", currentTimeMillis));
        }

        JsonObject channelToCreate = new JsonObject()
                .put("channel_id", channelId)
                .put("channel_type", channelType)
                .put("channel_name", channelName)
                .put("admins", new JsonObject().put(fromAccid, new JsonObject().put("createTime", currentTimeMillis)))
                .put("creator", new JsonObject().put(fromAccid, new JsonObject().put("createTime", currentTimeMillis)))
                .put("latest_active", currentTimeMillis)
                .put("accids", accidsJO);

        channelStorageService.createChannel(channelToCreate, ar -> {
            if (ar.succeeded()) {
                if (ar.result()) {
                    handler.handle(Future.succeededFuture(new JsonObject().put("channelId", channelId)));
                } else {
                    handler.handle(Future.failedFuture("Failed to execute update."));
                }
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void deleteChannel(String fromAccid, String channelId, Handler<AsyncResult<JsonObject>> handler) {

        // TODO need authentication
        channelStorageService.deleteChannel(channelId, ar -> {
            if (ar.succeeded()) {
                if (ar.result()) {
                    handler.handle(Future.succeededFuture(new JsonObject()));
                } else {
                    handler.handle(Future.failedFuture("Failed to execute update."));
                }
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void getMessage(String fromAccid, String channelId, long messageId, boolean after,
                            Handler<AsyncResult<JsonObject>> handler) {

        messageStorageService.retrieveMessageList(channelId, messageId, after, 10, ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Object origin : ar.result()) {
                    JsonObject resultElement = new JsonObject();
                    result.add(resultElement);
                    resultElement.put("messageId", ((JsonObject) origin).getLong("message_id"));
                    resultElement.put("fromAccid", ((JsonObject) origin).getString("from_accid"));
                    resultElement.put("createTime", ((JsonObject) origin).getLong("create_time"));
                    resultElement.put("messageBody", new JsonObject(((JsonObject) origin).getString("message_body")));
                }
                handler.handle(Future.succeededFuture(new JsonObject().put("messageList", result)));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void putMessage(String fromAccid, String channelId, long messageId, JsonObject messageBody,
                            Handler<AsyncResult<JsonObject>> handler) {
        messageStorageService.updateMessage(messageId, messageBody, ar -> {
            if (ar.succeeded()) {
                if (ar.result()) {
                    handler.handle(Future.succeededFuture(new JsonObject()));
                } else {
                    handler.handle(Future.failedFuture("Failed to execute update."));
                }
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void postMessage(String fromAccid, String channelId, JsonObject messageBody, Handler<AsyncResult<JsonObject>> handler) {
        long postTime = System.currentTimeMillis();
        messageStorageService.createMessage(fromAccid, channelId, postTime, messageBody, ar -> {
            if (ar.succeeded()) {
                if (ar.result() != null) {
                    handler.handle(Future.succeededFuture(new JsonObject().put("messageId", ar.result()).put("createTime", postTime)));
                } else {
                    handler.handle(Future.failedFuture("Failed to execute update."));
                }
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void deleteMessage(String fromAccid, String channelId, long messageId, Handler<AsyncResult<JsonObject>> handler) {

        // TODO need authentication
        messageStorageService.deleteMessage(messageId, ar -> {
            if (ar.succeeded()) {
                if (ar.result()) {
                    handler.handle(Future.succeededFuture(new JsonObject()));
                } else {
                    handler.handle(Future.failedFuture("Failed to execute update."));
                }
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    DataProcessingServiceImpl(Vertx vertx, Handler<AsyncResult<DataProcessingService>> readyHandler) {
        this.channelStorageService = ChannelStorageService.createProxy(vertx, StorageVerticle.STORAGE_CHANNEL);
        this.messageStorageService = MessageStorageService.createProxy(vertx, StorageVerticle.STORAGE_MESSAGE);
        readyHandler.handle(Future.succeededFuture(this));
    }
}
