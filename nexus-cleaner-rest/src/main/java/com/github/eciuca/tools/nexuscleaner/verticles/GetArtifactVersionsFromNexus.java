package com.github.eciuca.tools.nexuscleaner.verticles;

import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.config.NexusConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import com.github.eciuca.tools.nexuscleaner.util.NexusUtil;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.github.eciuca.tools.nexuscleaner.handlers.StartCleanupHandler.COMPUTE_VERSIONS_TO_DELETE;
import static com.github.eciuca.tools.nexuscleaner.xml.ParseUtil.extractVersions;

public class GetArtifactVersionsFromNexus extends AbstractVerticle {

    private final Logger LOG = LoggerFactory.getLogger(GetArtifactVersionsFromNexus.class);

    public static final String GET_ARTIFACT_VERSIONS_FROM_NEXUS = "get-artifact-versions-from-nexus";

    private final NexusCleanerConfiguration configuration;

    @Inject
    public GetArtifactVersionsFromNexus(NexusCleanerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start() throws Exception {
        LOG.info("Starting GetArtifactVersionsFromNexus verticle ...");
        WebClient client = WebClient.create(vertx, NexusUtil.buildWebClientOptions(configuration.getNexusConfig()));

        vertx.eventBus().consumer(GET_ARTIFACT_VERSIONS_FROM_NEXUS)
                .handler(
                        messageHandler -> {
                            ArtifactMetadata artifactMetadata = Json.decodeValue(messageHandler.body().toString(), ArtifactMetadata.class);

                            LOG.info("Retrieving maven-metadata.xml for " + artifactMetadata.getArtifact().getGroupId() + ":" + artifactMetadata.getArtifact().getArtifactId() + " ...");
                            getArtifactMetadataXmlFromNexus(client, artifactMetadata)
                                    .subscribe(
                                            artifactMetadataXmlFromNexus -> {
                                                if (artifactMetadataXmlFromNexus != null) {
                                                    List<String> versions = extractVersions(artifactMetadataXmlFromNexus);
                                                    artifactMetadata.setVersionsDeployed(versions);

                                                    LOG.debug("Found a total of " + artifactMetadata.getVersionsDeployed().size() + " artifact versions deployed and " +
                                                            artifactMetadata.getVersionsToKeep().size() + " versions to keep for artifact " +
                                                            artifactMetadata.getArtifact().getGroupId() + ":" + artifactMetadata.getArtifact().getArtifactId());

                                                    sendMessageToComputeVersionsToDelete(artifactMetadata);
                                                }
                                            },
                                            Throwable::printStackTrace
                                    );
                        }
                );
    }

    private void sendMessageToComputeVersionsToDelete(ArtifactMetadata artifactMetadata) {
        vertx.eventBus().publish(COMPUTE_VERSIONS_TO_DELETE, Json.encode(artifactMetadata));
    }

    private Single<String> getArtifactMetadataXmlFromNexus(WebClient client, ArtifactMetadata artifactMetadata) {
        NexusConfiguration nexusConfig = configuration.getNexusConfig();

        String requestURI = NexusUtil.buildArtifactMetadataUri(nexusConfig, artifactMetadata.getArtifact());
        return client.get(requestURI)
                .putHeader(HttpHeaders.AUTHORIZATION, nexusConfig.getAuthorizationHeader())
                .as(BodyCodec.string())
                .rxSend()
                .map(response -> {
                    String stringBody = null;

                    if (response.statusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                        LOG.error("XML metadata not found for artifact " + artifactMetadata.getArtifact().getGroupId() + ":" + artifactMetadata.getArtifact().getArtifactId() +
                                " at " + requestURI + " !");
                    } else if (response.statusCode() == Response.Status.OK.getStatusCode()) {
                        stringBody = response.body();
                    } else {
                        throw new IllegalStateException("Http response status: " + response.statusCode() + " message: " + response.statusMessage());
                    }

                    return stringBody;
                });
    }

}
