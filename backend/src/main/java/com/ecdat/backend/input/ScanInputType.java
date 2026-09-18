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
     * Local filesystem project directory.
     * Supported when explicitly configured/allowed by security policy.
     */
    DIRECTORY(true, "Local Directory", "Local filesystem project directory"),

    /**
     * Remote or cloned Git repository URL.
     * Planned for Phase 5.
     */
    GIT_REPOSITORY(false, "Git Repository", "Remote Git repository URL (e.g., GitHub, GitLab)"),

    /**
     * Direct loose files (configuration, certificates, keys, source).
     * Planned for Phase 6.
     */
    FILES(false, "Files & Artifacts", "Individual loose source, config, or certificate files"),

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

    /**
     * Application configuration files (YAML, properties, XML, JSON).
     * Planned for Phase 6.
     */
    CONFIGURATION(false, "Configuration File", "Application configuration file (e.g. application.yml)"),

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
}
