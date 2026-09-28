package com.ecdat.backend.risk;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed breakdown of a risk score calculation.
 * Provides transparency into how the ECDAT heuristic risk score was computed.
 * This is NOT a NIST or CVSS score - it is an ECDAT heuristic assessment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskScoreBreakdown {
    private final int totalScore;
    private final RiskLevel riskLevel;
    private final List<ScoreComponent> components;
    private final String methodologyNote;

    public RiskScoreBreakdown(int totalScore, RiskLevel riskLevel, List<ScoreComponent> components) {
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.components = new ArrayList<>(components);
        this.methodologyNote = "ECDAT Risk Score — heuristic assessment (not NIST/CVSS)";
    }

    public int getTotalScore() {
        return totalScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public List<ScoreComponent> getComponents() {
        return new ArrayList<>(components);
    }

    public String getMethodologyNote() {
        return methodologyNote;
    }

    /**
     * Represents a single component contributing to the risk score.
     */
    public static class ScoreComponent {
        private final String name;
        private final int score;
        private final String explanation;
        private final String category;

        public ScoreComponent(String name, int score, String explanation, String category) {
            this.name = name;
            this.score = score;
            this.explanation = explanation;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        public String getExplanation() {
            return explanation;
        }

        public String getCategory() {
            return category;
        }
    }
}
