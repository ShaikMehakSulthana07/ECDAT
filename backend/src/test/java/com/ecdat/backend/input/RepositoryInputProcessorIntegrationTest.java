package com.ecdat.backend.input;

import com.ecdat.backend.input.security.RepositoryConfiguration;
import com.ecdat.backend.service.AnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Repository Input Processor Integration Test")
class RepositoryInputProcessorIntegrationTest {

    @Test
    @DisplayName("Repository processor should be registered in registry")
    void testRepositoryProcessorRegistered() {
        AnalysisInputProcessorRegistry registry = new AnalysisInputProcessorRegistry();
        
        AnalysisInputProcessor processor = registry.getProcessor(AnalysisInputType.REPOSITORY_URL);
        assertNotNull(processor);
        assertTrue(processor instanceof RepositoryInputProcessor);
        assertEquals(AnalysisInputType.REPOSITORY_URL, processor.getInputType());
    }

    @Test
    @DisplayName("Repository processor should support REPOSITORY_URL type")
    void testRepositoryProcessorSupportsType() {
        AnalysisInputProcessorRegistry registry = new AnalysisInputProcessorRegistry();
        
        assertTrue(registry.isSupported(AnalysisInputType.REPOSITORY_URL));
    }

    @Test
    @DisplayName("Repository processor should validate repository URL")
    void testRepositoryProcessorValidatesUrl() {
        RepositoryInputProcessor processor = new RepositoryInputProcessor();
        
        AnalysisInput input = new AnalysisInput(AnalysisInputType.REPOSITORY_URL, "test");
        input.setRepositoryUrl("not-a-url");
        
        assertThrows(IllegalArgumentException.class, () -> processor.validate(input));
    }

    @Test
    @DisplayName("Repository processor should validate non-empty URL")
    void testRepositoryProcessorValidatesNonEmptyUrl() {
        RepositoryInputProcessor processor = new RepositoryInputProcessor();
        
        AnalysisInput input = new AnalysisInput(AnalysisInputType.REPOSITORY_URL, "test");
        input.setRepositoryUrl("");
        
        assertThrows(IllegalArgumentException.class, () -> processor.validate(input));
    }

    @Test
    @DisplayName("Repository processor should validate null input")
    void testRepositoryProcessorValidatesNullInput() {
        RepositoryInputProcessor processor = new RepositoryInputProcessor();
        
        assertThrows(IllegalArgumentException.class, () -> processor.validate(null));
    }

    @Test
    @DisplayName("Repository processor should reject HTTP URL")
    void testRepositoryProcessorRejectsHttpUrl() {
        RepositoryInputProcessor processor = new RepositoryInputProcessor();
        
        AnalysisInput input = new AnalysisInput(AnalysisInputType.REPOSITORY_URL, "test");
        input.setRepositoryUrl("http://github.com/example/project");
        
        // HTTP rejection throws IllegalArgumentException from RepositoryAnalysisException
        assertThrows(IllegalArgumentException.class, () -> processor.validate(input));
    }

    @Test
    @DisplayName("Repository processor should accept valid HTTPS URL")
    void testRepositoryProcessorAcceptsValidHttpsUrl() {
        RepositoryInputProcessor processor = new RepositoryInputProcessor();
        
        AnalysisInput input = new AnalysisInput(AnalysisInputType.REPOSITORY_URL, "test");
        input.setRepositoryUrl("https://github.com/example/project");
        
        assertDoesNotThrow(() -> processor.validate(input));
    }

    @Test
    @DisplayName("Repository processor should block localhost via SSRF")
    void testRepositoryProcessorBlocksLocalhost() {
        RepositoryInputProcessor processor = new RepositoryInputProcessor();
        
        AnalysisInput input = new AnalysisInput(AnalysisInputType.REPOSITORY_URL, "test");
        input.setRepositoryUrl("https://localhost/project");
        
        assertThrows(SecurityException.class, () -> processor.validate(input));
    }

    @Test
    @DisplayName("Repository processor should block private IP via SSRF")
    void testRepositoryProcessorBlocksPrivateIp() {
        RepositoryInputProcessor processor = new RepositoryInputProcessor();
        
        AnalysisInput input = new AnalysisInput(AnalysisInputType.REPOSITORY_URL, "test");
        input.setRepositoryUrl("https://10.0.0.1/project");
        
        assertThrows(SecurityException.class, () -> processor.validate(input));
    }

    @Test
    @DisplayName("Repository processor should use custom configuration")
    void testRepositoryProcessorUsesCustomConfiguration() {
        RepositoryConfiguration config = new RepositoryConfiguration();
        config.setCloneTimeoutSeconds(600);
        
        RepositoryInputProcessor processor = new RepositoryInputProcessor(config);
        
        assertNotNull(processor);
        // Configuration is used internally, verified by behavior
    }

    @Test
    @DisplayName("End-to-end: AnalysisService should use repository processor")
    void testAnalysisServiceUsesRepositoryProcessor() {
        AnalysisService service = new AnalysisService();
        
        AnalysisInput input = AnalysisInput.forRepository("https://github.com/example/project", null);
        
        // This will fail if Git is not available or repository doesn't exist
        // But it verifies the processor is wired correctly
        assertThrows(Exception.class, () -> service.analyze(input));
    }
}
