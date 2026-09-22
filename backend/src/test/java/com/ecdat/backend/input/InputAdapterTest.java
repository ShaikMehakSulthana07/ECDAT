package com.ecdat.backend.input;

import com.ecdat.backend.dto.ProjectAnalysisContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class InputAdapterTest {

    private final InputAdapterRegistry registry = new InputAdapterRegistry();

    @Test
    void testZipInputAdapterSuccess() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("src/Test.java");
            zos.putNextEntry(entry);
            zos.write("public class Test {}".getBytes());
            zos.closeEntry();
        }

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", "archive.zip", "application/zip", baos.toByteArray()
        );

        ScanRequest request = ScanRequest.forZip(zipFile, new ProjectAnalysisContext());
        InputAdapter adapter = registry.getAdapter(ScanInputType.ZIP_ARCHIVE);

        assertNotNull(adapter);
        assertTrue(adapter.supports(ScanInputType.ZIP_ARCHIVE));

        try (ScanWorkspace workspace = adapter.prepareWorkspace(request)) {
            assertNotNull(workspace);
            assertTrue(Files.exists(workspace.getRootPath()));
            assertTrue(Files.exists(workspace.getRootPath().resolve("src/Test.java")));
            assertTrue(workspace.isTemporary());
        }
    }

    @Test
    void testZipInputAdapterZipSlipProtection() throws IOException {
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

        ScanRequest request = ScanRequest.forZip(maliciousZip, new ProjectAnalysisContext());
        InputAdapter adapter = registry.getAdapter(ScanInputType.ZIP_ARCHIVE);

        assertThrows(SecurityException.class, () -> adapter.prepareWorkspace(request));
    }

    @Test
    void testZipInputAdapterRejectsNonZip() {
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello".getBytes()
        );
        ScanRequest request = ScanRequest.forZip(textFile, new ProjectAnalysisContext());
        InputAdapter adapter = registry.getAdapter(ScanInputType.ZIP_ARCHIVE);

        assertThrows(IllegalArgumentException.class, () -> adapter.prepareWorkspace(request));
    }

    @Test
    void testDirectoryInputAdapterSuccess(@TempDir Path tempDir) throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("myproject"));
        ScanRequest request = ScanRequest.forDirectory(subDir.toString(), new ProjectAnalysisContext());

        InputAdapter adapter = registry.getAdapter(ScanInputType.DIRECTORY);
        assertNotNull(adapter);
        assertTrue(adapter.supports(ScanInputType.DIRECTORY));

        try (ScanWorkspace workspace = adapter.prepareWorkspace(request)) {
            assertNotNull(workspace);
            assertEquals(subDir.toAbsolutePath().normalize(), workspace.getRootPath());
            assertFalse(workspace.isTemporary());
        }
    }

    @Test
    void testDirectoryInputAdapterNonexistentPath() {
        ScanRequest request = ScanRequest.forDirectory("nonexistent/dir/path/12345", new ProjectAnalysisContext());
        InputAdapter adapter = registry.getAdapter(ScanInputType.DIRECTORY);

        assertThrows(IllegalArgumentException.class, () -> adapter.prepareWorkspace(request));
    }

    @Test
    void testUnsupportedAdaptersThrowExplicitExceptions() {
        InputAdapter gitAdapter = registry.getAdapter(ScanInputType.GIT_REPOSITORY);
        ScanRequest gitRequest = ScanRequest.forGitRepository("https://github.com/org/repo.git", null);
        UnsupportedInputException gitEx = assertThrows(
                UnsupportedInputException.class, () -> gitAdapter.prepareWorkspace(gitRequest)
        );
        assertTrue(gitEx.getMessage().contains("not yet available"));
        assertEquals(ScanInputType.GIT_REPOSITORY, gitEx.getInputType());

        InputAdapter containerAdapter = registry.getAdapter(ScanInputType.CONTAINER_IMAGE);
        ScanRequest containerRequest = ScanRequest.forContainer("app:latest", null);
        UnsupportedInputException contEx = assertThrows(
                UnsupportedInputException.class, () -> containerAdapter.prepareWorkspace(containerRequest)
        );
        assertTrue(contEx.getMessage().contains("not yet available"));

        InputAdapter filesAdapter = registry.getAdapter(ScanInputType.FILES);
        ScanRequest filesRequest = ScanRequest.forFiles("config.yml", null);
        UnsupportedInputException filesEx = assertThrows(
                UnsupportedInputException.class, () -> filesAdapter.prepareWorkspace(filesRequest)
        );
        assertTrue(filesEx.getMessage().contains("not yet available"));

        assertThrows(UnsupportedInputException.class,
                () -> registry.getAdapter(ScanInputType.JAR).prepareWorkspace(new ScanRequest(ScanInputType.JAR, "lib.jar")));
        assertThrows(UnsupportedInputException.class,
                () -> registry.getAdapter(ScanInputType.CLASS).prepareWorkspace(new ScanRequest(ScanInputType.CLASS, "App.class")));
        assertThrows(UnsupportedInputException.class,
                () -> registry.getAdapter(ScanInputType.CONFIGURATION).prepareWorkspace(new ScanRequest(ScanInputType.CONFIGURATION, "application.yml")));
    }

    @Test
    void testRegistrySupportsCheck() {
        assertTrue(registry.isSupported(ScanInputType.ZIP_ARCHIVE));
        assertTrue(registry.isSupported(ScanInputType.DIRECTORY));
        assertFalse(registry.isSupported(ScanInputType.GIT_REPOSITORY));
        assertFalse(registry.isSupported(ScanInputType.CONTAINER_IMAGE));
    }
}
