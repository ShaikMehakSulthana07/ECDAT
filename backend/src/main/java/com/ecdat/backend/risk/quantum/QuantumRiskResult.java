package com.ecdat.backend.risk.quantum;

import com.ecdat.backend.dto.ValueSource;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;

/**
 * Result of quantum migration risk assessment using Mosca-style calculation.
 * Contains the assessment outcome and detailed explanation.
 * Includes value source attribution to distinguish user-provided, organizational defaults,
 * system defaults, and unknown values.
 */
public class QuantumRiskResult {
    private String algorithm;
    private QuantumVulnerabilityStatus quantumVulnerabilityStatus;
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
    
    // Value source attribution
    private ValueSource migrationTimeYearsSource;
    private ValueSource dataLifetimeYearsSource;
    private ValueSource threatHorizonYearsSource;
    private ValueSource businessCriticalitySource;
    private ValueSource dataSensitivitySource;

    public QuantumRiskResult() {
        this.quantumVulnerabilityStatus = QuantumVulnerabilityStatus.UNKNOWN;
        this.migrationUrgency = MigrationUrgency.UNKNOWN;
    }

    public QuantumRiskResult(String algorithm, QuantumVulnerabilityStatus quantumVulnerabilityStatus, boolean quantumVulnerable, boolean migrationRequired,
                            int migrationTimeYears, int dataLifetimeYears, int threatHorizonYears,
                            boolean moscaConditionMet, int totalExposureYears, int yearsUntilThreat,
                            BusinessCriticality businessCriticality, DataSensitivity dataSensitivity,
                            MigrationUrgency migrationUrgency, String explanation, String calculationDetails) {
        this.algorithm = algorithm;
        this.quantumVulnerabilityStatus = quantumVulnerabilityStatus;
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

    public QuantumVulnerabilityStatus getQuantumVulnerabilityStatus() { return quantumVulnerabilityStatus; }
    public void setQuantumVulnerabilityStatus(QuantumVulnerabilityStatus quantumVulnerabilityStatus) { this.quantumVulnerabilityStatus = quantumVulnerabilityStatus; }

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

    // Value source getters and setters
    public ValueSource getMigrationTimeYearsSource() { return migrationTimeYearsSource; }
    public void setMigrationTimeYearsSource(ValueSource migrationTimeYearsSource) { this.migrationTimeYearsSource = migrationTimeYearsSource; }

    public ValueSource getDataLifetimeYearsSource() { return dataLifetimeYearsSource; }
    public void setDataLifetimeYearsSource(ValueSource dataLifetimeYearsSource) { this.dataLifetimeYearsSource = dataLifetimeYearsSource; }

    public ValueSource getThreatHorizonYearsSource() { return threatHorizonYearsSource; }
    public void setThreatHorizonYearsSource(ValueSource threatHorizonYearsSource) { this.threatHorizonYearsSource = threatHorizonYearsSource; }

    public ValueSource getBusinessCriticalitySource() { return businessCriticalitySource; }
    public void setBusinessCriticalitySource(ValueSource businessCriticalitySource) { this.businessCriticalitySource = businessCriticalitySource; }

    public ValueSource getDataSensitivitySource() { return dataSensitivitySource; }
    public void setDataSensitivitySource(ValueSource dataSensitivitySource) { this.dataSensitivitySource = dataSensitivitySource; }
}
