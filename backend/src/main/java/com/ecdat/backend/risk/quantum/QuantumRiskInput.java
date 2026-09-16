package com.ecdat.backend.risk.quantum;

import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;

/**
 * Input parameters for quantum migration risk assessment.
 * This represents the context required for Mosca-style quantum risk calculation.
 */
public class QuantumRiskInput {
    private String algorithm;
    private Integer keySize;
    private String cryptographicPurpose;
    private int dataLifetimeYears;
    private int migrationTimeYears;
    private int threatHorizonYears;
    private BusinessCriticality businessCriticality;
    private DataSensitivity dataSensitivity;

    public QuantumRiskInput() {
        // Default values - these should be configured by the user
        this.dataLifetimeYears = 10; // Default 10 year data lifetime
        this.migrationTimeYears = 3; // Default 3 year migration time
        this.threatHorizonYears = 10; // Default 10 year threat horizon
        this.businessCriticality = BusinessCriticality.UNKNOWN;
        this.dataSensitivity = DataSensitivity.UNKNOWN;
    }

    public QuantumRiskInput(String algorithm, Integer keySize, String cryptographicPurpose,
                            int dataLifetimeYears, int migrationTimeYears, int threatHorizonYears,
                            BusinessCriticality businessCriticality, DataSensitivity dataSensitivity) {
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.cryptographicPurpose = cryptographicPurpose;
        this.dataLifetimeYears = dataLifetimeYears;
        this.migrationTimeYears = migrationTimeYears;
        this.threatHorizonYears = threatHorizonYears;
        this.businessCriticality = businessCriticality;
        this.dataSensitivity = dataSensitivity;
    }

    // Getters and Setters
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public Integer getKeySize() { return keySize; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }

    public String getCryptographicPurpose() { return cryptographicPurpose; }
    public void setCryptographicPurpose(String cryptographicPurpose) { this.cryptographicPurpose = cryptographicPurpose; }

    public int getDataLifetimeYears() { return dataLifetimeYears; }
    public void setDataLifetimeYears(int dataLifetimeYears) { this.dataLifetimeYears = dataLifetimeYears; }

    public int getMigrationTimeYears() { return migrationTimeYears; }
    public void setMigrationTimeYears(int migrationTimeYears) { this.migrationTimeYears = migrationTimeYears; }

    public int getThreatHorizonYears() { return threatHorizonYears; }
    public void setThreatHorizonYears(int threatHorizonYears) { this.threatHorizonYears = threatHorizonYears; }

    public BusinessCriticality getBusinessCriticality() { return businessCriticality; }
    public void setBusinessCriticality(BusinessCriticality businessCriticality) { this.businessCriticality = businessCriticality; }

    public DataSensitivity getDataSensitivity() { return dataSensitivity; }
    public void setDataSensitivity(DataSensitivity dataSensitivity) { this.dataSensitivity = dataSensitivity; }
}
