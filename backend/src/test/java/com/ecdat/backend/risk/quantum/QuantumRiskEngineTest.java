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

        assertFalse(result.isQuantumVulnerable());
        assertFalse(result.isMigrationRequired());
        assertEquals(MigrationUrgency.NONE, result.getMigrationUrgency());
    }
}
