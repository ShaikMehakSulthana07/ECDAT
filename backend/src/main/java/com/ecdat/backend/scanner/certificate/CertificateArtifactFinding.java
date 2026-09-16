package com.ecdat.backend.scanner.certificate;

import com.ecdat.backend.scanner.CryptoFinding;

/**
 * Represents a finding from certificate/key artifact analysis.
 * Contains metadata about discovered certificates and keys without exposing private key material.
 */
public class CertificateArtifactFinding {
    private final String fileName;
    private final String sourceFile;
    private final String fileType;
    private final String certificateType;
    private final String publicKeyAlgorithm;
    private final Integer keySize;
    private final String signatureAlgorithm;
    private final String validityDates;
    private final CryptoFinding.Confidence confidence;

    public CertificateArtifactFinding(String fileName, String sourceFile, String fileType,
                                    String certificateType, String publicKeyAlgorithm,
                                    Integer keySize, String signatureAlgorithm, String validityDates,
                                    CryptoFinding.Confidence confidence) {
        this.fileName = fileName;
        this.sourceFile = sourceFile;
        this.fileType = fileType;
        this.certificateType = certificateType;
        this.publicKeyAlgorithm = publicKeyAlgorithm;
        this.keySize = keySize;
        this.signatureAlgorithm = signatureAlgorithm;
        this.validityDates = validityDates;
        this.confidence = confidence;
    }

    public String getFileName() {
        return fileName;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getFileType() {
        return fileType;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public String getPublicKeyAlgorithm() {
        return publicKeyAlgorithm;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public String getValidityDates() {
        return validityDates;
    }

    public CryptoFinding.Confidence getConfidence() {
        return confidence;
    }

    public String getDisplayName() {
        StringBuilder name = new StringBuilder(fileName);
        if (publicKeyAlgorithm != null) {
            name.append(" (").append(publicKeyAlgorithm);
            if (keySize != null) {
                name.append("-").append(keySize);
            }
            name.append(")");
        }
        return name.toString();
    }
}
