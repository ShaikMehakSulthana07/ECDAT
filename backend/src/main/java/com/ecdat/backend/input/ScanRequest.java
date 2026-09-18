package com.ecdat.backend.input;

import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unified representation of a scan request across multiple potential input sources.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScanRequest {

    private ScanInputType inputType;
    private String sourceIdentifier;
    private transient MultipartFile archiveFile;
    private String directoryPath;
    private String repositoryUrl;
    private String projectName;
    private ProjectAnalysisContext context;
    private Set<AnalysisScope> scopes;

    public ScanRequest() {
        this.context = new ProjectAnalysisContext();
        this.scopes = AnalysisScope.defaultScopes();
    }

    public ScanRequest(ScanInputType inputType, String sourceIdentifier) {
        this();
        this.inputType = inputType;
        this.sourceIdentifier = sourceIdentifier;
    }

    /**
     * Factory method for creating a ZIP archive scan request.
     */
    public static ScanRequest forZip(MultipartFile file, ProjectAnalysisContext context) {
        ScanRequest request = new ScanRequest(ScanInputType.ZIP_ARCHIVE, file != null ? file.getOriginalFilename() : "archive.zip");
        request.setArchiveFile(file);
        if (context != null) {
            request.setContext(context);
            if (context.getApplicationName() != null) {
                request.setProjectName(context.getApplicationName());
            }
        }
        return request;
    }

    /**
     * Factory method for creating a local directory scan request.
     */
    public static ScanRequest forDirectory(String path, ProjectAnalysisContext context) {
        ScanRequest request = new ScanRequest(ScanInputType.DIRECTORY, path);
        request.setDirectoryPath(path);
        if (context != null) {
            request.setContext(context);
            if (context.getApplicationName() != null) {
                request.setProjectName(context.getApplicationName());
            }
        }
        return request;
    }

    /**
     * Factory method for creating a Git repository scan request (Roadmap Phase 5).
     */
    public static ScanRequest forGitRepository(String repositoryUrl, ProjectAnalysisContext context) {
        ScanRequest request = new ScanRequest(ScanInputType.GIT_REPOSITORY, repositoryUrl);
        request.setRepositoryUrl(repositoryUrl);
        if (context != null) {
            request.setContext(context);
        }
        return request;
    }

    /**
     * Factory method for creating a loose files scan request (Roadmap Phase 6).
     */
    public static ScanRequest forFiles(String sourceDescription, ProjectAnalysisContext context) {
        ScanRequest request = new ScanRequest(ScanInputType.FILES, sourceDescription);
        if (context != null) {
            request.setContext(context);
        }
        return request;
    }

    /**
     * Factory method for creating a container image scan request (Roadmap Phase 8).
     */
    public static ScanRequest forContainer(String imageName, ProjectAnalysisContext context) {
        ScanRequest request = new ScanRequest(ScanInputType.CONTAINER_IMAGE, imageName);
        if (context != null) {
            request.setContext(context);
        }
        return request;
    }

    // Getters and Setters
    public ScanInputType getInputType() {
        return inputType;
    }

    public void setInputType(ScanInputType inputType) {
        this.inputType = inputType;
    }

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    public MultipartFile getArchiveFile() {
        return archiveFile;
    }

    public void setArchiveFile(MultipartFile archiveFile) {
        this.archiveFile = archiveFile;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ProjectAnalysisContext getContext() {
        return context;
    }

    public void setContext(ProjectAnalysisContext context) {
        this.context = context;
    }

    public Set<AnalysisScope> getScopes() {
        return scopes != null ? Collections.unmodifiableSet(scopes) : Collections.emptySet();
    }

    public void setScopes(Set<AnalysisScope> scopes) {
        this.scopes = scopes != null ? new HashSet<>(scopes) : new HashSet<>();
    }

    public boolean isScopeEnabled(AnalysisScope scope) {
        return scopes != null && scopes.contains(scope);
    }
}
