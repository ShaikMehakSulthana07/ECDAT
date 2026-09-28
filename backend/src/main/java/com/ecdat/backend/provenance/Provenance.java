package com.ecdat.backend.provenance;

/**
 * Provenance indicates the source and reliability of information about cryptographic artifacts.
 * This helps distinguish between facts directly observed from scanned artifacts and information
 * that is inferred, supplied by users, or assumed by default.
 */
public enum Provenance {
    /**
     * Directly observed from scanned artifacts (source code, binaries, configurations, certificates).
     * Highest confidence level.
     */
    OBSERVED,

    /**
     * Inferred by the analysis engine based on context, patterns, or heuristics.
     * Medium confidence level.
     */
    INFERRED,

    /**
     * Derived from dependency metadata (Maven POM, package.json, etc.).
     * Low to medium confidence - presence does not guarantee usage.
     */
    DEPENDENCY_METADATA,

    /**
     * Explicitly provided by the user (configuration, manual input, API parameters).
     * Confidence depends on user trust level.
     */
    USER_PROVIDED,

    /**
     * Default assumption when no specific information is available.
     * Lowest confidence level - should be treated as a placeholder.
     */
    DEFAULT_ASSUMPTION,

    /**
     * Provenance cannot be determined.
     */
    UNKNOWN
}
