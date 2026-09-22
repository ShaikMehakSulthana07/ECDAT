package com.ecdat.backend.input;

import java.io.IOException;

/**
 * Adapter representing planned/unsupported input types.
 * Explicitly rejects scan attempts with clear phase roadmap information.
 */
public class UnsupportedInputAdapter implements InputAdapter {

    private final ScanInputType inputType;
    private final String reason;

    public UnsupportedInputAdapter(ScanInputType inputType, String reason) {
        this.inputType = inputType;
        this.reason = reason;
    }

    public static UnsupportedInputAdapter forGitRepository() {
        return new UnsupportedInputAdapter(
                ScanInputType.GIT_REPOSITORY,
                "Repository scanning is not yet available. Planned for Phase 5 (Repository URL Scanner)."
        );
    }

    public static UnsupportedInputAdapter forFiles() {
        return new UnsupportedInputAdapter(
                ScanInputType.FILES,
                "File and artifact scanning is not yet available. Planned for Phase 6 (Configuration & Loose Artifacts)."
        );
    }

    public static UnsupportedInputAdapter forJar() {
        return new UnsupportedInputAdapter(
                ScanInputType.JAR,
                "JAR scanning is not yet available. Planned for Phase 7 (Bytecode & Binary Scanner)."
        );
    }

    public static UnsupportedInputAdapter forClass() {
        return new UnsupportedInputAdapter(
                ScanInputType.CLASS,
                "Java class scanning is not yet available. Planned for Phase 7 (Bytecode & Binary Scanner)."
        );
    }

    public static UnsupportedInputAdapter forConfiguration() {
        return new UnsupportedInputAdapter(
                ScanInputType.CONFIGURATION,
                "Configuration scanning is not yet available. Planned for Phase 6."
        );
    }

    public static UnsupportedInputAdapter forContainer() {
        return new UnsupportedInputAdapter(
                ScanInputType.CONTAINER_IMAGE,
                "Container image scanning is not yet available. Planned for Phase 8 (Container Image Scanner)."
        );
    }

    @Override
    public ScanInputType getInputType() {
        return inputType;
    }

    @Override
    public boolean supports(ScanInputType type) {
        return type == inputType;
    }

    @Override
    public ScanWorkspace prepareWorkspace(ScanRequest request) throws IOException {
        throw new UnsupportedInputException(inputType, reason);
    }
}
