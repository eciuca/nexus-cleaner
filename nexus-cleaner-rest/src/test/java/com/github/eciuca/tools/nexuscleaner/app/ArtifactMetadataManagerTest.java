package com.github.eciuca.tools.nexuscleaner.app;

import com.google.common.io.Resources;
import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadataManager;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;

public class ArtifactMetadataManagerTest {

    private ArtifactMetadataManager manager;
    private String artifactMetadataJsonAcc;
    private String artifactMetadataJsonProd;
    private String maintainedArtifactsJson;

    @Before
    public void setUp() throws Exception {
        NexusCleanerConfiguration configuration = new NexusCleanerConfiguration();

        manager = new ArtifactMetadataManager(configuration);

        artifactMetadataJsonAcc = Resources.toString(Resources.getResource("test-artifact1-acceptance.nc.json"), Charset.defaultCharset());
        artifactMetadataJsonProd = Resources.toString(Resources.getResource("test-artifact1-production.nc.json"), Charset.defaultCharset());
        maintainedArtifactsJson = Resources.toString(Resources.getResource("maintained-artifacts.json"), Charset.defaultCharset());
    }

    @Test
    public void testMapFrom() throws Exception {
        ArtifactMetadata artifactMetadata = manager.mapFrom(new JsonObject(artifactMetadataJsonAcc));

        Assert.assertEquals("group.id.one", artifactMetadata.getArtifact().getGroupId());
        Assert.assertEquals("artifact1", artifactMetadata.getArtifact().getArtifactId());
        Assert.assertEquals(null, artifactMetadata.getArtifact().getVersion());
        Assert.assertEquals(-1, artifactMetadata.getKeepLast());
        Assert.assertEquals(null, artifactMetadata.getEnvironment());
        Assert.assertEquals(0, artifactMetadata.getVersionsDeployed().size());
        Assert.assertEquals(1, artifactMetadata.getVersionsToKeep().size());
    }

    @Test
    public void testLoadArtifacts() throws Exception {
        List<ArtifactMetadata> artifactMetadataList = manager.maintainedArtifactsJsonToArtifactMetadataList(new JsonObject(maintainedArtifactsJson));

        Assert.assertEquals("group.id.one", artifactMetadataList.get(0).getArtifact().getGroupId());
        Assert.assertEquals("artifact1", artifactMetadataList.get(0).getArtifact().getArtifactId());
        Assert.assertEquals(null, artifactMetadataList.get(0).getArtifact().getVersion());
        Assert.assertEquals(1, artifactMetadataList.get(0).getKeepLast());
        Assert.assertEquals(null, artifactMetadataList.get(0).getEnvironment());
        Assert.assertEquals(0, artifactMetadataList.get(0).getVersionsDeployed().size());
        Assert.assertEquals(0, artifactMetadataList.get(0).getVersionsToKeep().size());

        Assert.assertEquals("group.id.two", artifactMetadataList.get(1).getArtifact().getGroupId());
        Assert.assertEquals("artifact2", artifactMetadataList.get(1).getArtifact().getArtifactId());
        Assert.assertEquals(null, artifactMetadataList.get(1).getArtifact().getVersion());
        Assert.assertEquals(1, artifactMetadataList.get(1).getKeepLast());
        Assert.assertEquals(null, artifactMetadataList.get(1).getEnvironment());
        Assert.assertEquals(0, artifactMetadataList.get(1).getVersionsDeployed().size());
        Assert.assertEquals(0, artifactMetadataList.get(1).getVersionsToKeep().size());

        Assert.assertEquals("group.id.three", artifactMetadataList.get(2).getArtifact().getGroupId());
        Assert.assertEquals("artifact3", artifactMetadataList.get(2).getArtifact().getArtifactId());
        Assert.assertEquals(null, artifactMetadataList.get(2).getArtifact().getVersion());
        Assert.assertEquals(2, artifactMetadataList.get(2).getKeepLast());
        Assert.assertEquals(null, artifactMetadataList.get(2).getEnvironment());
        Assert.assertEquals(0, artifactMetadataList.get(2).getVersionsDeployed().size());
        Assert.assertEquals(0, artifactMetadataList.get(2).getVersionsToKeep().size());
    }
}
