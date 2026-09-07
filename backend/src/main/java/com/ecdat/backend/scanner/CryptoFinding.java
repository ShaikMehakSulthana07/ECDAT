package com.ecdat.backend.scanner;

public class CryptoFinding {
    public enum Purpose {
        ENCRYPTION, DECRYPTION, KEY_GENERATION, KEY_AGREEMENT, DIGITAL_SIGNATURE, HASHING, PROTOCOL, UNKNOWN
    }

    public enum Confidence {
        HIGH, MEDIUM, LOW
    }

    private String algorithm;
    private String variant;
    private Purpose purpose;
    private Integer keySize;
    private String file;
    private int line;
    private String evidence;
    private Confidence confidence;
    private String sourceType = "JAVA_AST";

    // Getters and Setters
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public Purpose getPurpose() { return purpose; }
    public void setPurpose(Purpose purpose) { this.purpose = purpose; }
    public Integer getKeySize() { return keySize; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }
    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }
    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public Confidence getConfidence() { return confidence; }
    public void setConfidence(Confidence confidence) { this.confidence = confidence; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
}