package com.ecdat.backend.dto;

/**
 * Source attribution for analysis context values.
 * Distinguishes between user-provided values, organizational defaults,
 * system defaults, and unknown/unavailable values.
 */
public enum ValueSource {
    /**
     * Value was explicitly provided by the user in the analysis request.
     * This is the most reliable source.
     */
    USER_PROVIDED,
    
    /**
     * Value comes from organization-level configuration or policy.
     * These are defaults set by the organization for all analyses.
     */
    ORGANIZATION_DEFAULT,
    
    /**
     * Value is a system default for demo/illustration purposes.
     * These are NOT observed from the scanned system and should be clearly marked.
     */
    SYSTEM_DEFAULT,
    
    /**
     * Value is unknown or unavailable.
     * The system cannot determine this value from the input or configuration.
     */
    UNKNOWN
}
