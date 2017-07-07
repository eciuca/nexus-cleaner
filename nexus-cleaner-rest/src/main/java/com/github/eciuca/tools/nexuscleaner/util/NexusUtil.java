package com.github.eciuca.tools.nexuscleaner.util;

import com.github.eciuca.tools.nexuscleaner.config.NexusConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.Artifact;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import io.vertx.ext.web.client.WebClientOptions;

public class NexusUtil {


    public static String buildArtifactMetadataUri(NexusConfiguration nexusConfiguration, Artifact artifact) {
        return nexusConfiguration.getRepositoriesPath() + nexusConfiguration.getReleasesRepositoryId() +
                '/' + artifact.getGroupId().replace('.', '/') +
                '/' + artifact.getArtifactId() +
                "/maven-metadata.xml";
    }

    public static String buildArtifactUri(NexusConfiguration nexusConfiguration, Artifact artifact) {
        return nexusConfiguration.getRepositoriesPath() + nexusConfiguration.getReleasesRepositoryId() +
                '/' + artifact.getGroupId().replace('.', '/') +
                '/' + artifact.getArtifactId() +
                '/' + artifact.getVersion();
    }

    public static String buildNexusCleanerArtifactMetadataUri(NexusConfiguration nexusConfig, Artifact artifact, String environment) {

        String repositoryUri = nexusConfig.getRepositoriesPath() + nexusConfig.getReleasesRepositoryId();
        String groupIdPath = artifact.getGroupId().replaceAll("\\.", "/");
        String artifactId = artifact.getArtifactId();

        return String.format("%s/%s/%s/%s-%s.nc.json", repositoryUri, groupIdPath, artifactId, artifactId, environment);
    }

    public static String buildMetadataJsonPath(NexusConfiguration nexusConfig, ArtifactMetadata artifactMetadata) {
        return nexusConfig.getRepositoriesPath() + nexusConfig.getReleasesRepositoryId() + "/" +
                artifactMetadata.getArtifact().getGroupId().replace('.', '/') + "/" + artifactMetadata.getArtifact().getArtifactId() + "/" +
                artifactMetadata.getArtifact().getArtifactId() + "-" + artifactMetadata.getEnvironment() + ".nc.json";
    }

    public static WebClientOptions buildWebClientOptions(NexusConfiguration nexusConfiguration) {
        WebClientOptions options = new WebClientOptions();

        options.setSsl(nexusConfiguration.isSsl());
        options.setDefaultHost(nexusConfiguration.getHost());
        options.setDefaultPort(nexusConfiguration.getPort());

        return options;
    }
}
