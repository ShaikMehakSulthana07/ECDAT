package com.ecdat.backend.risk.quantum;

/**
 * Migration urgency level based on quantum risk assessment.
 * Indicates how urgently a cryptographic asset needs to be migrated to post-quantum cryptography.
 */
public enum MigrationUrgency {
    /**
     * Immediate migration required - asset is at critical risk
     */
    CRITICAL,
    
    /**
     * High priority migration - asset has significant quantum vulnerability
     */
    HIGH,
    
    /**
     * Medium priority migration - asset should be planned for migration
     */
    MEDIUM,
    
    /**
     * Low priority migration - asset has some quantum concerns but not urgent
     */
    LOW,
    
    /**
     * No migration required - asset is quantum-resistant or not applicable
     */
    NONE,
    
    /**
     * Urgency cannot be determined - insufficient information
     */
    UNKNOWN
}
