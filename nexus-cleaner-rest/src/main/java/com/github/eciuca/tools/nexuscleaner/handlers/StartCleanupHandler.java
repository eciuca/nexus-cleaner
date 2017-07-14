package com.github.eciuca.tools.nexuscleaner.handlers;

import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.config.NexusConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import com.github.eciuca.tools.nexuscleaner.ArtifactMetadataManager;
import com.github.eciuca.tools.nexuscleaner.util.NexusUtil;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.github.eciuca.tools.nexuscleaner.verticles.GetArtifactVersionsFromNexus.GET_ARTIFACT_VERSIONS_FROM_NEXUS;


public abstract class StartCleanupHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(StartCleanupHandler.class);
    private static final int DELAY = 1000;

    protected final ArtifactMetadataManager artifactMetadataManger;

    public static final String COMPUTE_VERSIONS_TO_DELETE = "compute-versions-to-delete";
    protected final NexusCleanerConfiguration configuration;

    protected StartCleanupHandler(NexusCleanerConfiguration configuration) {
        this.configuration = configuration;
        this.artifactMetadataManger = new ArtifactMetadataManager(configuration);
    }

    public static LocalConfigurationStartCleanupHandler localConfigurationStartCleanupHandler(NexusCleanerConfiguration configuration) {
        return new LocalConfigurationStartCleanupHandler(configuration);
    }

    public static RequestBodyStartCleanupHandler requestBodyStartCleanupHandler(NexusCleanerConfiguration configuration) {
        return new RequestBodyStartCleanupHandler(configuration);
    }

    protected abstract List<ArtifactMetadata> loadArtifacts(RoutingContext routingContext);

    @Override
    public void handle(RoutingContext routingContext) {
        WebClient client = WebClient.create(routingContext.vertx(), NexusUtil.buildWebClientOptions(configuration.getNexusConfig()));

        List<ArtifactMetadata> artifactMetadataList = loadArtifacts(routingContext);
        LOG.info("Starting cleanup for " + artifactMetadataList.size() + " artifacts (one every " + DELAY + " ms)...");
        forEachArtifactMetadata(routingContext, artifactMetadataList, DELAY,
                artifactMetadata ->
                        readNexusConfig(client, artifactMetadata)
                                .map(Json::encode)
                                .subscribe(
                                        combinedArtifactMetadata -> routingContext.vertx().eventBus().publish(GET_ARTIFACT_VERSIONS_FROM_NEXUS, combinedArtifactMetadata),
                                        Throwable::printStackTrace
                                ));


        routingContext
                .response()
                .end("<html><body>Starting cleanup for " + artifactMetadataList.size() + " artifacts (one every " + DELAY + " ms). Watch the server logs for more info!</body><html>");
    }

    private <R> void forEachArtifactMetadata(RoutingContext routingContext, List<R> list, int delay, Consumer<R> consumer) {
        final AtomicInteger index = new AtomicInteger(0);
        routingContext.vertx()
                .periodicStream(delay)
                .toObservable()
                .subscribe(timerId -> {
                    if (index.get() < list.size()) {
                        consumer.accept(list.get(index.getAndIncrement()));
                    } else {
                        routingContext.vertx().cancelTimer(timerId);
                    }
                });
    }

    private Single<ArtifactMetadata> readNexusConfig(WebClient client, ArtifactMetadata maintainedArtifactsListItem) {
        LOG.debug("Retrieving acceptance and production metadata for artifact " +
                maintainedArtifactsListItem.getArtifact().getGroupId() + ":" + maintainedArtifactsListItem.getArtifact().getArtifactId() + " ...");

        Single<ArtifactMetadata> acceptanceMetadata = fetchNexusCleanerArtifactMetadata(client, maintainedArtifactsListItem, "acceptance");
        Single<ArtifactMetadata> productionMetadata = fetchNexusCleanerArtifactMetadata(client, maintainedArtifactsListItem, "production");

        return Single.zip(acceptanceMetadata, productionMetadata, ArtifactMetadata::combineArtifactMetadata);
    }

    private Single<ArtifactMetadata> fetchNexusCleanerArtifactMetadata(WebClient client, ArtifactMetadata artifactMetadata, String environment) {
        NexusConfiguration nexusConfig = configuration.getNexusConfig();

        return client.get(NexusUtil.buildNexusCleanerArtifactMetadataUri(nexusConfig, artifactMetadata.getArtifact(), environment))
                .putHeader(HttpHeaders.AUTHORIZATION, nexusConfig.getAuthorizationHeader())
                .as(BodyCodec.string())
                .rxSend()
                .map(response -> {
                    if (response.statusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                        LOG.debug("The metadata file " + artifactMetadata.getArtifact().getArtifactId() + "-" + environment + ".nc.json was not found, the following configuration will be used -> "
                                + artifactMetadata);
                        return artifactMetadata;
                    } else if (response.statusCode() == Response.Status.OK.getStatusCode()) {
                        ArtifactMetadata artifactMetadataFromJSON = artifactMetadataManger.mapFrom(response.getDelegate().bodyAsJsonObject());
                        LOG.debug("Successfully retrieved " + artifactMetadata.getArtifact().getArtifactId() + "-" + environment + ".nc.json -> " + artifactMetadataFromJSON);

                        return artifactMetadataFromJSON.combineArtifactMetadata(artifactMetadata);
                    }

                    throw new IllegalStateException("Http response status: " + response.statusCode() + " message: " + response.statusMessage());
                })
                .doOnError(error -> LOG.error("There was a problem while retrieving the artifact " + artifactMetadata.getArtifact().getArtifactId() + "-" + environment + ".nc.json", error));
    }
}
