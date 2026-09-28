package com.ecdat.backend.provenance;

import com.ecdat.backend.cbom.CBOMCryptoProperties;
import com.ecdat.backend.cbom.CBOMDocument;
import com.ecdat.backend.cbom.CBOMGenerator;
import com.ecdat.backend.pqc.MigrationPriority;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.pqc.PQCRecommendationStatus;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskFactor;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests to verify that provenance information is preserved through the entire analysis pipeline:
 * scanner → finding → risk → recommendation → CBOM
 */
class ProvenancePreservationTest {
    private CBOMGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CBOMGenerator();
    }

    @Test
    void testProvenancePreservedFromFindingToCBOM() {
        // Create a finding with specific provenance values
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setAlgorithmProvenance(Provenance.OBSERVED);
        finding.setKeySize(256);
        finding.setKeySizeProvenance(Provenance.OBSERVED);
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setPurposeProvenance(Provenance.INFERRED);
        finding.setLibrary("Bouncy Castle");
        finding.setLibraryProvenance(Provenance.DEPENDENCY_METADATA);
        finding.setBusinessCriticality(com.ecdat.backend.inventory.BusinessCriticality.HIGH);
        finding.setBusinessCriticalityProvenance(Provenance.USER_PROVIDED);
        finding.setDataSensitivity(com.ecdat.backend.inventory.DataSensitivity.CONFIDENTIAL);
        finding.setDataSensitivityProvenance(Provenance.DEFAULT_ASSUMPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);

        // Create risk assessment
        List<RiskFactor> factors = new ArrayList<>();
        factors.add(new RiskFactor("test_factor", 10, "Test factor"));
        RiskAssessment assessment = new RiskAssessment(10, factors, QuantumRisk.LOW, 
            CryptoFinding.Confidence.HIGH, finding);

        // Generate CBOM
        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        // Verify provenance is preserved in CBOM
        CBOMCryptoProperties cryptoProps = document.getComponents().get(0).getCryptoProperties();
        
        assertEquals(Provenance.OBSERVED, cryptoProps.getAlgorithmProvenance(), 
            "Algorithm provenance should be preserved");
        assertEquals(Provenance.OBSERVED, cryptoProps.getKeySizeProvenance(), 
            "Key size provenance should be preserved");
        assertEquals(Provenance.INFERRED, cryptoProps.getPurposeProvenance(), 
            "Purpose provenance should be preserved");
        assertEquals(Provenance.USER_PROVIDED, cryptoProps.getBusinessCriticalityProvenance(), 
            "Business criticality provenance should be preserved");
        assertEquals(Provenance.DEFAULT_ASSUMPTION, cryptoProps.getDataSensitivityProvenance(), 
            "Data sensitivity provenance should be preserved");
    }

    @Test
    void testProvenanceDefaultsWhenNotSet() {
        // Create a finding without setting provenance explicitly
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setKeySize(2048);
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);

        // Create risk assessment
        List<RiskFactor> factors = new ArrayList<>();
        factors.add(new RiskFactor("test_factor", 10, "Test factor"));
        RiskAssessment assessment = new RiskAssessment(10, factors, QuantumRisk.HIGH, 
            CryptoFinding.Confidence.HIGH, finding);

        // Generate CBOM
        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        // Verify default provenance values
        CBOMCryptoProperties cryptoProps = document.getComponents().get(0).getCryptoProperties();

        assertEquals(Provenance.OBSERVED, cryptoProps.getAlgorithmProvenance(),
            "Algorithm provenance should default to OBSERVED");
        assertEquals(Provenance.OBSERVED, cryptoProps.getKeySizeProvenance(),
            "Key size provenance should default to OBSERVED");
        assertEquals(Provenance.OBSERVED, cryptoProps.getPurposeProvenance(),
            "Purpose provenance should default to OBSERVED");
        assertEquals(Provenance.UNKNOWN, cryptoProps.getBusinessCriticalityProvenance(),
            "Business criticality provenance should default to UNKNOWN when not set");
        assertEquals(Provenance.UNKNOWN, cryptoProps.getDataSensitivityProvenance(),
            "Data sensitivity provenance should default to UNKNOWN when not set");
    }

    @Test
    void testPQCRecommendationProvenancePreserved() {
        // Create a PQC recommendation with provenance
        PQCRecommendation recommendation = new PQCRecommendation(
            PQCRecommendationStatus.RECOMMENDED,
            "RSA",
            CryptoFinding.Purpose.DIGITAL_SIGNATURE,
            "ML-DSA",
            List.of("SLH-DSA"),
            "Quantum-safe alternative",
            MigrationPriority.HIGH,
            QuantumRisk.HIGH,
            CryptoFinding.Confidence.HIGH,
            List.of("Consider hybrid approach")
        );

        recommendation.setRecommendationStatusProvenance(Provenance.INFERRED);
        recommendation.setRecommendedAlgorithmProvenance(Provenance.INFERRED);
        recommendation.setMigrationPriorityProvenance(Provenance.INFERRED);

        // Verify provenance is set
        assertEquals(Provenance.INFERRED, recommendation.getRecommendationStatusProvenance());
        assertEquals(Provenance.INFERRED, recommendation.getRecommendedAlgorithmProvenance());
        assertEquals(Provenance.INFERRED, recommendation.getMigrationPriorityProvenance());
    }

    @Test
    void testAllProvenanceTypesRepresented() {
        // Test that all provenance types can be set and preserved
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setAlgorithmProvenance(Provenance.OBSERVED);
        finding.setKeySize(256);
        finding.setKeySizeProvenance(Provenance.INFERRED);
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setPurposeProvenance(Provenance.DEPENDENCY_METADATA);
        finding.setLibrary("Bouncy Castle");
        finding.setLibraryProvenance(Provenance.USER_PROVIDED);
        finding.setBusinessCriticality(com.ecdat.backend.inventory.BusinessCriticality.MEDIUM);
        finding.setBusinessCriticalityProvenance(Provenance.DEFAULT_ASSUMPTION);
        finding.setDataSensitivity(com.ecdat.backend.inventory.DataSensitivity.INTERNAL);
        finding.setDataSensitivityProvenance(Provenance.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);

        // Verify all provenance types are set
        assertEquals(Provenance.OBSERVED, finding.getAlgorithmProvenance());
        assertEquals(Provenance.INFERRED, finding.getKeySizeProvenance());
        assertEquals(Provenance.DEPENDENCY_METADATA, finding.getPurposeProvenance());
        assertEquals(Provenance.USER_PROVIDED, finding.getLibraryProvenance());
        assertEquals(Provenance.DEFAULT_ASSUMPTION, finding.getBusinessCriticalityProvenance());
        assertEquals(Provenance.UNKNOWN, finding.getDataSensitivityProvenance());
    }

    @Test
    void testProvenanceEnumValues() {
        // Verify all enum values exist
        Provenance[] values = Provenance.values();
        assertEquals(6, values.length);
        
        assertTrue(List.of(values).contains(Provenance.OBSERVED));
        assertTrue(List.of(values).contains(Provenance.INFERRED));
        assertTrue(List.of(values).contains(Provenance.DEPENDENCY_METADATA));
        assertTrue(List.of(values).contains(Provenance.USER_PROVIDED));
        assertTrue(List.of(values).contains(Provenance.DEFAULT_ASSUMPTION));
        assertTrue(List.of(values).contains(Provenance.UNKNOWN));
    }

    @Test
    void testProvenanceBackwardCompatibility() {
        // Test that existing code without provenance still works
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setKeySize(256);
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);

        // Should not throw any exceptions
        assertNotNull(finding.getAlgorithm());
        assertNotNull(finding.getKeySize());
        assertNotNull(finding.getPurpose());
        
        // Provenance should have defaults
        assertEquals(Provenance.OBSERVED, finding.getAlgorithmProvenance());
        assertEquals(Provenance.OBSERVED, finding.getKeySizeProvenance());
        assertEquals(Provenance.OBSERVED, finding.getPurposeProvenance());
    }
}
