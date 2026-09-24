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

    // Test 5 — Confidence preservation
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

    // Test 6 — Multiple algorithms
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

    // Test 7 — Missing optional information
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

    // Test 8 — Empty input
    @Test
    void testEmptyInput() throws IOException {
        CBOMDocument document = generator.generate(new ArrayList<>(), new ArrayList<>());

        assertNotNull(document);
        assertEquals(0, document.getComponents().size());
        assertEquals("CycloneDX", document.getBomFormat());
        assertEquals("1.6", document.getSpecVersion());
    }

    // Test 9 — Determinism
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

    // Test 10 — Negative test (CBOM generation must NOT calculate new risk)
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

    // Test 11 — JSON serialization
    @Test
    void testJsonSerialization() throws IOException {
        CryptoFinding finding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION);
        RiskAssessment assessment = createAssessment(finding, 10, RiskLevel.LOW, QuantumRisk.NONE);

        List<RiskAssessment> assessments = List.of(assessment);
        CBOMDocument document = generator.generate(assessments, new ArrayList<>());

        String json = generator.toJson(document);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
    }

    // Test 12 — Valid JSON structure
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

    // Test 13 — JSON can be parsed as valid JSON
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

    // Test 14 — Mode and padding preservation in CBOM
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
