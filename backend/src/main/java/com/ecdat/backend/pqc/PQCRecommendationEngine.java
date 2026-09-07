package com.ecdat.backend.pqc;

import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.scanner.CryptoFinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PQCRecommendationEngine {

    public PQCRecommendation recommend(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        if (algorithm == null) {
            algorithm = "UNKNOWN";
        }

        CryptoFinding.Purpose purpose = finding.getPurpose();
        CryptoFinding.Confidence confidence = finding.getConfidence();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();

        // Handle low confidence or unknown algorithm
        if (confidence == CryptoFinding.Confidence.LOW || algorithm.equals("UNKNOWN")) {
            return createNeedsAnalysisRecommendation(algorithm, purpose, confidence, quantumRisk);
        }

        // Purpose-aware mappings
        switch (algorithm.toUpperCase()) {
            case "RSA":
                return recommendRSA(finding, assessment);
            case "ECDSA":
                return recommendECDSA(finding, assessment);
            case "ECDH":
                return recommendECDH(finding, assessment);
            case "AES":
                return recommendAES(finding, assessment);
            case "SHA-1":
            case "SHA1":
                return recommendSHA1(finding, assessment);
            case "MD5":
                return recommendMD5(finding, assessment);
            case "SHA-256":
            case "SHA256":
            case "SHA-512":
            case "SHA512":
                return recommendModernHash(finding, assessment);
            case "TLS":
                return recommendTLS(finding, assessment);
            default:
                return createNeedsAnalysisRecommendation(algorithm, purpose, confidence, quantumRisk);
        }
    }

    private PQCRecommendation recommendRSA(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        // RSA for digital signature
        if (purpose == CryptoFinding.Purpose.DIGITAL_SIGNATURE) {
            List<String> alternatives = Arrays.asList("SLH-DSA");
            List<String> considerations = Arrays.asList(
                "Certificate ecosystem compatibility requires verification",
                "Library and provider support for ML-DSA",
                "Signature size differences compared to RSA",
                "Protocol and interoperability considerations",
                "Deployment compatibility assessment required",
                "Hybrid deployment may be considered during transition"
            );

            String rationale = "RSA digital signatures are vulnerable to future cryptographically relevant quantum attacks. " +
                    "ML-DSA is a NIST-standardized post-quantum digital-signature scheme suitable for migration planning.";

            MigrationPriority priority = determinePriority(quantumRisk, riskLevel);

            return new PQCRecommendation(
                PQCRecommendationStatus.RECOMMENDED,
                algorithm,
                purpose,
                "ML-DSA",
                alternatives,
                rationale,
                priority,
                quantumRisk,
                confidence,
                considerations
            );
        }

        // RSA for key establishment/transport/encryption
        if (purpose == CryptoFinding.Purpose.KEY_AGREEMENT || 
            purpose == CryptoFinding.Purpose.KEY_GENERATION ||
            purpose == CryptoFinding.Purpose.ENCRYPTION) {
            
            List<String> considerations = Arrays.asList(
                "Purpose-specific compatibility analysis required",
                "Key-establishment protocol compatibility",
                "ML-KEM applicability depends on use case",
                "Hybrid migration options may be applicable",
                "Implementation and provider support verification"
            );

            String rationale = "RSA is being used for key establishment or encryption. " +
                    "Further usage analysis is required before selecting a PQC replacement. " +
                    "ML-KEM may be applicable depending on the specific use case.";

            MigrationPriority priority = determinePriority(quantumRisk, riskLevel);

            return new PQCRecommendation(
                PQCRecommendationStatus.NEEDS_ANALYSIS,
                algorithm,
                purpose,
                null,
                Arrays.asList("ML-KEM (if applicable)"),
                rationale,
                priority,
                quantumRisk,
                confidence,
                considerations
            );
        }

        // RSA with unknown purpose
        return createNeedsAnalysisRecommendation(algorithm, purpose, confidence, quantumRisk);
    }

    private PQCRecommendation recommendECDSA(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        List<String> alternatives = Arrays.asList("SLH-DSA");
        List<String> considerations = Arrays.asList(
            "Certificate and protocol support for ML-DSA",
            "Library and provider implementation availability",
            "Signature size differences compared to ECDSA",
            "Verification and signing performance characteristics",
            "Interoperability with existing systems",
            "Hybrid deployment may be considered during transition"
        );

        String rationale = "ECDSA is a classical public-key digital-signature algorithm and requires post-quantum migration planning. " +
                "ML-DSA is the NIST-standardized post-quantum digital-signature scheme suitable for migration.";

        MigrationPriority priority = determinePriority(quantumRisk, riskLevel);

        return new PQCRecommendation(
            PQCRecommendationStatus.RECOMMENDED,
            algorithm,
            purpose,
            "ML-DSA",
            alternatives,
            rationale,
            priority,
            quantumRisk,
            confidence,
            considerations
        );
    }

    private PQCRecommendation recommendECDH(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        List<String> considerations = Arrays.asList(
            "Protocol compatibility with ML-KEM",
            "Key and ciphertext size differences",
            "Implementation and provider support verification",
            "Performance characteristics assessment",
            "Hybrid migration options for compatibility",
            "Interoperability with existing systems"
        );

        String rationale = "ECDH is a classical public-key key-agreement mechanism with quantum-vulnerability concerns. " +
                "ML-KEM provides a standardized post-quantum key-establishment mechanism for migration.";

        MigrationPriority priority = determinePriority(quantumRisk, riskLevel);

        return new PQCRecommendation(
            PQCRecommendationStatus.RECOMMENDED,
            algorithm,
            purpose,
            "ML-KEM",
            new ArrayList<>(),
            rationale,
            priority,
            quantumRisk,
            confidence,
            considerations
        );
    }

    private PQCRecommendation recommendAES(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        String variant = finding.getVariant();

        // AES-256 - no PQC replacement needed
        if (variant != null && variant.contains("256")) {
            List<String> considerations = Arrays.asList(
                "Retain AES-256 with proper configuration",
                "Ensure secure key management practices",
                "Review mode of operation implementation",
                "Monitor cryptographic best practices"
            );

            String rationale = "AES-256 is a modern symmetric encryption algorithm. " +
                    "No PQC replacement is required solely due to quantum risk when properly configured.";

            return new PQCRecommendation(
                PQCRecommendationStatus.NOT_REQUIRED,
                algorithm,
                purpose,
                null,
                new ArrayList<>(),
                rationale,
                MigrationPriority.LOW,
                quantumRisk,
                confidence,
                considerations
            );
        }

        // AES-128 - review stronger symmetric
        if (variant != null && variant.contains("128")) {
            List<String> considerations = Arrays.asList(
                "Review long-term security requirements",
                "Consider migration to AES-256 where appropriate",
                "Assess quantum computing timeline impact",
                "Evaluate application-specific security needs"
            );

            String rationale = "AES-128 provides security but may be affected by quantum computing advances (Grover's algorithm). " +
                    "Review long-term security requirements and consider migration toward stronger symmetric security where appropriate.";

            return new PQCRecommendation(
                PQCRecommendationStatus.CONDITIONAL,
                algorithm,
                purpose,
                null,
                Arrays.asList("AES-256"),
                rationale,
                MigrationPriority.MEDIUM,
                quantumRisk,
                confidence,
                considerations
            );
        }

        // AES with unknown key size
        List<String> considerations = Arrays.asList(
            "Determine AES key size from configuration",
            "Review implementation for key size specification",
            "Assess security based on actual key size"
        );

        String rationale = "AES key size could not be determined. " +
                "Review implementation to determine appropriate security assessment.";

        return new PQCRecommendation(
            PQCRecommendationStatus.NEEDS_ANALYSIS,
            algorithm,
            purpose,
            null,
            new ArrayList<>(),
            rationale,
            MigrationPriority.MEDIUM,
            quantumRisk,
            confidence,
            considerations
        );
    }

    private PQCRecommendation recommendSHA1(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        List<String> considerations = Arrays.asList(
            "Replace SHA-1 with SHA-256 or SHA-3",
            "Review collision vulnerability impact",
            "Assess application-specific security requirements",
            "Update cryptographic library dependencies"
        );

        String rationale = "SHA-1 is considered cryptographically weak for security-sensitive applications. " +
                "Replace SHA-1 with a modern approved cryptographic hash appropriate to the application.";

        return new PQCRecommendation(
            PQCRecommendationStatus.CONDITIONAL,
            algorithm,
            purpose,
            null,
            Arrays.asList("SHA-256", "SHA-3"),
            rationale,
            determinePriority(quantumRisk, riskLevel),
            quantumRisk,
            confidence,
            considerations
        );
    }

    private PQCRecommendation recommendMD5(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        List<String> considerations = Arrays.asList(
            "Replace MD5 with SHA-256 or SHA-3 immediately",
            "Review collision vulnerability impact",
            "Assess application-specific security requirements",
            "Update cryptographic library dependencies"
        );

        String rationale = "MD5 is cryptographically broken for collision resistance. " +
                "Replace MD5 with a modern cryptographic hash appropriate to the application.";

        return new PQCRecommendation(
            PQCRecommendationStatus.CONDITIONAL,
            algorithm,
            purpose,
            null,
            Arrays.asList("SHA-256", "SHA-3"),
            rationale,
            MigrationPriority.CRITICAL,
            quantumRisk,
            confidence,
            considerations
        );
    }

    private PQCRecommendation recommendModernHash(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        List<String> considerations = Arrays.asList(
            "Retain modern hash function",
            "Ensure proper usage and implementation",
            "Review application-specific security requirements"
        );

        String rationale = algorithm + " is a modern cryptographic hash function. " +
                "No PQC replacement is required for this finding.";

        return new PQCRecommendation(
            PQCRecommendationStatus.NOT_REQUIRED,
            algorithm,
            purpose,
            null,
            new ArrayList<>(),
            rationale,
            MigrationPriority.LOW,
            quantumRisk,
            confidence,
            considerations
        );
    }

    private PQCRecommendation recommendTLS(CryptoFinding finding, RiskAssessment assessment) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();
        QuantumRisk quantumRisk = assessment.getQuantumRisk();
        RiskLevel riskLevel = assessment.getRiskLevel();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        List<String> considerations = Arrays.asList(
            "Analyze cipher suite configuration",
            "Identify key exchange mechanism",
            "Review certificate algorithm",
            "Assess signature algorithm usage",
            "Protocol-specific PQC migration planning required"
        );

        String rationale = "TLS protocol detected. " +
                "Further TLS cryptographic analysis is required to identify cipher suite, key exchange mechanism, " +
                "certificate algorithm, and signature algorithm before PQC recommendations can be provided.";

        return new PQCRecommendation(
            PQCRecommendationStatus.NEEDS_ANALYSIS,
            algorithm,
            purpose,
            null,
            new ArrayList<>(),
            rationale,
            MigrationPriority.MEDIUM,
            quantumRisk,
            confidence,
            considerations
        );
    }

    private PQCRecommendation createNeedsAnalysisRecommendation(String algorithm, 
                                                                 CryptoFinding.Purpose purpose,
                                                                 CryptoFinding.Confidence confidence,
                                                                 QuantumRisk quantumRisk) {
        List<String> considerations = Arrays.asList(
            "Cryptographic algorithm or usage could not be resolved",
            "Manual investigation required",
            "Static analysis confidence was insufficient",
            "Review code implementation and configuration"
        );

        String rationale = "The cryptographic algorithm or usage could not be resolved with sufficient confidence " +
                "for a reliable PQC recommendation. Further analysis is required.";

        return new PQCRecommendation(
            PQCRecommendationStatus.NEEDS_ANALYSIS,
            algorithm,
            purpose,
            null,
            new ArrayList<>(),
            rationale,
            MigrationPriority.MEDIUM,
            quantumRisk,
            confidence,
            considerations
        );
    }

    private MigrationPriority determinePriority(QuantumRisk quantumRisk, RiskLevel riskLevel) {
        if (quantumRisk == QuantumRisk.HIGH && riskLevel == RiskLevel.CRITICAL) {
            return MigrationPriority.CRITICAL;
        }
        if (quantumRisk == QuantumRisk.HIGH && riskLevel == RiskLevel.HIGH) {
            return MigrationPriority.HIGH;
        }
        if (quantumRisk == QuantumRisk.HIGH) {
            return MigrationPriority.MEDIUM;
        }
        if (riskLevel == RiskLevel.CRITICAL) {
            return MigrationPriority.HIGH;
        }
        if (riskLevel == RiskLevel.HIGH) {
            return MigrationPriority.MEDIUM;
        }
        if (riskLevel == RiskLevel.MEDIUM) {
            return MigrationPriority.LOW;
        }
        return MigrationPriority.LOW;
    }
}
