package com.hiwangzi.luv.storage.push;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PushStorageServiceImpl implements PushStorageService {


    private static final Logger LOGGER = LoggerFactory.getLogger(PushStorageServiceImpl.class);
    private final AsyncSQLClient asyncSQLClient;

    @Override
    public PushStorageService producePush(String fromAccid, String channelId, JsonObject pushContent,
                                          Handler<AsyncResult<List<Integer>>> resultHandler) {

        final String sql = "INSERT INTO push_queue (target_accid, push_content) " +
                "  SELECT * " +
                "  FROM " +
                "    (SELECT " +
                "       jsonb_array_elements_text(accounts) as target_accid, ? :: JSONB as push_content " +
                "     FROM channel " +
                "     WHERE channel_id = ?) AS push_added " +
                "  WHERE target_accid != ? " +
                "  RETURNING id";

        JsonArray params = new JsonArray().add(pushContent.encode()).add(channelId).add(fromAccid);
        asyncSQLClient.queryWithParams(sql, params, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(ar.result().getRows()
                        .parallelStream().map(j -> j.getInteger("id")).collect(Collectors.toList()))
                );
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
        return this;
    }

    PushStorageServiceImpl(AsyncSQLClient asyncSQLClient, Handler<AsyncResult<PushStorageService>> readyHandler) {
        this.asyncSQLClient = asyncSQLClient;
        asyncSQLClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                readyHandler.handle(Future.failedFuture(ar.cause()));
            } else {
                readyHandler.handle(Future.succeededFuture(this));
            }
        });
    }
}
