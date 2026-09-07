package com.ecdat.backend.risk;

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

    @Override
    public String toString() {
        return "RiskAssessment{" +
                "riskScore=" + riskScore +
                ", riskLevel=" + riskLevel +
                ", quantumRisk=" + quantumRisk +
                ", confidence=" + confidence +
                ", reasons=" + reasons +
                ", factors=" + factors +
                '}';
    }
}
