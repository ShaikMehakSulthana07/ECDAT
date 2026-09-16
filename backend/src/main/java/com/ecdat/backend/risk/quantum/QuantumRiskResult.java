package com.ecdat.backend.risk.quantum;

import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;

/**
 * Result of quantum migration risk assessment using Mosca-style calculation.
 * Contains the assessment outcome and detailed explanation.
 */
public class QuantumRiskResult {
    private String algorithm;
    private boolean quantumVulnerable;
    private boolean migrationRequired;
    private int migrationTimeYears;
    private int dataLifetimeYears;
    private int threatHorizonYears;
    private boolean moscaConditionMet; // X + Y > Z
    private int totalExposureYears; // X + Y
    private int yearsUntilThreat; // Z - (current year or 0)
    private BusinessCriticality businessCriticality;
    private DataSensitivity dataSensitivity;
    private MigrationUrgency migrationUrgency;
    private String explanation;
    private String calculationDetails;

    public QuantumRiskResult() {
        this.migrationUrgency = MigrationUrgency.UNKNOWN;
    }

    public QuantumRiskResult(String algorithm, boolean quantumVulnerable, boolean migrationRequired,
                            int migrationTimeYears, int dataLifetimeYears, int threatHorizonYears,
                            boolean moscaConditionMet, int totalExposureYears, int yearsUntilThreat,
                            BusinessCriticality businessCriticality, DataSensitivity dataSensitivity,
                            MigrationUrgency migrationUrgency, String explanation, String calculationDetails) {
        this.algorithm = algorithm;
        this.quantumVulnerable = quantumVulnerable;
        this.migrationRequired = migrationRequired;
        this.migrationTimeYears = migrationTimeYears;
        this.dataLifetimeYears = dataLifetimeYears;
        this.threatHorizonYears = threatHorizonYears;
        this.moscaConditionMet = moscaConditionMet;
        this.totalExposureYears = totalExposureYears;
        this.yearsUntilThreat = yearsUntilThreat;
        this.businessCriticality = businessCriticality;
        this.dataSensitivity = dataSensitivity;
        this.migrationUrgency = migrationUrgency;
        this.explanation = explanation;
        this.calculationDetails = calculationDetails;
    }

    // Getters and Setters
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public boolean isQuantumVulnerable() { return quantumVulnerable; }
    public void setQuantumVulnerable(boolean quantumVulnerable) { this.quantumVulnerable = quantumVulnerable; }

    public boolean isMigrationRequired() { return migrationRequired; }
    public void setMigrationRequired(boolean migrationRequired) { this.migrationRequired = migrationRequired; }

    public int getMigrationTimeYears() { return migrationTimeYears; }
    public void setMigrationTimeYears(int migrationTimeYears) { this.migrationTimeYears = migrationTimeYears; }

    public int getDataLifetimeYears() { return dataLifetimeYears; }
    public void setDataLifetimeYears(int dataLifetimeYears) { this.dataLifetimeYears = dataLifetimeYears; }

    public int getThreatHorizonYears() { return threatHorizonYears; }
    public void setThreatHorizonYears(int threatHorizonYears) { this.threatHorizonYears = threatHorizonYears; }

    public boolean isMoscaConditionMet() { return moscaConditionMet; }
    public void setMoscaConditionMet(boolean moscaConditionMet) { this.moscaConditionMet = moscaConditionMet; }

    public int getTotalExposureYears() { return totalExposureYears; }
    public void setTotalExposureYears(int totalExposureYears) { this.totalExposureYears = totalExposureYears; }

    public int getYearsUntilThreat() { return yearsUntilThreat; }
    public void setYearsUntilThreat(int yearsUntilThreat) { this.yearsUntilThreat = yearsUntilThreat; }

    public BusinessCriticality getBusinessCriticality() { return businessCriticality; }
    public void setBusinessCriticality(BusinessCriticality businessCriticality) { this.businessCriticality = businessCriticality; }

    public DataSensitivity getDataSensitivity() { return dataSensitivity; }
    public void setDataSensitivity(DataSensitivity dataSensitivity) { this.dataSensitivity = dataSensitivity; }

    public MigrationUrgency getMigrationUrgency() { return migrationUrgency; }
    public void setMigrationUrgency(MigrationUrgency migrationUrgency) { this.migrationUrgency = migrationUrgency; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getCalculationDetails() { return calculationDetails; }
    public void setCalculationDetails(String calculationDetails) { this.calculationDetails = calculationDetails; }
}
