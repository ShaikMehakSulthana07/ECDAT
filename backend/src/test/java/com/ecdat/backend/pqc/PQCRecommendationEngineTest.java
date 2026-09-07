package com.ecdat.backend.pqc;

import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskEngine;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PQCRecommendationEngineTest {

    private final PQCRecommendationEngine pqcEngine = new PQCRecommendationEngine();
    private final RiskEngine riskEngine = new RiskEngine();

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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.RECOMMENDED, recommendation.getRecommendationStatus());
        assertEquals("RSA", recommendation.getCurrentAlgorithm());
        assertEquals(CryptoFinding.Purpose.DIGITAL_SIGNATURE, recommendation.getCurrentPurpose());
        assertEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getAlternativeAlgorithms().contains("SLH-DSA"));
        assertEquals(MigrationPriority.HIGH, recommendation.getMigrationPriority());
        assertEquals(QuantumRisk.HIGH, recommendation.getQuantumRisk());
        assertEquals(CryptoFinding.Confidence.HIGH, recommendation.getConfidence());
        assertTrue(recommendation.getRationale().contains("digital"));
        assertTrue(recommendation.getRationale().contains("ML-DSA"));
        assertTrue(recommendation.getRationale().contains("quantum"));
        assertFalse(recommendation.getRationale().contains("broken"));
    }

    @Test
    void testRSAKeyAgreement() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-2048");
        finding.setKeySize(2048);
        finding.setPurpose(CryptoFinding.Purpose.KEY_AGREEMENT);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyAgreement.getInstance(\"RSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
        assertEquals("RSA", recommendation.getCurrentAlgorithm());
        assertEquals(CryptoFinding.Purpose.KEY_AGREEMENT, recommendation.getCurrentPurpose());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getAlternativeAlgorithms().contains("ML-KEM (if applicable)"));
        assertTrue(recommendation.getRationale().toLowerCase().contains("further usage analysis"));
        assertTrue(recommendation.getRationale().toLowerCase().contains("key establishment"));
    }

    @Test
    void testRSAEncryption() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-2048");
        finding.setKeySize(2048);
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(\"RSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
        assertEquals("RSA", recommendation.getCurrentAlgorithm());
        assertEquals(CryptoFinding.Purpose.ENCRYPTION, recommendation.getCurrentPurpose());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getRationale().toLowerCase().contains("further usage analysis"));
    }

    @Test
    void testRSAUnknownPurpose() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-2048");
        finding.setKeySize(2048);
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getRationale().contains("sufficient confidence"));
    }

    @Test
    void testECDSADigitalSignature() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDSA");
        finding.setVariant("SHA256withECDSA");
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Signature.getInstance(\"SHA256withECDSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.RECOMMENDED, recommendation.getRecommendationStatus());
        assertEquals("ECDSA", recommendation.getCurrentAlgorithm());
        assertEquals(CryptoFinding.Purpose.DIGITAL_SIGNATURE, recommendation.getCurrentPurpose());
        assertEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getAlternativeAlgorithms().contains("SLH-DSA"));
        assertEquals(MigrationPriority.HIGH, recommendation.getMigrationPriority());
        assertEquals(QuantumRisk.HIGH, recommendation.getQuantumRisk());
        assertTrue(recommendation.getRationale().contains("ECDSA"));
        assertTrue(recommendation.getRationale().contains("ML-DSA"));
        assertTrue(recommendation.getRationale().contains("digital-signature"));
    }

    @Test
    void testECDHKeyAgreement() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDH");
        finding.setVariant("ECDH");
        finding.setPurpose(CryptoFinding.Purpose.KEY_AGREEMENT);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyAgreement.getInstance(\"ECDH\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.RECOMMENDED, recommendation.getRecommendationStatus());
        assertEquals("ECDH", recommendation.getCurrentAlgorithm());
        assertEquals(CryptoFinding.Purpose.KEY_AGREEMENT, recommendation.getCurrentPurpose());
        assertEquals("ML-KEM", recommendation.getRecommendedAlgorithm());
        assertEquals(MigrationPriority.HIGH, recommendation.getMigrationPriority());
        assertEquals(QuantumRisk.HIGH, recommendation.getQuantumRisk());
        assertTrue(recommendation.getRationale().contains("ECDH"));
        assertTrue(recommendation.getRationale().contains("ML-KEM"));
        assertTrue(recommendation.getRationale().contains("key-agreement"));
        assertTrue(recommendation.getRationale().contains("key-establishment"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("encryption"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NOT_REQUIRED, recommendation.getRecommendationStatus());
        assertEquals("AES", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertEquals(MigrationPriority.LOW, recommendation.getMigrationPriority());
        assertTrue(recommendation.getRationale().contains("AES-256"));
        assertTrue(recommendation.getRationale().contains("No PQC replacement"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.CONDITIONAL, recommendation.getRecommendationStatus());
        assertEquals("AES", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getAlternativeAlgorithms().contains("AES-256"));
        assertEquals(MigrationPriority.MEDIUM, recommendation.getMigrationPriority());
        assertTrue(recommendation.getRationale().contains("AES-128"));
        assertTrue(recommendation.getRationale().contains("quantum"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NOT_REQUIRED, recommendation.getRecommendationStatus());
        assertEquals("SHA-256", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertEquals(MigrationPriority.LOW, recommendation.getMigrationPriority());
        assertTrue(recommendation.getRationale().contains("SHA-256"));
        assertTrue(recommendation.getRationale().contains("modern"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.CONDITIONAL, recommendation.getRecommendationStatus());
        assertEquals("SHA-1", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getAlternativeAlgorithms().contains("SHA-256"));
        assertTrue(recommendation.getAlternativeAlgorithms().contains("SHA-3"));
        assertTrue(recommendation.getRationale().contains("SHA-1"));
        assertTrue(recommendation.getRationale().contains("modern"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.CONDITIONAL, recommendation.getRecommendationStatus());
        assertEquals("MD5", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getAlternativeAlgorithms().contains("SHA-256"));
        assertTrue(recommendation.getAlternativeAlgorithms().contains("SHA-3"));
        assertEquals(MigrationPriority.CRITICAL, recommendation.getMigrationPriority());
        assertTrue(recommendation.getRationale().contains("MD5"));
        assertTrue(recommendation.getRationale().contains("modern"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
        assertEquals("TLS", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertEquals(MigrationPriority.MEDIUM, recommendation.getMigrationPriority());
        assertTrue(recommendation.getRationale().contains("TLS"));
        assertTrue(recommendation.getRationale().contains("cipher suite"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
        assertEquals("TLS", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getRationale().contains("TLS"));
        assertTrue(recommendation.getRationale().contains("cipher suite"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
        assertEquals("UNKNOWN", recommendation.getCurrentAlgorithm());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getRationale().contains("sufficient confidence"));
        assertTrue(recommendation.getRationale().contains("algorithm"));
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
        assertNull(recommendation.getRecommendedAlgorithm());
        assertTrue(recommendation.getRationale().contains("sufficient confidence"));
        assertEquals(CryptoFinding.Confidence.LOW, recommendation.getConfidence());
    }

    // Negative tests to ensure incorrect recommendations are NOT generated

    @Test
    void testAES256DoesNotRecommendMLKEM() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setVariant("AES-256");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(\"AES-256\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-KEM", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-KEM"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
    }

    @Test
    void testAES256DoesNotRecommendMLDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setVariant("AES-256");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(\"AES-256\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-DSA"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
    }

    @Test
    void testECDSADoesNotRecommendMLKEM() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDSA");
        finding.setVariant("SHA256withECDSA");
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Signature.getInstance(\"SHA256withECDSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-KEM", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-KEM"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
    }

    @Test
    void testECDHDoesNotRecommendMLDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDH");
        finding.setVariant("ECDH");
        finding.setPurpose(CryptoFinding.Purpose.KEY_AGREEMENT);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyAgreement.getInstance(\"ECDH\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-DSA"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
    }

    @Test
    void testMD5DoesNotRecommendMLDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("MD5");
        finding.setVariant("MD5");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"MD5\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-DSA"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
    }

    @Test
    void testTLS13DoesNotAutoRecommendMLKEM() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("TLS");
        finding.setVariant("TLSv1.3");
        finding.setPurpose(CryptoFinding.Purpose.PROTOCOL);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("SSLContext.getInstance(\"TLSv1.3\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-KEM", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-KEM"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
    }

    @Test
    void testSHA1DoesNotRecommendMLDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("SHA-1");
        finding.setVariant("SHA-1");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"SHA-1\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-DSA"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-dsa"));
    }

    @Test
    void testSHA1DoesNotRecommendMLKEM() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("SHA-1");
        finding.setVariant("SHA-1");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"SHA-1\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-KEM", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-KEM"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
    }

    @Test
    void testMD5DoesNotRecommendMLKEM() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("MD5");
        finding.setVariant("MD5");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("MessageDigest.getInstance(\"MD5\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-KEM", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-KEM"));
        assertFalse(recommendation.getRationale().toLowerCase().contains("ml-kem"));
    }

    @Test
    void testUnknownAlgorithmDoesNotRecommendMLKEM() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setVariant("UNKNOWN");
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(variable)");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-KEM", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-KEM"));
    }

    @Test
    void testUnknownAlgorithmDoesNotRecommendMLDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setVariant("UNKNOWN");
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(variable)");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertFalse(recommendation.getAlternativeAlgorithms().contains("ML-DSA"));
    }

    @Test
    void testRSAUnknownPurposeDoesNotAutoRecommendMLDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-2048");
        finding.setKeySize(2048);
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertNotEquals("ML-DSA", recommendation.getRecommendedAlgorithm());
        assertEquals(PQCRecommendationStatus.NEEDS_ANALYSIS, recommendation.getRecommendationStatus());
    }

    @Test
    void testConsiderationsArePresent() {
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertFalse(recommendation.getConsiderations().isEmpty());
        assertTrue(recommendation.getConsiderations().size() > 0);
    }

    @Test
    void testQuantumRiskPreserved() {
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(assessment.getQuantumRisk(), recommendation.getQuantumRisk());
    }

    @Test
    void testConfidencePreserved() {
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertEquals(assessment.getConfidence(), recommendation.getConfidence());
    }

    @Test
    void testRSADigitalSignatureHasSLHDSAAlternative() {
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertTrue(recommendation.getAlternativeAlgorithms().contains("SLH-DSA"));
    }

    @Test
    void testECDSAHasSLHDSAAlternative() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDSA");
        finding.setVariant("SHA256withECDSA");
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setFile("Test.java");
        finding.setLine(10);
        finding.setEvidence("Signature.getInstance(\"SHA256withECDSA\")");

        RiskAssessment assessment = riskEngine.assessRisk(finding);
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        assertTrue(recommendation.getAlternativeAlgorithms().contains("SLH-DSA"));
    }

    @Test
    void testMigrationPriorityBasedOnRisk() {
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
        PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);

        // RSA with HIGH quantum risk and HIGH risk level should have HIGH priority
        assertTrue(recommendation.getMigrationPriority() == MigrationPriority.HIGH || 
                   recommendation.getMigrationPriority() == MigrationPriority.MEDIUM);
    }
}
