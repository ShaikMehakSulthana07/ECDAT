package com.ecdat.backend.input;

import java.io.IOException;

/**
 * Strategy interface for converting a specific input source into a normalized ScanWorkspace.
 */
public interface InputAdapter {

    /**
     * Identifies the primary input type handled by this adapter.
     */
    ScanInputType getInputType();

    /**
     * Indicates whether this adapter can handle the given scan input type.
     */
    boolean supports(ScanInputType type);

    /**
     * Prepares and normalizes the target scan workspace from the given scan request.
     *
     * @param request the scan request containing input source details
     * @return a normalized ScanWorkspace ready for scanner pipeline execution
     * @throws IOException if workspace preparation or extraction fails
     * @throws SecurityException if security limits (Zip Slip, path traversal, bomb) are violated
     * @throws UnsupportedOperationException if this adapter represents an unimplemented input type
     */
    ScanWorkspace prepareWorkspace(ScanRequest request) throws IOException;
}
