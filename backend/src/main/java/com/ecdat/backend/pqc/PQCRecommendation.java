package com.ecdat.backend.pqc;

import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.scanner.CryptoFinding;
import java.util.ArrayList;
import java.util.List;

public class PQCRecommendation {
    private final PQCRecommendationStatus recommendationStatus;
    private final String currentAlgorithm;
    private final CryptoFinding.Purpose currentPurpose;
    private final String recommendedAlgorithm;
    private final List<String> alternativeAlgorithms;
    private final String rationale;
    private final MigrationPriority migrationPriority;
    private final QuantumRisk quantumRisk;
    private final CryptoFinding.Confidence confidence;
    private final List<String> considerations;

    public PQCRecommendation(PQCRecommendationStatus recommendationStatus,
                           String currentAlgorithm,
                           CryptoFinding.Purpose currentPurpose,
                           String recommendedAlgorithm,
                           List<String> alternativeAlgorithms,
                           String rationale,
                           MigrationPriority migrationPriority,
                           QuantumRisk quantumRisk,
                           CryptoFinding.Confidence confidence,
                           List<String> considerations) {
        this.recommendationStatus = recommendationStatus;
        this.currentAlgorithm = currentAlgorithm;
        this.currentPurpose = currentPurpose;
        this.recommendedAlgorithm = recommendedAlgorithm;
        this.alternativeAlgorithms = new ArrayList<>(alternativeAlgorithms);
        this.rationale = rationale;
        this.migrationPriority = migrationPriority;
        this.quantumRisk = quantumRisk;
        this.confidence = confidence;
        this.considerations = new ArrayList<>(considerations);
    }

    public PQCRecommendationStatus getRecommendationStatus() {
        return recommendationStatus;
    }

    public String getCurrentAlgorithm() {
        return currentAlgorithm;
    }

    public CryptoFinding.Purpose getCurrentPurpose() {
        return currentPurpose;
    }

    public String getRecommendedAlgorithm() {
        return recommendedAlgorithm;
    }

    public List<String> getAlternativeAlgorithms() {
        return new ArrayList<>(alternativeAlgorithms);
    }

    public String getRationale() {
        return rationale;
    }

    public MigrationPriority getMigrationPriority() {
        return migrationPriority;
    }

    public QuantumRisk getQuantumRisk() {
        return quantumRisk;
    }

    public CryptoFinding.Confidence getConfidence() {
        return confidence;
    }

    public List<String> getConsiderations() {
        return new ArrayList<>(considerations);
    }

    @Override
    public String toString() {
        return "PQCRecommendation{" +
                "recommendationStatus=" + recommendationStatus +
                ", currentAlgorithm='" + currentAlgorithm + '\'' +
                ", currentPurpose=" + currentPurpose +
                ", recommendedAlgorithm='" + recommendedAlgorithm + '\'' +
                ", alternativeAlgorithms=" + alternativeAlgorithms +
                ", rationale='" + rationale + '\'' +
                ", migrationPriority=" + migrationPriority +
                ", quantumRisk=" + quantumRisk +
                ", confidence=" + confidence +
                ", considerations=" + considerations +
                '}';
    }
}
