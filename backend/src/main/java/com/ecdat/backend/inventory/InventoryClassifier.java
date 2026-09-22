package com.ecdat.backend.inventory;

import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.scanner.CryptoFinding;

import java.util.ArrayList;
import java.util.List;

public class InventoryClassifier {

    /**
     * Classifies a discovered CryptoFinding into an enterprise CryptoAsset.
     * Enriches the finding with classification fields while creating a structured CryptoAsset.
     *
     * @param finding discovered finding from AST/discovery layer
     * @return enriched CryptoAsset
     */
    public CryptoAsset classify(CryptoFinding finding) {
        if (finding == null) {
            return null;
        }

        AssetCategory assetCategory = determineAssetCategory(finding);
        CryptoUsageCategory usageCategory = determineUsageCategory(finding);
        LifecycleStatus lifecycleStatus = determineLifecycleStatus(finding);
        BusinessCriticality businessCriticality = BusinessCriticality.UNKNOWN;
        DataSensitivity dataSensitivity = DataSensitivity.UNKNOWN;
        String protocol = determineProtocol(finding);
        String library = determineLibrary(finding);

        // Populate classification fields on finding for backward compatibility & convenience
        finding.setAssetCategory(assetCategory);
        finding.setUsageCategory(usageCategory);
        finding.setLifecycleStatus(lifecycleStatus);
        finding.setBusinessCriticality(businessCriticality);
        finding.setDataSensitivity(dataSensitivity);
        finding.setProtocol(protocol);
        finding.setLibrary(library);

        CryptoAsset asset = new CryptoAsset();
        asset.setAlgorithm(finding.getAlgorithm());
        asset.setVariant(finding.getVariant());
        asset.setPurpose(finding.getPurpose());
        asset.setKeySize(finding.getKeySize());
        asset.setProtocol(protocol);
        asset.setLibrary(library);
        asset.setSourceFile(finding.getFile());
        asset.setSourceLine(finding.getLine());
        asset.setEvidence(finding.getEvidence());
        asset.setConfidence(finding.getConfidence());
        asset.setSourceType(finding.getSourceType());
        asset.setAssetCategory(assetCategory);
        asset.setUsageCategory(usageCategory);
        asset.setLifecycleStatus(lifecycleStatus);
        asset.setBusinessCriticality(businessCriticality);
        asset.setDataSensitivity(dataSensitivity);
        asset.setOriginalFinding(finding);

        return asset;
    }

    /**
     * Classifies a list of CryptoFindings into a list of CryptoAssets.
     */
    public List<CryptoAsset> classifyAll(List<CryptoFinding> findings) {
        List<CryptoAsset> assets = new ArrayList<>();
        if (findings != null) {
            for (CryptoFinding finding : findings) {
                CryptoAsset asset = classify(finding);
                if (asset != null) {
                    assets.add(asset);
                }
            }
        }
        return assets;
    }

    /**
     * Builds a unified CryptoInventory container from classified assets, risk assessments, and PQC recommendations.
     */
    public CryptoInventory buildInventory(List<CryptoAsset> assets,
                                          List<RiskAssessment> riskAssessments,
                                          List<PQCRecommendation> pqcRecommendations) {
        if (assets == null) {
            return new CryptoInventory();
        }

        for (int i = 0; i < assets.size(); i++) {
            CryptoAsset asset = assets.get(i);
            if (riskAssessments != null && i < riskAssessments.size()) {
                asset.setRiskAssessment(riskAssessments.get(i));
            }
            if (pqcRecommendations != null && i < pqcRecommendations.size()) {
                asset.setPqcRecommendation(pqcRecommendations.get(i));
            }
        }

        return new CryptoInventory(assets);
    }

    private AssetCategory determineAssetCategory(CryptoFinding finding) {
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Purpose purpose = finding.getPurpose();

        if (purpose != null) {
            switch (purpose) {
                case ENCRYPTION:
                case DECRYPTION:
                    return AssetCategory.ENCRYPTION;
                case DIGITAL_SIGNATURE:
                    return AssetCategory.DIGITAL_SIGNATURE;
                case KEY_AGREEMENT:
                    return AssetCategory.KEY_ESTABLISHMENT;
                case KEY_GENERATION:
                    return AssetCategory.KEY_GENERATION;
                case HASHING:
                    return AssetCategory.HASHING;
                case PROTOCOL:
                    return AssetCategory.TLS_PROTOCOL;
                case UNKNOWN:
                    break;
            }
        }

        if (algorithm != null) {
            String upperAlgo = algorithm.toUpperCase();
            if (upperAlgo.startsWith("TLS") || upperAlgo.startsWith("SSL")) {
                return AssetCategory.TLS_PROTOCOL;
            }
        }

        return AssetCategory.UNKNOWN;
    }

