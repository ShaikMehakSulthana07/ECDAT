package com.ecdat.backend.pqc;

/**
 * Migration strategy for post-quantum cryptography transition.
 * Defines how an organization should approach migrating from classical to post-quantum algorithms.
 */
public enum MigrationStrategy {
    /**
     * Direct migration to PQC algorithms without hybrid approach.
     * Suitable when compatibility requirements are minimal or can be managed.
     */
    DIRECT_PQC,
    
    /**
     * Hybrid approach using both classical and PQC algorithms during transition.
     * Provides backward compatibility while maintaining security.
     */
    HYBRID,
    
    /**
     * Requires further analysis before determining migration strategy.
     * Used when the use case, constraints, or requirements are not fully understood.
     */
    NEEDS_ANALYSIS,
    
    /**
     * No migration action required at this time.
     * Used for quantum-resistant algorithms or non-critical applications.
     */
    NO_ACTION,
    
    /**
     * Migration strategy cannot be determined.
     */
    UNKNOWN
}
