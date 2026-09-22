package com.ecdat.backend.input.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Handles secure Git repository acquisition using ProcessBuilder.
 * Detects Git availability, performs shallow clone with timeout.
 */
public class RepositoryAcquisition {

    private final RepositoryConfiguration configuration;

    public RepositoryAcquisition(RepositoryConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Checks if Git is available on the system.
     *
     * @return true if git is installed and executable
     */
    public boolean isGitAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "--version");
            Process process = pb.start();
            boolean completed = process.waitFor(5, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Clones a repository to the specified target directory.
     * Uses shallow clone (--depth 1) and disables submodules.
     *
     * @param repositoryUrl the repository URL
     * @param targetDirectory the target directory for the clone
     * @throws RepositoryAnalysisException if clone fails
     */
    public void cloneRepository(URI repositoryUrl, Path targetDirectory) throws RepositoryAnalysisException {
        if (!isGitAvailable()) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.REPOSITORY_TOOL_UNAVAILABLE,
                "Git is not installed or not available on the system."
            );
        }

        // Ensure target directory exists
        try {
            Files.createDirectories(targetDirectory);
        } catch (IOException e) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.REPOSITORY_CLONE_FAILED,
                "Failed to create target directory: " + e.getMessage()
            );
        }

        // Build git clone command with argument array (no shell string construction)
        ProcessBuilder pb = new ProcessBuilder(
            "git",
            "clone",
            "--depth", "1",           // Shallow clone
            "--no-tags",              // Don't fetch tags
            "--single-branch",        // Clone only default branch
            "--no-recurse-submodules", // Disable submodules
            repositoryUrl.toString(),
            targetDirectory.toString()
        );

        pb.redirectErrorStream(true); // Merge stderr into stdout

        try {
            Process process = pb.start();

            // Read output for error messages
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for completion with timeout
            boolean completed = process.waitFor(configuration.getCloneTimeoutSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new RepositoryAnalysisException(
                    RepositoryErrorCode.REPOSITORY_TIMEOUT,
                    "Repository clone timed out after " + configuration.getCloneTimeoutSeconds() + " seconds."
                );
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorOutput = output.toString();
                String errorMessage = "Git clone failed with exit code " + exitCode;
                if (!errorOutput.trim().isEmpty()) {
                    errorMessage += ": " + errorOutput.trim();
                }
                throw new RepositoryAnalysisException(
                    RepositoryErrorCode.REPOSITORY_CLONE_FAILED,
                    errorMessage
                );
            }

        } catch (IOException e) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.REPOSITORY_CLONE_FAILED,
                "Failed to execute git clone: " + e.getMessage()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.REPOSITORY_CLONE_FAILED,
                "Repository clone was interrupted."
            );
        }
    }

    /**
     * Validates that the cloned repository does not exceed resource limits.
     * Measures workspace size incrementally without loading into memory.
     *
     * @param workspacePath the cloned repository path
     * @throws RepositoryAnalysisException if limits are exceeded
     */
    public void validateResourceLimits(Path workspacePath) throws RepositoryAnalysisException {
        long totalSize = 0;
        int fileCount = 0;

        try (var stream = Files.walk(workspacePath)) {
            for (Path path : stream.toList()) {
                if (Files.isRegularFile(path)) {
                    fileCount++;
                    if (fileCount > configuration.getMaxFileCount()) {
                        throw new RepositoryAnalysisException(
                            RepositoryErrorCode.REPOSITORY_LIMIT_EXCEEDED,
                            "Repository exceeds maximum file count of " + configuration.getMaxFileCount()
                        );
                    }

                    long fileSize = Files.size(path);
                    if (fileSize > configuration.getMaxFileSizeBytes()) {
                        throw new RepositoryAnalysisException(
                            RepositoryErrorCode.REPOSITORY_LIMIT_EXCEEDED,
                            "Repository contains file exceeding maximum size of " + 
                            (configuration.getMaxFileSizeBytes() / (1024 * 1024)) + " MB: " + 
                            path.getFileName()
                        );
                    }

                    totalSize += fileSize;
                    if (totalSize > configuration.getMaxRepositorySizeBytes()) {
                        throw new RepositoryAnalysisException(
                            RepositoryErrorCode.REPOSITORY_LIMIT_EXCEEDED,
                            "Repository exceeds maximum size of " + 
                            (configuration.getMaxRepositorySizeBytes() / (1024 * 1024)) + " MB"
                        );
                    }
                }
            }
        } catch (IOException e) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.REPOSITORY_ANALYSIS_FAILED,
                "Failed to validate repository resource limits: " + e.getMessage()
            );
        }
    }
}
