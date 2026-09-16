package com.ecdat.backend.scanner.maven;

/**
 * Represents a Maven dependency extracted from pom.xml.
 */
public class MavenDependency {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String scope;
    private final String sourceFile;

    public MavenDependency(String groupId, String artifactId, String version, String scope, String sourceFile) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        this.sourceFile = sourceFile;
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

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
