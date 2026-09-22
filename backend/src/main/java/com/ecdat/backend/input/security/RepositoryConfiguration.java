package com.ecdat.backend.input.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for repository scanning operations.
 */
@Component
@ConfigurationProperties(prefix = "ecdat.repository")
public class RepositoryConfiguration {

    /**
     * Maximum time in seconds allowed for git clone operation.
     */
    private long cloneTimeoutSeconds = 300; // 5 minutes

    /**
     * Maximum repository size in bytes after clone.
     */
    private long maxRepositorySizeBytes = 500 * 1024 * 1024; // 500 MB

    /**
     * Maximum number of files allowed in repository.
     */
    private int maxFileCount = 50000;

    /**
     * Maximum individual file size in bytes.
     */
    private long maxFileSizeBytes = 50 * 1024 * 1024; // 50 MB

    public long getCloneTimeoutSeconds() {
        return cloneTimeoutSeconds;
    }

    public void setCloneTimeoutSeconds(long cloneTimeoutSeconds) {
        this.cloneTimeoutSeconds = cloneTimeoutSeconds;
    }

    public long getMaxRepositorySizeBytes() {
        return maxRepositorySizeBytes;
    }

    public void setMaxRepositorySizeBytes(long maxRepositorySizeBytes) {
        this.maxRepositorySizeBytes = maxRepositorySizeBytes;
    }

    public int getMaxFileCount() {
        return maxFileCount;
    }

    public void setMaxFileCount(int maxFileCount) {
        this.maxFileCount = maxFileCount;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }
}
