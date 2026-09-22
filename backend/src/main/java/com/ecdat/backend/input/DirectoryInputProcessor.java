package com.ecdat.backend.input;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Processor responsible for preparing a ScanWorkspace from a local filesystem directory.
 */
@Component
public class DirectoryInputProcessor implements AnalysisInputProcessor {

    @Override
    public AnalysisInputType getInputType() {
        return AnalysisInputType.DIRECTORY;
    }

    @Override
    public boolean supports(AnalysisInputType type) {
        return type == AnalysisInputType.DIRECTORY;
    }

    @Override
    public void validate(AnalysisInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Analysis input must not be null.");
        }

        String dirPath = input.getDirectoryPath();
        if (dirPath == null || dirPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory path must not be null or empty.");
        }

        Path path = Paths.get(dirPath).normalize();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Specified directory path does not exist: " + dirPath);
        }

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Specified path is not a directory: " + dirPath);
        }
    }

    @Override
    public ScanWorkspace process(AnalysisInput input) throws IOException {
        validate(input);
        Path path = Paths.get(input.getDirectoryPath()).normalize().toAbsolutePath();
        return ScanWorkspace.forExistingDirectory(path);
    }
}
