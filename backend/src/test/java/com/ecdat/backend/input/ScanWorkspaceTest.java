package com.ecdat.backend.input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ScanWorkspaceTest {

    @Test
    void testTemporaryWorkspaceCleanup() throws IOException {
        Path tempPath;
        try (ScanWorkspace workspace = ScanWorkspace.createTemporary("test-ws-")) {
            tempPath = workspace.getRootPath();
            assertTrue(Files.exists(tempPath));
            assertTrue(workspace.isTemporary());
            
            // Create a test file inside workspace
            Files.writeString(tempPath.resolve("sample.txt"), "hello");
            assertTrue(Files.exists(tempPath.resolve("sample.txt")));
        }

        // After close, temporary directory must be cleaned up
        assertFalse(Files.exists(tempPath), "Temporary workspace directory must be deleted on close");
    }

    @Test
    void testExistingDirectoryWorkspaceNotDeletedOnClose(@TempDir Path tempDir) {
        Path existingDir = tempDir.resolve("my-app");
        try {
            Files.createDirectory(existingDir);
            Files.writeString(existingDir.resolve("Main.java"), "class Main {}");

            try (ScanWorkspace workspace = ScanWorkspace.forExistingDirectory(existingDir)) {
                assertEquals(existingDir.toAbsolutePath().normalize(), workspace.getRootPath());
                assertFalse(workspace.isTemporary());
                assertEquals(existingDir.toAbsolutePath().normalize(), workspace.getSourcePath());
            }

            // Existing directory must still exist
            assertTrue(Files.exists(existingDir));
            assertTrue(Files.exists(existingDir.resolve("Main.java")));
        } catch (IOException e) {
            fail("IOException thrown during test: " + e.getMessage());
        }
    }

    @Test
    void testNormalizedSubpaths() throws IOException {
        try (ScanWorkspace workspace = ScanWorkspace.createTemporary("test-paths-")) {
            Path root = workspace.getRootPath();
            assertEquals(root.resolve("binaries"), workspace.getBinariesPath());
            assertEquals(root.resolve("config"), workspace.getConfigPath());
            assertEquals(root.resolve("certificates"), workspace.getCertificatesPath());
            assertEquals(root.resolve("metadata"), workspace.getMetadataPath());
            assertFalse(Files.exists(workspace.getBinariesPath()), "Phase 4 must not create unused binaries/");
            assertFalse(Files.exists(workspace.getConfigPath()), "Phase 4 must not create unused config/");
            assertFalse(Files.exists(workspace.getCertificatesPath()), "Phase 4 must not create unused certificates/");
            assertFalse(Files.exists(workspace.getMetadataPath()), "Phase 4 must not create unused metadata/");
        }
    }
}
