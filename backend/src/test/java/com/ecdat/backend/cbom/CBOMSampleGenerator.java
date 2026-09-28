package com.ecdat.backend.cbom;

import com.ecdat.backend.pqc.MigrationPriority;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.pqc.PQCRecommendationStatus;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskFactor;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.scanner.CryptoFinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to generate sample CBOM JSON for inspection and validation.
 */
public class CBOMSampleGenerator {
    
    public static void main(String[] args) throws IOException {
        CBOMGenerator generator = new CBOMGenerator();
        
        // Create sample findings
        CryptoFinding aesFinding = createFinding("AES", CryptoFinding.Purpose.ENCRYPTION, "GCM", null);
        aesFinding.setFile("src/main/java/com/example/CryptoUtil.java");
        aesFinding.setLine(42);
        aesFinding.setEvidence("Cipher.getInstance(\"AES/GCM/NoPadding\")");
        aesFinding.setKeySize(256);
        aesFinding.setConfidence(CryptoFinding.Confidence.HIGH);
        aesFinding.setSourceType("JAVA_AST");
        
        CryptoFinding rsaFinding = createFinding("RSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE, null, null);
        rsaFinding.setFile("src/main/java/com/example/SignatureUtil.java");
        rsaFinding.setLine(15);
        rsaFinding.setEvidence("Signature.getInstance(\"SHA256withRSA\")");
        rsaFinding.setKeySize(2048);
        rsaFinding.setConfidence(CryptoFinding.Confidence.HIGH);
        rsaFinding.setSourceType("JAVA_AST");
        
        // Create risk assessments
        RiskAssessment aesAssessment = createAssessment(aesFinding, 18, RiskLevel.LOW, QuantumRisk.LOW);
        RiskAssessment rsaAssessment = createAssessment(rsaFinding, 80, RiskLevel.HIGH, QuantumRisk.HIGH);
        
        // Create PQC recommendations
        PQCRecommendation rsaRecommendation = createRecommendation(
            "RSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE,
            "ML-DSA", List.of("SLH-DSA"), 
            PQCRecommendationStatus.RECOMMENDED, MigrationPriority.HIGH
        );
        
        // Generate CBOM
        List<RiskAssessment> assessments = List.of(aesAssessment, rsaAssessment);
        List<PQCRecommendation> recommendations = List.of(rsaRecommendation);
        
        CBOMDocument document = generator.generate(assessments, recommendations);
        
        // Serialize to JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(document);
        
        System.out.println("=== GENERATED CBOM JSON ===");
        System.out.println(json);
        System.out.println("=== END CBOM JSON ===");
    }
    
    private static CryptoFinding createFinding(String algorithm, CryptoFinding.Purpose purpose, String mode, String padding) {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm(algorithm);
        finding.setPurpose(purpose);
        finding.setMode(mode);
        finding.setPadding(padding);
        finding.setVariant(mode != null ? mode : "");
        return finding;
    }
    
    private static RiskAssessment createAssessment(CryptoFinding finding, int score, RiskLevel level, QuantumRisk quantumRisk) {
        List<RiskFactor> factors = new ArrayList<>();
        factors.add(new RiskFactor("sample_factor_1", score/2, "Sample risk factor 1"));
        factors.add(new RiskFactor("sample_factor_2", score/2, "Sample risk factor 2"));
        return new RiskAssessment(score, factors, quantumRisk, finding.getConfidence(), finding);
    }
    
    private static PQCRecommendation createRecommendation(String algorithm, CryptoFinding.Purpose purpose,
                                                          String recommended, List<String> alternatives,
                                                          PQCRecommendationStatus status, MigrationPriority priority) {
        return new PQCRecommendation(
            status,
            algorithm,
            purpose,
            recommended,
            alternatives,
            "Sample rationale for PQC migration",
            priority,
            QuantumRisk.HIGH,
            CryptoFinding.Confidence.HIGH,
            List.of("Consideration 1", "Consideration 2")
        );
    }
}
