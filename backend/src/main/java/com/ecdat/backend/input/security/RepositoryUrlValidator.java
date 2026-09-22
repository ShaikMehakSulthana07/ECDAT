package com.ecdat.backend.input.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Validates repository URLs for security and format compliance.
 * Enforces HTTPS, rejects credentials, and normalizes URLs.
 */
public class RepositoryUrlValidator {

    private static final Set<String> ALLOWED_PROTOCOLS = Set.of("https", "http");
    private static final Set<String> PREFERRED_PROTOCOLS = Set.of("https");

    /**
     * Validates and normalizes a repository URL.
     *
     * @param urlString the repository URL string
     * @return normalized URI
     * @throws RepositoryAnalysisException if validation fails
     */
    public URI validateAndNormalize(String urlString) throws RepositoryAnalysisException {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.INVALID_REPOSITORY_URL,
                "Repository URL cannot be empty."
            );
        }

        URI uri;
        try {
            uri = new URI(urlString.trim()).normalize();
        } catch (URISyntaxException e) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.INVALID_REPOSITORY_URL,
                "Repository URL has invalid syntax: " + e.getMessage()
            );
        }

        // Validate protocol
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.INVALID_REPOSITORY_URL,
                "Repository URL must specify a protocol (https:// or http://)."
            );
        }

        String lowerScheme = scheme.toLowerCase();
        if (!ALLOWED_PROTOCOLS.contains(lowerScheme)) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.UNSUPPORTED_REPOSITORY_PROTOCOL,
                "Repository URL protocol '" + lowerScheme + "' is not supported. Only HTTPS and HTTP are allowed."
            );
        }

        // Prefer HTTPS over HTTP
        if (!PREFERRED_PROTOCOLS.contains(lowerScheme)) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.UNSUPPORTED_REPOSITORY_PROTOCOL,
                "Repository URL must use HTTPS for security. HTTP is not supported."
            );
        }

        // Validate host
        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.INVALID_REPOSITORY_URL,
                "Repository URL must include a valid hostname."
            );
        }

        // Reject URLs with embedded credentials
        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.INVALID_REPOSITORY_URL,
                "Repository URL must not contain embedded credentials (username:password)."
            );
        }

        // Validate path is present
        String path = uri.getPath();
        if (path == null || path.trim().isEmpty() || path.equals("/")) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.INVALID_REPOSITORY_URL,
                "Repository URL must include a repository path (e.g., https://github.com/user/project)."
            );
        }

        return uri;
    }

    /**
     * Extracts the repository name from a validated URI.
     *
     * @param uri the validated repository URI
     * @return repository name (last segment of path)
     */
    public String extractRepositoryName(URI uri) {
        String path = uri.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }
}
