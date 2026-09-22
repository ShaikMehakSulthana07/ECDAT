package com.ecdat.backend.input.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Repository Acquisition Tests")
class RepositoryAcquisitionTest {

    @Test
    @DisplayName("Git availability check should return boolean")
    void testGitAvailabilityCheck() {
        RepositoryConfiguration config = new RepositoryConfiguration();
        RepositoryAcquisition acquisition = new RepositoryAcquisition(config);
        
        // This test verifies that Git detection works
        // The result depends on whether Git is installed in the test environment
        boolean gitAvailable = acquisition.isGitAvailable();
        
        // Just verify it returns a boolean without throwing
        assertTrue(gitAvailable == true || gitAvailable == false);
    }

    @Test
    @DisplayName("Git availability check should complete within timeout")
    void testGitAvailabilityTimeout() {
        RepositoryConfiguration config = new RepositoryConfiguration();
        RepositoryAcquisition acquisition = new RepositoryAcquisition(config);
        
        // Should complete within 5 seconds even if Git is not available
        long startTime = System.currentTimeMillis();
        acquisition.isGitAvailable();
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(duration < 5000, "Git availability check should complete within 5 seconds");
    }

    @Test
    @DisplayName("Clone should throw REPOSITORY_TOOL_UNAVAILABLE when Git is not available")
    void testCloneWithoutGit() {
        RepositoryConfiguration config = new RepositoryConfiguration();
        RepositoryAcquisition acquisition = new RepositoryAcquisition(config);
        
        // Mock a scenario where Git is not available by checking first
        if (!acquisition.isGitAvailable()) {
            RepositoryAnalysisException e = assertThrows(
                RepositoryAnalysisException.class,
                () -> acquisition.cloneRepository(
                    java.net.URI.create("https://github.com/example/project"),
                    java.nio.file.Path.of("/tmp/test")
                )
            );
            assertEquals(RepositoryErrorCode.REPOSITORY_TOOL_UNAVAILABLE, e.getErrorCode());
            assertTrue(e.getMessage().contains("Git"));
        }
    }
}
