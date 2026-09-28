package com.ecdat.backend.risk;

import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import com.ecdat.backend.scanner.CryptoFinding;
import java.util.ArrayList;
import java.util.List;

public class RiskEngine {

    public RiskAssessment assessRisk(CryptoFinding finding) {
        return assessRisk(finding, null, null);
    }

    public RiskAssessment assessRisk(CryptoFinding finding, BusinessCriticality businessCriticality, 
                                     DataSensitivity dataSensitivity) {
        List<RiskFactor> factors = new ArrayList<>();
        QuantumRisk quantumRisk = QuantumRisk.NONE;
        int totalScore = 0;

        String algorithm = finding.getAlgorithm();
        if (algorithm == null) {
            algorithm = "UNKNOWN";
        }

        switch (algorithm.toUpperCase()) {
            case "RSA":
                assessRSA(finding, factors);
                quantumRisk = QuantumRisk.HIGH;
                break;
            case "ECDSA":
                assessECDSA(finding, factors);
                quantumRisk = QuantumRisk.HIGH;
                break;
            case "ECDH":
                assessECDH(finding, factors);
                quantumRisk = QuantumRisk.HIGH;
                break;
            case "AES":
                assessAES(finding, factors);
                quantumRisk = QuantumRisk.LOW;
                break;
            case "SHA-256":
            case "SHA256":
                assessSHA256(finding, factors);
                quantumRisk = QuantumRisk.LOW;
                break;
            case "SHA-512":
            case "SHA512":
                assessSHA512(finding, factors);
                quantumRisk = QuantumRisk.LOW;
                break;
            case "SHA-1":
            case "SHA1":
                assessSHA1(finding, factors);
                quantumRisk = QuantumRisk.NONE;
                break;
            case "MD5":
                assessMD5(finding, factors);
                quantumRisk = QuantumRisk.NONE;
                break;
            case "TLS":
                assessTLS(finding, factors);
                quantumRisk = QuantumRisk.NONE;
                break;
            case "UNKNOWN":
                assessUnknown(finding, factors);
                quantumRisk = QuantumRisk.NONE;
                break;
            default:
                assessUnknown(finding, factors);
                quantumRisk = QuantumRisk.NONE;
                break;
        }

        // Add business context factors if provided
        if (businessCriticality != null || dataSensitivity != null) {
            addBusinessContextFactors(factors, businessCriticality, dataSensitivity);
        }

        // Calculate total score from factors
        totalScore = factors.stream().mapToInt(RiskFactor::getScore).sum();

        // Cap score at 100
        totalScore = Math.min(totalScore, 100);

        // Ensure minimum score of 0
        totalScore = Math.max(totalScore, 0);

        return new RiskAssessment(totalScore, factors, quantumRisk, finding.getConfidence(), finding);
    }

    private void addBusinessContextFactors(List<RiskFactor> factors, BusinessCriticality businessCriticality, 
                                          DataSensitivity dataSensitivity) {
        // Data sensitivity factor
        if (dataSensitivity != null) {
            switch (dataSensitivity) {
                case HIGHLY_SENSITIVE:
                    factors.add(new RiskFactor("DATA_SENSITIVITY", 15, 
                            "Data is marked as highly sensitive, increasing risk impact."));
                    break;
                case CONFIDENTIAL:
                    factors.add(new RiskFactor("DATA_SENSITIVITY", 10, 
                            "Data is marked as confidential, increasing risk impact."));
                    break;
                case INTERNAL:
                    factors.add(new RiskFactor("DATA_SENSITIVITY", 5, 
                            "Data is marked as internal use only."));
                    break;
                case PUBLIC:
                    factors.add(new RiskFactor("DATA_SENSITIVITY", 0, 
                            "Data is marked as public, reducing risk impact."));
                    break;
                case UNKNOWN:
                    // No factor added for unknown
                    break;
            }
        }

        // Business criticality factor
        if (businessCriticality != null) {
            switch (businessCriticality) {
                case CRITICAL:
                    factors.add(new RiskFactor("BUSINESS_CRITICALITY", 15, 
                            "System is marked as business-critical, increasing risk impact."));
                    break;
                case HIGH:
                    factors.add(new RiskFactor("BUSINESS_CRITICALITY", 10, 
                            "System is marked as high importance, increasing risk impact."));
                    break;
                case MEDIUM:
                    factors.add(new RiskFactor("BUSINESS_CRITICALITY", 5, 
                            "System is marked as medium importance."));
                    break;
                case LOW:
                    factors.add(new RiskFactor("BUSINESS_CRITICALITY", 0, 
                            "System is marked as low importance, reducing risk impact."));
                    break;
                case UNKNOWN:
                    // No factor added for unknown
                    break;
            }
        }
    }

