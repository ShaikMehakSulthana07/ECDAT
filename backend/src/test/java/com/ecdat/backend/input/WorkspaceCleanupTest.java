package com.ecdat.backend.input;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Workspace Cleanup Tests")
class WorkspaceCleanupTest {

    @Test
    @DisplayName("Temporary workspace should be cleaned up on close")
    void testTemporaryWorkspaceCleanup() throws Exception {
        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-test-");
        Path rootPath = workspace.getRootPath();
        
        assertTrue(Files.exists(rootPath), "Workspace should exist before close");
        
        workspace.close();
        
        // Workspace should be deleted after close
        assertFalse(Files.exists(rootPath), "Workspace should be deleted after close");
    }

    @Test
    @DisplayName("Temporary workspace should create unique directories")
    void testTemporaryWorkspaceUniqueness() throws Exception {
        ScanWorkspace workspace1 = ScanWorkspace.createTemporary("ecdat-test-");
        ScanWorkspace workspace2 = ScanWorkspace.createTemporary("ecdat-test-");
        
        assertNotEquals(
            workspace1.getRootPath(),
            workspace2.getRootPath(),
            "Workspaces should have unique paths"
        );
        
        workspace1.close();
        workspace2.close();
    }

    @Test
    @DisplayName("Non-temporary workspace should not be cleaned up on close")
    void testNonTemporaryWorkspaceNotCleaned() throws Exception {
        Path tempDir = Files.createTempDirectory("ecdat-test-");
        ScanWorkspace workspace = ScanWorkspace.forExistingDirectory(tempDir);
        
        assertTrue(Files.exists(tempDir), "Workspace should exist before close");
        
        workspace.close();
        
        // Non-temporary workspace should NOT be deleted
        assertTrue(Files.exists(tempDir), "Non-temporary workspace should not be deleted");
        
        // Clean up manually
        Files.deleteIfExists(tempDir);
    }

    @Test
    @DisplayName("Workspace cleanup should handle non-existent directory gracefully")
    void testWorkspaceCleanupHandlesNonExistent() {
        ScanWorkspace workspace = ScanWorkspace.forExistingDirectory(Path.of("/nonexistent/path"));
        
        assertDoesNotThrow(() -> workspace.close(), "Cleanup should not throw for non-existent path");
    }

    @Test
    @DisplayName("Workspace close should be idempotent")
    void testWorkspaceCloseIdempotent() throws Exception {
        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-test-");
        Path rootPath = workspace.getRootPath();
        
        workspace.close();
        assertFalse(Files.exists(rootPath));
        
        // Second close should not throw
        assertDoesNotThrow(() -> workspace.close());
    }

    @Test
    @DisplayName("Workspace should provide source path")
    void testWorkspaceSourcePath() throws Exception {
        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-test-");
        
        Path sourcePath = workspace.getSourcePath();
        assertNotNull(sourcePath);
        assertTrue(Files.exists(sourcePath));
        
        workspace.close();
    }

    @Test
    @DisplayName("Workspace should provide subdirectories")
    void testWorkspaceSubdirectories() throws Exception {
        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-test-");
        Path rootPath = workspace.getRootPath();
        
        assertEquals(rootPath.resolve("binaries"), workspace.getBinariesPath());
        assertEquals(rootPath.resolve("config"), workspace.getConfigPath());
        assertEquals(rootPath.resolve("certificates"), workspace.getCertificatesPath());
        assertEquals(rootPath.resolve("metadata"), workspace.getMetadataPath());
        
        workspace.close();
    }

    @Test
    @DisplayName("Workspace should be marked as temporary")
    void testWorkspaceTemporaryFlag() throws Exception {
        ScanWorkspace tempWorkspace = ScanWorkspace.createTemporary("ecdat-test-");
        assertTrue(tempWorkspace.isTemporary());
        tempWorkspace.close();
        
        Path tempDir = Files.createTempDirectory("ecdat-test-");
        ScanWorkspace existingWorkspace = ScanWorkspace.forExistingDirectory(tempDir);
        assertFalse(existingWorkspace.isTemporary());
        existingWorkspace.close();
        Files.deleteIfExists(tempDir);
    }
}
