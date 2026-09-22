package com.ecdat.backend.input;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Processor for configuration file analysis inputs.
 * Validates configuration file formats, extracts files safely,
 * and produces a normalized ScanWorkspace for the existing analysis pipeline.
 */
@Component
public class ConfigurationInputProcessor implements AnalysisInputProcessor {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final String[] SUPPORTED_EXTENSIONS = {
        ".properties", ".yml", ".yaml", ".xml", ".conf", ".cfg", ".ini"
    };

    @Override
    public AnalysisInputType getInputType() {
        return AnalysisInputType.CONFIGURATION_FILE;
    }

    @Override
    public boolean supports(AnalysisInputType type) {
        return type == AnalysisInputType.CONFIGURATION_FILE;
    }

    @Override
    public void validate(AnalysisInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Analysis input must not be null.");
        }

        MultipartFile file = input.getConfigurationFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Configuration file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Configuration file name must not be null.");
        }

        String lowerName = originalFilename.toLowerCase();
        boolean isSupported = false;
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                isSupported = true;
                break;
            }
        }

        if (!isSupported) {
            throw new IllegalArgumentException(
                "Unsupported configuration file format. Supported formats: .properties, .yml, .yaml, .xml, .conf, .cfg, .ini"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                "Configuration file exceeds maximum allowed size of 10 MB."
            );
        }
    }

    @Override
    public ScanWorkspace process(AnalysisInput input) throws IOException {
        validate(input);
        MultipartFile file = input.getConfigurationFile();

        ScanWorkspace workspace = ScanWorkspace.createTemporary("ecdat-config-");
        try {
            Path targetPath = workspace.getRootPath().resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), targetPath);
            return workspace;
        } catch (Exception e) {
            workspace.close();
            if (e instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException("Failed to process configuration file: " + e.getMessage(), e);
        }
    }
}
