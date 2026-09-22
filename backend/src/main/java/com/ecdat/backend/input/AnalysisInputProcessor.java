package com.ecdat.backend.input;

import java.io.IOException;

/**
 * Strategy interface for validating, extracting, and normalizing an analysis input
 * into a structured ScanWorkspace.
 */
public interface AnalysisInputProcessor {

    /**
     * Identifies the primary input type handled by this processor.
     */
    AnalysisInputType getInputType();

    /**
     * Indicates whether this processor supports the given input type.
     */
    boolean supports(AnalysisInputType type);

    /**
     * Validates input parameters and security constraints prior to processing.
     *
     * @param input the incoming analysis input request
     * @throws IllegalArgumentException if required fields or formats are invalid
     * @throws SecurityException if security policy is violated
     * @throws UnsupportedInputException if the input type is not implemented
     */
    void validate(AnalysisInput input);

    /**
     * Prepares and normalizes a scan workspace for the given input.
     *
     * @param input the validated analysis input
     * @return a normalized ScanWorkspace containing extracted/accessible files
     * @throws IOException if workspace preparation or extraction fails
     * @throws UnsupportedInputException if input represents an unsupported roadmap type
     */
    ScanWorkspace process(AnalysisInput input) throws IOException;
}
