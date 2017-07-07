package com.github.eciuca.tools.nexuscleaner.handlers;

import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import io.vertx.rxjava.ext.web.RoutingContext;

import java.util.List;

public class LocalConfigurationStartCleanupHandler extends StartCleanupHandler {

    protected LocalConfigurationStartCleanupHandler(NexusCleanerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected List<ArtifactMetadata> loadArtifacts(RoutingContext routingContext) {
        return artifactMetadataManger.loadArtifactsFromResource();
    }
}
