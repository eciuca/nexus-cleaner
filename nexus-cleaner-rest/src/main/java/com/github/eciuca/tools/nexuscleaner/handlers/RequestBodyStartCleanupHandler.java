package com.github.eciuca.tools.nexuscleaner.handlers;

import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.web.RoutingContext;

import java.util.List;

public class RequestBodyStartCleanupHandler extends StartCleanupHandler {

    protected RequestBodyStartCleanupHandler(NexusCleanerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected List<ArtifactMetadata> loadArtifacts(RoutingContext routingContext) {
        JsonObject maintainedArtifactsJsonObject = routingContext.getBodyAsJson();

        return artifactMetadataManger.maintainedArtifactsJsonToArtifactMetadataList(maintainedArtifactsJsonObject);
    }
}
