package com.ecdat.backend.dto;

import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Project-level analysis context for quantum migration assessment.
 * This provides the business and operational context required for accurate risk calculation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectAnalysisContext {
    private String applicationName;
    private BusinessCriticality businessCriticality;
    private DataSensitivity dataSensitivity;
    private Integer dataLifetimeYears;
    private Integer migrationTimeYears;
    private Integer threatHorizonYears;

    public ProjectAnalysisContext() {
        // Set sensible defaults but make it clear these are assumptions
        this.businessCriticality = BusinessCriticality.MEDIUM;
        this.dataSensitivity = DataSensitivity.INTERNAL;
        this.dataLifetimeYears = 10;
        this.migrationTimeYears = 3;
        this.threatHorizonYears = 10;
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
    }

    // Getters and Setters
    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }

    public BusinessCriticality getBusinessCriticality() { return businessCriticality; }
    public void setBusinessCriticality(BusinessCriticality businessCriticality) { this.businessCriticality = businessCriticality; }

    public DataSensitivity getDataSensitivity() { return dataSensitivity; }
    public void setDataSensitivity(DataSensitivity dataSensitivity) { this.dataSensitivity = dataSensitivity; }

    public Integer getDataLifetimeYears() { return dataLifetimeYears; }
    public void setDataLifetimeYears(Integer dataLifetimeYears) { this.dataLifetimeYears = dataLifetimeYears; }

    public Integer getMigrationTimeYears() { return migrationTimeYears; }
    public void setMigrationTimeYears(Integer migrationTimeYears) { this.migrationTimeYears = migrationTimeYears; }

    public Integer getThreatHorizonYears() { return threatHorizonYears; }
    public void setThreatHorizonYears(Integer threatHorizonYears) { this.threatHorizonYears = threatHorizonYears; }
}
