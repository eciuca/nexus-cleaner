package com.github.eciuca.tools.nexuscleaner;

import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.Artifact;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArtifactMetadataManager {

    public static final String MAINTAINED_ARTIFACTS_JSON = "maintained-artifacts.json";
    private final NexusCleanerConfiguration configuration;

    public ArtifactMetadataManager(NexusCleanerConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<ArtifactMetadata> loadArtifactsFromResource() {
        URL resource = getClass().getClassLoader().getResource(MAINTAINED_ARTIFACTS_JSON);
        if (resource == null) throw new NullPointerException("Resource not found!");

        try {
            File file = new File(resource.getFile());
            String artifactsString = new String(Files.readAllBytes(file.toPath()));
            JsonObject maintainedArtifactsJsonObject = new JsonObject(artifactsString);

            return maintainedArtifactsJsonToArtifactMetadataList(maintainedArtifactsJsonObject);
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    public List<ArtifactMetadata> maintainedArtifactsJsonToArtifactMetadataList(JsonObject maintainedArtifactsJsonObject) {
        int defaultKeepLast = maintainedArtifactsJsonObject.containsKey("keepLast") ? maintainedArtifactsJsonObject.getInteger("keepLast") : configuration.getKeepLast();
        Function<ArtifactMetadata, ArtifactMetadata> setDefaultKeepLastIfMissingFunction = artifactMetadata -> setDefaultKeepLastIfMissing(artifactMetadata, defaultKeepLast);

        return maintainedArtifactsJsonObject
                .getJsonArray("artifactMetadataList")
                .stream()
                .map(JsonObject::mapFrom)
                .map(this::mapFrom)
                .map(setDefaultKeepLastIfMissingFunction)
                .collect(Collectors.toList());
    }

    public ArtifactMetadata mapFrom(JsonObject artifactMetadataJsonObject) {
        Artifact artifact = new Artifact();
        JsonObject artifactJson = artifactMetadataJsonObject.getJsonObject("artifact");
        artifact.setArtifactId(artifactJson.getString("artifactId"));
        artifact.setGroupId(artifactJson.getString("groupId"));

        ArtifactMetadata artifactMetadata = new ArtifactMetadata();
        artifactMetadata.setArtifact(artifact);

        if (artifactJson.containsKey("keepLast")) {
            artifactMetadata.setKeepLast(artifactJson.getInteger("keepLast"));
        }

        if (artifactMetadataJsonObject.containsKey("versionsDeployed")) {
            List versions = artifactMetadataJsonObject.getJsonArray("versions").getList();
            artifactMetadata.setVersionsDeployed(versions);
        }

        if (artifactMetadataJsonObject.containsKey("versionsToKeep")) {
            artifactMetadata.setVersionsToKeep(
                    artifactMetadataJsonObject.getJsonArray("versionsToKeep").stream()
                            .map(Object::toString)
                            .collect(Collectors.toSet()));
        }


        return artifactMetadata;
    }

    private ArtifactMetadata setDefaultKeepLastIfMissing(ArtifactMetadata artifactMetadata, int defaultKeepLast) {
        artifactMetadata.setKeepLast(artifactMetadata.getKeepLast() < 0 ? defaultKeepLast : artifactMetadata.getKeepLast());

        return artifactMetadata;
    }
}
