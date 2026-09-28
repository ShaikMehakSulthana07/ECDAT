package com.ecdat.backend.pqc;

import com.ecdat.backend.provenance.Provenance;
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
    private final MigrationStrategy migrationStrategy;
    private final QuantumRisk quantumRisk;
    private final CryptoFinding.Confidence confidence;
    private final List<String> considerations;

    // Provenance fields for recommendation attributes
    private Provenance recommendationStatusProvenance = Provenance.INFERRED;
    private Provenance recommendedAlgorithmProvenance = Provenance.INFERRED;
    private Provenance migrationPriorityProvenance = Provenance.INFERRED;

    public PQCRecommendation(PQCRecommendationStatus recommendationStatus,
                           String currentAlgorithm,
                           CryptoFinding.Purpose currentPurpose,
                           String recommendedAlgorithm,
                           List<String> alternativeAlgorithms,
                           String rationale,
                           MigrationPriority migrationPriority,
                           MigrationStrategy migrationStrategy,
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
        this.migrationStrategy = migrationStrategy != null ? migrationStrategy : MigrationStrategy.UNKNOWN;
        this.quantumRisk = quantumRisk;
        this.confidence = confidence;
        this.considerations = new ArrayList<>(considerations);
    }

    // Legacy constructor for backward compatibility
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
        this(recommendationStatus, currentAlgorithm, currentPurpose, recommendedAlgorithm,
             alternativeAlgorithms, rationale, migrationPriority, MigrationStrategy.UNKNOWN,
             quantumRisk, confidence, considerations);
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

    public MigrationStrategy getMigrationStrategy() {
        return migrationStrategy;
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

    // Provenance getters/setters
    public Provenance getRecommendationStatusProvenance() { return recommendationStatusProvenance; }
    public void setRecommendationStatusProvenance(Provenance recommendationStatusProvenance) { this.recommendationStatusProvenance = recommendationStatusProvenance; }
    public Provenance getRecommendedAlgorithmProvenance() { return recommendedAlgorithmProvenance; }
    public void setRecommendedAlgorithmProvenance(Provenance recommendedAlgorithmProvenance) { this.recommendedAlgorithmProvenance = recommendedAlgorithmProvenance; }
    public Provenance getMigrationPriorityProvenance() { return migrationPriorityProvenance; }
    public void setMigrationPriorityProvenance(Provenance migrationPriorityProvenance) { this.migrationPriorityProvenance = migrationPriorityProvenance; }

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
                ", migrationStrategy=" + migrationStrategy +
                ", quantumRisk=" + quantumRisk +
                ", confidence=" + confidence +
                ", considerations=" + considerations +
                '}';
    }
}
