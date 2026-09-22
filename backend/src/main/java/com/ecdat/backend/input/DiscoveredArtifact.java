package com.ecdat.backend.input;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a normalized discovered artifact extracted from an analysis input.
 */
public class DiscoveredArtifact {

    private final ArtifactType artifactType;
    private final String name;
    private final String relativePath;
    private final Path path;
    private final long size;
    private final String mediaType;
    private final String origin;
    private final Map<String, String> metadata;

    public DiscoveredArtifact(ArtifactType artifactType,
                              String name,
                              String relativePath,
                              Path path,
                              long size,
                              String mediaType,
                              String origin,
                              Map<String, String> metadata) {
        this.artifactType = artifactType != null ? artifactType : ArtifactType.OTHER;
        this.name = name != null ? name : (path != null ? path.getFileName().toString() : "unknown");
        this.relativePath = relativePath != null ? relativePath : this.name;
        this.path = path;
        this.size = size;
        this.mediaType = mediaType != null ? mediaType : "application/octet-stream";
        this.origin = origin != null ? origin : "UNKNOWN";
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public static DiscoveredArtifact of(ArtifactType artifactType, Path path, Path rootPath, String origin) {
        String name = path.getFileName().toString();
        String relativePath = rootPath != null ? rootPath.relativize(path).toString().replace("\\", "/") : name;
        long size = 0;
        try {
            size = java.nio.file.Files.size(path);
        } catch (Exception ignored) {
        }
        return new DiscoveredArtifact(artifactType, name, relativePath, path, size, null, origin, null);
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public String getName() {
        return name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public Path getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getOrigin() {
        return origin;
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String toString() {
        return "DiscoveredArtifact{" +
                "artifactType=" + artifactType +
                ", name='" + name + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", size=" + size +
                '}';
    }
}
