package com.ecdat.backend.inventory;

import com.ecdat.backend.pqc.MigrationPriority;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.pqc.PQCRecommendationStatus;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CryptoInventoryTest {

    @Test
    @DisplayName("Compute correct breakdowns in CryptoInventory")
    void testInventoryBreakdowns() {
        List<CryptoAsset> assets = new ArrayList<>();

        // Asset 1: AES
        CryptoAsset asset1 = new CryptoAsset();
        asset1.setAlgorithm("AES");
        asset1.setAssetCategory(AssetCategory.ENCRYPTION);
        asset1.setLifecycleStatus(LifecycleStatus.ACTIVE);
        asset1.setUsageCategory(CryptoUsageCategory.DIRECT_USAGE);
        assets.add(asset1);

        // Asset 2: RSA
        CryptoAsset asset2 = new CryptoAsset();
        asset2.setAlgorithm("RSA");
        asset2.setAssetCategory(AssetCategory.KEY_GENERATION);
        asset2.setLifecycleStatus(LifecycleStatus.ACTIVE);
        asset2.setUsageCategory(CryptoUsageCategory.DIRECT_USAGE);
        assets.add(asset2);

        // Asset 3: MD5
        CryptoAsset asset3 = new CryptoAsset();
        asset3.setAlgorithm("MD5");
        asset3.setAssetCategory(AssetCategory.HASHING);
        asset3.setLifecycleStatus(LifecycleStatus.DEPRECATED);
        asset3.setUsageCategory(CryptoUsageCategory.DIRECT_USAGE);
        assets.add(asset3);

        // Asset 4: Unknown
        CryptoAsset asset4 = new CryptoAsset();
        asset4.setAlgorithm("UNKNOWN");
        asset4.setAssetCategory(AssetCategory.UNKNOWN);
        asset4.setLifecycleStatus(LifecycleStatus.UNKNOWN);
        asset4.setUsageCategory(CryptoUsageCategory.UNKNOWN);
        assets.add(asset4);

        CryptoInventory inventory = new CryptoInventory(assets);

        assertEquals(4, inventory.getTotalAssets());
        assertEquals(4, inventory.getAssets().size());
        assertNotNull(inventory.getGeneratedAt());

        // Category breakdown
        assertEquals(1, inventory.getCategoryBreakdown().get(AssetCategory.ENCRYPTION));
        assertEquals(1, inventory.getCategoryBreakdown().get(AssetCategory.KEY_GENERATION));
        assertEquals(1, inventory.getCategoryBreakdown().get(AssetCategory.HASHING));
        assertEquals(1, inventory.getCategoryBreakdown().get(AssetCategory.UNKNOWN));

        // Lifecycle breakdown
        assertEquals(2, inventory.getLifecycleBreakdown().get(LifecycleStatus.ACTIVE));
        assertEquals(1, inventory.getLifecycleBreakdown().get(LifecycleStatus.DEPRECATED));
        assertEquals(1, inventory.getLifecycleBreakdown().get(LifecycleStatus.UNKNOWN));

        // Usage breakdown
        assertEquals(3, inventory.getUsageBreakdown().get(CryptoUsageCategory.DIRECT_USAGE));
        assertEquals(1, inventory.getUsageBreakdown().get(CryptoUsageCategory.UNKNOWN));
    }

    @Test
    @DisplayName("Build inventory attaches RiskAssessment and PQCRecommendation to CryptoAssets")
    void testBuildInventoryAttachesAssessments() {
        InventoryClassifier classifier = new InventoryClassifier();

        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("ECDSA");
        finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
        finding.setConfidence(CryptoFinding.Confidence.HIGH);
        finding.setSourceType("JAVA_AST");
        finding.setEvidence("Signature.getInstance(\"SHA256withECDSA\")");

        List<CryptoAsset> assets = classifier.classifyAll(List.of(finding));
        RiskAssessment assessment = new RiskAssessment(57, List.of(), QuantumRisk.HIGH, CryptoFinding.Confidence.HIGH, finding);
        PQCRecommendation recommendation = new PQCRecommendation(
                PQCRecommendationStatus.RECOMMENDED, "ECDSA", CryptoFinding.Purpose.DIGITAL_SIGNATURE,
                "ML-DSA", List.of("SLH-DSA"), "NIST standard", MigrationPriority.HIGH,
                QuantumRisk.HIGH, CryptoFinding.Confidence.HIGH, List.of()
        );

        CryptoInventory inventory = classifier.buildInventory(assets, List.of(assessment), List.of(recommendation));

        assertEquals(1, inventory.getTotalAssets());
        CryptoAsset asset = inventory.getAssets().get(0);
        assertEquals(assessment, asset.getRiskAssessment());
        assertEquals(recommendation, asset.getPqcRecommendation());
        assertEquals(AssetCategory.DIGITAL_SIGNATURE, asset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, asset.getLifecycleStatus());
    }
}
