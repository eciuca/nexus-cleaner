package com.github.eciuca.tools.nexuscleaner.app;

import com.google.common.io.Resources;
import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadata;
import com.github.eciuca.tools.nexuscleaner.domain.ArtifactMetadataManager;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.github.eciuca.tools.nexuscleaner.xml.ParseUtil.extractVersions;

public class ArtifactMetadataTest {

    private ArtifactMetadataManager manager;
    private String artifactMetadataJsonAcc;
    private String artifactMetadataJsonProd;
    private String artifactMetadataXml;
    private String maintainedArtifactsJson;

    @Before
    public void setUp() throws IOException {
        NexusCleanerConfiguration configuration = new NexusCleanerConfiguration();

        manager = new ArtifactMetadataManager(configuration);

        artifactMetadataJsonAcc = Resources.toString(Resources.getResource("test-artifact1-acceptance.nc.json"), Charset.defaultCharset());
        artifactMetadataJsonProd = Resources.toString(Resources.getResource("test-artifact1-production.nc.json"), Charset.defaultCharset());
        artifactMetadataXml = Resources.toString(Resources.getResource("metadata.xml"), Charset.defaultCharset());
        maintainedArtifactsJson = Resources.toString(Resources.getResource("maintained-artifacts.json"), Charset.defaultCharset());
    }

    @Test
    public void testCombineMetadata() throws Exception {
        ArtifactMetadata artifactMetadataAcc = Json.decodeValue(artifactMetadataJsonAcc, ArtifactMetadata.class);
        ArtifactMetadata artifactMetadataProd = Json.decodeValue(artifactMetadataJsonProd, ArtifactMetadata.class);

        ArtifactMetadata combined = artifactMetadataAcc.combineArtifactMetadata(artifactMetadataProd);

        Assert.assertEquals("group.id.one", combined.getArtifact().getGroupId());
        Assert.assertEquals("artifact1", combined.getArtifact().getArtifactId());
        Assert.assertEquals(null, combined.getArtifact().getVersion());
        Assert.assertEquals(-1, combined.getKeepLast());
        Assert.assertEquals(null, combined.getEnvironment());
        Assert.assertEquals(0, combined.getVersionsDeployed().size());
        Assert.assertEquals(2, combined.getVersionsToKeep().size());
    }

    @Test
    public void testGetArtifactVersionsFromNexusXml() throws Exception {
        //StartCleanupHandler - load artifacts step
        List<ArtifactMetadata> artifactMetadataList = manager.maintainedArtifactsJsonToArtifactMetadataList(new JsonObject(maintainedArtifactsJson));

        //StartCleanupHandler - read configuration from nexus
        ArtifactMetadata artifactMetadataAcc = Json.decodeValue(artifactMetadataJsonAcc, ArtifactMetadata.class);
        ArtifactMetadata artifactMetadataProd = Json.decodeValue(artifactMetadataJsonProd, ArtifactMetadata.class);

        //StartCleanupHandler - combine metadata
        ArtifactMetadata combined = artifactMetadataAcc.combineArtifactMetadata(artifactMetadataProd).combineArtifactMetadata(artifactMetadataList.get(0));

        //GetArtifactVersionsFromNexus - read configuration from nexus
        List<String> versions = extractVersions(artifactMetadataXml);
        combined.setVersionsDeployed(versions);

        Assert.assertEquals("group.id.one", combined.getArtifact().getGroupId());
        Assert.assertEquals("artifact1", combined.getArtifact().getArtifactId());
        Assert.assertEquals(null, combined.getArtifact().getVersion());
        Assert.assertEquals(1, combined.getKeepLast());
        Assert.assertEquals(null, combined.getEnvironment());
        Assert.assertEquals(5, combined.getVersionsDeployed().size());
        Assert.assertEquals(2, combined.getVersionsToKeep().size());
    }
}
