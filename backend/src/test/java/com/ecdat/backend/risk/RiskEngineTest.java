package com.ecdat.backend.risk;

import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RiskEngineTest {

    private final RiskEngine riskEngine = new RiskEngine();

    @Test
    void testRSA2048() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-2048");
        finding.setKeySize(2048);
        finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.HIGH, assessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, assessment.getQuantumRisk());
        assertEquals(CryptoFinding.Confidence.HIGH, assessment.getConfidence());
        assertTrue(assessment.getRiskScore() >= 50 && assessment.getRiskScore() <= 74);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("public-key") || r.contains("quantum")));
        
        assertTrue(assessment.getFactors().stream().anyMatch(f -> 
            f.getName().equals("QUANTUM_VULNERABILITY")));
    }

    @Test
    void testRSAUnknownKeySize() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA");
        finding.setKeySize(null);
        finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.HIGH, assessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() >= 50);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("unknown") || r.contains("key size")));
    }

    @Test
    void testRSA1024() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-1024");
        finding.setKeySize(1024);
        finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.HIGH, assessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() >= 50);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("1024") || r.contains("below")));
    }

    @Test
    void testECDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDSA");
        finding.setVariant("SHA256withECDSA");
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Signature.getInstance(\"SHA256withECDSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.HIGH, assessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() >= 50 && assessment.getRiskScore() <= 74);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("public-key") || r.contains("digital-signature")));
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("post-quantum")));
    }

    @Test
    void testECDH() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDH");
        finding.setVariant("ECDH");
        finding.setPurpose(CryptoFinding.Purpose.KEY_AGREEMENT);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyAgreement.getInstance(\"ECDH\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.HIGH, assessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() >= 50 && assessment.getRiskScore() <= 74);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("key-agreement") || r.contains("public-key")));
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("post-quantum")));
    }

    @Test
    void testAES256() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setVariant("AES-256");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(\"AES-256\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
        assertEquals(QuantumRisk.LOW, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() <= 24);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("symmetric")));
    }

    @Test
    void testAES128() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setVariant("AES/128");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(\"AES/128\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
        assertEquals(QuantumRisk.LOW, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() <= 24);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("symmetric")));
    }

    @Test
    void testSHA256() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("SHA-256");
        finding.setVariant("SHA-256");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"SHA-256\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
        assertEquals(QuantumRisk.LOW, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() <= 24);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("modern") || r.contains("hash")));
    }

    @Test
    void testSHA512() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("SHA-512");
        finding.setVariant("SHA-512");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"SHA-512\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
        assertEquals(QuantumRisk.LOW, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() <= 24);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("modern") || r.contains("hash")));
    }

    @Test
    void testSHA1() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("SHA-1");
        finding.setVariant("SHA-1");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"SHA-1\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.HIGH, assessment.getRiskLevel());
        assertEquals(QuantumRisk.NONE, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() >= 50 && assessment.getRiskScore() <= 74);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("weak") || r.contains("deprecated")));
        
        assertFalse(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("quantum") && r.contains("primary")));
    }

    @Test
    void testMD5() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("MD5");
        finding.setVariant("MD5");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"MD5\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.CRITICAL, assessment.getRiskLevel());
        assertEquals(QuantumRisk.NONE, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() >= 75);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("broken") || r.contains("collision")));
        
        assertFalse(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("quantum") && r.contains("main")));
    }

    @Test
    void testTLS13() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("TLS");
        finding.setVariant("TLSv1.3");
        finding.setPurpose(CryptoFinding.Purpose.PROTOCOL);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("SSLContext.getInstance(\"TLSv1.3\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
        assertEquals(QuantumRisk.NONE, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() <= 24);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("TLS") || r.contains("1.3")));
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("limited")));
    }

    @Test
    void testTLS12() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("TLS");
        finding.setVariant("TLSv1.2");
        finding.setPurpose(CryptoFinding.Purpose.PROTOCOL);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("SSLContext.getInstance(\"TLSv1.2\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
        assertEquals(QuantumRisk.NONE, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() <= 24);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("TLS") || r.contains("1.2")));
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("limited")));
    }

    @Test
    void testUnknownAlgorithm() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setVariant("UNKNOWN");
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(variable)");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.MEDIUM, assessment.getRiskLevel());
        assertEquals(QuantumRisk.NONE, assessment.getQuantumRisk());
        assertTrue(assessment.getRiskScore() >= 25 && assessment.getRiskScore() <= 49);
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("algorithm could not be resolved")));
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("low-confidence")));
    }

    @Test
    void testLowConfidenceFinding() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA");
        finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyPairGenerator.getInstance(variable)");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(CryptoFinding.Confidence.LOW, assessment.getConfidence());
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("low-confidence")));
    }

    @Test
    void testRSADigitalSignature() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-2048");
        finding.setKeySize(2048);
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Signature.getInstance(\"SHA256withRSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertEquals(RiskLevel.HIGH, assessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, assessment.getQuantumRisk());
        
        assertTrue(assessment.getReasons().stream().anyMatch(r -> 
            r.contains("digital") || r.contains("signature")));
    }

    @Test
    void testScoreCapping() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("MD5");
        finding.setVariant("MD5");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"MD5\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertTrue(assessment.getRiskScore() <= 100, 
            "Risk score should be capped at 100");
    }

    @Test
    void testNegativeScoreFactors() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setVariant("AES-256");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(\"AES-256\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);

        assertTrue(assessment.getRiskScore() >= 0, 
            "Risk score should not be negative");
    }
}
