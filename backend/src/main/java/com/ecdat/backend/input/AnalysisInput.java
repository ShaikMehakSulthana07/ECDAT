package com.ecdat.backend.input;

import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Normalized analysis request context encapsulating input type, source, and metadata.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisInput {

    private AnalysisInputType inputType;
    private String originalName;
    private String sourceIdentifier;
    private transient MultipartFile archiveFile;
    private transient MultipartFile sourceFile;
    private transient MultipartFile configurationFile;
    private transient MultipartFile binaryFile;
    private transient MultipartFile containerFile;
    private String directoryPath;
    private String repositoryUrl;
    private String projectName;
    private ProjectAnalysisContext context;
    private Set<AnalysisScope> scopes;
    private Map<String, Object> metadata;
    private Map<String, Object> securityMetadata;

    public AnalysisInput() {
        this.context = new ProjectAnalysisContext();
        this.scopes = AnalysisScope.defaultScopes();
        this.metadata = new HashMap<>();
        this.securityMetadata = new HashMap<>();
    }

    public AnalysisInput(AnalysisInputType inputType, String originalName) {
        this();
        this.inputType = inputType;
        this.originalName = originalName;
        this.sourceIdentifier = originalName;
    }

    public static AnalysisInput forZip(MultipartFile file, ProjectAnalysisContext context) {
        String name = file != null ? file.getOriginalFilename() : "archive.zip";
        AnalysisInput input = new AnalysisInput(AnalysisInputType.ZIP_ARCHIVE, name);
        input.setArchiveFile(file);
        if (context != null) {
            input.setContext(context);
            if (context.getApplicationName() != null) {
                input.setProjectName(context.getApplicationName());
            }
        }
        return input;
    }

    public static AnalysisInput forSourceFile(MultipartFile file, ProjectAnalysisContext context) {
        String name = file != null ? file.getOriginalFilename() : "Source.java";
        AnalysisInput input = new AnalysisInput(AnalysisInputType.SOURCE_FILE, name);
        input.setSourceFile(file);
        if (context != null) {
            input.setContext(context);
            if (context.getApplicationName() != null) {
                input.setProjectName(context.getApplicationName());
            }
        }
        return input;
    }

    public static AnalysisInput forDirectory(String path, ProjectAnalysisContext context) {
        AnalysisInput input = new AnalysisInput(AnalysisInputType.DIRECTORY, path);
        input.setDirectoryPath(path);
        if (context != null) {
            input.setContext(context);
            if (context.getApplicationName() != null) {
                input.setProjectName(context.getApplicationName());
            }
        }
        return input;
    }

    public static AnalysisInput forRepository(String repositoryUrl, ProjectAnalysisContext context) {
        AnalysisInput input = new AnalysisInput(AnalysisInputType.REPOSITORY_URL, repositoryUrl);
        input.setRepositoryUrl(repositoryUrl);
        if (context != null) {
            input.setContext(context);
        }
        return input;
    }

    public static AnalysisInput forConfiguration(MultipartFile file, ProjectAnalysisContext context) {
        String name = file != null ? file.getOriginalFilename() : "config.properties";
        AnalysisInput input = new AnalysisInput(AnalysisInputType.CONFIGURATION_FILE, name);
        input.setConfigurationFile(file);
        if (context != null) {
            input.setContext(context);
            if (context.getApplicationName() != null) {
                input.setProjectName(context.getApplicationName());
            }
        }
        return input;
    }

    public static AnalysisInput forBinary(MultipartFile file, ProjectAnalysisContext context) {
        String name = file != null ? file.getOriginalFilename() : "binary.jar";
        AnalysisInput input = new AnalysisInput(AnalysisInputType.BINARY_FILE, name);
        input.setBinaryFile(file);
        if (context != null) {
            input.setContext(context);
            if (context.getApplicationName() != null) {
                input.setProjectName(context.getApplicationName());
            }
        }
        return input;
    }

    public static AnalysisInput forContainer(MultipartFile file, ProjectAnalysisContext context) {
        String name = file != null ? file.getOriginalFilename() : "container.tar";
        AnalysisInput input = new AnalysisInput(AnalysisInputType.CONTAINER_IMAGE, name);
        input.setContainerFile(file);
        if (context != null) {
            input.setContext(context);
            if (context.getApplicationName() != null) {
                input.setProjectName(context.getApplicationName());
            }
        }
        return input;
    }

    // Getters and Setters
    public AnalysisInputType getInputType() {
        return inputType;
    }

    public void setInputType(AnalysisInputType inputType) {
        this.inputType = inputType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getSourceIdentifier() {
        return sourceIdentifier != null ? sourceIdentifier : originalName;
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

    public MultipartFile getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(MultipartFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public MultipartFile getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(MultipartFile configurationFile) {
        this.configurationFile = configurationFile;
    }

    public MultipartFile getBinaryFile() {
        return binaryFile;
    }

    public void setBinaryFile(MultipartFile binaryFile) {
        this.binaryFile = binaryFile;
    }

    public MultipartFile getContainerFile() {
        return containerFile;
    }

    public void setContainerFile(MultipartFile containerFile) {
        this.containerFile = containerFile;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public Map<String, Object> getSecurityMetadata() {
        return securityMetadata;
    }

    public void setSecurityMetadata(Map<String, Object> securityMetadata) {
        this.securityMetadata = securityMetadata != null ? new HashMap<>(securityMetadata) : new HashMap<>();
    }
}
