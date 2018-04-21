package com.hiwangzi.luv.run;

import com.hiwangzi.luv.message.listening.MessageListeningVerticle;
import com.hiwangzi.luv.message.storage.StorageVerticle;
import com.hiwangzi.luv.connection.ConnectionListeningVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Run {

    private static Vertx vertx = Vertx.vertx();
    private static Logger LOGGER = LoggerFactory.getLogger(Run.class);

    public static void main(String[] args) {

        Future.<String>future(storageVerticleDeployment ->
                vertx.deployVerticle(StorageVerticle.class.getName(), storageVerticleDeployment.completer())

        ).compose(id -> Future.<String>future(messageListeningVerticleDeployment ->
                vertx.deployVerticle(MessageListeningVerticle.class.getName(), messageListeningVerticleDeployment.completer()))

        ).setHandler(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Deploy message verticle successfully");
            } else {
                LOGGER.error("Deploy message verticle unsuccessfully: ", ar.cause());
            }
        });

        vertx.deployVerticle(ConnectionListeningVerticle.class.getName(), ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Deploy connection verticle successfully");
            } else {
                LOGGER.error("Deploy connection verticle unsuccessfully: ", ar.cause());
            }
        });
    }
}
