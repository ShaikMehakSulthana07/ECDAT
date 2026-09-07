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
}
