package com.ecdat.backend.scanner.container;

import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for container image scanning.
 * Tests the full container scanning pipeline with artifact discovery and scanner routing.
 */
class ContainerScannerIntegrationTest {

    @Test
    void testContainerImageScannerInitialization() {
        ContainerImageScanner scanner = new ContainerImageScanner();
        assertNotNull(scanner);
    }

    @Test
    void testDockerManifestReaderInitialization() {
        DockerManifestReader reader = new DockerManifestReader();
        assertNotNull(reader);
    }

    @Test
    void testLayerExtractorInitialization(@TempDir Path tempDir) throws IOException {
        LayerExtractor extractor = new LayerExtractor(tempDir);
        assertNotNull(extractor);
        assertNotNull(extractor.getLayersDir());
        assertTrue(Files.exists(extractor.getLayersDir()));
    }

    @Test
    void testArtifactDiscoveryInitialization() {
        ArtifactDiscovery discovery = new ArtifactDiscovery();
        assertNotNull(discovery);
    }

    @Test
    void testInvalidDockerImageDirectory(@TempDir Path tempDir) throws IOException {
        // Create a directory without manifest.json
        Path invalidDir = tempDir.resolve("invalid-image");
        Files.createDirectories(invalidDir);

        DockerManifestReader reader = new DockerManifestReader();
        assertFalse(reader.isValidDockerImage(invalidDir));
    }

    @Test
    void testArtifactDiscoveryFromEmptyDirectory(@TempDir Path tempDir) throws IOException {
        ArtifactDiscovery discovery = new ArtifactDiscovery();
        var artifacts = discovery.discoverArtifacts(tempDir, "test-source");
        assertNotNull(artifacts);
        assertTrue(artifacts.isEmpty());
    }

    @Test
    void testArtifactDiscoveryFromDirectoryWithJar(@TempDir Path tempDir) throws IOException {
        // Create a test JAR file
        Path jarFile = tempDir.resolve("test.jar");
        Files.write(jarFile, new byte[100]);

        ArtifactDiscovery discovery = new ArtifactDiscovery();
        var artifacts = discovery.discoverArtifacts(tempDir, "test-source");
        
        assertNotNull(artifacts);
        assertFalse(artifacts.isEmpty());
        assertEquals(1, artifacts.size());
    }

    @Test
    void testArtifactDiscoveryFromDirectoryWithClass(@TempDir Path tempDir) throws IOException {
        // Create a test CLASS file
        Path classFile = tempDir.resolve("Test.class");
        Files.write(classFile, new byte[100]);

        ArtifactDiscovery discovery = new ArtifactDiscovery();
        var artifacts = discovery.discoverArtifacts(tempDir, "test-source");
        
        assertNotNull(artifacts);
        assertFalse(artifacts.isEmpty());
        assertEquals(1, artifacts.size());
    }

    @Test
    void testArtifactDiscoveryFromDirectoryWithCertificate(@TempDir Path tempDir) throws IOException {
        // Create a test certificate file
        Path certFile = tempDir.resolve("test.crt");
        Files.write(certFile, new byte[100]);

        ArtifactDiscovery discovery = new ArtifactDiscovery();
        var artifacts = discovery.discoverArtifacts(tempDir, "test-source");
        
        assertNotNull(artifacts);
        assertFalse(artifacts.isEmpty());
        assertEquals(1, artifacts.size());
    }

    @Test
    void testArtifactDiscoveryFromDirectoryWithConfiguration(@TempDir Path tempDir) throws IOException {
        // Create a test configuration file
        Path configFile = tempDir.resolve("application.properties");
        Files.write(configFile, "server.port=8080".getBytes());

        ArtifactDiscovery discovery = new ArtifactDiscovery();
        var artifacts = discovery.discoverArtifacts(tempDir, "test-source");
        
        assertNotNull(artifacts);
        assertFalse(artifacts.isEmpty());
        assertEquals(1, artifacts.size());
    }

    @Test
    void testArtifactDiscoveryFromDirectoryWithPom(@TempDir Path tempDir) throws IOException {
        // Create a test pom.xml file
        Path pomFile = tempDir.resolve("pom.xml");
        Files.write(pomFile, "<project></project>".getBytes());

        ArtifactDiscovery discovery = new ArtifactDiscovery();
        var artifacts = discovery.discoverArtifacts(tempDir, "test-source");
        
        assertNotNull(artifacts);
        assertFalse(artifacts.isEmpty());
        assertEquals(1, artifacts.size());
    }

    @Test
    void testArtifactDiscoveryFromDirectoryWithUnsupportedFile(@TempDir Path tempDir) throws IOException {
        // Create an unsupported file type
        Path unsupportedFile = tempDir.resolve("test.txt");
        Files.write(unsupportedFile, "test content".getBytes());

        ArtifactDiscovery discovery = new ArtifactDiscovery();
        var artifacts = discovery.discoverArtifacts(tempDir, "test-source");
        
        assertNotNull(artifacts);
        assertTrue(artifacts.isEmpty());
    }

    @Test
    void testContainerImageScannerWithInvalidImage(@TempDir Path tempDir) {
        ContainerImageScanner scanner = new ContainerImageScanner();
        
        // Should throw IOException for invalid image
        assertThrows(IOException.class, () -> {
            scanner.scanExtractedImage(tempDir, "test-image");
        });
    }

    @Test
    void testLayerExtractorResourceTracking(@TempDir Path tempDir) throws IOException {
        LayerExtractor extractor = new LayerExtractor(tempDir);
        
        assertEquals(0, extractor.getTotalExtractedFiles());
        assertEquals(0, extractor.getTotalExtractedSize());
        
        assertNotNull(extractor.getLayersDir());
    }

    @Test
    void testSupportedExtensionsAreNonEmpty() {
        assertTrue(ContainerAnalysisLimits.SUPPORTED_EXTENSIONS.length > 0);
    }

    @Test
    void testSupportedArchiveExtensionsAreNonEmpty() {
        assertTrue(ContainerAnalysisLimits.SUPPORTED_ARCHIVE_EXTENSIONS.length > 0);
    }

    @Test
    void testSecurityLimitsArePositive() {
        assertTrue(ContainerAnalysisLimits.MAX_IMAGE_ARCHIVE_SIZE > 0);
        assertTrue(ContainerAnalysisLimits.MAX_TOTAL_LAYER_SIZE > 0);
        assertTrue(ContainerAnalysisLimits.MAX_LAYER_COUNT > 0);
        assertTrue(ContainerAnalysisLimits.MAX_LAYER_ENTRIES > 0);
        assertTrue(ContainerAnalysisLimits.MAX_EXTRACTED_FILE_SIZE > 0);
        assertTrue(ContainerAnalysisLimits.MAX_TOTAL_EXTRACTED_FILES > 0);
        assertTrue(ContainerAnalysisLimits.MAX_NESTED_ARCHIVE_DEPTH > 0);
        assertTrue(ContainerAnalysisLimits.MAX_PATH_DEPTH > 0);
        assertTrue(ContainerAnalysisLimits.MAX_ANALYSIS_TIME_MS > 0);
    }
}
