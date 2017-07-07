package com.github.eciuca.tools.nexuscleaner.config;

import java.util.Base64;

public class NexusConfiguration {
    private boolean ssl = true;
    private int port;
    private String host;
    private String repositoriesPath;
    private String releasesRepositoryId;
    private String snapshotsRepositoryId;
    private String username;
    private String password;

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRepositoriesPath() {
        return repositoriesPath;
    }

    public void setRepositoriesPath(String repositoriesPath) {
        this.repositoriesPath = repositoriesPath;
    }

    public String getReleasesRepositoryId() {
        return releasesRepositoryId;
    }

    public void setReleasesRepositoryId(String releasesRepositoryId) {
        this.releasesRepositoryId = releasesRepositoryId;
    }

    public String getSnapshotsRepositoryId() {
        return snapshotsRepositoryId;
    }

    public void setSnapshotsRepositoryId(String snapshotsRepositoryId) {
        this.snapshotsRepositoryId = snapshotsRepositoryId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthorizationHeader() {
        return "Bearer " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
    }

    public String getProtocol() {
        return ssl ? "https://" : "http://";
    }
}
