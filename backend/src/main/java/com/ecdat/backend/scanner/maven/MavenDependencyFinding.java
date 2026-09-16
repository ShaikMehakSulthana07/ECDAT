package com.ecdat.backend.scanner.maven;

import com.ecdat.backend.scanner.CryptoFinding;

/**
 * Represents a finding from Maven dependency analysis.
 * This extends the concept of crypto findings to include library-level dependencies.
 */
public class MavenDependencyFinding {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String scope;
    private final String sourceFile;
    private final boolean isCryptoRelated;
    private final String cryptoLibraryName;
    private final CryptoFinding.Confidence confidence;

    public MavenDependencyFinding(String groupId, String artifactId, String version, String scope,
                                 String sourceFile, boolean isCryptoRelated, String cryptoLibraryName,
                                 CryptoFinding.Confidence confidence) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        this.sourceFile = sourceFile;
        this.isCryptoRelated = isCryptoRelated;
        this.cryptoLibraryName = cryptoLibraryName;
        this.confidence = confidence;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getScope() {
        return scope;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public boolean isCryptoRelated() {
        return isCryptoRelated;
    }

    public String getCryptoLibraryName() {
        return cryptoLibraryName;
    }

    public CryptoFinding.Confidence getConfidence() {
        return confidence;
    }

    public String getLibraryDisplayName() {
        if (cryptoLibraryName != null) {
            return cryptoLibraryName + " " + version;
        }
        return groupId + ":" + artifactId + ":" + version;
    }
}
