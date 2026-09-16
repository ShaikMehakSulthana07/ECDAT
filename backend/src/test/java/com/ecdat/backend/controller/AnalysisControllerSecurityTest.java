package com.ecdat.backend.controller;

import com.ecdat.backend.dto.AnalyzeRequest;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisControllerSecurityTest {

    @Test
    void testAnalyzeRequestValidation() {
        // Test that AnalyzeRequest properly handles null path
        AnalyzeRequest request = new AnalyzeRequest();
        assertNull(request.getPath());
        
        request.setPath("/some/path");
        assertEquals("/some/path", request.getPath());
    }

    @Test
    void testProjectAnalysisContextDefaults() {
        // Test that context has sensible defaults
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        
        assertNotNull(context.getBusinessCriticality());
        assertEquals(BusinessCriticality.MEDIUM, context.getBusinessCriticality());
        
        assertNotNull(context.getDataSensitivity());
        assertEquals(DataSensitivity.INTERNAL, context.getDataSensitivity());
        
        assertEquals(10, context.getDataLifetimeYears());
        assertEquals(3, context.getMigrationTimeYears());
        assertEquals(10, context.getThreatHorizonYears());
    }

    @Test
    void testProjectAnalysisContextCustomValues() {
        // Test that context accepts custom values
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setApplicationName("TestApp");
        context.setBusinessCriticality(BusinessCriticality.CRITICAL);
        context.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);
        context.setDataLifetimeYears(15);
        context.setMigrationTimeYears(5);
        context.setThreatHorizonYears(12);
        
        assertEquals("TestApp", context.getApplicationName());
        assertEquals(BusinessCriticality.CRITICAL, context.getBusinessCriticality());
        assertEquals(DataSensitivity.HIGHLY_SENSITIVE, context.getDataSensitivity());
        assertEquals(15, context.getDataLifetimeYears());
        assertEquals(5, context.getMigrationTimeYears());
        assertEquals(12, context.getThreatHorizonYears());
    }

    @Test
    void testProjectAnalysisContextConstructor() {
        // Test context constructor with all values
        ProjectAnalysisContext context = new ProjectAnalysisContext(
            "TestApp",
            BusinessCriticality.HIGH,
            DataSensitivity.CONFIDENTIAL,
            20,
            4,
            15
        );
        
        assertEquals("TestApp", context.getApplicationName());
        assertEquals(BusinessCriticality.HIGH, context.getBusinessCriticality());
        assertEquals(DataSensitivity.CONFIDENTIAL, context.getDataSensitivity());
        assertEquals(20, context.getDataLifetimeYears());
        assertEquals(4, context.getMigrationTimeYears());
        assertEquals(15, context.getThreatHorizonYears());
    }

    @Test
    void testProjectAnalysisContextNullHandling() {
        // Test that constructor handles null values gracefully
        ProjectAnalysisContext context = new ProjectAnalysisContext(
            null,
            null,
            null,
            null,
            null,
            null
        );
        
        // Should use defaults when null is provided
        assertEquals(BusinessCriticality.MEDIUM, context.getBusinessCriticality());
        assertEquals(DataSensitivity.INTERNAL, context.getDataSensitivity());
        assertEquals(10, context.getDataLifetimeYears());
        assertEquals(3, context.getMigrationTimeYears());
        assertEquals(10, context.getThreatHorizonYears());
    }

    @Test
    void testAnalyzeRequestWithContext() {
        // Test that AnalyzeRequest can hold context
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setApplicationName("SecureApp");
        context.setBusinessCriticality(BusinessCriticality.CRITICAL);
        
        AnalyzeRequest request = new AnalyzeRequest();
        request.setPath("/secure/path");
        request.setContext(context);
        
        assertEquals("/secure/path", request.getPath());
        assertNotNull(request.getContext());
        assertEquals("SecureApp", request.getContext().getApplicationName());
        assertEquals(BusinessCriticality.CRITICAL, request.getContext().getBusinessCriticality());
    }

    @Test
    void testBusinessCriticalityEnumValues() {
        // Test all enum values are accessible
        BusinessCriticality[] values = BusinessCriticality.values();
        assertEquals(5, values.length); // LOW, MEDIUM, HIGH, CRITICAL, UNKNOWN
        
        assertTrue(java.util.Arrays.asList(values).contains(BusinessCriticality.LOW));
        assertTrue(java.util.Arrays.asList(values).contains(BusinessCriticality.MEDIUM));
        assertTrue(java.util.Arrays.asList(values).contains(BusinessCriticality.HIGH));
        assertTrue(java.util.Arrays.asList(values).contains(BusinessCriticality.CRITICAL));
        assertTrue(java.util.Arrays.asList(values).contains(BusinessCriticality.UNKNOWN));
    }

    @Test
    void testDataSensitivityEnumValues() {
        // Test all enum values are accessible
        DataSensitivity[] values = DataSensitivity.values();
        assertEquals(5, values.length); // PUBLIC, INTERNAL, CONFIDENTIAL, HIGHLY_SENSITIVE, UNKNOWN
        
        assertTrue(java.util.Arrays.asList(values).contains(DataSensitivity.PUBLIC));
        assertTrue(java.util.Arrays.asList(values).contains(DataSensitivity.INTERNAL));
        assertTrue(java.util.Arrays.asList(values).contains(DataSensitivity.CONFIDENTIAL));
        assertTrue(java.util.Arrays.asList(values).contains(DataSensitivity.HIGHLY_SENSITIVE));
        assertTrue(java.util.Arrays.asList(values).contains(DataSensitivity.UNKNOWN));
    }
}
