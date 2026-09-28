package com.ecdat.backend.risk;

import com.ecdat.backend.risk.quantum.QuantumRiskResult;
import com.ecdat.backend.scanner.CryptoFinding;
import java.util.ArrayList;
import java.util.List;

public class RiskAssessment {
    private final int riskScore;
    private final RiskLevel riskLevel;
    private final List<String> reasons;
    private final List<RiskFactor> factors;
    private final QuantumRisk quantumRisk;
    private final CryptoFinding.Confidence confidence;
    private final CryptoFinding originalFinding;
    private QuantumRiskResult quantumRiskResult;
    private RiskScoreBreakdown scoreBreakdown;

    public RiskAssessment(int riskScore, List<RiskFactor> factors, QuantumRisk quantumRisk, 
                         CryptoFinding.Confidence confidence, CryptoFinding originalFinding) {
        this.riskScore = riskScore;
        this.riskLevel = RiskLevel.fromScore(riskScore);
        this.factors = factors;
        this.quantumRisk = quantumRisk;
        this.confidence = confidence;
        this.originalFinding = originalFinding;
        this.reasons = new ArrayList<>();
        
        // Generate reasons from factors
        for (RiskFactor factor : factors) {
            reasons.add(factor.getExplanation());
        }
        
        // Add confidence-related reason if needed
        if (confidence == CryptoFinding.Confidence.LOW) {
            reasons.add("Risk assessment is affected by low-confidence static analysis.");
        }
        
        // Generate score breakdown
        this.scoreBreakdown = generateScoreBreakdown();
    }

    private RiskScoreBreakdown generateScoreBreakdown() {
        List<RiskScoreBreakdown.ScoreComponent> components = new ArrayList<>();
        
        for (RiskFactor factor : factors) {
            String category = determineCategory(factor.getName());
            components.add(new RiskScoreBreakdown.ScoreComponent(
                factor.getName(),
                factor.getScore(),
                factor.getExplanation(),
                category
            ));
        }
        
        return new RiskScoreBreakdown(riskScore, riskLevel, components);
    }

    private String determineCategory(String factorName) {
        if (factorName.contains("QUANTUM")) return "Quantum Vulnerability";
        if (factorName.contains("KEY_SIZE")) return "Key Size";
        if (factorName.contains("CRYPTOGRAPHIC_CONCERN") || factorName.contains("PUBLIC_KEY") || 
            factorName.contains("SYMMETRIC") || factorName.contains("HASH")) return "Algorithm Strength";
        if (factorName.contains("DATA_SENSITIVITY")) return "Data Sensitivity";
        if (factorName.contains("BUSINESS_CRITICALITY")) return "Business Criticality";
        if (factorName.contains("PURPOSE")) return "Usage Context";
        if (factorName.contains("DEPRECATED") || factorName.contains("BROKEN")) return "Algorithm Status";
        return "Other";
    }

    public int getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public List<String> getReasons() {
        return new ArrayList<>(reasons);
    }

    public List<RiskFactor> getFactors() {
        return new ArrayList<>(factors);
    }

    public QuantumRisk getQuantumRisk() {
        return quantumRisk;
    }

    public CryptoFinding.Confidence getConfidence() {
        return confidence;
    }

    public CryptoFinding getOriginalFinding() {
        return originalFinding;
    }

    public QuantumRiskResult getQuantumRiskResult() {
        return quantumRiskResult;
    }

    public void setQuantumRiskResult(QuantumRiskResult quantumRiskResult) {
        this.quantumRiskResult = quantumRiskResult;
    }

    public RiskScoreBreakdown getScoreBreakdown() {
        return scoreBreakdown;
    }

    @Override
    public String toString() {
        return "RiskAssessment{" +
                "riskScore=" + riskScore +
                ", riskLevel=" + riskLevel +
                ", quantumRisk=" + quantumRisk +
                ", confidence=" + confidence +
                ", reasons=" + reasons +
                ", factors=" + factors +
                ", quantumRiskResult=" + quantumRiskResult +
                '}';
    }
}
