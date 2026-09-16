package com.ecdat.backend.risk.quantum;

import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import org.springframework.stereotype.Component;

/**
 * Quantum Risk Engine implementing Mosca-style quantum migration assessment.
 * 
 * Mosca calculation: X + Y > Z
 * Where:
 * X = migration time (years required to migrate to PQC)
 * Y = data lifetime (years the data must remain protected)
 * Z = threat horizon (years until quantum computers become a threat)
 * 
 * If X + Y > Z, migration is required because the data will still need protection
 * when quantum computers become capable of breaking the current cryptography.
 */
@Component
public class QuantumRiskEngine {

    /**
     * Assess quantum migration risk using Mosca-style calculation.
     * 
     * @param input quantum risk input parameters
     * @return quantum risk assessment result
     */
    public QuantumRiskResult assessQuantumRisk(QuantumRiskInput input) {
        if (input == null) {
            throw new IllegalArgumentException("QuantumRiskInput cannot be null");
        }

        String algorithm = input.getAlgorithm() != null ? input.getAlgorithm().toUpperCase() : "UNKNOWN";
        
        // Determine if algorithm is quantum-vulnerable
        boolean quantumVulnerable = isQuantumVulnerable(algorithm);
        
        // Calculate Mosca condition: X + Y > Z
        int migrationTime = input.getMigrationTimeYears();
        int dataLifetime = input.getDataLifetimeYears();
        int threatHorizon = input.getThreatHorizonYears();
        
        int totalExposure = migrationTime + dataLifetime;
        boolean moscaConditionMet = totalExposure > threatHorizon;
        
        // Determine if migration is required
        boolean migrationRequired = quantumVulnerable && moscaConditionMet;
        
        // Calculate years until threat (can be negative if threat is past)
        int yearsUntilThreat = threatHorizon; // Simplified - assumes current year is 0
        
        // Determine migration urgency based on multiple factors
        MigrationUrgency urgency = determineMigrationUrgency(
            quantumVulnerable, 
            migrationRequired, 
            moscaConditionMet,
            totalExposure,
            yearsUntilThreat,
            input.getBusinessCriticality(),
            input.getDataSensitivity()
        );
        
        // Generate explanation
        String explanation = generateExplanation(
            algorithm,
            quantumVulnerable,
            migrationRequired,
            migrationTime,
            dataLifetime,
            threatHorizon,
            moscaConditionMet,
            totalExposure,
            urgency,
            input.getBusinessCriticality(),
            input.getDataSensitivity()
        );
        
        // Generate calculation details
        String calculationDetails = String.format(
            "Mosca Calculation: %d (migration time) + %d (data lifetime) = %d (total exposure)%n" +
            "Threat Horizon: %d years%n" +
            "Condition: %d > %d = %s%n" +
            "Algorithm %s quantum-vulnerable: %s",
            migrationTime, dataLifetime, totalExposure,
            threatHorizon,
            totalExposure, threatHorizon, moscaConditionMet,
            algorithm, quantumVulnerable
        );
        
        return new QuantumRiskResult(
            algorithm,
            quantumVulnerable,
            migrationRequired,
            migrationTime,
            dataLifetime,
            threatHorizon,
            moscaConditionMet,
            totalExposure,
            yearsUntilThreat,
            input.getBusinessCriticality(),
            input.getDataSensitivity(),
            urgency,
            explanation,
            calculationDetails
        );
    }
    
    /**
     * Determine if an algorithm is vulnerable to quantum attacks.
     * 
     * @param algorithm the cryptographic algorithm
     * @return true if quantum-vulnerable, false otherwise
     */
    private boolean isQuantumVulnerable(String algorithm) {
        if (algorithm == null) {
            return false;
        }
        
        // Public-key algorithms vulnerable to Shor's algorithm
        return switch (algorithm) {
            case "RSA", "ECDSA", "ECDH", "DH", "DSA", "EC" -> true;
            // Symmetric algorithms and hash functions are more quantum-resistant
            // (affected by Grover's algorithm but require doubling key size)
            case "AES", "DES", "3DES", "CHAHA20", "SHA-1", "SHA-256", "SHA-512", "SHA-3", "MD5" -> false;
            default -> false; // Conservative default - unknown algorithms assumed not vulnerable
        };
    }
    
