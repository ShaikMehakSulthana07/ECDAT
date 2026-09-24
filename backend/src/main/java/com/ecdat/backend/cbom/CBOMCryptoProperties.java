package com.ecdat.backend.cbom;

import com.ecdat.backend.inventory.LifecycleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CBOMCryptoProperties {
    private String algorithm;
    private String algorithmVariant;
    private String mode;
    private String padding;
    private String purpose;
    private Integer keySize;
    private String protocol;
    private String library;
    private String sourceFile;
    private Integer sourceLine;
    private String evidence;
    private String confidence;
    private String sourceType;
    private CBOMRiskInfo risk;
    private CBOMPQCInfo pqcRecommendation;

    // Phase 7 Enterprise Inventory Properties
    private String assetCategory;
    private String usageCategory;
    private String lifecycleStatus;
    private String businessCriticality;
    private String dataSensitivity;

    // Default constructor for Jackson deserialization
    public CBOMCryptoProperties() {
    }

    public CBOMCryptoProperties(String algorithm, String algorithmVariant, String purpose, Integer keySize,
                                String protocol, String library, String sourceFile, Integer sourceLine,
                                String evidence, String confidence, String sourceType,
                                CBOMRiskInfo risk, CBOMPQCInfo pqcRecommendation) {
        this.algorithm = algorithm;
        this.algorithmVariant = algorithmVariant;
        this.purpose = purpose;
        this.keySize = keySize;
        this.protocol = protocol;
        this.library = library;
        this.sourceFile = sourceFile;
        this.sourceLine = sourceLine;
        this.evidence = evidence;
        this.confidence = confidence;
        this.sourceType = sourceType;
        this.risk = risk;
        this.pqcRecommendation = pqcRecommendation;
    }

    public CBOMCryptoProperties(String algorithm, String algorithmVariant, String purpose, Integer keySize,
                                String protocol, String library, String sourceFile, Integer sourceLine,
                                String evidence, String confidence, String sourceType,
                                CBOMRiskInfo risk, CBOMPQCInfo pqcRecommendation,
                                String assetCategory, String usageCategory, String lifecycleStatus,
                                String businessCriticality, String dataSensitivity) {
        this(algorithm, algorithmVariant, null, null, purpose, keySize, protocol, library, sourceFile, sourceLine, evidence, confidence, sourceType, risk, pqcRecommendation, assetCategory, usageCategory, lifecycleStatus, businessCriticality, dataSensitivity);
    }

    public CBOMCryptoProperties(String algorithm, String algorithmVariant, String mode, String padding,
                                String purpose, Integer keySize,
                                String protocol, String library, String sourceFile, Integer sourceLine,
                                String evidence, String confidence, String sourceType,
                                CBOMRiskInfo risk, CBOMPQCInfo pqcRecommendation,
                                String assetCategory, String usageCategory, String lifecycleStatus,
                                String businessCriticality, String dataSensitivity) {
        this(algorithm, algorithmVariant, purpose, keySize, protocol, library, sourceFile, sourceLine, evidence, confidence, sourceType, risk, pqcRecommendation);
        this.mode = mode;
        this.padding = padding;
        this.assetCategory = assetCategory;
        this.usageCategory = usageCategory;
        this.lifecycleStatus = lifecycleStatus;
        this.businessCriticality = businessCriticality;
        this.dataSensitivity = dataSensitivity;
    }

    public String getAlgorithm() { return algorithm; }
    public String getAlgorithmVariant() { return algorithmVariant; }
    public String getMode() { return mode; }
    public String getPadding() { return padding; }
    public String getPurpose() { return purpose; }
    public Integer getKeySize() { return keySize; }
    public String getProtocol() { return protocol; }
    public String getLibrary() { return library; }
    public String getSourceFile() { return sourceFile; }
    public Integer getSourceLine() { return sourceLine; }
    public String getEvidence() { return evidence; }
    public String getConfidence() { return confidence; }
    public String getSourceType() { return sourceType; }
    public CBOMRiskInfo getRisk() { return risk; }
    public CBOMPQCInfo getPqcRecommendation() { return pqcRecommendation; }

    public String getAssetCategory() { return assetCategory; }
    public String getUsageCategory() { return usageCategory; }
    public String getLifecycleStatus() { return lifecycleStatus; }
    public String getBusinessCriticality() { return businessCriticality; }
    public String getDataSensitivity() { return dataSensitivity; }

    // Setters for Jackson deserialization
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public void setAlgorithmVariant(String algorithmVariant) { this.algorithmVariant = algorithmVariant; }
    public void setMode(String mode) { this.mode = mode; }
    public void setPadding(String padding) { this.padding = padding; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public void setLibrary(String library) { this.library = library; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
    public void setSourceLine(Integer sourceLine) { this.sourceLine = sourceLine; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public void setRisk(CBOMRiskInfo risk) { this.risk = risk; }
    public void setPqcRecommendation(CBOMPQCInfo pqcRecommendation) { this.pqcRecommendation = pqcRecommendation; }

    public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }
    public void setUsageCategory(String usageCategory) { this.usageCategory = usageCategory; }
    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) { this.lifecycleStatus = lifecycleStatus != null ? lifecycleStatus.name() : null; }
    public void setLifecycleStatus(String lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }
    public void setBusinessCriticality(String businessCriticality) { this.businessCriticality = businessCriticality; }
    public void setDataSensitivity(String dataSensitivity) { this.dataSensitivity = dataSensitivity; }
}
