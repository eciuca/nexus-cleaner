package com.github.eciuca.tools.nexuscleaner.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArtifactMetadata {

    private Artifact artifact;
    private List<String> versionsDeployed = new ArrayList<>();
    private Set<String> versionsToKeep = new HashSet<>();
    private String environment;
    private int keepLast = -1;

    public Artifact getArtifactWithVersion(String version) {
        return new Artifact(artifact.getArtifactId(), artifact.getGroupId(), version);
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public Set<String> getVersionsToKeep() {
        return versionsToKeep;
    }

    public List<String> getVersionsDeployed() {
        return versionsDeployed;
    }

    public void setVersionsDeployed(List<String> versionsDeployed) {
        this.versionsDeployed = versionsDeployed;
    }

    public void setVersionsToKeep(Set<String> versionsToKeep) {
        this.versionsToKeep = versionsToKeep;
    }

    public void addVersionToKeep(String version) {
        versionsToKeep.add(version);
    }

    public ArtifactMetadata addVersionsToKeep(Set<String> versionsToAdd) {
        versionsToKeep.addAll(versionsToAdd);

        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public int getKeepLast() {
        return keepLast;
    }

    public void setKeepLast(int keepLast) {
        this.keepLast = keepLast;
    }

    public ArtifactMetadata combineArtifactMetadata(ArtifactMetadata artifactMetadata) {
        if (!this.getArtifact().getGroupId().equals(artifactMetadata.getArtifact().getGroupId()) || !this.getArtifact().getArtifactId().equals(artifactMetadata.getArtifact().getArtifactId())) {
            throw new IllegalArgumentException("The 2 artifacts metadata objects refer to different artifacts, therefore they cannot be combined");
        }
        this.setKeepLast(this.getKeepLast() > artifactMetadata.getKeepLast() ? this.getKeepLast() : artifactMetadata.getKeepLast());
        this.setEnvironment(null);
        this.versionsToKeep.addAll(artifactMetadata.getVersionsToKeep());

        return this;
    }

    public static ArtifactMetadata identity(Artifact artifact, String environment, int keepMinimum) {
        ArtifactMetadata artifactMetadata = new ArtifactMetadata();

        artifactMetadata.setArtifact(artifact);
        artifactMetadata.setEnvironment(environment);
        artifactMetadata.setKeepLast(keepMinimum);

        return artifactMetadata;

    }

    @Override
    public String toString() {
        return "ArtifactMetadata{" +
                "artifact='" + artifact.toString() + '\'' +
                ", versionsDeployed=[ " + versionsDeployed.stream().collect(Collectors.joining(", ")) + " ]" +
                ", versionsToKeep=[ " + versionsToKeep.stream().collect(Collectors.joining(", ")) + " ]" +
                ", environment='" + environment + '\'' +
                ", keepLast=" + keepLast +
                '}';
    }
}
