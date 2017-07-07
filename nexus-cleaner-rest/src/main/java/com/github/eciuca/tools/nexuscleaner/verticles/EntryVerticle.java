package com.github.eciuca.tools.nexuscleaner.verticles;

import be.fluid_it.bootique.vertx.VertxFactory;
import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.handlers.UpdateMetadataHandler;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.github.eciuca.tools.nexuscleaner.handlers.StartCleanupHandler.localConfigurationStartCleanupHandler;
import static com.github.eciuca.tools.nexuscleaner.handlers.StartCleanupHandler.requestBodyStartCleanupHandler;

public class EntryVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(EntryVerticle.class);

    private final NexusCleanerConfiguration configuration;
    private final VertxFactory vertxFactory;
    private final Router router;

    @Inject
    public EntryVerticle(NexusCleanerConfiguration configuration, VertxFactory vertxFactory, Router router) {
        this.configuration = configuration;
        this.vertxFactory = vertxFactory;
        this.router = router;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting EntryVerticle ...");

        router.route()
                .handler(BodyHandler.create())
                .failureHandler(failureHandler ->
                        LOG.error("Oups, something went wrong!", failureHandler.failure()));

        configureRouter(router);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(vertxFactory.http().server().port())
                .subscribe(rec -> LOG.info("[Nexus Cleaner] Http server started on port " + vertxFactory.http().server().port() + "!"));
    }

    private void configureRouter(Router router) {
        router.get("/start-cleanup").handler(localConfigurationStartCleanupHandler(configuration));
        router.post("/start-cleanup").handler(requestBodyStartCleanupHandler(configuration));
        router.put("/update-artifact-metadata").handler(new UpdateMetadataHandler(configuration));
        router.get("/healthcheck").handler(handler -> handler.response().end(new JsonObject().put("healty", true).encodePrettily()));
    }
}