    private void assessRSA(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 25; // Base cryptographic concern for RSA
        int quantumScore = 20; // Quantum vulnerability
        int publicKeyScore = 5; // Public-key algorithm
        int keySizeScore = 0;
        String keySizeReason = "";

        // Key size assessment
        Integer keySize = finding.getKeySize();
        if (keySize == null) {
            keySizeScore = 15; // Unknown key size increases risk
            keySizeReason = "RSA key size is unknown, which prevents accurate security assessment.";
        } else if (keySize < 1024) {
            keySizeScore = 40; // Very small key size - increased to reach CRITICAL
            keySizeReason = "RSA key size of " + keySize + " bits is below current security recommendations.";
        } else if (keySize < 2048) {
            keySizeScore = 10; // Small but acceptable key size
            keySizeReason = "RSA key size of " + keySize + " bits is below recommended 2048 bits.";
        } else if (keySize == 2048) {
            keySizeScore = 0; // Standard key size
            keySizeReason = "RSA key size of 2048 bits meets current minimum security requirements.";
        } else {
            keySizeScore = -5; // Larger key size reduces risk slightly
            keySizeReason = "RSA key size of " + keySize + " bits exceeds minimum requirements.";
        }

        // Add factors
        factors.add(new RiskFactor("PUBLIC_KEY_CRYPTOGRAPHY", publicKeyScore, 
                "RSA is a public-key cryptographic algorithm."));
        factors.add(new RiskFactor("QUANTUM_VULNERABILITY", quantumScore, 
                "RSA is vulnerable to future cryptographically relevant quantum attacks."));
        factors.add(new RiskFactor("CRYPTOGRAPHIC_CONCERN", baseScore, 
                "RSA requires proper key management and migration planning."));
        
        if (keySizeScore > 0) {
            factors.add(new RiskFactor("KEY_SIZE", keySizeScore, keySizeReason));
        } else if (keySizeScore < 0) {
            factors.add(new RiskFactor("KEY_SIZE", keySizeScore, keySizeReason));
        }

        // Purpose-specific reasoning
        if (finding.getPurpose() == CryptoFinding.Purpose.DIGITAL_SIGNATURE) {
            factors.add(new RiskFactor("PURPOSE_SIGNATURE", 2, 
                    "RSA used for digital signatures requires post-quantum migration planning."));
        } else if (finding.getPurpose() == CryptoFinding.Purpose.KEY_GENERATION) {
            factors.add(new RiskFactor("PURPOSE_KEY_GENERATION", 2, 
                    "RSA used for key generation requires post-quantum migration planning."));
        }
    }

    private void assessECDSA(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 30; // Base risk for ECDSA
        int quantumScore = 20; // Quantum vulnerability
        int publicKeyScore = 5; // Public-key algorithm

        factors.add(new RiskFactor("PUBLIC_KEY_CRYPTOGRAPHY", publicKeyScore, 
                "ECDSA is a public-key digital-signature algorithm."));
        factors.add(new RiskFactor("QUANTUM_VULNERABILITY", quantumScore, 
                "ECDSA is vulnerable to future cryptographically relevant quantum attacks."));
        factors.add(new RiskFactor("CRYPTOGRAPHIC_CONCERN", baseScore, 
                "ECDSA requires post-quantum migration planning."));

        // Purpose-specific reasoning
        if (finding.getPurpose() == CryptoFinding.Purpose.DIGITAL_SIGNATURE) {
            factors.add(new RiskFactor("PURPOSE_SIGNATURE", 2, 
                    "ECDSA is specifically used for digital signatures, requiring post-quantum migration planning."));
        }
    }

    private void assessECDH(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 30; // Base risk for ECDH
        int quantumScore = 20; // Quantum vulnerability
        int publicKeyScore = 5; // Public-key algorithm

        factors.add(new RiskFactor("PUBLIC_KEY_CRYPTOGRAPHY", publicKeyScore, 
                "ECDH is a public-key key-agreement mechanism."));
        factors.add(new RiskFactor("QUANTUM_VULNERABILITY", quantumScore, 
                "ECDH is vulnerable to future cryptographically relevant quantum attacks."));
        factors.add(new RiskFactor("CRYPTOGRAPHIC_CONCERN", baseScore, 
                "ECDH requires post-quantum migration planning."));

        // Purpose-specific reasoning
        if (finding.getPurpose() == CryptoFinding.Purpose.KEY_AGREEMENT) {
            factors.add(new RiskFactor("PURPOSE_KEY_AGREEMENT", 2, 
                    "ECDH is specifically used for key agreement, requiring post-quantum migration planning."));
        }
    }

