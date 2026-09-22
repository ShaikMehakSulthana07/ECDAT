package com.ecdat.backend.input;

/**
 * Enumerates all potential cryptographic analysis input sources.
 * Clearly demarcates currently supported input types versus roadmap/planned types.
 */
public enum ScanInputType {

    /**
     * Uploaded compressed project archive (ZIP/TAR).
     * Fully supported in Phase 1-4.
     */
    ZIP_ARCHIVE(true, "ZIP / TAR Archive", "Uploaded compressed project archive (.zip)"),

    /**
     * Direct individual source file (e.g. Java source .java).
     * Supported in Phase 4.
     */
    SOURCE_FILE(true, "Source File", "Direct Java source file (.java)"),

    /**
     * Local filesystem project directory.
     * Supported when explicitly configured/allowed by security policy.
     */
    DIRECTORY(true, "Local Directory", "Local filesystem project directory"),

    /**
     * Remote or cloned Git repository URL.
     * Planned for Phase 5.
     */
    GIT_REPOSITORY(false, "Git Repository", "Remote Git repository URL (e.g., GitHub, GitLab)"),
    REPOSITORY_URL(false, "Repository URL", "Remote Git repository URL (e.g., GitHub, GitLab)"),

    /**
     * Direct loose files (configuration, certificates, keys, source).
     * Planned for Phase 6.
     */
    FILES(false, "Files & Artifacts", "Individual loose source, config, or certificate files"),

    /**
     * Application configuration files (YAML, properties, XML, JSON).
     * Planned for Phase 6.
     */
    CONFIGURATION(false, "Configuration File", "Application configuration file (e.g. application.yml)"),
    CONFIGURATION_FILE(false, "Configuration File", "Application configuration file (e.g. application.yml)"),

    /**
     * Compiled Java Archive (.jar).
     * Planned for Phase 7.
     */
    JAR(false, "JAR Archive", "Compiled Java Archive binary (.jar)"),

    /**
     * Compiled Java bytecode class file (.class).
     * Planned for Phase 7.
     */
    CLASS(false, "Java Class File", "Compiled Java bytecode class file (.class)"),
    BINARY_FILE(false, "Binary File", "Compiled Java Archive or bytecode (.jar, .class)"),

    /**
     * Container image or image archive (Docker/OCI).
     * Planned for Phase 8.
     */
    CONTAINER_IMAGE(false, "Container Image", "Container image or image archive");

    private final boolean supported;
    private final String displayName;
    private final String description;

    ScanInputType(boolean supported, String displayName, String description) {
        this.supported = supported;
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isSupported() {
        return supported;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public AnalysisInputType toAnalysisInputType() {
        return switch (this) {
            case ZIP_ARCHIVE -> AnalysisInputType.ZIP_ARCHIVE;
            case SOURCE_FILE -> AnalysisInputType.SOURCE_FILE;
            case DIRECTORY -> AnalysisInputType.DIRECTORY;
            case GIT_REPOSITORY, REPOSITORY_URL -> AnalysisInputType.REPOSITORY_URL;
            case FILES, CONFIGURATION, CONFIGURATION_FILE -> AnalysisInputType.CONFIGURATION_FILE;
            case JAR, CLASS, BINARY_FILE -> AnalysisInputType.BINARY_FILE;
            case CONTAINER_IMAGE -> AnalysisInputType.CONTAINER_IMAGE;
        };
    }
}
