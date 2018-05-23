package com.hiwangzi.luv.storage;

import com.hiwangzi.luv.storage.account.AccountStorageService;
import com.hiwangzi.luv.storage.channel.ChannelStorageService;
import com.hiwangzi.luv.storage.cms.ChannelMessageSyncStorageService;
import com.hiwangzi.luv.storage.message.MessageStorageService;
import com.hiwangzi.luv.storage.wstoken.WsTokenStorageService;
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
    public static final String STORAGE_CMS = "storage.channel-message-sync";
    public static final String STORAGE_ACCOUNT = "storage.account";
    public static final String STORAGE_WSTOKEN = "storage.ws-token";

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        AsyncSQLClient asyncSQLClient = PostgreSQLClient.createShared(vertx, new JsonObject()
                .put("host", config().getString(CONFIG_DB_HOST, "127.0.0.1"))
                .put("port", config().getInteger(CONFIG_DB_PORT, 5432))
                .put("username", config().getString(CONFIG_DB_USERNAME, "luv"))
                .put("password", config().getString(CONFIG_DB_PASSWORD, "zillPG"))
                .put("database", config().getString(CONFIG_DB_NAME, "luv"))
                .put("maxPoolSize", config().getInteger(CONFIG_DB_MAX_POOL_SIZE, 30)));

        (Future.<Void>future(createChannelDao ->
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

        ).compose(nothing -> Future.<Void>future(createCMSDao ->
                ChannelMessageSyncStorageService.create(asyncSQLClient, ar -> {
                    if (ar.succeeded()) {
                        new ServiceBinder(vertx)
                                .setAddress(STORAGE_CMS)
                                .register(ChannelMessageSyncStorageService.class, ar.result());
                        createCMSDao.complete();
                    } else {
                        createCMSDao.fail(ar.cause());
                    }
                }))

        ).compose(nothing -> Future.<Void>future(createAccountDao ->
                AccountStorageService.create(asyncSQLClient, ar -> {
                    if (ar.succeeded()) {
                        new ServiceBinder(vertx)
                                .setAddress(STORAGE_ACCOUNT)
                                .register(AccountStorageService.class, ar.result());
                        createAccountDao.complete();
                    } else {
                        createAccountDao.fail(ar.cause());
                    }
                }))

        ).compose(nothing -> Future.<Void>future(createWsTokenDao ->
                WsTokenStorageService.create(asyncSQLClient, ar -> {
                    if (ar.succeeded()) {
                        new ServiceBinder(vertx)
                                .setAddress(STORAGE_WSTOKEN)
                                .register(WsTokenStorageService.class, ar.result());
                        createWsTokenDao.complete();
                    } else {
                        createWsTokenDao.fail(ar.cause());
                    }
                }))

        ).setHandler(startFuture.completer());
    }
}
