package com.ecdat.backend.scanner.binary;

/**
 * Security limits for binary analysis (JAR/CLASS files).
 * Prevents resource exhaustion attacks and archive bombs.
 */
public class BinaryAnalysisLimits {

    /**
     * Maximum size for a single binary file (JAR or CLASS) in bytes.
     * 100 MB limit to prevent memory exhaustion.
     */
    public static final long MAX_BINARY_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * Maximum number of entries in a JAR file.
     * Prevents zip bomb attacks with excessive entry counts.
     */
    public static final int MAX_JAR_ENTRIES = 10000;

    /**
     * Maximum number of class files to analyze in a single JAR.
     * Prevents analysis time exhaustion.
     */
    public static final int MAX_CLASSES_PER_JAR = 5000;

    /**
     * Maximum depth for nested JAR analysis.
     * Prevents recursive archive explosion.
     */
    public static final int MAX_NESTED_JAR_DEPTH = 2;

    /**
     * Maximum total uncompressed size for all files in a JAR.
     * 200 MB limit to prevent zip bomb attacks.
     */
    public static final long MAX_TOTAL_UNCOMPRESSED_SIZE = 200 * 1024 * 1024;

    /**
     * Maximum size for a single CLASS file in bytes.
     * 10 MB limit to prevent malformed bytecode attacks.
     */
    public static final long MAX_CLASS_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Maximum number of methods to analyze per class.
     * Prevents analysis time exhaustion on large classes.
     */
    public static final int MAX_METHODS_PER_CLASS = 1000;

    /**
     * Maximum bytecode instruction count per method.
     * Prevents analysis time exhaustion on large methods.
     */
    public static final int MAX_INSTRUCTIONS_PER_METHOD = 10000;

    private BinaryAnalysisLimits() {
        // Utility class - prevent instantiation
    }
}
