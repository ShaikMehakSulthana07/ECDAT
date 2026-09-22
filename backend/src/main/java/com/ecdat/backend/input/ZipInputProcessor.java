package com.ecdat.backend.input;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Processor responsible for safely validating and extracting uploaded ZIP archives
 * into a normalized ScanWorkspace with Zip Slip and archive bomb protections.
 */
@Component
public class ZipInputProcessor implements AnalysisInputProcessor {

    public static final int MAX_ENTRIES = 10000;
    public static final long MAX_TOTAL_SIZE = 200 * 1024 * 1024; // 200 MB

    @Override
    public AnalysisInputType getInputType() {
        return AnalysisInputType.ZIP_ARCHIVE;
    }

    @Override
    public boolean supports(AnalysisInputType type) {
        return type == AnalysisInputType.ZIP_ARCHIVE;
    }

    @Override
    public void validate(AnalysisInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Analysis input must not be null.");
        }

        MultipartFile file = input.getArchiveFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded archive file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only .zip archive files are supported for upload.");
        }
    }

    @Override
    public ScanWorkspace process(AnalysisInput input) throws IOException {
        validate(input);
        MultipartFile file = input.getArchiveFile();

        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-zip-");
        try {
            extractZipSafely(file, workspace.getRootPath());
            return workspace;
        } catch (Exception e) {
            workspace.close();
            if (e instanceof SecurityException se) {
                throw se;
            }
            if (e instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException("Failed to extract uploaded archive: " + e.getMessage(), e);
        }
    }

    /**
     * Safely extracts a zip file with Zip Slip and zip bomb protections.
     */
    public void extractZipSafely(MultipartFile file, Path targetDir) throws IOException {
        int entryCount = 0;
        long totalBytes = 0;

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw new SecurityException("Archive exceeds maximum allowed entry count of " + MAX_ENTRIES);
                }

                Path entryDestination = targetDir.resolve(entry.getName()).normalize();
                if (!entryDestination.startsWith(targetDir.normalize())) {
                    throw new SecurityException("Zip Slip path traversal attempt detected in entry: " + entry.getName());
                }

                if (entry.isDirectory() || entry.getName().endsWith("/") || entry.getName().endsWith("\\")) {
                    if (!Files.exists(entryDestination)) {
                        Files.createDirectories(entryDestination);
                    }
                } else {
                    Path parent = entryDestination.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    if (!Files.isDirectory(entryDestination)) {
                        try (OutputStream os = Files.newOutputStream(entryDestination)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                totalBytes += len;
                                if (totalBytes > MAX_TOTAL_SIZE) {
                                    throw new SecurityException("Archive uncompressed size exceeds maximum allowed limit (200MB).");
                                }
                                os.write(buffer, 0, len);
                            }
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
