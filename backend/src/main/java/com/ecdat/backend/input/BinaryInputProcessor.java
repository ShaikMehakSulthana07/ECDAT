package com.ecdat.backend.input;

import com.ecdat.backend.scanner.binary.BinaryAnalysisLimits;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Processor for binary file analysis inputs (JAR and CLASS files).
 * Validates file types, applies security limits, and produces a normalized ScanWorkspace.
 */
@Component
public class BinaryInputProcessor implements AnalysisInputProcessor {

    @Override
    public AnalysisInputType getInputType() {
        return AnalysisInputType.BINARY_FILE;
    }

    @Override
    public boolean supports(AnalysisInputType type) {
        return type == AnalysisInputType.BINARY_FILE;
    }

    @Override
    public void validate(AnalysisInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Analysis input must not be null.");
        }

        MultipartFile file = input.getBinaryFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded binary file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File name is missing.");
        }

        String lowerName = originalFilename.toLowerCase();
        if (!lowerName.endsWith(".jar") && !lowerName.endsWith(".class")) {
            throw new IllegalArgumentException("Only .jar and .class files are supported for binary analysis.");
        }

        // Check file size
        long fileSize = file.getSize();
        if (fileSize > BinaryAnalysisLimits.MAX_BINARY_FILE_SIZE) {
            throw new SecurityException("Binary file exceeds maximum size limit of " + 
                (BinaryAnalysisLimits.MAX_BINARY_FILE_SIZE / (1024 * 1024)) + " MB.");
        }
    }

    @Override
    public ScanWorkspace process(AnalysisInput input) throws IOException {
        validate(input);
        MultipartFile file = input.getBinaryFile();

        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-binary-");
        try {
            Path targetPath = workspace.getRootPath().resolve(file.getOriginalFilename());
            copyBinaryFileSafely(file, targetPath);
            return workspace;
        } catch (Exception e) {
            workspace.close();
            if (e instanceof SecurityException se) {
                throw se;
            }
            if (e instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException("Failed to process binary file: " + e.getMessage(), e);
        }
    }

    /**
     * Safely copies the uploaded binary file to the workspace.
     */
    private void copyBinaryFileSafely(MultipartFile file, Path targetPath) throws IOException {
        // Ensure parent directory exists
        Path parent = targetPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        // Copy the file
        try (OutputStream os = Files.newOutputStream(targetPath)) {
            byte[] buffer = new byte[8192];
            int len;
            try (var is = file.getInputStream()) {
                while ((len = is.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                }
            }
        }
    }
}
