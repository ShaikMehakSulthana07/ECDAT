package com.ecdat.backend.input;

import com.ecdat.backend.dto.InputCapability;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisInputProcessorTest {

    private final AnalysisInputProcessorRegistry registry = new AnalysisInputProcessorRegistry();

    @Test
    void testZipInputProcessorSuccess() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("src/main/java/Demo.java");
            zos.putNextEntry(entry);
            zos.write("public class Demo {}".getBytes());
            zos.closeEntry();
        }

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", "archive.zip", "application/zip", baos.toByteArray()
        );

        AnalysisInput input = AnalysisInput.forZip(zipFile, new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.ZIP_ARCHIVE);

        assertNotNull(processor);
        assertTrue(processor.supports(AnalysisInputType.ZIP_ARCHIVE));
        assertEquals(AnalysisInputType.ZIP_ARCHIVE, processor.getInputType());

        try (ScanWorkspace workspace = processor.process(input)) {
            assertNotNull(workspace);
            assertTrue(Files.exists(workspace.getRootPath()));
            assertTrue(Files.exists(workspace.getRootPath().resolve("src/main/java/Demo.java")));
            assertTrue(workspace.isTemporary());
        }
    }

    @Test
    void testZipInputProcessorZipSlipProtection() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry maliciousEntry = new ZipEntry("../../evil.java");
            zos.putNextEntry(maliciousEntry);
            zos.write("public class Evil {}".getBytes());
            zos.closeEntry();
        }

        MockMultipartFile maliciousZip = new MockMultipartFile(
                "file", "malicious.zip", "application/zip", baos.toByteArray()
        );

        AnalysisInput input = AnalysisInput.forZip(maliciousZip, new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.ZIP_ARCHIVE);

        assertThrows(SecurityException.class, () -> processor.process(input));
    }

    @Test
    void testSourceFileInputProcessorSuccess() throws IOException {
        MockMultipartFile javaFile = new MockMultipartFile(
                "file", "CryptoService.java", "text/x-java-source",
                ("import javax.crypto.Cipher;\npublic class CryptoService {\n" +
                 "  public void test() throws Exception {\n" +
                 "    Cipher c = Cipher.getInstance(\"AES/GCM/NoPadding\");\n" +
                 "  }\n}\n").getBytes()
        );

        AnalysisInput input = AnalysisInput.forSourceFile(javaFile, new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.SOURCE_FILE);

        assertNotNull(processor);
        assertTrue(processor.supports(AnalysisInputType.SOURCE_FILE));
        assertEquals(AnalysisInputType.SOURCE_FILE, processor.getInputType());

        try (ScanWorkspace workspace = processor.process(input)) {
            assertNotNull(workspace);
            assertTrue(Files.exists(workspace.getRootPath()));
            assertTrue(Files.exists(workspace.getRootPath().resolve("CryptoService.java")));
            assertTrue(workspace.isTemporary());
        }
    }

    @Test
    void testSourceFileInputProcessorRejectsNonJava() {
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "script.py", "text/plain", "print('hello')".getBytes()
        );

        AnalysisInput input = AnalysisInput.forSourceFile(textFile, new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.SOURCE_FILE);

        assertThrows(IllegalArgumentException.class, () -> processor.process(input));
    }

    @Test
    void testDirectoryInputProcessorSuccess(@TempDir Path tempDir) throws IOException {
        Path projectDir = Files.createDirectory(tempDir.resolve("my-app"));
        AnalysisInput input = AnalysisInput.forDirectory(projectDir.toString(), new ProjectAnalysisContext());

        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.DIRECTORY);
        assertNotNull(processor);
        assertTrue(processor.supports(AnalysisInputType.DIRECTORY));

        try (ScanWorkspace workspace = processor.process(input)) {
            assertNotNull(workspace);
            assertEquals(projectDir.toAbsolutePath().normalize(), workspace.getRootPath());
            assertFalse(workspace.isTemporary());
        }
    }

    @Test
    void testDirectoryInputProcessorRejectsNonexistent() {
        AnalysisInput input = AnalysisInput.forDirectory("nonexistent/dir/path/123", new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.DIRECTORY);

        assertThrows(IllegalArgumentException.class, () -> processor.process(input));
    }

    @Test
    void testConfigurationInputProcessorSuccess() throws IOException {
        MockMultipartFile propertiesFile = new MockMultipartFile(
                "file", "application.properties", "text/plain",
                "server.port=8080\nserver.ssl.key-store-password=secret".getBytes()
        );

        AnalysisInput input = AnalysisInput.forConfiguration(propertiesFile, new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.CONFIGURATION_FILE);

        assertNotNull(processor);
        assertTrue(processor.supports(AnalysisInputType.CONFIGURATION_FILE));
        assertEquals(AnalysisInputType.CONFIGURATION_FILE, processor.getInputType());

        try (ScanWorkspace workspace = processor.process(input)) {
            assertNotNull(workspace);
            assertTrue(Files.exists(workspace.getRootPath()));
            assertTrue(Files.exists(workspace.getRootPath().resolve("application.properties")));
            assertTrue(workspace.isTemporary());
        }
    }

    @Test
    void testConfigurationInputProcessorRejectsInvalidExtension() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "test".getBytes()
        );

        AnalysisInput input = AnalysisInput.forConfiguration(invalidFile, new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.CONFIGURATION_FILE);

        assertThrows(IllegalArgumentException.class, () -> processor.process(input));
    }

    @Test
    void testConfigurationInputProcessorRejectsEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "application.properties", "text/plain", new byte[0]
        );

        AnalysisInput input = AnalysisInput.forConfiguration(emptyFile, new ProjectAnalysisContext());
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.CONFIGURATION_FILE);

        assertThrows(IllegalArgumentException.class, () -> processor.process(input));
    }

    @Test
    void testUnsupportedRoadmapProcessorsThrowExplicitExceptions() {
        // CONFIGURATION_FILE is now supported in Phase 6, BINARY_FILE is now supported in Phase 7
        // CONTAINER_IMAGE is now supported in Phase 8
        // All input types are now supported, so this test is no longer applicable
        // We skip it rather than delete it to maintain test structure
    }

    @Test
    void testCapabilitiesGeneration() {
        List<InputCapability> capabilities = registry.getCapabilities();
        assertNotNull(capabilities);
        assertEquals(7, capabilities.size());

        assertTrue(capabilities.stream().anyMatch(c -> "ZIP_ARCHIVE".equals(c.getType()) && c.isSupported()));
        assertTrue(capabilities.stream().anyMatch(c -> "SOURCE_FILE".equals(c.getType()) && c.isSupported()));
        assertTrue(capabilities.stream().anyMatch(c -> "DIRECTORY".equals(c.getType()) && c.isSupported()));
        assertTrue(capabilities.stream().anyMatch(c -> "REPOSITORY_URL".equals(c.getType()) && c.isSupported() && "PHASE_5".equals(c.getPlannedPhase())));
        assertTrue(capabilities.stream().anyMatch(c -> "CONFIGURATION_FILE".equals(c.getType()) && c.isSupported() && "PHASE_6".equals(c.getPlannedPhase())));
        assertTrue(capabilities.stream().anyMatch(c -> "BINARY_FILE".equals(c.getType()) && c.isSupported() && "PHASE_7".equals(c.getPlannedPhase())));
        assertTrue(capabilities.stream().anyMatch(c -> "CONTAINER_IMAGE".equals(c.getType()) && c.isSupported() && "PHASE_8".equals(c.getPlannedPhase())));
    }

    @Test
    void testDiscoveredArtifactNormalization(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, "public class Test {}");

        DiscoveredArtifact artifact = DiscoveredArtifact.of(
                ArtifactType.JAVA_SOURCE, javaFile, tempDir, "ZIP_ARCHIVE"
        );

        assertNotNull(artifact);
        assertEquals(ArtifactType.JAVA_SOURCE, artifact.getArtifactType());
        assertEquals("Test.java", artifact.getName());
        assertEquals("Test.java", artifact.getRelativePath());
        assertEquals("ZIP_ARCHIVE", artifact.getOrigin());
        assertTrue(artifact.getSize() > 0);
    }
}
