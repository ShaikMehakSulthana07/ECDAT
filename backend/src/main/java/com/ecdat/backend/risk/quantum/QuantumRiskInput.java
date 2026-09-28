package com.ecdat.backend.risk.quantum;

import com.ecdat.backend.dto.ValueSource;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;

/**
 * Input parameters for quantum migration risk assessment.
 * This represents the context required for Mosca-style quantum risk calculation.
 * Each value includes source attribution to distinguish user-provided, organizational defaults,
 * system defaults, and unknown values.
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
    
    // Value source attribution
    private ValueSource dataLifetimeYearsSource;
    private ValueSource migrationTimeYearsSource;
    private ValueSource threatHorizonYearsSource;
    private ValueSource businessCriticalitySource;
    private ValueSource dataSensitivitySource;

    public QuantumRiskInput() {
        // Default values - marked as SYSTEM_DEFAULT (illustrative, not observed)
        this.dataLifetimeYears = 10;
        this.migrationTimeYears = 3;
        this.threatHorizonYears = 10;
        this.businessCriticality = BusinessCriticality.UNKNOWN;
        this.dataSensitivity = DataSensitivity.UNKNOWN;
        
        // Mark defaults as SYSTEM_DEFAULT
        this.dataLifetimeYearsSource = ValueSource.SYSTEM_DEFAULT;
        this.migrationTimeYearsSource = ValueSource.SYSTEM_DEFAULT;
        this.threatHorizonYearsSource = ValueSource.SYSTEM_DEFAULT;
        this.businessCriticalitySource = ValueSource.UNKNOWN;
        this.dataSensitivitySource = ValueSource.UNKNOWN;
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
        
        // If values were provided, mark as USER_PROVIDED
        this.dataLifetimeYearsSource = ValueSource.USER_PROVIDED;
        this.migrationTimeYearsSource = ValueSource.USER_PROVIDED;
        this.threatHorizonYearsSource = ValueSource.USER_PROVIDED;
        this.businessCriticalitySource = businessCriticality != null ? ValueSource.USER_PROVIDED : ValueSource.UNKNOWN;
        this.dataSensitivitySource = dataSensitivity != null ? ValueSource.USER_PROVIDED : ValueSource.UNKNOWN;
    }

    // Getters and Setters
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public Integer getKeySize() { return keySize; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }

    public String getCryptographicPurpose() { return cryptographicPurpose; }
    public void setCryptographicPurpose(String cryptographicPurpose) { this.cryptographicPurpose = cryptographicPurpose; }

    public int getDataLifetimeYears() { return dataLifetimeYears; }
    public void setDataLifetimeYears(int dataLifetimeYears) { 
        this.dataLifetimeYears = dataLifetimeYears;
        this.dataLifetimeYearsSource = ValueSource.USER_PROVIDED;
    }

    public int getMigrationTimeYears() { return migrationTimeYears; }
    public void setMigrationTimeYears(int migrationTimeYears) { 
        this.migrationTimeYears = migrationTimeYears;
        this.migrationTimeYearsSource = ValueSource.USER_PROVIDED;
    }

    public int getThreatHorizonYears() { return threatHorizonYears; }
    public void setThreatHorizonYears(int threatHorizonYears) { 
        this.threatHorizonYears = threatHorizonYears;
        this.threatHorizonYearsSource = ValueSource.USER_PROVIDED;
    }

    public BusinessCriticality getBusinessCriticality() { return businessCriticality; }
    public void setBusinessCriticality(BusinessCriticality businessCriticality) { 
        this.businessCriticality = businessCriticality;
        this.businessCriticalitySource = ValueSource.USER_PROVIDED;
    }

    public DataSensitivity getDataSensitivity() { return dataSensitivity; }
    public void setDataSensitivity(DataSensitivity dataSensitivity) { 
        this.dataSensitivity = dataSensitivity;
        this.dataSensitivitySource = ValueSource.USER_PROVIDED;
    }

    // Value source getters and setters
    public ValueSource getDataLifetimeYearsSource() { return dataLifetimeYearsSource; }
    public void setDataLifetimeYearsSource(ValueSource dataLifetimeYearsSource) { this.dataLifetimeYearsSource = dataLifetimeYearsSource; }

    public ValueSource getMigrationTimeYearsSource() { return migrationTimeYearsSource; }
    public void setMigrationTimeYearsSource(ValueSource migrationTimeYearsSource) { this.migrationTimeYearsSource = migrationTimeYearsSource; }

    public ValueSource getThreatHorizonYearsSource() { return threatHorizonYearsSource; }
    public void setThreatHorizonYearsSource(ValueSource threatHorizonYearsSource) { this.threatHorizonYearsSource = threatHorizonYearsSource; }

    public ValueSource getBusinessCriticalitySource() { return businessCriticalitySource; }
    public void setBusinessCriticalitySource(ValueSource businessCriticalitySource) { this.businessCriticalitySource = businessCriticalitySource; }

    public ValueSource getDataSensitivitySource() { return dataSensitivitySource; }
    public void setDataSensitivitySource(ValueSource dataSensitivitySource) { this.dataSensitivitySource = dataSensitivitySource; }
}
