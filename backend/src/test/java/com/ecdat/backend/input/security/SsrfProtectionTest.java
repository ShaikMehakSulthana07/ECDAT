package com.ecdat.backend.input.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SSRF Protection Tests")
class SsrfProtectionTest {

    private final SsrfProtection ssrfProtection = new SsrfProtection();

    @Test
    @DisplayName("Valid public GitHub URL should pass SSRF validation")
    void testValidPublicUrl() {
        URI uri = URI.create("https://github.com/example/project");
        assertDoesNotThrow(() -> ssrfProtection.validateDestination(uri));
    }

    @Test
    @DisplayName("Valid public GitLab URL should pass SSRF validation")
    void testValidPublicGitLabUrl() {
        URI uri = URI.create("https://gitlab.com/example/project");
        assertDoesNotThrow(() -> ssrfProtection.validateDestination(uri));
    }

    @Test
    @DisplayName("localhost hostname should be blocked")
    void testLocalhostBlocked() {
        URI uri = URI.create("https://localhost/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("127.0.0.1 should be blocked")
    void testLoopbackIpBlocked() {
        URI uri = URI.create("https://127.0.0.1/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("0.0.0.0 should be blocked")
    void testZeroIpBlocked() {
        URI uri = URI.create("https://0.0.0.0/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("10.0.0.1 (private IPv4) should be blocked")
    void testPrivateClassABlocked() {
        URI uri = URI.create("https://10.0.0.1/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("172.16.0.1 (private IPv4) should be blocked")
    void testPrivateClassBBlocked() {
        URI uri = URI.create("https://172.16.0.1/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("192.168.1.1 (private IPv4) should be blocked")
    void testPrivateClassCBlocked() {
        URI uri = URI.create("https://192.168.1.1/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("169.254.169.254 (cloud metadata) should be blocked")
    void testCloudMetadataEndpointBlocked() {
        URI uri = URI.create("https://169.254.169.254/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("169.254.0.1 (link-local) should be blocked")
    void testLinkLocalBlocked() {
        URI uri = URI.create("https://169.254.0.1/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("metadata hostname should be blocked")
    void testMetadataHostnameBlocked() {
        URI uri = URI.create("https://metadata/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("metadata.localdomain should be blocked")
    void testMetadataLocaldomainBlocked() {
        URI uri = URI.create("https://metadata.localdomain/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("IPv6 loopback ::1 should be blocked")
    void testIpv6LoopbackBlocked() {
        // This test documents that IPv6 loopback is blocked
        // Actual resolution may vary by test environment
        URI uri = URI.create("https://[::1]/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateDestination(uri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("Redirect to localhost should be blocked")
    void testRedirectToLocalhostBlocked() {
        URI redirectUri = URI.create("https://localhost/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateRedirect(redirectUri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("Redirect to private IP should be blocked")
    void testRedirectToPrivateIpBlocked() {
        URI redirectUri = URI.create("https://10.0.0.1/project");
        RepositoryAnalysisException e = assertThrows(
            RepositoryAnalysisException.class,
            () -> ssrfProtection.validateRedirect(redirectUri)
        );
        assertEquals(RepositoryErrorCode.REPOSITORY_HOST_BLOCKED, e.getErrorCode());
    }

    @Test
    @DisplayName("Redirect to public URL should pass validation")
    void testRedirectToPublicUrlAllowed() {
        URI redirectUri = URI.create("https://github.com/other/project");
        assertDoesNotThrow(() -> ssrfProtection.validateRedirect(redirectUri));
    }
}
