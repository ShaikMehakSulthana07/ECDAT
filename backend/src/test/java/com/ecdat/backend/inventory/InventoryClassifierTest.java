package com.ecdat.backend.inventory;

import com.ecdat.backend.pqc.MigrationPriority;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.pqc.PQCRecommendationEngine;
import com.ecdat.backend.pqc.PQCRecommendationStatus;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskEngine;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryClassifierTest {

    private InventoryClassifier classifier;
    private RiskEngine riskEngine;
    private PQCRecommendationEngine pqcEngine;

    @BeforeEach
    void setUp() {
        classifier = new InventoryClassifier();
        riskEngine = new RiskEngine();
        pqcEngine = new PQCRecommendationEngine();
    }

    @Test
    @DisplayName("Classify AES Cipher as ENCRYPTION, ACTIVE, DIRECT_USAGE")
    void testClassifyAES() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setVariant("GCM/NoPadding");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setFile("AESExample.java");
        finding.setLine(15);
        finding.setEvidence("Cipher.getInstance(\"AES/GCM/NoPadding\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals("AES", asset.getAlgorithm());
        assertEquals("GCM/NoPadding", asset.getVariant());
        assertEquals(AssetCategory.ENCRYPTION, asset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, asset.getLifecycleStatus());
        assertEquals(CryptoUsageCategory.DIRECT_USAGE, asset.getUsageCategory());
        assertEquals(BusinessCriticality.UNKNOWN, asset.getBusinessCriticality());
        assertEquals(DataSensitivity.UNKNOWN, asset.getDataSensitivity());
        assertEquals("Java Cryptography Architecture (JCA)", asset.getLibrary());
        assertNull(asset.getProtocol());

        // Also check finding mutation
        assertEquals(AssetCategory.ENCRYPTION, finding.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, finding.getLifecycleStatus());
        assertEquals(CryptoUsageCategory.DIRECT_USAGE, finding.getUsageCategory());
    }

    @Test
    @DisplayName("Classify RSA-2048 KeyPairGenerator as KEY_GENERATION, ACTIVE")
    void testClassifyRSA2048() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-2048");
        finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        finding.setKeySize(2048);
        finding.setFile("RSAExample.java");
        finding.setLine(20);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.KEY_GENERATION, asset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, asset.getLifecycleStatus());
        assertEquals(CryptoUsageCategory.DIRECT_USAGE, asset.getUsageCategory());
        assertEquals(2048, asset.getKeySize());
    }

    @Test
    @DisplayName("Classify RSA with key size < 1024 as DEPRECATED")
    void testClassifyRSA512Deprecated() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setVariant("RSA-512");
        finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        finding.setKeySize(512);
        finding.setFile("WeakRSA.java");
        finding.setLine(10);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.KEY_GENERATION, asset.getAssetCategory());
        assertEquals(LifecycleStatus.DEPRECATED, asset.getLifecycleStatus());
    }

    @Test
    @DisplayName("Classify ECDSA Signature as DIGITAL_SIGNATURE, ACTIVE")
    void testClassifyECDSA() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDSA");
        finding.setVariant("SHA256withECDSA");
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setFile("ECDSAExample.java");
        finding.setLine(12);
        finding.setEvidence("Signature.getInstance(\"SHA256withECDSA\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.DIGITAL_SIGNATURE, asset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, asset.getLifecycleStatus());
        assertEquals(CryptoUsageCategory.DIRECT_USAGE, asset.getUsageCategory());
    }

    @Test
    @DisplayName("Classify ECDH KeyAgreement as KEY_ESTABLISHMENT, ACTIVE")
    void testClassifyECDH() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDH");
        finding.setPurpose(CryptoFinding.Purpose.KEY_AGREEMENT);
        finding.setFile("ECDHExample.java");
        finding.setLine(8);
        finding.setEvidence("KeyAgreement.getInstance(\"ECDH\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.KEY_ESTABLISHMENT, asset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, asset.getLifecycleStatus());
    }

    @Test
    @DisplayName("Classify SHA-256 as HASHING, ACTIVE")
    void testClassifySHA256() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("SHA-256");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setFile("HashExample.java");
        finding.setLine(6);
        finding.setEvidence("MessageDigest.getInstance(\"SHA-256\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.HASHING, asset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, asset.getLifecycleStatus());
    }

    @Test
    @DisplayName("Classify SHA-1 as HASHING, DEPRECATED")
    void testClassifySHA1Deprecated() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("SHA-1");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setFile("HashExample.java");
        finding.setLine(18);
        finding.setEvidence("MessageDigest.getInstance(\"SHA-1\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.HASHING, asset.getAssetCategory());
        assertEquals(LifecycleStatus.DEPRECATED, asset.getLifecycleStatus());
    }

    @Test
    @DisplayName("Classify MD5 as HASHING, DEPRECATED")
    void testClassifyMD5Deprecated() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("MD5");
        finding.setPurpose(CryptoFinding.Purpose.HASHING);
        finding.setFile("HashExample.java");
        finding.setLine(24);
        finding.setEvidence("MessageDigest.getInstance(\"MD5\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.HASHING, asset.getAssetCategory());
        assertEquals(LifecycleStatus.DEPRECATED, asset.getLifecycleStatus());
    }

    @Test
    @DisplayName("Classify TLS as TLS_PROTOCOL, ACTIVE, with JSSE library and protocol variant")
    void testClassifyTLS() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("TLS");
        finding.setVariant("TLSv1.3");
        finding.setPurpose(CryptoFinding.Purpose.PROTOCOL);
        finding.setFile("TLSExample.java");
        finding.setLine(5);
        finding.setEvidence("SSLContext.getInstance(\"TLSv1.3\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.TLS_PROTOCOL, asset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, asset.getLifecycleStatus());
        assertEquals("TLSv1.3", asset.getProtocol());
        assertEquals("Java Cryptography Architecture (JCA/JSSE)", asset.getLibrary());
    }

    // ==========================================
    // NEGATIVE TESTS (Requirement 16)
    // ==========================================

    @Test
    @DisplayName("Negative Test 1: Dependency presence alone does not become direct algorithm usage")
    void testNegativeDependencyPresenceNotDirectUsage() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("BouncyCastle");
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setFile("pom.xml");
        finding.setLine(42);
        finding.setEvidence("<artifactId>bcprov-jdk15on</artifactId>");
        finding.setConfidence(CryptoFinding.Confidence.MEDIUM);
        finding.setSourceType("DEPENDENCY");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(CryptoUsageCategory.DEPENDENCY_PRESENCE, asset.getUsageCategory());
        assertNotEquals(CryptoUsageCategory.DIRECT_USAGE, asset.getUsageCategory());
    }

    @Test
    @DisplayName("Negative Test 2: Unknown purpose remains UNKNOWN")
    void testNegativeUnknownPurposeRemainsUnknown() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("CustomCrypto");
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setFile("Custom.java");
        finding.setLine(10);
        finding.setEvidence("CustomCrypto.init()");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(AssetCategory.UNKNOWN, asset.getAssetCategory());
    }

    @Test
    @DisplayName("Negative Test 3: Missing key size remains UNKNOWN / null")
    void testNegativeMissingKeySizeRemainsNull() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        finding.setKeySize(null); // Not statically inferred
        finding.setFile("RSAExample.java");
        finding.setLine(12);
        finding.setEvidence("KeyPairGenerator.getInstance(\"RSA\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertNull(asset.getKeySize());
    }

    @Test
    @DisplayName("Negative Test 4: Missing business context does not become CRITICAL (remains UNKNOWN)")
    void testNegativeMissingBusinessContextRemainsUnknown() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("AES");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setFile("SecretService.java");
        finding.setLine(10);
        finding.setEvidence("Cipher.getInstance(\"AES\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(BusinessCriticality.UNKNOWN, asset.getBusinessCriticality());
        assertNotEquals(BusinessCriticality.CRITICAL, asset.getBusinessCriticality());
    }

    @Test
    @DisplayName("Negative Test 5: Missing data classification does not become HIGH (remains UNKNOWN)")
    void testNegativeMissingDataSensitivityRemainsUnknown() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("RSA");
        finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        finding.setFile("PaymentService.java");
        finding.setLine(15);
        finding.setEvidence("Cipher.getInstance(\"RSA\")");
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(DataSensitivity.UNKNOWN, asset.getDataSensitivity());
        assertNotEquals(DataSensitivity.HIGH, asset.getDataSensitivity());
    }

    @Test
    @DisplayName("Negative Test 6: Low-confidence discovery does not become high-confidence inventory")
    void testNegativeLowConfidenceDiscoveryRemainsLowAndUsageUnknown() {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setFile("DynamicLoader.java");
        finding.setLine(30);
        finding.setEvidence("Cipher.getInstance(dynamicVar)");
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setSourceType("JAVA_AST");

        CryptoAsset asset = classifier.classify(finding);

        assertNotNull(asset);
        assertEquals(CryptoFinding.Confidence.LOW, asset.getConfidence());
        assertEquals(CryptoUsageCategory.UNKNOWN, asset.getUsageCategory());
        assertEquals(AssetCategory.UNKNOWN, asset.getAssetCategory());
        assertEquals(LifecycleStatus.UNKNOWN, asset.getLifecycleStatus());
    }

    @Test
    @DisplayName("Negative Test 7: Existing Phase 2 risk scores do not unexpectedly change")
    void testNegativePhase2RiskScoresUnchanged() {
        // RSA 2048: 25 base + 20 quantum + 5 public key + 0 key size + 2 key gen = 52 (HIGH)
        CryptoFinding rsaFinding = new CryptoFinding();
        rsaFinding.setAlgorithm("RSA");
        rsaFinding.setVariant("RSA-2048");
        rsaFinding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
        rsaFinding.setKeySize(2048);
        rsaFinding.setConfidence(CryptoFinding.Confidence.HIGH);

        classifier.classify(rsaFinding);
        RiskAssessment rsaAssessment = riskEngine.assessRisk(rsaFinding);
        assertEquals(52, rsaAssessment.getRiskScore());
        assertEquals(RiskLevel.HIGH, rsaAssessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, rsaAssessment.getQuantumRisk());

        // MD5: 85 base + 15 deprecated = 100 (CRITICAL)
        CryptoFinding md5Finding = new CryptoFinding();
        md5Finding.setAlgorithm("MD5");
        md5Finding.setPurpose(CryptoFinding.Purpose.HASHING);
        md5Finding.setConfidence(CryptoFinding.Confidence.HIGH);

        classifier.classify(md5Finding);
        RiskAssessment md5Assessment = riskEngine.assessRisk(md5Finding);
        assertEquals(100, md5Assessment.getRiskScore());
        assertEquals(RiskLevel.CRITICAL, md5Assessment.getRiskLevel());
        assertEquals(QuantumRisk.NONE, md5Assessment.getQuantumRisk());

        // ECDSA: 30 base + 20 quantum + 5 public key + 2 signature = 57 (HIGH)
        CryptoFinding ecdsaFinding = new CryptoFinding();
        ecdsaFinding.setAlgorithm("ECDSA");
        ecdsaFinding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        ecdsaFinding.setConfidence(CryptoFinding.Confidence.HIGH);

        classifier.classify(ecdsaFinding);
        RiskAssessment ecdsaAssessment = riskEngine.assessRisk(ecdsaFinding);
        assertEquals(57, ecdsaAssessment.getRiskScore());
        assertEquals(RiskLevel.HIGH, ecdsaAssessment.getRiskLevel());
        assertEquals(QuantumRisk.HIGH, ecdsaAssessment.getQuantumRisk());
    }

    @Test
    @DisplayName("Negative Test 8: Existing Phase 3 PQC recommendations do not unexpectedly change")
    void testNegativePhase3PQCRecommendationsUnchanged() {
        // ECDSA -> ML-DSA (RECOMMENDED), SLH-DSA alternative
        CryptoFinding ecdsaFinding = new CryptoFinding();
        ecdsaFinding.setAlgorithm("ECDSA");
        ecdsaFinding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        ecdsaFinding.setConfidence(CryptoFinding.Confidence.HIGH);
        classifier.classify(ecdsaFinding);
        RiskAssessment ecdsaAssessment = riskEngine.assessRisk(ecdsaFinding);

        PQCRecommendation ecdsaRec = pqcEngine.recommend(ecdsaFinding, ecdsaAssessment);
        assertEquals(PQCRecommendationStatus.RECOMMENDED, ecdsaRec.getRecommendationStatus());
        assertEquals("ML-DSA", ecdsaRec.getRecommendedAlgorithm());
        assertTrue(ecdsaRec.getAlternativeAlgorithms().contains("SLH-DSA"));

        // ECDH -> ML-KEM (RECOMMENDED)
        CryptoFinding ecdhFinding = new CryptoFinding();
        ecdhFinding.setAlgorithm("ECDH");
        ecdhFinding.setPurpose(CryptoFinding.Purpose.KEY_AGREEMENT);
        ecdhFinding.setConfidence(CryptoFinding.Confidence.HIGH);
        classifier.classify(ecdhFinding);
        RiskAssessment ecdhAssessment = riskEngine.assessRisk(ecdhFinding);

        PQCRecommendation ecdhRec = pqcEngine.recommend(ecdhFinding, ecdhAssessment);
        assertEquals(PQCRecommendationStatus.RECOMMENDED, ecdhRec.getRecommendationStatus());
        assertEquals("ML-KEM", ecdhRec.getRecommendedAlgorithm());

        // AES-256 -> NOT_REQUIRED
        CryptoFinding aesFinding = new CryptoFinding();
        aesFinding.setAlgorithm("AES");
        aesFinding.setVariant("AES-256");
        aesFinding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
        aesFinding.setConfidence(CryptoFinding.Confidence.HIGH);
        classifier.classify(aesFinding);
        RiskAssessment aesAssessment = riskEngine.assessRisk(aesFinding);

        PQCRecommendation aesRec = pqcEngine.recommend(aesFinding, aesAssessment);
        assertEquals(PQCRecommendationStatus.NOT_REQUIRED, aesRec.getRecommendationStatus());
        assertNull(aesRec.getRecommendedAlgorithm());

        // MD5 -> CONDITIONAL, SHA-256 / SHA-3, Migration Priority CRITICAL
        CryptoFinding md5Finding = new CryptoFinding();
        md5Finding.setAlgorithm("MD5");
        md5Finding.setPurpose(CryptoFinding.Purpose.HASHING);
        md5Finding.setConfidence(CryptoFinding.Confidence.HIGH);
        classifier.classify(md5Finding);
        RiskAssessment md5Assessment = riskEngine.assessRisk(md5Finding);

        PQCRecommendation md5Rec = pqcEngine.recommend(md5Finding, md5Assessment);
        assertEquals(PQCRecommendationStatus.CONDITIONAL, md5Rec.getRecommendationStatus());
        assertTrue(md5Rec.getAlternativeAlgorithms().contains("SHA-256"));
        assertEquals(MigrationPriority.CRITICAL, md5Rec.getMigrationPriority());
    }
}
