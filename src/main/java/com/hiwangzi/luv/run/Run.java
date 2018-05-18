package com.hiwangzi.luv.run;


import com.hiwangzi.luv.connection.ConnectionListeningVerticle;
import com.hiwangzi.luv.processing.DataProcessingVerticle;
import com.hiwangzi.luv.push.PushVerticle;
import com.hiwangzi.luv.storage.StorageVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Run {

    private static Vertx vertx = Vertx.vertx();
    private static Logger LOGGER = LoggerFactory.getLogger(Run.class);

    public static void main(String[] args) {

        Future.<String>future(storageVerticleDeployment ->
                vertx.deployVerticle(StorageVerticle.class.getName(),
                        storageVerticleDeployment.completer())

        ).compose(id -> Future.<String>future(dataProcessingVerticleDeployment ->
                vertx.deployVerticle(DataProcessingVerticle.class.getName(),
                        dataProcessingVerticleDeployment.completer()))

        ).compose(id -> Future.<String>future(pushVerticleDeployment ->
                vertx.deployVerticle(PushVerticle.class.getName(),
                        pushVerticleDeployment.completer()))

        ).compose(id -> Future.<String>future(connectionListeningVerticleDeployment ->
                vertx.deployVerticle(ConnectionListeningVerticle.class.getName(),
                        connectionListeningVerticleDeployment.completer()))

        ).setHandler(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Deploy successfully");
            } else {
                LOGGER.error("Deploy unsuccessfully: ", ar.cause());
            }
        });
    }
}
