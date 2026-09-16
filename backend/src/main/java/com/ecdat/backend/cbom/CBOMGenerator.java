package com.ecdat.backend.cbom;

import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.scanner.CryptoFinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CBOMGenerator {
    private final ObjectMapper objectMapper;

    public CBOMGenerator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public CBOMDocument generate(List<RiskAssessment> riskAssessments, List<PQCRecommendation> pqcRecommendations) {
        CBOMDocument document = new CBOMDocument();

        for (RiskAssessment assessment : riskAssessments) {
            CryptoFinding finding = assessment.getOriginalFinding();
            PQCRecommendation recommendation = findRecommendation(finding, pqcRecommendations);

            CBOMComponent component = createComponent(finding, assessment, recommendation);
            document.addComponent(component);
        }

        return document;
    }

    private PQCRecommendation findRecommendation(CryptoFinding finding, List<PQCRecommendation> recommendations) {
        for (PQCRecommendation recommendation : recommendations) {
            if (recommendation.getCurrentAlgorithm().equals(finding.getAlgorithm()) &&
                recommendation.getCurrentPurpose() == finding.getPurpose()) {
                return recommendation;
            }
        }
        return null;
    }

    private CBOMComponent createComponent(CryptoFinding finding, RiskAssessment assessment, PQCRecommendation recommendation) {
        String componentName = generateComponentName(finding);
        String description = generateDescription(finding);

        CBOMRiskInfo riskInfo = createRiskInfo(assessment);
        CBOMPQCInfo pqcInfo = recommendation != null ? createPQCInfo(recommendation) : null;

        String assetCategoryStr = finding.getAssetCategory() != null ? finding.getAssetCategory().name() : null;
        String usageCategoryStr = finding.getUsageCategory() != null ? finding.getUsageCategory().name() : null;
        String lifecycleStatusStr = finding.getLifecycleStatus() != null ? finding.getLifecycleStatus().name() : null;
        String businessCriticalityStr = finding.getBusinessCriticality() != null && finding.getBusinessCriticality() != com.ecdat.backend.inventory.BusinessCriticality.UNKNOWN
            ? finding.getBusinessCriticality().name()
            : (assessment != null && assessment.getQuantumRiskResult() != null && assessment.getQuantumRiskResult().getBusinessCriticality() != null
                ? assessment.getQuantumRiskResult().getBusinessCriticality().name()
                : null);
        String dataSensitivityStr = finding.getDataSensitivity() != null && finding.getDataSensitivity() != com.ecdat.backend.inventory.DataSensitivity.UNKNOWN
            ? finding.getDataSensitivity().name()
            : (assessment != null && assessment.getQuantumRiskResult() != null && assessment.getQuantumRiskResult().getDataSensitivity() != null
                ? assessment.getQuantumRiskResult().getDataSensitivity().name()
                : null);

        CBOMCryptoProperties cryptoProperties = new CBOMCryptoProperties(
            finding.getAlgorithm(),
            finding.getVariant(),
            finding.getPurpose() != null ? finding.getPurpose().name() : null,
            finding.getKeySize(),
            finding.getProtocol(),
            finding.getLibrary(),
            finding.getFile(),
            finding.getLine(),
            finding.getEvidence(),
            finding.getConfidence() != null ? finding.getConfidence().name() : null,
            finding.getSourceType(),
            riskInfo,
            pqcInfo,
            assetCategoryStr,
            usageCategoryStr,
            lifecycleStatusStr,
            businessCriticalityStr,
            dataSensitivityStr
        );

        CBOMComponent component = new CBOMComponent(componentName, description, cryptoProperties);

        // Add ECDAT-specific properties as custom properties
        component.addProperty("ecdat:risk_assessment_version", "prototype");
        component.addProperty("ecdat:confidence_preserved", "true");
        if (assetCategoryStr != null) {
            component.addProperty("ecdat:asset_category", assetCategoryStr);
        }
        if (usageCategoryStr != null) {
            component.addProperty("ecdat:usage_category", usageCategoryStr);
        }
        if (lifecycleStatusStr != null) {
            component.addProperty("ecdat:lifecycle_status", lifecycleStatusStr);
        }
        if (businessCriticalityStr != null) {
            component.addProperty("ecdat:business_criticality", businessCriticalityStr);
        }
        if (dataSensitivityStr != null) {
            component.addProperty("ecdat:data_sensitivity", dataSensitivityStr);
        }

        return component;
    }

    private String generateComponentName(CryptoFinding finding) {
        StringBuilder name = new StringBuilder();
        if (finding.getAlgorithm() != null) {
            name.append(finding.getAlgorithm());
        }
        if (finding.getVariant() != null && !finding.getVariant().isEmpty()) {
            name.append("-").append(finding.getVariant());
        }
        if (finding.getPurpose() != null) {
            name.append("-").append(finding.getPurpose().name().toLowerCase());
        }
        if (finding.getFile() != null) {
            name.append("-").append(finding.getFile().replace("/", "-").replace("\\", "-"));
        }
        if (finding.getLine() > 0) {
            name.append("-L").append(finding.getLine());
        }
        return name.length() > 0 ? name.toString() : "unknown-crypto-asset";
    }

    private String generateDescription(CryptoFinding finding) {
        StringBuilder description = new StringBuilder();
        if (finding.getAlgorithm() != null) {
            description.append("Cryptographic asset: ").append(finding.getAlgorithm());
        }
        if (finding.getPurpose() != null) {
            description.append(" used for ").append(finding.getPurpose().name().toLowerCase().replace("_", " "));
        }
        if (finding.getFile() != null) {
            description.append(" found in ").append(finding.getFile());
            if (finding.getLine() > 0) {
                description.append(" at line ").append(finding.getLine());
            }
        }
        return description.length() > 0 ? description.toString() : "Unknown cryptographic asset";
    }

    private CBOMRiskInfo createRiskInfo(RiskAssessment assessment) {
        CBOMRiskInfo riskInfo = new CBOMRiskInfo(
            assessment.getRiskLevel() != null ? assessment.getRiskLevel().name() : null,
            assessment.getRiskScore(),
            assessment.getQuantumRisk() != null ? assessment.getQuantumRisk().name() : null,
            assessment.getReasons()
        );
        
        // Populate quantum migration risk details if available
        if (assessment.getQuantumRiskResult() != null) {
            riskInfo.setQuantumVulnerable(assessment.getQuantumRiskResult().isQuantumVulnerable());
            riskInfo.setMigrationRequired(assessment.getQuantumRiskResult().isMigrationRequired());
            riskInfo.setDataLifetimeYears(assessment.getQuantumRiskResult().getDataLifetimeYears());
            riskInfo.setMigrationTimeYears(assessment.getQuantumRiskResult().getMigrationTimeYears());
            riskInfo.setThreatHorizonYears(assessment.getQuantumRiskResult().getThreatHorizonYears());
            riskInfo.setMoscaConditionMet(assessment.getQuantumRiskResult().isMoscaConditionMet());
            riskInfo.setTotalExposureYears(assessment.getQuantumRiskResult().getTotalExposureYears());
            riskInfo.setMigrationUrgency(assessment.getQuantumRiskResult().getMigrationUrgency() != null ? 
                assessment.getQuantumRiskResult().getMigrationUrgency().name() : null);
            riskInfo.setQuantumRiskExplanation(assessment.getQuantumRiskResult().getExplanation());
            riskInfo.setMoscaCalculationDetails(assessment.getQuantumRiskResult().getCalculationDetails());
        }
        
        return riskInfo;
    }

    private CBOMPQCInfo createPQCInfo(PQCRecommendation recommendation) {
        return new CBOMPQCInfo(
            recommendation.getRecommendationStatus() != null ? recommendation.getRecommendationStatus().name() : null,
            recommendation.getRecommendedAlgorithm(),
            recommendation.getAlternativeAlgorithms(),
            recommendation.getRationale(),
            recommendation.getMigrationPriority() != null ? recommendation.getMigrationPriority().name() : null,
            recommendation.getConsiderations()
        );
    }

    public String toJson(CBOMDocument document) throws IOException {
        return objectMapper.writeValueAsString(document);
    }
}
