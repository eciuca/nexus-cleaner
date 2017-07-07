package com.github.eciuca.tools.nexuscleaner.domain;

public class Artifact {
    private String artifactId;
    private String groupId;
    private String version;

    public Artifact() {
    }

    public Artifact(String artifactId, String groupId) {
        this.artifactId = artifactId;
        this.groupId = groupId;
    }

    public Artifact(String artifactId, String groupId, String version) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.version = version;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Artifact withVersion(String version) {
        return new Artifact(this.artifactId, this.groupId, version);
    }

    @Override
    public String toString() {
        return groupId + ':' + artifactId + ':' + version;
    }
}
