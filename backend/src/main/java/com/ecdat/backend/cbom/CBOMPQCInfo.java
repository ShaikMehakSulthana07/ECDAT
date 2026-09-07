package com.ecdat.backend.cbom;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CBOMPQCInfo {
    private String recommendationStatus;
    private String recommendedAlgorithm;
    private List<String> alternativeAlgorithms;
    private String rationale;
    private String migrationPriority;
    private List<String> considerations;

    // Default constructor for Jackson deserialization
    public CBOMPQCInfo() {
        this.alternativeAlgorithms = new ArrayList<>();
        this.considerations = new ArrayList<>();
    }

    public CBOMPQCInfo(String recommendationStatus, String recommendedAlgorithm,
                       List<String> alternativeAlgorithms, String rationale,
                       String migrationPriority, List<String> considerations) {
        this.recommendationStatus = recommendationStatus;
        this.recommendedAlgorithm = recommendedAlgorithm;
        this.alternativeAlgorithms = new ArrayList<>(alternativeAlgorithms);
        this.rationale = rationale;
        this.migrationPriority = migrationPriority;
        this.considerations = new ArrayList<>(considerations);
    }

    public String getRecommendationStatus() {
        return recommendationStatus;
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

    public String getMigrationPriority() {
        return migrationPriority;
    }

    public List<String> getConsiderations() {
        return new ArrayList<>(considerations);
    }

    // Setters for Jackson deserialization
    public void setRecommendationStatus(String recommendationStatus) {
        this.recommendationStatus = recommendationStatus;
    }

    public void setRecommendedAlgorithm(String recommendedAlgorithm) {
        this.recommendedAlgorithm = recommendedAlgorithm;
    }

    public void setAlternativeAlgorithms(List<String> alternativeAlgorithms) {
        this.alternativeAlgorithms = alternativeAlgorithms;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public void setMigrationPriority(String migrationPriority) {
        this.migrationPriority = migrationPriority;
    }

    public void setConsiderations(List<String> considerations) {
        this.considerations = considerations;
    }
}
