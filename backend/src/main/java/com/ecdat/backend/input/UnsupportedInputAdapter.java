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
                "Git repository scanning is not yet available in Phase 4. Planned for Phase 5 (Repository URL Scanner)."
        );
    }

    public static UnsupportedInputAdapter forFiles() {
        return new UnsupportedInputAdapter(
                ScanInputType.FILES,
                "Direct file/artifact scanning is not yet available in Phase 4. Planned for Phase 6 (Configuration & Loose Artifacts)."
        );
    }

    public static UnsupportedInputAdapter forJar() {
        return new UnsupportedInputAdapter(
                ScanInputType.JAR,
                "JAR bytecode scanning is not yet available in Phase 4. Planned for Phase 7 (Bytecode & Binary Scanner)."
        );
    }

    public static UnsupportedInputAdapter forClass() {
        return new UnsupportedInputAdapter(
                ScanInputType.CLASS,
                "Java Class bytecode scanning is not yet available in Phase 4. Planned for Phase 7 (Bytecode & Binary Scanner)."
        );
    }

    public static UnsupportedInputAdapter forConfiguration() {
        return new UnsupportedInputAdapter(
                ScanInputType.CONFIGURATION,
                "Configuration file scanning is not yet available in Phase 4. Planned for Phase 6."
        );
    }

    public static UnsupportedInputAdapter forContainer() {
        return new UnsupportedInputAdapter(
                ScanInputType.CONTAINER_IMAGE,
                "Container image scanning is not yet available in Phase 4. Planned for Phase 8 (Container Image Scanner)."
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
        throw new UnsupportedOperationException(reason);
    }
}
