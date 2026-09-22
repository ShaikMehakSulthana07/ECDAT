package com.ecdat.backend.scanner.configuration;

import com.ecdat.backend.scanner.CryptoFinding;

/**
 * Represents a finding from configuration file analysis.
 * Contains metadata about discovered cryptographic configuration without exposing secrets.
 */
public class ConfigurationFinding {
    private final String findingType;
    private final String detectedValue;
    private final String fileName;
    private final int lineNumber;
    private final String evidence;
    private final CryptoFinding.Confidence confidence;
    private final String format;

    public ConfigurationFinding(String findingType, String detectedValue, String fileName,
                               int lineNumber, String evidence, CryptoFinding.Confidence confidence,
                               String format) {
        this.findingType = findingType;
        this.detectedValue = detectedValue;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.evidence = evidence;
        this.confidence = confidence;
        this.format = format;
    }

    public String getFindingType() {
        return findingType;
    }

    public String getDetectedValue() {
        return detectedValue;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getEvidence() {
        return evidence;
    }

    public CryptoFinding.Confidence getConfidence() {
        return confidence;
    }

    public String getFormat() {
        return format;
    }

    public String getDisplayName() {
        return findingType + ": " + detectedValue + " (" + fileName + ")";
    }
}
