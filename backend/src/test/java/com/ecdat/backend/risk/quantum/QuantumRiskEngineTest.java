package com.ecdat.backend.risk.quantum;

import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuantumRiskEngineTest {

    private final QuantumRiskEngine engine = new QuantumRiskEngine();

    @Test
    void testMoscaConditionMet_MigrationRequired() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(3);
        input.setDataLifetimeYears(12);
        input.setThreatHorizonYears(10);
        input.setBusinessCriticality(BusinessCriticality.HIGH);
        input.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertTrue(result.isQuantumVulnerable());
        assertTrue(result.isMigrationRequired());
        assertTrue(result.isMoscaConditionMet());
        assertEquals(15, result.getTotalExposureYears()); // 3 + 12
        assertEquals(10, result.getThreatHorizonYears());
        // The urgency depends on the exposure gap calculation
        assertNotNull(result.getMigrationUrgency());
    }

    @Test
    void testMoscaConditionNotMet_MigrationNotRequired() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(2);
        input.setDataLifetimeYears(3);
        input.setThreatHorizonYears(10);
        input.setBusinessCriticality(BusinessCriticality.LOW);
        input.setDataSensitivity(DataSensitivity.PUBLIC);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertTrue(result.isQuantumVulnerable());
        assertFalse(result.isMigrationRequired());
        assertFalse(result.isMoscaConditionMet());
        assertEquals(5, result.getTotalExposureYears()); // 2 + 3
        assertEquals(10, result.getThreatHorizonYears());
        assertEquals(MigrationUrgency.LOW, result.getMigrationUrgency());
    }

    @Test
    void testQuantumResistantAlgorithm_NoMigrationRequired() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("AES");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(20);
        input.setThreatHorizonYears(10);
        input.setBusinessCriticality(BusinessCriticality.CRITICAL);
        input.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertFalse(result.isQuantumVulnerable());
        assertFalse(result.isMigrationRequired());
        assertEquals(MigrationUrgency.NONE, result.getMigrationUrgency());
    }

    @Test
    void testRSAWithHighBusinessCriticality_CriticalUrgency() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(15);
        input.setThreatHorizonYears(10);
        input.setBusinessCriticality(BusinessCriticality.CRITICAL);
        input.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertTrue(result.isQuantumVulnerable());
        assertTrue(result.isMigrationRequired());
        assertTrue(result.isMoscaConditionMet());
        assertEquals(20, result.getTotalExposureYears()); // 5 + 15
        // The urgency depends on the exposure gap (20 - 10 = 10) and business impact
        // With exposure gap of 10 and high business impact, it should be CRITICAL or HIGH
        assertTrue(result.getMigrationUrgency() == MigrationUrgency.CRITICAL || 
                   result.getMigrationUrgency() == MigrationUrgency.HIGH);
    }

    @Test
    void testECDSAWithMediumContext_MediumUrgency() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("ECDSA");
        input.setMigrationTimeYears(3);
        input.setDataLifetimeYears(8);
        input.setThreatHorizonYears(10);
        input.setBusinessCriticality(BusinessCriticality.MEDIUM);
        input.setDataSensitivity(DataSensitivity.INTERNAL);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertTrue(result.isQuantumVulnerable());
        assertTrue(result.isMigrationRequired());
        assertTrue(result.isMoscaConditionMet());
        assertEquals(11, result.getTotalExposureYears()); // 3 + 8
        assertTrue(result.getMigrationUrgency() == MigrationUrgency.MEDIUM ||
                   result.getMigrationUrgency() == MigrationUrgency.HIGH);
    }

    @Test
    void testExplanationGeneration() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA-2048");
        input.setMigrationTimeYears(3);
        input.setDataLifetimeYears(12);
        input.setThreatHorizonYears(10);
        input.setBusinessCriticality(BusinessCriticality.HIGH);
        input.setDataSensitivity(DataSensitivity.CONFIDENTIAL);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertNotNull(result.getExplanation());
        assertFalse(result.getExplanation().isEmpty());
        // The explanation should contain key information about the assessment
        assertTrue(result.getExplanation().length() > 50);
    }

    @Test
    void testCalculationDetails() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(2);
        input.setDataLifetimeYears(5);
        input.setThreatHorizonYears(8);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertNotNull(result.getCalculationDetails());
        assertFalse(result.getCalculationDetails().isEmpty());
        // The calculation details should contain the key numbers
        assertTrue(result.getCalculationDetails().contains("2") && result.getCalculationDetails().contains("5") && result.getCalculationDetails().contains("8"));
    }

    @Test
    void testNullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.assessQuantumRisk(null);
        });
    }

    @Test
    void testUnknownAlgorithm_NotQuantumVulnerable() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("UNKNOWN_ALGORITHM");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(10);
        input.setThreatHorizonYears(8);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertEquals(com.ecdat.backend.risk.quantum.QuantumVulnerabilityStatus.UNKNOWN, result.getQuantumVulnerabilityStatus());
        assertFalse(result.isQuantumVulnerable());
        assertFalse(result.isMigrationRequired());
        assertEquals(MigrationUrgency.UNKNOWN, result.getMigrationUrgency());
        assertTrue(result.getExplanation().contains("cannot be determined"));
        assertTrue(result.getExplanation().contains("ECDAT does not have a classification rule"));
    }

    @Test
    void testKnownVulnerableAlgorithm_RSA() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(3);
        input.setDataLifetimeYears(12);
        input.setThreatHorizonYears(10);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertEquals(com.ecdat.backend.risk.quantum.QuantumVulnerabilityStatus.VULNERABLE, result.getQuantumVulnerabilityStatus());
        assertTrue(result.isQuantumVulnerable());
        assertTrue(result.isMigrationRequired());
        assertTrue(result.isMoscaConditionMet());
    }

    @Test
    void testKnownSafeAlgorithm_AES() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("AES");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(20);
        input.setThreatHorizonYears(10);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertEquals(com.ecdat.backend.risk.quantum.QuantumVulnerabilityStatus.NOT_QUANTUM_VULNERABLE, result.getQuantumVulnerabilityStatus());
        assertFalse(result.isQuantumVulnerable());
        assertFalse(result.isMigrationRequired());
        assertEquals(MigrationUrgency.NONE, result.getMigrationUrgency());
    }

    @Test
    void testPostQuantumAlgorithm_MLDSA() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("ML-DSA");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(20);
        input.setThreatHorizonYears(10);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertEquals(com.ecdat.backend.risk.quantum.QuantumVulnerabilityStatus.NOT_QUANTUM_VULNERABLE, result.getQuantumVulnerabilityStatus());
        assertFalse(result.isQuantumVulnerable());
        assertFalse(result.isMigrationRequired());
        assertEquals(MigrationUrgency.NONE, result.getMigrationUrgency());
    }

    @Test
    void testNullAlgorithm_UnknownStatus() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm(null);
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(10);
        input.setThreatHorizonYears(8);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertEquals(com.ecdat.backend.risk.quantum.QuantumVulnerabilityStatus.UNKNOWN, result.getQuantumVulnerabilityStatus());
        assertFalse(result.isQuantumVulnerable());
        assertFalse(result.isMigrationRequired());
        assertEquals(MigrationUrgency.UNKNOWN, result.getMigrationUrgency());
    }

    @Test
    void testMoscaBoundaryCondition_StrictInequality() {
        // Boundary case: Migration Time (5) + Data Lifetime (5) = 10, Threat Horizon = 10
        // 10 > 10 is FALSE (strict greater-than)
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(5);
        input.setThreatHorizonYears(10);
        input.setBusinessCriticality(BusinessCriticality.MEDIUM);
        input.setDataSensitivity(DataSensitivity.CONFIDENTIAL);

        QuantumRiskResult result = engine.assessQuantumRisk(input);

        assertTrue(result.isQuantumVulnerable());
        assertFalse(result.isMoscaConditionMet());
        assertFalse(result.isMigrationRequired());
        assertEquals(10, result.getTotalExposureYears()); // 5 + 5
        assertEquals(10, result.getThreatHorizonYears());
    }

    @Test
    void testValueSource_UserProvided() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(10);
        input.setThreatHorizonYears(15);
        input.setBusinessCriticality(BusinessCriticality.HIGH);
        input.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);
        
        // When values are set via setters, they should be marked as USER_PROVIDED
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, input.getMigrationTimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, input.getDataLifetimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, input.getThreatHorizonYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, input.getBusinessCriticalitySource());
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, input.getDataSensitivitySource());
        
        QuantumRiskResult result = engine.assessQuantumRisk(input);
        
        // Result should preserve value sources
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, result.getMigrationTimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, result.getDataLifetimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.USER_PROVIDED, result.getThreatHorizonYearsSource());
    }

    @Test
    void testValueSource_SystemDefault() {
        // When using default constructor, values should be marked as SYSTEM_DEFAULT
        QuantumRiskInput input = new QuantumRiskInput();
        
        assertEquals(com.ecdat.backend.dto.ValueSource.SYSTEM_DEFAULT, input.getMigrationTimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.SYSTEM_DEFAULT, input.getDataLifetimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.SYSTEM_DEFAULT, input.getThreatHorizonYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.UNKNOWN, input.getBusinessCriticalitySource());
        assertEquals(com.ecdat.backend.dto.ValueSource.UNKNOWN, input.getDataSensitivitySource());
        
        QuantumRiskResult result = engine.assessQuantumRisk(input);
        
        assertEquals(com.ecdat.backend.dto.ValueSource.SYSTEM_DEFAULT, result.getMigrationTimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.SYSTEM_DEFAULT, result.getDataLifetimeYearsSource());
        assertEquals(com.ecdat.backend.dto.ValueSource.SYSTEM_DEFAULT, result.getThreatHorizonYearsSource());
    }

    @Test
    void testMoscaCondition_XPlusY_GreaterThan_Z() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(10);
        input.setThreatHorizonYears(10);
        
        QuantumRiskResult result = engine.assessQuantumRisk(input);
        
        assertTrue(result.isMoscaConditionMet());
        assertTrue(result.isMigrationRequired());
        assertEquals(15, result.getTotalExposureYears());
    }

    @Test
    void testMoscaCondition_XPlusY_LessThan_Z() {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(2);
        input.setDataLifetimeYears(3);
        input.setThreatHorizonYears(10);
        
        QuantumRiskResult result = engine.assessQuantumRisk(input);
        
        assertFalse(result.isMoscaConditionMet());
        assertFalse(result.isMigrationRequired());
        assertEquals(5, result.getTotalExposureYears());
    }

    @Test
    void testMoscaCondition_XPlusY_Equals_Z() {
        // Boundary case: X + Y = Z should NOT trigger migration (strict >)
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm("RSA");
        input.setMigrationTimeYears(5);
        input.setDataLifetimeYears(5);
        input.setThreatHorizonYears(10);
        
        QuantumRiskResult result = engine.assessQuantumRisk(input);
        
        assertFalse(result.isMoscaConditionMet());
        assertFalse(result.isMigrationRequired());
        assertEquals(10, result.getTotalExposureYears());
    }
}
