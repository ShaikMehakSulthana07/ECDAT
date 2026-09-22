package com.ecdat.backend.input;

import java.io.IOException;

/**
 * Input processor for roadmap input types that are not yet implemented.
 * Explicitly rejects scan attempts with clear phase guidance without fake findings.
 */
public class UnsupportedInputProcessor implements AnalysisInputProcessor {

    private final AnalysisInputType inputType;
    private final String reason;

    public UnsupportedInputProcessor(AnalysisInputType inputType, String reason) {
        this.inputType = inputType;
        this.reason = reason;
    }

    public static UnsupportedInputProcessor forRepository() {
        return new UnsupportedInputProcessor(
                AnalysisInputType.REPOSITORY_URL,
                "Repository scanning is not yet available. Planned for Phase 5 (Repository URL Scanner)."
        );
    }

    public static UnsupportedInputProcessor forConfiguration() {
        return new UnsupportedInputProcessor(
                AnalysisInputType.CONFIGURATION_FILE,
                "Configuration file scanning is not yet available. Planned for Phase 6 (Configuration Scanner)."
        );
    }

    // Note: forBinary() and forContainer() were removed as these types are now supported
    // in Phase 7 and Phase 8 respectively.

    @Override
    public AnalysisInputType getInputType() {
        return inputType;
    }

    @Override
    public boolean supports(AnalysisInputType type) {
        return type == this.inputType;
    }

    @Override
    public void validate(AnalysisInput input) {
        throw new UnsupportedInputException(inputType, reason);
    }

    @Override
    public ScanWorkspace process(AnalysisInput input) throws IOException {
        throw new UnsupportedInputException(inputType, reason);
    }
}
