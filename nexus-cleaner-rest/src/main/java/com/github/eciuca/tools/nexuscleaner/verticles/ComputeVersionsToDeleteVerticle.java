package com.github.eciuca.tools.nexuscleaner.verticles;

import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.config.NexusConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.Artifact;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import com.github.eciuca.tools.nexuscleaner.util.NexusUtil;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import static com.github.eciuca.tools.nexuscleaner.handlers.StartCleanupHandler.COMPUTE_VERSIONS_TO_DELETE;

public class ComputeVersionsToDeleteVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(ComputeVersionsToDeleteVerticle.class);

    private final NexusCleanerConfiguration configuration;
    private WebClient webClient;

    @Inject
    public ComputeVersionsToDeleteVerticle(NexusCleanerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start() throws Exception {
        LOG.info("Starting ComputeVersionsToDeleteVerticle verticle ...");
        webClient = WebClient.create(vertx, NexusUtil.buildWebClientOptions(configuration.getNexusConfig()));

        vertx.eventBus().consumer(COMPUTE_VERSIONS_TO_DELETE)
                .handler(handler -> {
                    ArtifactMetadata metadata = Json.decodeValue(handler.body().toString(), ArtifactMetadata.class);
                    LOG.info("Computing artifacts to delete from metadata -> " + metadata);

                    metadata.getVersionsDeployed() //versions should be sorted in reverse order
                            .stream()
                            .skip(metadata.getKeepLast())
                            .filter(version -> !metadata.getVersionsToKeep().contains(version))
                            .map(metadata::getArtifactWithVersion)
                            .forEach(this::deleteArtifact);
                });
    }

    private void deleteArtifact(Artifact artifact) {
        LOG.debug("Deleting artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + " ...");

        NexusConfiguration nexusConfig = configuration.getNexusConfig();
        webClient.delete(NexusUtil.buildArtifactUri(nexusConfig, artifact))
                .putHeader(HttpHeaders.AUTHORIZATION, nexusConfig.getAuthorizationHeader())
                .as(BodyCodec.string())
                .rxSend()
                .subscribe(
                        onSuccess -> LOG.info("Deleted artifact " + artifact + "!"),
                        Throwable::printStackTrace
                );
    }

}
