package com.ecdat.backend.input;

import com.ecdat.backend.input.security.RepositoryAcquisition;
import com.ecdat.backend.input.security.RepositoryAnalysisException;
import com.ecdat.backend.input.security.RepositoryConfiguration;
import com.ecdat.backend.input.security.RepositoryUrlValidator;
import com.ecdat.backend.input.security.SsrfProtection;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * Processor for Git repository URL analysis inputs.
 * Validates URLs, performs SSRF protection, clones repositories securely,
 * and produces a normalized ScanWorkspace for the existing analysis pipeline.
 */
@Component
public class RepositoryInputProcessor implements AnalysisInputProcessor {

    private final RepositoryUrlValidator urlValidator;
    private final SsrfProtection ssrfProtection;
    private final RepositoryAcquisition acquisition;
    private final RepositoryConfiguration configuration;

    public RepositoryInputProcessor() {
        this.configuration = new RepositoryConfiguration();
        this.urlValidator = new RepositoryUrlValidator();
        this.ssrfProtection = new SsrfProtection();
        this.acquisition = new RepositoryAcquisition(configuration);
    }

    public RepositoryInputProcessor(RepositoryConfiguration configuration) {
        this.configuration = configuration != null ? configuration : new RepositoryConfiguration();
        this.urlValidator = new RepositoryUrlValidator();
        this.ssrfProtection = new SsrfProtection();
        this.acquisition = new RepositoryAcquisition(this.configuration);
    }

    @Override
    public AnalysisInputType getInputType() {
        return AnalysisInputType.REPOSITORY_URL;
    }

    @Override
    public boolean supports(AnalysisInputType type) {
        return type == AnalysisInputType.REPOSITORY_URL;
    }

    @Override
    public void validate(AnalysisInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Analysis input must not be null.");
        }

        String repositoryUrl = input.getRepositoryUrl();
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository URL must not be empty.");
        }

        // Validate URL format and security
        URI uri;
        try {
            uri = urlValidator.validateAndNormalize(repositoryUrl);
        } catch (RepositoryAnalysisException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        // SSRF protection: validate DNS resolution and IP addresses
        try {
            ssrfProtection.validateDestination(uri);
        } catch (RepositoryAnalysisException e) {
            throw new SecurityException(e.getMessage(), e);
        }
    }

    @Override
    public ScanWorkspace process(AnalysisInput input) throws IOException {
        validate(input);

        String repositoryUrl = input.getRepositoryUrl();
        URI uri;
        try {
            uri = urlValidator.validateAndNormalize(repositoryUrl);
        } catch (RepositoryAnalysisException e) {
            throw new IOException("Failed to validate repository URL: " + e.getMessage(), e);
        }

        // Create temporary workspace (responsible for lifecycle/cleanup)
        String repositoryName = urlValidator.extractRepositoryName(uri);
        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-repo-" + repositoryName + "-");

        try {
            // Acquire repository into workspace
            Path workspaceRoot = workspace.getRootPath();
            acquisition.cloneRepository(uri, workspaceRoot);

            // Validate resource limits (incremental measurement, not loading into memory)
            acquisition.validateResourceLimits(workspaceRoot);

            return workspace;
        } catch (RepositoryAnalysisException e) {
            // Clean up workspace on failure
            workspace.close();
            throw new IOException("Failed to acquire repository: " + e.getMessage(), e);
        } catch (Exception e) {
            // Clean up workspace on any failure
            workspace.close();
            throw new IOException("Failed to process repository: " + e.getMessage(), e);
        }
    }
}