    private void assessAES(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 5; // Base risk for AES (generally secure)
        int keySizeScore = 0;
        String keySizeReason = "";

        // Key size assessment from variant or evidence
        String variant = finding.getVariant();
        if (variant != null) {
            if (variant.contains("256")) {
                keySizeScore = -5; // AES-256 is very secure
                keySizeReason = "AES-256 is considered secure for most applications including against quantum attacks.";
            } else if (variant.contains("128")) {
                keySizeScore = 5; // AES-128 has some quantum concerns
                keySizeReason = "AES-128 provides security but may be affected by quantum computing advances (Grover's algorithm).";
            } else {
                keySizeScore = 10; // Unknown key size
                keySizeReason = "AES key size could not be determined from the code, preventing full security assessment.";
            }
        } else {
            keySizeScore = 10; // Unknown key size
            keySizeReason = "AES key size is unknown, which prevents accurate security assessment.";
        }

        factors.add(new RiskFactor("SYMMETRIC_CRYPTOGRAPHY", 3, 
                "AES is a symmetric encryption algorithm, which is more quantum-resistant than public-key cryptography."));
        factors.add(new RiskFactor("CRYPTOGRAPHIC_CONCERN", baseScore, 
                "AES is a modern symmetric encryption algorithm when used with proper key sizes and modes."));

        if (keySizeScore > 0) {
            factors.add(new RiskFactor("KEY_SIZE", keySizeScore, keySizeReason));
        } else if (keySizeScore < 0) {
            factors.add(new RiskFactor("KEY_SIZE", keySizeScore, keySizeReason));
        }
    }

    private void assessSHA256(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 10; // Low risk for SHA-256

        factors.add(new RiskFactor("MODERN_HASH", baseScore, 
                "SHA-256 is a modern cryptographic hash function."));
        factors.add(new RiskFactor("QUANTUM_RESISTANCE", 5, 
                "SHA-256 is a modern cryptographic hash function; this finding does not represent the same quantum migration concern as RSA/ECC public-key cryptography."));
    }

    private void assessSHA512(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 10; // Low risk for SHA-512

        factors.add(new RiskFactor("MODERN_HASH", baseScore, 
                "SHA-512 is a modern cryptographic hash function."));
        factors.add(new RiskFactor("QUANTUM_RESISTANCE", 5, 
                "SHA-512 is a modern cryptographic hash function; this finding does not represent the same quantum migration concern as RSA/ECC public-key cryptography."));
    }

    private void assessSHA1(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 60; // High risk for SHA-1

        factors.add(new RiskFactor("WEAK_HASH", baseScore, 
                "SHA-1 is considered cryptographically weak for security-sensitive applications."));
        factors.add(new RiskFactor("DEPRECATED_ALGORITHM", 10, 
                "SHA-1 has been deprecated by most security standards due to collision vulnerabilities."));
    }

    private void assessMD5(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 85; // Critical risk for MD5

        factors.add(new RiskFactor("BROKEN_HASH", baseScore, 
                "MD5 is cryptographically broken for collision resistance and should not be used for security-sensitive purposes."));
        factors.add(new RiskFactor("DEPRECATED_ALGORITHM", 15, 
                "MD5 is completely broken for cryptographic purposes and should be replaced immediately."));
    }

    private void assessTLS(CryptoFinding finding, List<RiskFactor> factors) {
        String variant = finding.getVariant();
        int baseScore = 10; // Base risk for TLS
        String versionReason = "";

        if (variant != null) {
            if (variant.contains("1.3")) {
                baseScore = 5; // TLS 1.3 is modern
                versionReason = "TLS 1.3 is the most current and secure version of the TLS protocol.";
            } else if (variant.contains("1.2")) {
                baseScore = 10; // TLS 1.2 is acceptable but older
                versionReason = "TLS 1.2 is widely used but older than TLS 1.3; security depends on cipher suite configuration.";
            } else {
                baseScore = 25; // Unknown or older version
                versionReason = "TLS version could not be definitively identified or is an older version.";
            }
        } else {
            baseScore = 20; // Unknown version
            versionReason = "TLS version could not be determined from the code.";
        }

        factors.add(new RiskFactor("PROTOCOL_SECURITY", baseScore, versionReason));
        factors.add(new RiskFactor("LIMITED_ASSESSMENT", 8, 
                "Protocol-level assessment is limited because the current scanner has not yet analyzed cipher suite, certificate, key exchange, or signature algorithm."));
    }

    private void assessUnknown(CryptoFinding finding, List<RiskFactor> factors) {
        int baseScore = 35; // Moderate risk for unknown

        factors.add(new RiskFactor("UNKNOWN_ALGORITHM", baseScore, 
                "Cryptographic usage was detected, but the algorithm could not be resolved statically."));
        factors.add(new RiskFactor("REQUIRES_INVESTIGATION", 10, 
                "This finding requires manual investigation to determine the specific algorithm and security implications."));
    }
}
