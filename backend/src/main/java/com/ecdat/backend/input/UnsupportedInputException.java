package com.ecdat.backend.input;

/**
 * Thrown when a scan is requested for an input type that is planned but not yet implemented.
 * Must never be converted into a successful or empty AnalysisResponse.
 */
public class UnsupportedInputException extends UnsupportedOperationException {

    private final String inputTypeName;

    public UnsupportedInputException(AnalysisInputType inputType, String message) {
        super(message);
        this.inputTypeName = inputType != null ? inputType.name() : "UNKNOWN";
    }

    public UnsupportedInputException(ScanInputType inputType, String message) {
        super(message);
        this.inputTypeName = inputType != null ? inputType.name() : "UNKNOWN";
    }

    public UnsupportedInputException(String inputTypeName, String message) {
        super(message);
        this.inputTypeName = inputTypeName;
    }

    public String getInputTypeName() {
        return inputTypeName;
    }

    public ScanInputType getInputType() {
        try {
            return ScanInputType.valueOf(inputTypeName);
        } catch (Exception e) {
            return null;
        }
    }

    public AnalysisInputType getAnalysisInputType() {
        try {
            return AnalysisInputType.valueOf(inputTypeName);
        } catch (Exception e) {
            return null;
        }
    }
}
