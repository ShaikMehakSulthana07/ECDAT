package com.ecdat.backend.inventory;

import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.scanner.CryptoFinding;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CryptoAsset {
    private String assetId;
    private String algorithm;
    private String variant;
    private String mode;
    private String padding;
    private CryptoFinding.Purpose purpose;
    private Integer keySize;
    private String protocol;
    private String library;
    private String sourceFile;
    private int sourceLine;
    private String evidence;
    private CryptoFinding.Confidence confidence;
    private String sourceType;
    private AssetCategory assetCategory;
    private CryptoUsageCategory usageCategory;
    private LifecycleStatus lifecycleStatus;
    private BusinessCriticality businessCriticality;
    private DataSensitivity dataSensitivity;
    private RiskAssessment riskAssessment;
    private PQCRecommendation pqcRecommendation;
    private CryptoFinding originalFinding;

    public CryptoAsset() {
        this.assetId = UUID.randomUUID().toString();
        this.businessCriticality = BusinessCriticality.UNKNOWN;
        this.dataSensitivity = DataSensitivity.UNKNOWN;
    }

    public CryptoAsset(String algorithm, String variant, CryptoFinding.Purpose purpose,
                       Integer keySize, String protocol, String library, String sourceFile,
                       int sourceLine, String evidence, CryptoFinding.Confidence confidence,
                       String sourceType, AssetCategory assetCategory, CryptoUsageCategory usageCategory,
                       LifecycleStatus lifecycleStatus, BusinessCriticality businessCriticality,
                       DataSensitivity dataSensitivity, RiskAssessment riskAssessment,
                       PQCRecommendation pqcRecommendation, CryptoFinding originalFinding) {
        this.assetId = UUID.randomUUID().toString();
        this.algorithm = algorithm;
        this.variant = variant;
        this.purpose = purpose;
        this.keySize = keySize;
        this.protocol = protocol;
        this.library = library;
        this.sourceFile = sourceFile;
        this.sourceLine = sourceLine;
        this.evidence = evidence;
        this.confidence = confidence;
        this.sourceType = sourceType;
        this.assetCategory = assetCategory != null ? assetCategory : AssetCategory.UNKNOWN;
        this.usageCategory = usageCategory != null ? usageCategory : CryptoUsageCategory.UNKNOWN;
        this.lifecycleStatus = lifecycleStatus != null ? lifecycleStatus : LifecycleStatus.UNKNOWN;
        this.businessCriticality = businessCriticality != null ? businessCriticality : BusinessCriticality.UNKNOWN;
        this.dataSensitivity = dataSensitivity != null ? dataSensitivity : DataSensitivity.UNKNOWN;
        this.riskAssessment = riskAssessment;
        this.pqcRecommendation = pqcRecommendation;
        this.originalFinding = originalFinding;
    }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getPadding() { return padding; }
    public void setPadding(String padding) { this.padding = padding; }

    public CryptoFinding.Purpose getPurpose() { return purpose; }
    public void setPurpose(CryptoFinding.Purpose purpose) { this.purpose = purpose; }

    public Integer getKeySize() { return keySize; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getLibrary() { return library; }
    public void setLibrary(String library) { this.library = library; }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }

    public int getSourceLine() { return sourceLine; }
    public void setSourceLine(int sourceLine) { this.sourceLine = sourceLine; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public CryptoFinding.Confidence getConfidence() { return confidence; }
    public void setConfidence(CryptoFinding.Confidence confidence) { this.confidence = confidence; }

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

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }

    public PQCRecommendation getPqcRecommendation() { return pqcRecommendation; }
    public void setPqcRecommendation(PQCRecommendation pqcRecommendation) { this.pqcRecommendation = pqcRecommendation; }

    public CryptoFinding getOriginalFinding() { return originalFinding; }
    public void setOriginalFinding(CryptoFinding originalFinding) { this.originalFinding = originalFinding; }
}
