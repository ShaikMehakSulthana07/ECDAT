package com.ecdat.backend.risk;

public class RiskFactor {
    private final String name;
    private final int score;
    private final String explanation;

    public RiskFactor(String name, int score, String explanation) {
        this.name = name;
        this.score = score;
        this.explanation = explanation;
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

    @Override
    public String toString() {
        return "RiskFactor{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
