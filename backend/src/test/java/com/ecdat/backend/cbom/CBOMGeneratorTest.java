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

class CBOMGeneratorTest {
    private CBOMGenerator generator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        generator = new CBOMGenerator();
        objectMapper = new ObjectMapper();
    }

    // Test 1 — Single finding
    @Test
    void testSingleFindingGeneratesOneComponent() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        assertNotNull(document);
        assertEquals(1, document.getComponents().size());
        assertEquals("CycloneDX", document.getBomFormat());
        assertEquals("1.6", document.getSpecVersion());
    }

    // Test 2 — Risk integration
    @Test
    void testRiskIntegration() throws IOException {
        CryptoFinding finding = createFinding("RSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        RiskAssessment assessment = createAssessment(finding, 52, RiskLevel.HIGH, QuantumRisk.HIGH);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        CBOMComponent component = document.getComponents().get(0);
        CBOMRiskInfo riskInfo = component.getCryptoProperties().getRisk();

        assertNotNull(riskInfo);
        assertEquals("HIGH", riskInfo.getRiskLevel());
        assertEquals(52, riskInfo.getRiskScore());
        assertEquals("HIGH", riskInfo.getQuantumRisk());
        assertFalse(riskInfo.getRiskFactors().isEmpty());
    }

    // Test 3 — PQC integration
    @Test
    void testPQCIntegration() throws IOException {
        CryptoFinding finding = createFinding("RSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        RiskAssessment assessment = createAssessment(finding, 52, RiskLevel.HIGH, QuantumRisk.HIGH);
        PQCRecommendation recommendation = createRecommendation("RSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE,
            "ML-DSA", List.of("SLH-DSA"), PQCRecommendationStatus.RECOMMENDED, MigrationPriority.HIGH);

        List<RiskAssessment> assessments = List.of(assessment);
        List<PQCRecommendation> recommendations = List.of(recommendation);
        CBOMDocument document = generator.generate(assessments, recommendations);

        CBOMComponent component = document.getComponents().get(0);
        CBOMPQCInfo pqcInfo = component.getCryptoProperties().getPqcRecommendation();

        assertNotNull(pqcInfo);
        assertEquals("RECOMMENDED", pqcInfo.getRecommendationStatus());
        assertEquals("ML-DSA", pqcInfo.getRecommendedAlgorithm());
        assertEquals(List.of("SLH-DSA"), pqcInfo.getAlternativeAlgorithms());
        assertEquals("HIGH", pqcInfo.getMigrationPriority());
    }

    // Test 4 — Evidence preservation
    @Test
    void testEvidencePreservation() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setFile("src/main/java/com/example/CryptoUtil.java");
        finding.setLine(42);
        finding.setEvidence("Cipher.getInstance(\"AES\")");

        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        CBOMComponent component = document.getComponents().get(0);

        assertEquals("src/main/java/com/example/CryptoUtil.java", component.getCryptoProperties().getSourceFile());
        assertEquals(42, component.getCryptoProperties().getSourceLine());
        assertEquals("Cipher.getInstance(\"AES\")", component.getCryptoProperties().getEvidence());
    }

    // Test 5 — JSON serialization and structure
    @Test
    void testJsonSerialization() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setFile("src/main/java/com/example/CryptoUtil.java");
        finding.setLine(42);
        finding.setEvidence("Cipher.getInstance(\"AES/GCM/NoPadding\")");
        finding.setKeySize(256);
        finding.setMode("GCM");
        finding.setPadding("NoPadding");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        RiskAssessment assessment = createAssessment(finding, 18, RiskLevel.LOW, QuantumRisk.LOW);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        
        // Verify JSON structure
        JsonNode rootNode = objectMapper.readTree(json);
        
        assertEquals("CycloneDX", rootNode.get("bomFormat").asText());
        assertEquals("1.6", rootNode.get("specVersion").asText());
        assertTrue(rootNode.has("serialNumber"));
        assertEquals(1, rootNode.get("version").asInt());
        assertTrue(rootNode.has("metadata"));
        assertTrue(rootNode.has("components"));
        
        JsonNode components = rootNode.get("components");
        assertEquals(1, components.size());
        
        JsonNode component = components.get(0);
        assertEquals("cryptographic-asset", component.get("type").asText());
        assertTrue(component.has("name"));
        assertTrue(component.has("description"));
        assertTrue(component.has("cryptoProperties"));
        assertTrue(component.has("properties"));
        
        JsonNode cryptoProps = component.get("cryptoProperties");
        assertEquals("AES", cryptoProps.get("algorithm").asText());
        assertEquals("ENCRYPTION", cryptoProps.get("purpose").asText());
        assertEquals(256, cryptoProps.get("keySize").asInt());
        assertEquals("GCM", cryptoProps.get("mode").asText());
        assertEquals("NoPadding", cryptoProps.get("padding").asText());
        assertEquals("src/main/java/com/example/CryptoUtil.java", cryptoProps.get("sourceFile").asText());
        assertEquals(42, cryptoProps.get("sourceLine").asInt());
        assertEquals("Cipher.getInstance(\"AES/GCM/NoPadding\")", cryptoProps.get("evidence").asText());
        assertEquals("HIGH", cryptoProps.get("confidence").asText());
        assertEquals("JAVA_AST", cryptoProps.get("sourceType").asText());
        
        JsonNode riskInfo = cryptoProps.get("risk");
        assertNotNull(riskInfo);
        assertEquals("LOW", riskInfo.get("riskLevel").asText());
        assertEquals(18, riskInfo.get("riskScore").asInt());
        assertEquals("LOW", riskInfo.get("quantumRisk").asText());
        assertTrue(riskInfo.has("riskFactors"));
        
        // Verify ECDAT-specific properties
        JsonNode properties = component.get("properties");
        assertTrue(properties.isArray());
        
        boolean foundRiskVersion = false;
        boolean foundConfidencePreserved = false;
        for (JsonNode prop : properties) {
            String name = prop.get("name").asText();
            if ("ecdat:risk_assessment_version".equals(name)) {
                foundRiskVersion = true;
                assertEquals("prototype", prop.get("value").asText());
            }
            if ("ecdat:confidence_preserved".equals(name)) {
                foundConfidencePreserved = true;
                assertEquals("true", prop.get("value").asText());
            }
        }
        assertTrue(foundRiskVersion, "ecdat:risk_assessment_version property not found");
        assertTrue(foundConfidencePreserved, "ecdat:confidence_preserved property not found");
    }

    // Test 6 — Deterministic component names
    @Test
    void testDeterministicComponentNames() throws IOException {
        CryptoFinding finding1 = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding1.setFile("src/main/java/com/example/CryptoUtil.java");
        finding1.setLine(42);
        
        CryptoFinding finding2 = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding2.setFile("src/main/java/com/example/CryptoUtil.java");
        finding2.setLine(42);
        
        RiskAssessment assessment1 = createAssessment(finding1, 10, RiskLevel.LOW, QuantumRisk.NONE);
        RiskAssessment assessment2 = createAssessment(finding2, 10, RiskLevel.LOW, QuantumRisk.NONE);
        
        CBOMDocument document1 = generator.generate(List.of(assessment1), new ArrayList<>());
        CBOMDocument document2 = generator.generate(List.of(assessment2), new ArrayList<>());
        
        String name1 = document1.getComponents().get(0).getName();
        String name2 = document2.getComponents().get(0).getName();
        
        assertEquals(name1, name2, "Component names should be deterministic for identical findings");
    }

    // Test 7 — Unknown quantum status handling
    @Test
    void testUnknownQuantumStatusHandling() throws IOException {
        CryptoFinding finding = createFinding("UNKNOWN", CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        
        RiskAssessment assessment = createAssessment(finding, 0, RiskLevel.LOW, QuantumRisk.NONE);
        
        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());
        
        String json = generator.toJson(document);
        JsonNode rootNode = objectMapper.readTree(json);
        
        JsonNode component = rootNode.get("components").get(0);
        JsonNode cryptoProps = component.get("cryptoProperties");
        
        assertEquals("UNKNOWN", cryptoProps.get("algorithm").asText());
        assertEquals("UNKNOWN", cryptoProps.get("purpose").asText());
        assertEquals("LOW", cryptoProps.get("confidence").asText());
        
        // Should not have quantum risk info for unknown algorithms
        JsonNode riskInfo = cryptoProps.get("risk");
        assertNotNull(riskInfo);
        // quantumRisk should be present but may be NONE for unknown
        assertTrue(riskInfo.has("quantumRisk"));
    }

    // Test 8 — Confidence preservation
    @Test
    void testConfidencePreservation() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);

        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        CBOMComponent component = document.getComponents().get(0);
        assertEquals("HIGH", component.getCryptoProperties().getConfidence());
    }

    // Test 9 — Multiple algorithms
    @Test
    void testMultipleAlgorithms() throws IOException {
        List<CryptoFinding> findings = List.of(
            createFinding("AES", CryptoFinding.Purpose.ENCRYPTION),
            createFinding("RSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE),
            createFinding("ECDSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE),
            createFinding("ECDH", CryptoFinding.Purpose.KEY_AGREEMENT),
            createFinding("SHA-256", CryptoFinding.Purpose.HASHING),
            createFinding("SHA-1", CryptoFinding.Purpose.HASHING),
            createFinding("MD5", CryptoFinding.Purpose.HASHING),
            createFinding("TLS", CryptoFinding.Purpose.PROTOCOL)
        );

        List<RiskAssessment> assessments = new ArrayList<>();
        for (CryptoFinding finding : findings) {
            assessments.add(createAssessment(finding, 25, RiskLevel.MEDIUM, QuantumRisk.LOW));
        }

        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        assertEquals(8, document.getComponents().size());
    }

    // Test 10 — Missing optional information
    @Test
    void testMissingOptionalInformation() throws IOException {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);

        RiskAssessment assessment = createAssessment(finding, 0, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        assertNotNull(document);
        assertEquals(1, document.getComponents().size());
    }

    // Test 11 — Empty input
    @Test
    void testEmptyInput() throws IOException {
        CBOMDocument document = generator.generate(new ArrayList<>(), new ArrayList<>());

        assertNotNull(document);
        assertEquals(0, document.getComponents().size());
        assertEquals("CycloneDX", document.getBomFormat());
        assertEquals("1.6", document.getSpecVersion());
    }

    // Test 12 — Determinism
    @Test
    void testDeterminism() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);

        CBOMDocument document1 = generator.generate(assessments, new ArrayList<>());
        String json1 = generator.toJson(document1);

        CBOMDocument document2 = generator.generate(assessments, new ArrayList<>());
        String json2 = generator.toJson(document2);

        JsonNode node1 = objectMapper.readTree(json1);
        JsonNode node2 = objectMapper.readTree(json2);

        // Compare all fields except serialNumber (which is intentionally unique per generation)
        assertEquals(node1.get("bomFormat"), node2.get("bomFormat"));
        assertEquals(node1.get("specVersion"), node2.get("specVersion"));
        assertEquals(node1.get("version"), node2.get("version"));
        assertEquals(node1.get("components"), node2.get("components"));
        assertEquals(node1.get("metadata"), node2.get("metadata"));
    }

    // Test 13 — Negative test (CBOM generation must NOT calculate new risk)
    @Test
    void testCBOMDoesNotCalculateNewRisk() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        CBOMComponent component = document.getComponents().get(0);
        CBOMRiskInfo riskInfo = component.getCryptoProperties().getRisk();

        // Verify the risk score is exactly what was provided, not recalculated
        assertEquals(10, riskInfo.getRiskScore());
        assertEquals("LOW", riskInfo.getRiskLevel());
    }

    // Test 14 — JSON serialization
    @Test
    void testJsonSerializationBasic() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
    }

    // Test 15 — Valid JSON structure
    @Test
    void testValidJsonStructure() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        JsonNode root = objectMapper.readTree(json);

        assertTrue(root.has("bomFormat"));
        assertTrue(root.has("specVersion"));
        assertTrue(root.has("serialNumber"));
        assertTrue(root.has("version"));
        assertTrue(root.has("components"));
        assertTrue(root.has("metadata"));
    }

    // Test 16 — JSON can be parsed as valid JSON
    @Test
    void testJsonCanBeParsed() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);

        // Verify it's valid JSON by parsing it
        JsonNode root = objectMapper.readTree(json);
        assertNotNull(root);
    }

    // Test 17 — Mode and padding preservation in CBOM
    @Test
    void testModeAndPaddingInCBOM() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        finding.setMode("GCM");
        finding.setPadding("NoPadding");
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        CBOMComponent component = document.getComponents().get(0);
        assertEquals("GCM", component.getCryptoProperties().getMode());
        assertEquals("NoPadding", component.getCryptoProperties().getPadding());

        String json = generator.toJson(document);
        JsonNode root = objectMapper.readTree(json);
        JsonNode cryptoProps = root.get("components").get(0).get("cryptoProperties");
        assertEquals("GCM", cryptoProps.get("mode").asText());
        assertEquals("NoPadding", cryptoProps.get("padding").asText());
    }

    // Test 18 — Quantum vulnerability status serialization for UNKNOWN
    @Test
    void testQuantumVulnerabilityStatusUnknownSerialization() throws IOException {
        CryptoFinding finding = createFinding("UNKNOWN_ALGORITHM", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);
        
        // Create a quantum risk result with UNKNOWN status
        com.ecdat.backend.risk.quantum.QuantumRiskResult quantumResult = 
            new com.ecdat.backend.risk.quantum.QuantumRiskResult();
        quantumResult.setAlgorithm("UNKNOWN_ALGORITHM");
        quantumResult.setQuantumVulnerabilityStatus(com.ecdat.backend.risk.quantum.QuantumVulnerabilityStatus.UNKNOWN);
        quantumResult.setQuantumVulnerable(false);
        quantumResult.setMigrationRequired(false);
        quantumResult.setMigrationUrgency(com.ecdat.backend.risk.quantum.MigrationUrgency.UNKNOWN);
        quantumResult.setExplanation("Quantum vulnerability cannot be determined.");
        assessment.setQuantumRiskResult(quantumResult);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        CBOMComponent component = document.getComponents().get(0);
        CBOMRiskInfo riskInfo = component.getCryptoProperties().getRisk();

        assertNotNull(riskInfo);
        assertEquals("UNKNOWN", riskInfo.getQuantumVulnerabilityStatus());
        assertEquals(Boolean.FALSE, riskInfo.getQuantumVulnerable());
        assertEquals("UNKNOWN", riskInfo.getMigrationUrgency());
        assertTrue(riskInfo.getQuantumRiskExplanation().contains("cannot be determined"));

        // Verify JSON serialization
        String json = generator.toJson(document);
        JsonNode root = objectMapper.readTree(json);
        JsonNode cryptoProps = root.get("components").get(0).get("cryptoProperties");
        JsonNode riskNode = cryptoProps.get("risk");
        assertEquals("UNKNOWN", riskNode.get("quantumVulnerabilityStatus").asText());
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

    private PQCRecommendation createRecommendation(String algorithm, CryptoFinding.Purpose purpose,
                                                   String recommended, List<String> alternatives,
                                                   PQCRecommendationStatus status, MigrationPriority priority) {
        return new PQCRecommendation(
            status,
            algorithm,
            purpose,
            recommended,
            alternatives,
            "Test rationale",
            priority,
            QuantumRisk.HIGH,
            CryptoFinding.Confidence.HIGH,
            List.of("Test consideration")
        );
    }
}