    private CryptoUsageCategory determineUsageCategory(CryptoFinding finding) {
        String sourceType = finding.getSourceType();
        String algorithm = finding.getAlgorithm();
        CryptoFinding.Confidence confidence = finding.getConfidence();

        // If source type is dependency-based (e.g. POM / Gradle / Dependency check)
        if (sourceType != null && (sourceType.equalsIgnoreCase("DEPENDENCY") ||
                sourceType.equalsIgnoreCase("POM") ||
                sourceType.equalsIgnoreCase("DEPENDENCY_PRESENCE"))) {
            return CryptoUsageCategory.DEPENDENCY_PRESENCE;
        }

        // If source type is configuration-based
        if (sourceType != null && (sourceType.equalsIgnoreCase("CONFIG") ||
                sourceType.equalsIgnoreCase("CONFIGURATION") ||
                sourceType.equalsIgnoreCase("PROTOCOL_CONFIG"))) {
            return CryptoUsageCategory.INDIRECT_CONFIGURATION;
        }

        // If confidence is LOW or algorithm is UNKNOWN or evidence is missing
        if (confidence == CryptoFinding.Confidence.LOW ||
                algorithm == null ||
                algorithm.equalsIgnoreCase("UNKNOWN") ||
                finding.getEvidence() == null ||
                finding.getEvidence().trim().isEmpty()) {
            return CryptoUsageCategory.UNKNOWN;
        }

        // Direct AST method call invocation (e.g., Cipher.getInstance(...), enriched with Maven metadata)
        // Handles JAVA_AST, JAVA_AST_MAVEN, and other JAVA_AST_* variants
        if (sourceType == null || sourceType.toUpperCase().startsWith("JAVA_AST")) {
            return CryptoUsageCategory.DIRECT_USAGE;
        }

        return CryptoUsageCategory.UNKNOWN;
    }

    private LifecycleStatus determineLifecycleStatus(CryptoFinding finding) {
        String algorithm = finding.getAlgorithm();
        if (algorithm == null || algorithm.equalsIgnoreCase("UNKNOWN")) {
            return LifecycleStatus.UNKNOWN;
        }

        String upperAlgo = algorithm.toUpperCase();

        // Deprecated/broken algorithms
        if (upperAlgo.equals("MD5") || upperAlgo.equals("SHA-1") || upperAlgo.equals("SHA1")) {
            return LifecycleStatus.DEPRECATED;
        }

        // RSA key size check
        if (upperAlgo.equals("RSA")) {
            Integer keySize = finding.getKeySize();
            if (keySize != null && keySize < 1024) {
                return LifecycleStatus.DEPRECATED;
            }
            return LifecycleStatus.ACTIVE;
        }

        // Active modern cryptographic algorithms
        if (upperAlgo.equals("AES") ||
                upperAlgo.equals("ECDSA") ||
                upperAlgo.equals("ECDH") ||
                upperAlgo.equals("SHA-256") || upperAlgo.equals("SHA256") ||
                upperAlgo.equals("SHA-512") || upperAlgo.equals("SHA512") ||
                upperAlgo.startsWith("TLS") || upperAlgo.startsWith("SSL")) {
            return LifecycleStatus.ACTIVE;
        }

        return LifecycleStatus.UNKNOWN;
    }

    private String determineProtocol(CryptoFinding finding) {
        String algorithm = finding.getAlgorithm();
        if (algorithm != null && (algorithm.equalsIgnoreCase("TLS") || algorithm.equalsIgnoreCase("SSL"))) {
            return finding.getVariant() != null ? finding.getVariant() : "TLS";
        }
        if (finding.getPurpose() == CryptoFinding.Purpose.PROTOCOL) {
            return finding.getVariant() != null ? finding.getVariant() : "TLS";
        }
        return null;
    }

    private String determineLibrary(CryptoFinding finding) {
        String algorithm = finding.getAlgorithm();
        if (algorithm == null || algorithm.equalsIgnoreCase("UNKNOWN")) {
            return "UNKNOWN";
        }

        if (algorithm.equalsIgnoreCase("TLS") || finding.getPurpose() == CryptoFinding.Purpose.PROTOCOL) {
            return "Java Cryptography Architecture (JCA/JSSE)";
        }

        return "Java Cryptography Architecture (JCA)";
    }
}
