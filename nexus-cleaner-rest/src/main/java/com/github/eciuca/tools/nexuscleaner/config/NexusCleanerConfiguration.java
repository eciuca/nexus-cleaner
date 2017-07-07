package com.github.eciuca.tools.nexuscleaner.config;

import com.github.eciuca.tools.nexuscleaner.verticles.ComputeVersionsToDeleteVerticle;
import com.github.eciuca.tools.nexuscleaner.verticles.GetArtifactVersionsFromNexus;

public class NexusCleanerConfiguration {
    private String name;
    private int keepLast = 5;
    private NexusConfiguration nexusConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeepLast() {
        return keepLast;
    }

    public void setKeepLast(int keepLast) {
        this.keepLast = keepLast;
    }

    public NexusConfiguration getNexusConfig() {
        return nexusConfig;
    }

    public void setNexusConfig(NexusConfiguration nexusConfig) {
        this.nexusConfig = nexusConfig;
    }

    public GetArtifactVersionsFromNexus createGetArtifactVersionsFromNexus() {
        return new GetArtifactVersionsFromNexus(this);
    }

    public ComputeVersionsToDeleteVerticle createComputeVersionsToDeleteVerticle() {
        return new ComputeVersionsToDeleteVerticle(this);
    }
}