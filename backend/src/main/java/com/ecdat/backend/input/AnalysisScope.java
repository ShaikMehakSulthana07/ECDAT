package com.ecdat.backend.input;

import java.util.EnumSet;
import java.util.Set;

/**
 * Defines what cryptographic analysis dimensions are executed during a scan.
 * Clarifies the distinction between "Where are we scanning?" (ScanInputType)
 * and "What should we analyze?" (AnalysisScope).
 */
public enum AnalysisScope {

    /**
     * Parse Java AST for JCA/JCE/BouncyCastle cryptographic primitive invocations.
     */
    CRYPTO_APIS("Cryptographic APIs", "Extract Java AST primitives, algorithms, and key sizes"),

    /**
     * Scan build files (e.g., Maven pom.xml) for cryptographic libraries and dependencies.
     */
    DEPENDENCIES("Dependencies", "Resolve pom.xml declarations and cryptographic libraries"),

    /**
     * Discover and analyze X.509 certificates and public key properties.
     */
    CERTIFICATES("Certificates", "Inspect certificates, keystores, and public-key properties"),

    /**
     * Compute Mosca exposure conditions and quantum vulnerability metrics.
     */
    QUANTUM_RISK("Quantum Risk", "Calculate Mosca exposure conditions and Shor vulnerability"),

    /**
     * Recommend NIST FIPS 203/204/205 post-quantum replacement algorithms.
     */
    PQC_MIGRATION("PQC Migration", "Synthesize NIST post-quantum migration pathways"),

    /**
     * Generate CycloneDX 1.6 Cryptographic Bill of Materials document.
     */
    CBOM("CBOM", "Compile CycloneDX 1.6 Cryptographic Bill of Materials");

    private final String displayName;
    private final String description;

    AnalysisScope(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns all supported analysis scopes.
     */
    public static Set<AnalysisScope> all() {
        return EnumSet.allOf(AnalysisScope.class);
    }

    /**
     * Returns default analysis scopes for standard scans.
     */
    public static Set<AnalysisScope> defaultScopes() {
        return EnumSet.allOf(AnalysisScope.class);
    }
}
