package com.ecdat.backend.cbom;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CBOMRiskInfo {
    private String riskLevel;
    private Integer riskScore;
    private String quantumRisk;
    private List<String> riskFactors;
    
    // Quantum migration risk details (Mosca-style assessment)
    private Boolean quantumVulnerable;
    private Boolean migrationRequired;
    private Integer dataLifetimeYears;
    private Integer migrationTimeYears;
    private Integer threatHorizonYears;
    private Boolean moscaConditionMet;
    private Integer totalExposureYears;
    private String migrationUrgency;
    private String quantumRiskExplanation;
    private String moscaCalculationDetails;

    // Default constructor for Jackson deserialization
    public CBOMRiskInfo() {
        this.riskFactors = new ArrayList<>();
    }

    public CBOMRiskInfo(String riskLevel, Integer riskScore, String quantumRisk, List<String> riskFactors) {
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.quantumRisk = quantumRisk;
        this.riskFactors = new ArrayList<>(riskFactors);
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public String getQuantumRisk() {
        return quantumRisk;
    }

    public List<String> getRiskFactors() {
        return new ArrayList<>(riskFactors);
    }

    // Setters for Jackson deserialization
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public void setQuantumRisk(String quantumRisk) {
        this.quantumRisk = quantumRisk;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }

    // Getters and setters for quantum migration risk details
    public Boolean getQuantumVulnerable() { return quantumVulnerable; }
    public void setQuantumVulnerable(Boolean quantumVulnerable) { this.quantumVulnerable = quantumVulnerable; }

    public Boolean getMigrationRequired() { return migrationRequired; }
    public void setMigrationRequired(Boolean migrationRequired) { this.migrationRequired = migrationRequired; }

    public Integer getDataLifetimeYears() { return dataLifetimeYears; }
    public void setDataLifetimeYears(Integer dataLifetimeYears) { this.dataLifetimeYears = dataLifetimeYears; }

    public Integer getMigrationTimeYears() { return migrationTimeYears; }
    public void setMigrationTimeYears(Integer migrationTimeYears) { this.migrationTimeYears = migrationTimeYears; }

    public Integer getThreatHorizonYears() { return threatHorizonYears; }
    public void setThreatHorizonYears(Integer threatHorizonYears) { this.threatHorizonYears = threatHorizonYears; }

    public Boolean getMoscaConditionMet() { return moscaConditionMet; }
    public void setMoscaConditionMet(Boolean moscaConditionMet) { this.moscaConditionMet = moscaConditionMet; }

    public Integer getTotalExposureYears() { return totalExposureYears; }
    public void setTotalExposureYears(Integer totalExposureYears) { this.totalExposureYears = totalExposureYears; }

    public String getMigrationUrgency() { return migrationUrgency; }
    public void setMigrationUrgency(String migrationUrgency) { this.migrationUrgency = migrationUrgency; }

    public String getQuantumRiskExplanation() { return quantumRiskExplanation; }
    public void setQuantumRiskExplanation(String quantumRiskExplanation) { this.quantumRiskExplanation = quantumRiskExplanation; }

    public String getMoscaCalculationDetails() { return moscaCalculationDetails; }
    public void setMoscaCalculationDetails(String moscaCalculationDetails) { this.moscaCalculationDetails = moscaCalculationDetails; }
}
