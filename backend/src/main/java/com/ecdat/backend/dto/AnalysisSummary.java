package com.ecdat.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisSummary {
    private int totalFindings;
    private int lowRiskCount;
    private int mediumRiskCount;
    private int highRiskCount;
    private int criticalRiskCount;
    private int quantumHighRiskCount;
    private int pqcRecommendedCount;
    private int pqcConditionalCount;
    private int pqcNeedsAnalysisCount;
    private int pqcNotRequiredCount;

    // Phase 7 Enterprise Inventory Metrics
    private int activeAssetCount;
    private int deprecatedAssetCount;
    private int unknownLifecycleCount;
    private int directUsageCount;

    public AnalysisSummary() {
    }

    public AnalysisSummary(int totalFindings, int lowRiskCount, int mediumRiskCount,
                           int highRiskCount, int criticalRiskCount, int quantumHighRiskCount,
                           int pqcRecommendedCount, int pqcConditionalCount,
                           int pqcNeedsAnalysisCount, int pqcNotRequiredCount) {
        this.totalFindings = totalFindings;
        this.lowRiskCount = lowRiskCount;
        this.mediumRiskCount = mediumRiskCount;
        this.highRiskCount = highRiskCount;
        this.criticalRiskCount = criticalRiskCount;
        this.quantumHighRiskCount = quantumHighRiskCount;
        this.pqcRecommendedCount = pqcRecommendedCount;
        this.pqcConditionalCount = pqcConditionalCount;
        this.pqcNeedsAnalysisCount = pqcNeedsAnalysisCount;
        this.pqcNotRequiredCount = pqcNotRequiredCount;
    }

    public AnalysisSummary(int totalFindings, int lowRiskCount, int mediumRiskCount,
                           int highRiskCount, int criticalRiskCount, int quantumHighRiskCount,
                           int pqcRecommendedCount, int pqcConditionalCount,
                           int pqcNeedsAnalysisCount, int pqcNotRequiredCount,
                           int activeAssetCount, int deprecatedAssetCount,
                           int unknownLifecycleCount, int directUsageCount) {
        this(totalFindings, lowRiskCount, mediumRiskCount, highRiskCount, criticalRiskCount,
             quantumHighRiskCount, pqcRecommendedCount, pqcConditionalCount,
             pqcNeedsAnalysisCount, pqcNotRequiredCount);
        this.activeAssetCount = activeAssetCount;
        this.deprecatedAssetCount = deprecatedAssetCount;
        this.unknownLifecycleCount = unknownLifecycleCount;
        this.directUsageCount = directUsageCount;
    }

    public int getTotalFindings() { return totalFindings; }
    public void setTotalFindings(int totalFindings) { this.totalFindings = totalFindings; }

    public int getLowRiskCount() { return lowRiskCount; }
    public void setLowRiskCount(int lowRiskCount) { this.lowRiskCount = lowRiskCount; }

    public int getMediumRiskCount() { return mediumRiskCount; }
    public void setMediumRiskCount(int mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }

    public int getHighRiskCount() { return highRiskCount; }
    public void setHighRiskCount(int highRiskCount) { this.highRiskCount = highRiskCount; }

    public int getCriticalRiskCount() { return criticalRiskCount; }
    public void setCriticalRiskCount(int criticalRiskCount) { this.criticalRiskCount = criticalRiskCount; }

    public int getQuantumHighRiskCount() { return quantumHighRiskCount; }
    public void setQuantumHighRiskCount(int quantumHighRiskCount) { this.quantumHighRiskCount = quantumHighRiskCount; }

    public int getPqcRecommendedCount() { return pqcRecommendedCount; }
    public void setPqcRecommendedCount(int pqcRecommendedCount) { this.pqcRecommendedCount = pqcRecommendedCount; }

    public int getPqcConditionalCount() { return pqcConditionalCount; }
    public void setPqcConditionalCount(int pqcConditionalCount) { this.pqcConditionalCount = pqcConditionalCount; }

    public int getPqcNeedsAnalysisCount() { return pqcNeedsAnalysisCount; }
    public void setPqcNeedsAnalysisCount(int pqcNeedsAnalysisCount) { this.pqcNeedsAnalysisCount = pqcNeedsAnalysisCount; }

    public int getPqcNotRequiredCount() { return pqcNotRequiredCount; }
    public void setPqcNotRequiredCount(int pqcNotRequiredCount) { this.pqcNotRequiredCount = pqcNotRequiredCount; }

    public int getActiveAssetCount() { return activeAssetCount; }
    public void setActiveAssetCount(int activeAssetCount) { this.activeAssetCount = activeAssetCount; }

    public int getDeprecatedAssetCount() { return deprecatedAssetCount; }
    public void setDeprecatedAssetCount(int deprecatedAssetCount) { this.deprecatedAssetCount = deprecatedAssetCount; }

    public int getUnknownLifecycleCount() { return unknownLifecycleCount; }
    public void setUnknownLifecycleCount(int unknownLifecycleCount) { this.unknownLifecycleCount = unknownLifecycleCount; }

    public int getDirectUsageCount() { return directUsageCount; }
    public void setDirectUsageCount(int directUsageCount) { this.directUsageCount = directUsageCount; }
}
