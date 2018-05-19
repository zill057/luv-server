package com.hiwangzi.luv.storage;

import com.hiwangzi.luv.storage.channel.ChannelStorageService;
import com.hiwangzi.luv.storage.message.MessageStorageService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.serviceproxy.ServiceBinder;

public class StorageVerticle extends AbstractVerticle {

    public static final String CONFIG_DB_HOST = "db.luv.host";
    public static final String CONFIG_DB_PORT = "db.luv.port";
    public static final String CONFIG_DB_USERNAME = "db.luv.username";
    public static final String CONFIG_DB_PASSWORD = "db.luv.password";
    public static final String CONFIG_DB_NAME = "db.luv.name";
    public static final String CONFIG_DB_MAX_POOL_SIZE = "db.luv.max_pool_size";
    public static final String CONFIG_DB_QUEUE = "message.db.queue";
    public static final String STORAGE_CHANNEL = "storage.channel";
    public static final String STORAGE_MESSAGE = "storage.message";

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        AsyncSQLClient asyncSQLClient = PostgreSQLClient.createShared(vertx, new JsonObject()
                .put("host", config().getString(CONFIG_DB_HOST, "127.0.0.1"))
                .put("port", config().getInteger(CONFIG_DB_PORT, 5432))
                .put("username", config().getString(CONFIG_DB_USERNAME, "luv"))
                .put("password", config().getString(CONFIG_DB_PASSWORD, "zillPG"))
                .put("database", config().getString(CONFIG_DB_NAME, "luv"))
                .put("maxPoolSize", config().getInteger(CONFIG_DB_MAX_POOL_SIZE, 30)));

        Future.<Void>future(createMessageStorageFuture -> {
            StorageService.create(asyncSQLClient, ready -> {
                if (ready.succeeded()) {
                    new ServiceBinder(vertx)
                            .setAddress(CONFIG_DB_QUEUE)
                            .register(StorageService.class, ready.result());
                    createMessageStorageFuture.complete();
                } else {
                    createMessageStorageFuture.fail(ready.cause());
                }
            });

        }).compose(nothing -> Future.<Void>future(createChannelDao ->
                ChannelStorageService.create(asyncSQLClient, ar -> {
                    if (ar.succeeded()) {
                        new ServiceBinder(vertx)
                                .setAddress(STORAGE_CHANNEL)
                                .register(ChannelStorageService.class, ar.result());
                        createChannelDao.complete();
                    } else {
                        createChannelDao.fail(ar.cause());
                    }
                }))

        ).compose(nothing -> Future.<Void>future(createMessageDao ->
                MessageStorageService.create(asyncSQLClient, ar -> {
                    if (ar.succeeded()) {
                        new ServiceBinder(vertx)
                                .setAddress(STORAGE_MESSAGE)
                                .register(MessageStorageService.class, ar.result());
                        createMessageDao.complete();
                    } else {
                        createMessageDao.fail(ar.cause());
                    }
                }))

        ).setHandler(startFuture.completer());
    }
}
