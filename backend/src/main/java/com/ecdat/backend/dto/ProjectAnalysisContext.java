package com.ecdat.backend.dto;

import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Project-level analysis context for quantum migration assessment.
 * This provides the business and operational context required for accurate risk calculation.
 * Each value includes source attribution to distinguish user-provided, organizational defaults,
 * system defaults, and unknown values.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectAnalysisContext {
    private String applicationName;
    private BusinessCriticality businessCriticality;
    private DataSensitivity dataSensitivity;
    private Integer dataLifetimeYears;
    private Integer migrationTimeYears;
    private Integer threatHorizonYears;
    
    // Value source attribution
    private ValueSource businessCriticalitySource;
    private ValueSource dataSensitivitySource;
    private ValueSource dataLifetimeYearsSource;
    private ValueSource migrationTimeYearsSource;
    private ValueSource threatHorizonYearsSource;

    public ProjectAnalysisContext() {
        // Set sensible defaults but mark them as SYSTEM_DEFAULT (illustrative)
        this.businessCriticality = BusinessCriticality.MEDIUM;
        this.dataSensitivity = DataSensitivity.INTERNAL;
        this.dataLifetimeYears = 10;
        this.migrationTimeYears = 3;
        this.threatHorizonYears = 10;
        
        // Mark all defaults as SYSTEM_DEFAULT (illustrative, not observed)
        this.businessCriticalitySource = ValueSource.SYSTEM_DEFAULT;
        this.dataSensitivitySource = ValueSource.SYSTEM_DEFAULT;
        this.dataLifetimeYearsSource = ValueSource.SYSTEM_DEFAULT;
        this.migrationTimeYearsSource = ValueSource.SYSTEM_DEFAULT;
        this.threatHorizonYearsSource = ValueSource.SYSTEM_DEFAULT;
    }

    public ProjectAnalysisContext(String applicationName, BusinessCriticality businessCriticality,
                                 DataSensitivity dataSensitivity, Integer dataLifetimeYears,
                                 Integer migrationTimeYears, Integer threatHorizonYears) {
        this.applicationName = applicationName;
        this.businessCriticality = businessCriticality != null ? businessCriticality : BusinessCriticality.MEDIUM;
        this.dataSensitivity = dataSensitivity != null ? dataSensitivity : DataSensitivity.INTERNAL;
        this.dataLifetimeYears = dataLifetimeYears != null ? dataLifetimeYears : 10;
        this.migrationTimeYears = migrationTimeYears != null ? migrationTimeYears : 3;
        this.threatHorizonYears = threatHorizonYears != null ? threatHorizonYears : 10;
        
        // If values were provided, mark as USER_PROVIDED; otherwise SYSTEM_DEFAULT
        this.businessCriticalitySource = businessCriticality != null ? ValueSource.USER_PROVIDED : ValueSource.SYSTEM_DEFAULT;
        this.dataSensitivitySource = dataSensitivity != null ? ValueSource.USER_PROVIDED : ValueSource.SYSTEM_DEFAULT;
        this.dataLifetimeYearsSource = dataLifetimeYears != null ? ValueSource.USER_PROVIDED : ValueSource.SYSTEM_DEFAULT;
        this.migrationTimeYearsSource = migrationTimeYears != null ? ValueSource.USER_PROVIDED : ValueSource.SYSTEM_DEFAULT;
        this.threatHorizonYearsSource = threatHorizonYears != null ? ValueSource.USER_PROVIDED : ValueSource.SYSTEM_DEFAULT;
    }

    // Getters and Setters
    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }

    public BusinessCriticality getBusinessCriticality() { return businessCriticality; }
    public void setBusinessCriticality(BusinessCriticality businessCriticality) { 
        this.businessCriticality = businessCriticality;
        // When set via setter, assume user-provided unless explicitly marked
        this.businessCriticalitySource = ValueSource.USER_PROVIDED;
    }

    public DataSensitivity getDataSensitivity() { return dataSensitivity; }
    public void setDataSensitivity(DataSensitivity dataSensitivity) { 
        this.dataSensitivity = dataSensitivity;
        this.dataSensitivitySource = ValueSource.USER_PROVIDED;
    }

    public Integer getDataLifetimeYears() { return dataLifetimeYears; }
    public void setDataLifetimeYears(Integer dataLifetimeYears) { 
        this.dataLifetimeYears = dataLifetimeYears;
        this.dataLifetimeYearsSource = ValueSource.USER_PROVIDED;
    }

    public Integer getMigrationTimeYears() { return migrationTimeYears; }
    public void setMigrationTimeYears(Integer migrationTimeYears) { 
        this.migrationTimeYears = migrationTimeYears;
        this.migrationTimeYearsSource = ValueSource.USER_PROVIDED;
    }

    public Integer getThreatHorizonYears() { return threatHorizonYears; }
    public void setThreatHorizonYears(Integer threatHorizonYears) { 
        this.threatHorizonYears = threatHorizonYears;
        this.threatHorizonYearsSource = ValueSource.USER_PROVIDED;
    }

    // Value source getters and setters
    public ValueSource getBusinessCriticalitySource() { return businessCriticalitySource; }
    public void setBusinessCriticalitySource(ValueSource businessCriticalitySource) { this.businessCriticalitySource = businessCriticalitySource; }

    public ValueSource getDataSensitivitySource() { return dataSensitivitySource; }
    public void setDataSensitivitySource(ValueSource dataSensitivitySource) { this.dataSensitivitySource = dataSensitivitySource; }

    public ValueSource getDataLifetimeYearsSource() { return dataLifetimeYearsSource; }
    public void setDataLifetimeYearsSource(ValueSource dataLifetimeYearsSource) { this.dataLifetimeYearsSource = dataLifetimeYearsSource; }

    public ValueSource getMigrationTimeYearsSource() { return migrationTimeYearsSource; }
    public void setMigrationTimeYearsSource(ValueSource migrationTimeYearsSource) { this.migrationTimeYearsSource = migrationTimeYearsSource; }

    public ValueSource getThreatHorizonYearsSource() { return threatHorizonYearsSource; }
    public void setThreatHorizonYearsSource(ValueSource threatHorizonYearsSource) { this.threatHorizonYearsSource = threatHorizonYearsSource; }
}