    /**
     * Determine migration urgency based on quantum risk and business context.
     */
    private MigrationUrgency determineMigrationUrgency(
        boolean quantumVulnerable,
        boolean migrationRequired,
        boolean moscaConditionMet,
        int totalExposure,
        int yearsUntilThreat,
        BusinessCriticality businessCriticality,
        DataSensitivity dataSensitivity
    ) {
        // If not quantum-vulnerable, no migration needed
        if (!quantumVulnerable) {
            return MigrationUrgency.NONE;
        }
        
        // If Mosca condition not met, migration is less urgent
        if (!moscaConditionMet) {
            return MigrationUrgency.LOW;
        }
        
        // Base urgency on how much total exposure exceeds threat horizon
        int exposureGap = totalExposure - yearsUntilThreat;
        
        // Adjust based on business criticality and data sensitivity
        boolean highBusinessImpact = businessCriticality == BusinessCriticality.CRITICAL || 
                                    businessCriticality == BusinessCriticality.HIGH;
        boolean highDataSensitivity = dataSensitivity == DataSensitivity.HIGHLY_SENSITIVE || 
                                    dataSensitivity == DataSensitivity.CONFIDENTIAL;
        
        if (exposureGap > 10 && (highBusinessImpact || highDataSensitivity)) {
            return MigrationUrgency.CRITICAL;
        }
        
        if (exposureGap > 5) {
            return MigrationUrgency.HIGH;
        }
        
        if (exposureGap > 0) {
            return MigrationUrgency.MEDIUM;
        }
        
        return MigrationUrgency.LOW;
    }
    
    /**
     * Generate human-readable explanation of the quantum risk assessment.
     */
    private String generateExplanation(
        String algorithm,
        boolean quantumVulnerable,
        boolean migrationRequired,
        int migrationTime,
        int dataLifetime,
        int threatHorizon,
        boolean moscaConditionMet,
        int totalExposure,
        MigrationUrgency urgency,
        BusinessCriticality businessCriticality,
        DataSensitivity dataSensitivity
    ) {
        StringBuilder explanation = new StringBuilder();
        
        explanation.append("Algorithm: ").append(algorithm).append("\n");
        explanation.append("Quantum Vulnerable: ").append(quantumVulnerable ? "YES" : "NO").append("\n");
        
        if (quantumVulnerable) {
            explanation.append("Migration Time: ").append(migrationTime).append(" years\n");
            explanation.append("Data Lifetime: ").append(dataLifetime).append(" years\n");
            explanation.append("Threat Horizon: ").append(threatHorizon).append(" years\n");
            explanation.append("\n");
            explanation.append("Mosca Calculation: ").append(migrationTime).append(" + ").append(dataLifetime)
                      .append(" = ").append(totalExposure).append("\n");
            explanation.append("Condition: ").append(totalExposure).append(" > ").append(threatHorizon)
                      .append(" = ").append(moscaConditionMet ? "TRUE" : "FALSE").append("\n");
            explanation.append("\n");
            
            if (migrationRequired) {
                explanation.append("Result: QUANTUM MIGRATION REQUIRED\n");
                explanation.append("Reason: The data will require protection beyond the quantum threat horizon.\n");
            } else {
                explanation.append("Result: Migration not immediately required based on timeline.\n");
                explanation.append("Reason: Data lifetime and migration time fit within the threat horizon.\n");
            }
            
            explanation.append("\n");
            explanation.append("Migration Urgency: ").append(urgency).append("\n");
            
            if (businessCriticality != BusinessCriticality.UNKNOWN) {
                explanation.append("Business Criticality: ").append(businessCriticality).append("\n");
            }
            if (dataSensitivity != DataSensitivity.UNKNOWN) {
                explanation.append("Data Sensitivity: ").append(dataSensitivity).append("\n");
            }
        } else {
            explanation.append("Result: This algorithm is not considered quantum-vulnerable.\n");
            explanation.append("Reason: ").append(algorithm)
                      .append(" is a symmetric algorithm or hash function, which has better quantum resistance.\n");
        }
        
        return explanation.toString();
    }
}
