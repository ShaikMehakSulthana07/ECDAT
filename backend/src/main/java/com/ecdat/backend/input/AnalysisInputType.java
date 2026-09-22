package com.ecdat.backend.input;

/**
 * Enumerates all potential cryptographic analysis input types in ECDAT.
 * Clearly demarcates currently implemented inputs from phased/roadmap inputs.
 */
public enum AnalysisInputType {

    /**
     * Uploaded compressed project archive (.zip / .tar).
     * Fully supported and implemented in Phase 1-4.
     */
    ZIP_ARCHIVE(true, "ZIP / TAR Archive", "Uploaded compressed project archive (.zip)", "PHASE_1_4"),

    /**
     * Direct individual source file (e.g. Java source .java).
     * Supported and implemented in Phase 4.
     */
    SOURCE_FILE(true, "Source File", "Direct Java source file (.java)", "PHASE_4"),

    /**
     * Local filesystem project directory.
     * Supported when explicitly configured/allowed by security policy.
     */
    DIRECTORY(true, "Local Directory", "Local filesystem project directory", "PHASE_1_4"),

    /**
     * Remote or cloned Git repository URL.
     * Implemented in Phase 5.
     */
    REPOSITORY_URL(true, "Repository URL", "Remote Git repository URL (e.g., GitHub, GitLab)", "PHASE_5"),

    /**
     * Application configuration files (YAML, properties, XML, JSON).
     * Implemented in Phase 6.
     */
    CONFIGURATION_FILE(true, "Configuration File", "Application configuration file (e.g. application.yml)", "PHASE_6"),

    /**
     * Compiled Java Archive (.jar) or bytecode class file (.class).
     * Implemented in Phase 7.
     */
    BINARY_FILE(true, "Binary File", "Compiled Java Archive or bytecode (.jar, .class)", "PHASE_7"),

    /**
     * Container image or image archive (Docker/OCI).
     * Implemented in Phase 8.
     */
    CONTAINER_IMAGE(true, "Container Image", "Container image or image archive (.tar, .tar.gz)", "PHASE_8");

    private final boolean supported;
    private final String displayName;
    private final String description;
    private final String plannedPhase;

    AnalysisInputType(boolean supported, String displayName, String description, String plannedPhase) {
        this.supported = supported;
        this.displayName = displayName;
        this.description = description;
        this.plannedPhase = plannedPhase;
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

    public String getPlannedPhase() {
        return plannedPhase;
    }
}
