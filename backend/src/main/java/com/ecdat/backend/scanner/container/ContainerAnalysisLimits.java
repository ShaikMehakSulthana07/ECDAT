package com.ecdat.backend.scanner.container;

/**
 * Security limits for container image analysis.
 * Prevents resource exhaustion attacks and ensures safe static analysis.
 */
public class ContainerAnalysisLimits {

    /**
     * Maximum size for a container image archive in bytes.
     * 500 MB limit to prevent memory exhaustion.
     */
    public static final long MAX_IMAGE_ARCHIVE_SIZE = 500 * 1024 * 1024;

    /**
     * Maximum total uncompressed size for all layers combined.
     * 1 GB limit to prevent zip/tar bomb attacks.
     */
    public static final long MAX_TOTAL_LAYER_SIZE = 1024 * 1024 * 1024;

    /**
     * Maximum number of layers in a container image.
     * Prevents excessive layer count attacks.
     */
    public static final int MAX_LAYER_COUNT = 100;

    /**
     * Maximum number of entries per layer (tar entries).
     * Prevents tar bomb attacks within layers.
     */
    public static final int MAX_LAYER_ENTRIES = 10000;

    /**
     * Maximum size for a single file extracted from a layer.
     * 50 MB limit to prevent individual file exhaustion.
     */
    public static final long MAX_EXTRACTED_FILE_SIZE = 50 * 1024 * 1024;

    /**
     * Maximum total number of files extracted across all layers.
     * Prevents file count exhaustion.
     */
    public static final int MAX_TOTAL_EXTRACTED_FILES = 50000;

    /**
     * Maximum depth for nested archive extraction within layers.
     * Prevents recursive archive explosion.
     */
    public static final int MAX_NESTED_ARCHIVE_DEPTH = 2;

    /**
     * Maximum nested path depth for filesystem traversal.
     * Prevents deep path traversal attempts.
     */
    public static final int MAX_PATH_DEPTH = 20;

    /**
     * Maximum analysis time in milliseconds for container scanning.
     * Prevents indefinite analysis loops.
     */
    public static final long MAX_ANALYSIS_TIME_MS = 300000; // 5 minutes

    /**
     * Supported file extensions for artifact discovery.
     */
    public static final String[] SUPPORTED_EXTENSIONS = {
        ".jar", ".class", ".java", ".properties", ".yml", ".yaml", ".xml",
        ".conf", ".cfg", ".ini", ".crt", ".pem", ".der", ".jks", ".p12", ".pfx",
        ".pom"
    };

    /**
     * Supported archive extensions for container image upload.
     */
    public static final String[] SUPPORTED_ARCHIVE_EXTENSIONS = {
        ".tar", ".tar.gz", ".tgz"
    };

    private ContainerAnalysisLimits() {
        // Utility class - prevent instantiation
    }
}
