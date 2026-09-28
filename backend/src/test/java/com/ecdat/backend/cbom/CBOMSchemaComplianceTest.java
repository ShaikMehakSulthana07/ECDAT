package com.ecdat.backend.cbom;

import com.ecdat.backend.pqc.MigrationPriority;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.pqc.PQCRecommendationStatus;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskFactor;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.scanner.CryptoFinding;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests to document and validate CBOM schema compliance status.
 * 
 * IMPORTANT: ECDAT's CBOM implementation is NOT schema-compliant with CycloneDX 1.6.
 * This test suite documents the current structure and validates that it is
 * intentionally CycloneDX-aligned but not schema-compliant.
 * 
 * See CBOM_AUDIT_FINDINGS.md for detailed compatibility analysis.
 */
class CBOMSchemaComplianceTest {
    private CBOMGenerator generator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        generator = new CBOMGenerator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCBOMUsesCycloneDXHeaderFields() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);

        // Standard CycloneDX header fields are present
        assertEquals("CycloneDX", rootNode.get("bomFormat").asText());
        assertEquals("1.6", rootNode.get("specVersion").asText());
        assertTrue(rootNode.has("serialNumber"));
        assertEquals(1, rootNode.get("version").asInt());
        assertTrue(rootNode.has("metadata"));
    }

    @Test
    void testComponentTypeIsNotStandardCycloneDX() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode component = rootNode.get("components").get(0);

        // ECDAT uses custom string "cryptographic-asset" instead of standard classification enum
        String componentType = component.get("type").asText();
        assertEquals("cryptographic-asset", componentType);
        
        // In standard CycloneDX 1.6, this should be CLASSIFICATION_CRYPTOGRAPHIC_ASSET (value 13)
        // This is a documented schema violation - see CBOM_AUDIT_FINDINGS.md
    }

    @Test
    void testCryptoPropertiesStructureIsCustom() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setMode("GCM");
        finding.setPadding("NoPadding");
        finding.setKeySize(256);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode cryptoProps = rootNode.get("components").get(0).get("cryptoProperties");

        // ECDAT uses flat custom structure
        assertTrue(cryptoProps.has("algorithm"));
        assertTrue(cryptoProps.has("mode"));
        assertTrue(cryptoProps.has("padding"));
        assertTrue(cryptoProps.has("purpose"));
        assertTrue(cryptoProps.has("keySize"));

        // Standard CycloneDX 1.6 requires:
        // - assetType (required): algorithm/certificate/protocol/related-crypto-material
        // - algorithmProperties (optional wrapper)
        // - certificateProperties (optional wrapper)
        // - relatedCryptoMaterialProperties (optional wrapper)
        // This is a documented schema violation - see CBOM_AUDIT_FINDINGS.md
    }

    @Test
    void testPaddingEnumValueViolation() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setPadding("NoPadding");
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode cryptoProps = rootNode.get("components").get(0).get("cryptoProperties");

        String padding = cryptoProps.get("padding").asText();
        assertEquals("NoPadding", padding);
        
        // Standard CycloneDX 1.6 padding enum values: pkcs5, pkcs7, pkcs1v15, oaep, raw, other, unknown
        // "NoPadding" is NOT in the standard enum - should be "raw"
        // This is a documented schema violation - see CBOM_AUDIT_FINDINGS.md
    }

    @Test
    void testModeEnumValueCompliance() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setMode("GCM");
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode cryptoProps = rootNode.get("components").get(0).get("cryptoProperties");

        String mode = cryptoProps.get("mode").asText();
        assertEquals("GCM", mode);
        
        // "GCM" is in the standard CycloneDX 1.6 mode enum (gcm)
        // However, it should be lowercase per the spec
        // This is a documented schema violation - see CBOM_AUDIT_FINDINGS.md
    }

    @Test
    void testMissingStandardCycloneDXFields() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode cryptoProps = rootNode.get("components").get(0).get("cryptoProperties");

        // Standard CycloneDX 1.6 fields that ECDAT does not implement:
        assertFalse(cryptoProps.has("primitive")); // Should be: block-cipher, stream-cipher, etc.
        assertFalse(cryptoProps.has("parameterSetIdentifier")); // Should be: "256" for AES-256
        assertFalse(cryptoProps.has("curve")); // For elliptic curves
        assertFalse(cryptoProps.has("executionEnvironment")); // software-plain-ram, hardware, etc.
        assertFalse(cryptoProps.has("implementationPlatform")); // x86_64, armv8-a, etc.
        assertFalse(cryptoProps.has("certificationLevel")); // FIPS 140-3, CC-EAL, etc.
        assertFalse(cryptoProps.has("cryptoFunctions")); // Array: encrypt, decrypt, sign, etc.
        assertFalse(cryptoProps.has("classicalSecurityLevel")); // Bits
        assertFalse(cryptoProps.has("nistQuantumSecurityLevel")); // 0-6
        assertFalse(cryptoProps.has("assetType")); // Required field in standard spec
        
        // These are documented gaps - see CBOM_AUDIT_FINDINGS.md
    }

    @Test
    void testECDATSpecificFieldsInCryptoProperties() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setFile("src/main/java/CryptoUtil.java");
        finding.setLine(42);
        finding.setEvidence("Cipher.getInstance(\"AES\")");
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode cryptoProps = rootNode.get("components").get(0).get("cryptoProperties");

        // ECDAT-specific fields that should be in properties array with ecdat: prefix
        assertTrue(cryptoProps.has("sourceFile"));
        assertTrue(cryptoProps.has("sourceLine"));
        assertTrue(cryptoProps.has("evidence"));
        assertTrue(cryptoProps.has("confidence"));
        assertTrue(cryptoProps.has("sourceType"));
        assertTrue(cryptoProps.has("risk"));
        
        // These are ECDAT extensions - see CBOM_AUDIT_FINDINGS.md
    }

    @Test
    void testNoteFieldDocumentsNonCompliance() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);

        assertTrue(rootNode.has("note"));
        String note = rootNode.get("note").asText();
        assertTrue(note.contains("not schema-compliant"));
        assertTrue(note.contains("CBOM_AUDIT_FINDINGS.md"));
    }

    @Test
    void testECDATPropertiesArrayExists() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode component = rootNode.get("components").get(0);

        assertTrue(component.has("properties"));
        JsonNode properties = component.get("properties");
        assertTrue(properties.isArray());
        
        // Check for standard ECDAT properties
        boolean foundRiskVersion = false;
        boolean foundConfidencePreserved = false;
        for (JsonNode prop : properties) {
            String name = prop.get("name").asText();
            if ("ecdat:risk_assessment_version".equals(name)) {
                foundRiskVersion = true;
            }
            if ("ecdat:confidence_preserved".equals(name)) {
                foundConfidencePreserved = true;
            }
        }
        assertTrue(foundRiskVersion);
        assertTrue(foundConfidencePreserved);
    }

    @Test
    void testDeterministicComponentNames() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setFile("src/main/java/CryptoUtil.java");
        finding.setLine(42);

        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document1 = generator.generate(assessments, new ArrayList<>());
        CBOMDocument document2 = generator.generate(assessments, new ArrayList<>());

        String name1 = document1.getComponents().get(0).getName();
        String name2 = document2.getComponents().get(0).getName();

        assertEquals(name1, name2, "Component names should be deterministic");
    }

    @Test
    void testSerialNumberIsRandomPerDocument() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document1 = generator.generate(assessments, new ArrayList<>());
        CBOMDocument document2 = generator.generate(assessments, new ArrayList<>());

        String serial1 = document1.getSerialNumber();
        String serial2 = document2.getSerialNumber();

        assertNotEquals(serial1, serial2, "Serial numbers should be unique per document");
        assertTrue(serial1.startsWith("urn:uuid:"));
        assertTrue(serial2.startsWith("urn:uuid:"));
    }

    // Helper methods
    private CryptoFinding createFinding(String algorithm, CryptoFinding.Purpose purpose) {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm(algorithm);
        finding.setPurpose(purpose);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");
        return finding;
    }

    private RiskAssessment createAssessment(CryptoFinding finding, int score, RiskLevel level, QuantumRisk quantumRisk) {
        List<RiskFactor> factors = new ArrayList<>();
        factors.add(new RiskFactor("test_factor", score, "Test risk factor"));
        return new RiskAssessment(score, factors, quantumRisk, finding.getConfidence(), finding);
    }
}
