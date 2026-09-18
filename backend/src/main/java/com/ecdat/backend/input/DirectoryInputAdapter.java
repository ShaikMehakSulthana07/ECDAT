package com.ecdat.backend.input;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Adapter responsible for normalizing local filesystem directory inputs into a ScanWorkspace.
 */
@Component
public class DirectoryInputAdapter implements InputAdapter {

    @Override
    public ScanInputType getInputType() {
        return ScanInputType.DIRECTORY;
    }

    @Override
    public boolean supports(ScanInputType type) {
        return type == ScanInputType.DIRECTORY;
    }

    @Override
    public ScanWorkspace prepareWorkspace(ScanRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Scan request must not be null.");
        }

        String dirPath = request.getDirectoryPath();
        if (dirPath == null || dirPath.trim().isEmpty()) {
            dirPath = request.getSourceIdentifier();
        }

        if (dirPath == null || dirPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Source path must not be empty or blank.");
        }

        Path path = Paths.get(dirPath.trim()).normalize().toAbsolutePath();

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Source path does not exist: " + dirPath);
        }

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Source path is not a directory: " + dirPath);
        }

        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("Source directory is not readable: " + dirPath);
        }

        return ScanWorkspace.forExistingDirectory(path);
    }
}
