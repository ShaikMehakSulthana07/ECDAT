package com.ecdat.backend.input;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Processor for individual source files (specifically Java source files).
 * Prepares a normalized workspace for direct AST analysis without requiring archive packaging.
 */
@Component
public class SourceFileInputProcessor implements AnalysisInputProcessor {

    public static final long MAX_SOURCE_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Override
    public AnalysisInputType getInputType() {
        return AnalysisInputType.SOURCE_FILE;
    }

    @Override
    public boolean supports(AnalysisInputType type) {
        return type == AnalysisInputType.SOURCE_FILE;
    }

    @Override
    public void validate(AnalysisInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Analysis input must not be null.");
        }

        MultipartFile file = input.getSourceFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Source file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".java")) {
            throw new IllegalArgumentException("Only Java source files (.java) are currently supported for direct source analysis.");
        }

        if (file.getSize() > MAX_SOURCE_FILE_SIZE) {
            throw new SecurityException("Source file exceeds maximum allowed size of 10MB.");
        }
    }

    @Override
    public ScanWorkspace process(AnalysisInput input) throws IOException {
        validate(input);
        MultipartFile file = input.getSourceFile();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "Source.java";
        }

        // Sanitize filename to prevent directory traversal
        Path sanitizedName = Path.of(originalFilename).getFileName();
        if (sanitizedName == null) {
            sanitizedName = Path.of("Source.java");
        }

        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-source-");
        try {
            Path targetFile = workspace.getRootPath().resolve(sanitizedName);
            try (InputStream is = file.getInputStream();
                 OutputStream os = Files.newOutputStream(targetFile)) {
                is.transferTo(os);
            }
            return workspace;
        } catch (Exception e) {
            workspace.close();
            if (e instanceof SecurityException se) {
                throw se;
            }
            if (e instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException("Failed to prepare source file workspace: " + e.getMessage(), e);
        }
    }
}
