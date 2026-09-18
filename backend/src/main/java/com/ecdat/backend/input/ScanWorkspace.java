package com.ecdat.backend.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Represents a normalized workspace directory structure for cryptographic discovery.
 * Implements AutoCloseable to ensure reliable temporary workspace resource cleanup.
 */
public class ScanWorkspace implements AutoCloseable {

    private final Path rootPath;
    private final boolean temporary;

    public ScanWorkspace(Path rootPath, boolean temporary) {
        if (rootPath == null) {
            throw new IllegalArgumentException("Root path cannot be null.");
        }
        this.rootPath = rootPath.normalize().toAbsolutePath();
        this.temporary = temporary;
    }

    /**
     * Creates a new temporary scan workspace on the filesystem.
     */
    public static ScanWorkspace createTemporary(String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(prefix != null ? prefix : "ecdat-scan-");
        return new ScanWorkspace(tempDir, true);
    }

    /**
     * Creates a scan workspace referencing an existing local directory.
     */
    public static ScanWorkspace forExistingDirectory(Path directory) {
        return new ScanWorkspace(directory, false);
    }

    /**
     * Returns the root directory of the workspace.
     */
    public Path getRootPath() {
        return rootPath;
    }

    /**
     * Returns the source directory. By default points to rootPath.
     */
    public Path getSourcePath() {
        Path sourceSub = rootPath.resolve("source");
        if (Files.exists(sourceSub) && Files.isDirectory(sourceSub)) {
            return sourceSub;
        }
        return rootPath;
    }

    /**
     * Returns the binaries directory.
     */
    public Path getBinariesPath() {
        return rootPath.resolve("binaries");
    }

    /**
     * Returns the configuration directory.
     */
    public Path getConfigPath() {
        return rootPath.resolve("config");
    }

    /**
     * Returns the certificates directory.
     */
    public Path getCertificatesPath() {
        return rootPath.resolve("certificates");
    }

    /**
     * Returns the metadata directory.
     */
    public Path getMetadataPath() {
        return rootPath.resolve("metadata");
    }

    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public void close() {
        if (temporary && Files.exists(rootPath)) {
            deleteDirectoryRecursively(rootPath);
        }
    }

    private void deleteDirectoryRecursively(Path path) {
        try {
            if (Files.exists(path)) {
                try (Stream<Path> stream = Files.walk(path)) {
                    stream.sorted(Comparator.reverseOrder())
                          .map(Path::toFile)
                          .forEach(File::delete);
                }
            }
        } catch (Exception ignored) {
            // Best effort cleanup for temporary files
        }
    }
}
