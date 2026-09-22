package com.ecdat.backend.scanner;

import com.ecdat.backend.inventory.AssetCategory;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.CryptoUsageCategory;
import com.ecdat.backend.inventory.DataSensitivity;
import com.ecdat.backend.inventory.LifecycleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
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

    // Phase 7 Enterprise Classification Fields
    private AssetCategory assetCategory;
    private CryptoUsageCategory usageCategory;
    private LifecycleStatus lifecycleStatus;
    private BusinessCriticality businessCriticality = BusinessCriticality.UNKNOWN;
    private DataSensitivity dataSensitivity = DataSensitivity.UNKNOWN;
    private String protocol;
    private String library;

    // Container-specific provenance fields (Phase 8)
    private String containerImage;
    private String layerId;
    private String artifactName;

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

    public AssetCategory getAssetCategory() { return assetCategory; }
    public void setAssetCategory(AssetCategory assetCategory) { this.assetCategory = assetCategory; }
    public CryptoUsageCategory getUsageCategory() { return usageCategory; }
    public void setUsageCategory(CryptoUsageCategory usageCategory) { this.usageCategory = usageCategory; }
    public LifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }
    public BusinessCriticality getBusinessCriticality() { return businessCriticality; }
    public void setBusinessCriticality(BusinessCriticality businessCriticality) { this.businessCriticality = businessCriticality; }
    public DataSensitivity getDataSensitivity() { return dataSensitivity; }
    public void setDataSensitivity(DataSensitivity dataSensitivity) { this.dataSensitivity = dataSensitivity; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getLibrary() { return library; }
    public void setLibrary(String library) { this.library = library; }

    // Container provenance getters/setters
    public String getContainerImage() { return containerImage; }
    public void setContainerImage(String containerImage) { this.containerImage = containerImage; }
    public String getLayerId() { return layerId; }
    public void setLayerId(String layerId) { this.layerId = layerId; }
    public String getArtifactName() { return artifactName; }
    public void setArtifactName(String artifactName) { this.artifactName = artifactName; }
}