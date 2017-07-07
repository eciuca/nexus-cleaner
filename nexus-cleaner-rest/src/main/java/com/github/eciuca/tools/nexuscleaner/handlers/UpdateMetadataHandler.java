package com.github.eciuca.tools.nexuscleaner.handlers;

import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.config.NexusConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import com.github.eciuca.tools.nexuscleaner.util.NexusUtil;
import io.vertx.core.Handler;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class UpdateMetadataHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateMetadataHandler.class);

    private final NexusCleanerConfiguration configuration;

    public UpdateMetadataHandler(NexusCleanerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        NexusConfiguration nexusConfig = configuration.getNexusConfig();
        WebClient client = WebClient.create(routingContext.vertx(), NexusUtil.buildWebClientOptions(nexusConfig));

        ArtifactMetadata artifactMetadata = routingContext.getBodyAsJson().mapTo(ArtifactMetadata.class);
        LOG.info("Received request to update metadata for artifact " + artifactMetadata.getArtifact().getGroupId() + "/" + artifactMetadata.getArtifact().getArtifactId() + " for " + artifactMetadata.getEnvironment());

        verifyIfOlderArtifactMetedataExists(nexusConfig, client, artifactMetadata,
                response -> {
                    if (olderArtifactFound(response)) {
                        deleteExistingMetadataJson(nexusConfig, client, artifactMetadata,
                                then -> uploadMetadataJson(routingContext, nexusConfig, client, artifactMetadata));
                    } else if (olderArtifactNotFound(response)) {
                        uploadMetadataJson(routingContext, nexusConfig, client, artifactMetadata);
                    } else {
                        routingContext.response().setStatusCode(response.statusCode()).setStatusMessage(response.statusMessage()).end();
                    }
                });
    }

    private boolean olderArtifactNotFound(HttpResponse<Buffer> response) {
        return response.statusCode() == Response.Status.NOT_FOUND.getStatusCode();
    }

    private boolean olderArtifactFound(HttpResponse<Buffer> response) {
        return response.statusCode() == Response.Status.OK.getStatusCode();
    }

    private void verifyIfOlderArtifactMetedataExists(NexusConfiguration nexusConfig, WebClient client, ArtifactMetadata artifactMetadata, Action1<HttpResponse<Buffer>> successHandler) {
        client.get(NexusUtil.buildMetadataJsonPath(nexusConfig, artifactMetadata))
                .putHeader(HttpHeaders.AUTHORIZATION, nexusConfig.getAuthorizationHeader())
                .rxSend()
                .subscribe(successHandler, Throwable::printStackTrace);
    }

    private void deleteExistingMetadataJson(NexusConfiguration nexusConfig, WebClient client, ArtifactMetadata artifactMetadata, Action1<HttpResponse<Buffer>> successHandler) {
        client.delete(NexusUtil.buildMetadataJsonPath(nexusConfig, artifactMetadata))
                .putHeader(HttpHeaders.AUTHORIZATION, nexusConfig.getAuthorizationHeader())
                .rxSend()
                .subscribe(successHandler, Throwable::printStackTrace);
    }

    private void uploadMetadataJson(RoutingContext routingContext, NexusConfiguration nexusConfig, WebClient client, ArtifactMetadata artifactMetadata) {
        LOG.info("Uploading new metadata for artifact " + artifactMetadata.getArtifact().getGroupId() + "/" + artifactMetadata.getArtifact().getArtifactId() + " for " + artifactMetadata.getEnvironment());
        client.put(NexusUtil.buildMetadataJsonPath(nexusConfig, artifactMetadata))
                .putHeader(HttpHeaders.AUTHORIZATION, nexusConfig.getAuthorizationHeader())
                .rxSendJson(artifactMetadata)
                .subscribe(
                        onUploadSuccess -> routingContext.response().end("Artifact metadata updated successfully"),
                        Throwable::printStackTrace
                );
    }
}
