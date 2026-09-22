package com.ecdat.backend.input.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Repository URL Validation Tests")
class RepositoryUrlValidatorTest {

    private final RepositoryUrlValidator validator = new RepositoryUrlValidator();

    @Test
    @DisplayName("Valid HTTPS GitHub URL should pass validation")
    void testValidHttpsGitHubUrl() {
        String url = "https://github.com/example/project";
        URI uri = validator.validateAndNormalize(url);
        assertNotNull(uri);
        assertEquals("https", uri.getScheme());
        assertEquals("github.com", uri.getHost());
        assertEquals("/example/project", uri.getPath());
    }

    @Test
    @DisplayName("Valid HTTPS GitLab URL should pass validation")
    void testValidHttpsGitLabUrl() {
        String url = "https://gitlab.com/example/project";
        URI uri = validator.validateAndNormalize(url);
        assertNotNull(uri);
        assertEquals("https", uri.getScheme());
        assertEquals("gitlab.com", uri.getHost());
    }

    @Test
    @DisplayName("Empty URL should throw INVALID_REPOSITORY_URL")
    void testEmptyUrl() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("")
        );
        assertEquals(RepositoryErrorCode.INVALID_REPOSITORY_URL, e.getErrorCode());
    }

    @Test
    @DisplayName("Null URL should throw INVALID_REPOSITORY_URL")
    void testNullUrl() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize(null)
        );
        assertEquals(RepositoryErrorCode.INVALID_REPOSITORY_URL, e.getErrorCode());
    }

    @Test
    @DisplayName("Malformed URL should throw INVALID_REPOSITORY_URL")
    void testMalformedUrl() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("not-a-url")
        );
        assertEquals(RepositoryErrorCode.INVALID_REPOSITORY_URL, e.getErrorCode());
    }

    @Test
    @DisplayName("HTTP URL should throw UNSUPPORTED_REPOSITORY_PROTOCOL")
    void testHttpUrlRejected() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("http://github.com/example/project")
        );
        assertEquals(RepositoryErrorCode.UNSUPPORTED_REPOSITORY_PROTOCOL, e.getErrorCode());
        assertTrue(e.getMessage().contains("HTTPS"));
    }

    @Test
    @DisplayName("FTP URL should throw UNSUPPORTED_REPOSITORY_PROTOCOL")
    void testFtpUrlRejected() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("ftp://example.com/project")
        );
        assertEquals(RepositoryErrorCode.UNSUPPORTED_REPOSITORY_PROTOCOL, e.getErrorCode());
    }

    @Test
    @DisplayName("URL without protocol should throw INVALID_REPOSITORY_URL")
    void testUrlWithoutProtocol() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("github.com/example/project")
        );
        assertEquals(RepositoryErrorCode.INVALID_REPOSITORY_URL, e.getErrorCode());
        assertTrue(e.getMessage().contains("protocol"));
    }

    @Test
    @DisplayName("URL with embedded credentials should throw INVALID_REPOSITORY_URL")
    void testUrlWithCredentials() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("https://user:pass@github.com/example/project")
        );
        assertEquals(RepositoryErrorCode.INVALID_REPOSITORY_URL, e.getErrorCode());
        assertTrue(e.getMessage().contains("credentials"));
    }

    @Test
    @DisplayName("URL without path should throw INVALID_REPOSITORY_URL")
    void testUrlWithoutPath() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("https://github.com")
        );
        assertEquals(RepositoryErrorCode.INVALID_REPOSITORY_URL, e.getErrorCode());
        assertTrue(e.getMessage().contains("path"));
    }

    @Test
    @DisplayName("URL with only slash path should throw INVALID_REPOSITORY_URL")
    void testUrlWithOnlySlash() {
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> validator.validateAndNormalize("https://github.com/")
        );
        assertEquals(RepositoryErrorCode.INVALID_REPOSITORY_URL, e.getErrorCode());
    }

    @Test
    @DisplayName("Extract repository name from URL")
    void testExtractRepositoryName() {
        URI uri = URI.create("https://github.com/example/my-project");
        String name = validator.extractRepositoryName(uri);
        assertEquals("my-project", name);
    }

    @Test
    @DisplayName("Extract repository name from URL with trailing slash")
    void testExtractRepositoryNameWithTrailingSlash() {
        URI uri = URI.create("https://github.com/example/my-project/");
        String name = validator.extractRepositoryName(uri);
        assertEquals("my-project", name);
    }

    @Test
    @DisplayName("Extract repository name from nested path")
    void testExtractRepositoryNameFromNestedPath() {
        URI uri = URI.create("https://github.com/org/group/project");
        String name = validator.extractRepositoryName(uri);
        assertEquals("project", name);
    }
}
