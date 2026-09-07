package com.ecdat.backend.service;

import com.ecdat.backend.cbom.CBOMDocument;
import com.ecdat.backend.cbom.CBOMGenerator;
import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.AnalysisSummary;
import com.ecdat.backend.inventory.CryptoAsset;
import com.ecdat.backend.inventory.CryptoInventory;
import com.ecdat.backend.inventory.CryptoUsageCategory;
import com.ecdat.backend.inventory.InventoryClassifier;
import com.ecdat.backend.inventory.LifecycleStatus;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.pqc.PQCRecommendationEngine;
import com.ecdat.backend.pqc.PQCRecommendationStatus;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.risk.RiskEngine;
import com.ecdat.backend.risk.RiskLevel;
import com.ecdat.backend.scanner.CryptoFinding;
import com.ecdat.backend.scanner.JavaSourceScanner;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class AnalysisService {

    private final JavaSourceScanner scanner;
    private final InventoryClassifier inventoryClassifier;
    private final RiskEngine riskEngine;
    private final PQCRecommendationEngine pqcEngine;
    private final CBOMGenerator cbomGenerator;

    public AnalysisService() {
        this.scanner = new JavaSourceScanner();
        this.inventoryClassifier = new InventoryClassifier();
        this.riskEngine = new RiskEngine();
        this.pqcEngine = new PQCRecommendationEngine();
        this.cbomGenerator = new CBOMGenerator();
    }

    public AnalysisService(JavaSourceScanner scanner, RiskEngine riskEngine,
                           PQCRecommendationEngine pqcEngine, CBOMGenerator cbomGenerator) {
        this.scanner = scanner;
        this.inventoryClassifier = new InventoryClassifier();
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
    }

    public AnalysisService(JavaSourceScanner scanner, InventoryClassifier inventoryClassifier,
                           RiskEngine riskEngine, PQCRecommendationEngine pqcEngine,
                           CBOMGenerator cbomGenerator) {
        this.scanner = scanner;
        this.inventoryClassifier = inventoryClassifier;
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
    }

    /**
     * Executes the end-to-end cryptographic analysis pipeline on a target source directory.
     *
     * @param sourcePath directory path containing source files
     * @return integrated AnalysisResponse containing findings, risk assessments, PQC recommendations, inventory, and CBOM
     */
    public AnalysisResponse analyzeDirectory(String sourcePath) {
        if (sourcePath == null || sourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Source path must not be empty or blank.");
        }

        Path path = Paths.get(sourcePath.trim()).normalize().toAbsolutePath();

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Source path does not exist: " + sourcePath);
        }

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Source path is not a directory: " + sourcePath);
        }

        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("Source directory is not readable: " + sourcePath);
        }

        return executePipeline(path.toString(), sourcePath);
    }

    /**
     * Extracts an uploaded zip archive safely and executes the cryptographic analysis pipeline.
     *
     * @param file uploaded zip archive
     * @return integrated AnalysisResponse
     */
    public AnalysisResponse analyzeArchive(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded archive file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only .zip archive files are supported for upload.");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("ecdat-upload-");
            extractZipSafely(file, tempDir);
            return executePipeline(tempDir.toString(), originalFilename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process uploaded archive: " + e.getMessage(), e);
        } finally {
            if (tempDir != null) {
                deleteDirectoryRecursively(tempDir);
            }
        }
    }

    /**
     * Orchestrates the integrated multi-phase analysis pipeline across modules.
     */
    private AnalysisResponse executePipeline(String scanDirectory, String displayPath) {
        // Phase 1 — Crypto Discovery
        List<CryptoFinding> findings = scanner.scanDirectory(scanDirectory);
        if (findings == null) {
            findings = new ArrayList<>();
        }

        // Phase 7 Layer — Inventory Classification
        List<CryptoAsset> cryptoAssets = inventoryClassifier.classifyAll(findings);

        // Phase 2 — Risk Assessment
        List<RiskAssessment> riskAssessments = new ArrayList<>();
        for (CryptoFinding finding : findings) {
            RiskAssessment assessment = riskEngine.assessRisk(finding);
            riskAssessments.add(assessment);
        }

        // Phase 3 — PQC Recommendations
        List<PQCRecommendation> pqcRecommendations = new ArrayList<>();
        for (int i = 0; i < findings.size(); i++) {
            CryptoFinding finding = findings.get(i);
            RiskAssessment assessment = riskAssessments.get(i);
            PQCRecommendation recommendation = pqcEngine.recommend(finding, assessment);
            pqcRecommendations.add(recommendation);
        }

        // Complete Inventory Assembly
        CryptoInventory inventory = inventoryClassifier.buildInventory(cryptoAssets, riskAssessments, pqcRecommendations);

        // Phase 4 — CBOM Generation
        CBOMDocument cbom = cbomGenerator.generate(riskAssessments, pqcRecommendations);

        // Summary Aggregation
        AnalysisSummary summary = buildSummary(findings, cryptoAssets, riskAssessments, pqcRecommendations);

        return new AnalysisResponse(
                "SUCCESS",
                displayPath,
                findings,
                riskAssessments,
                pqcRecommendations,
                cryptoAssets,
                inventory,
                cbom,
                summary
        );
    }

    /**
     * Safely extracts a zip file with Zip Slip and zip bomb protections.
     */
    private void extractZipSafely(MultipartFile file, Path targetDir) throws IOException {
        final int MAX_ENTRIES = 10000;
        final long MAX_TOTAL_SIZE = 200 * 1024 * 1024; // 200 MB uncompressed limit
        int entryCount = 0;
        long totalBytes = 0;

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw new SecurityException("Archive exceeds maximum allowed entry count of " + MAX_ENTRIES);
                }

                Path entryDestination = targetDir.resolve(entry.getName()).normalize();
                if (!entryDestination.startsWith(targetDir.normalize())) {
                    throw new SecurityException("Zip Slip path traversal attempt detected in entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryDestination);
                } else {
                    if (entryDestination.getParent() != null) {
                        Files.createDirectories(entryDestination.getParent());
                    }
                    try (OutputStream os = Files.newOutputStream(entryDestination)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            totalBytes += len;
                            if (totalBytes > MAX_TOTAL_SIZE) {
                                throw new SecurityException("Archive uncompressed size exceeds maximum allowed limit (200MB).");
                            }
                            os.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Recursively deletes a temporary directory.
     */
    private void deleteDirectoryRecursively(Path path) {
        try {
            if (Files.exists(path)) {
                try (var stream = Files.walk(path)) {
                    stream.sorted(Comparator.reverseOrder())
                          .map(Path::toFile)
                          .forEach(File::delete);
                }
            }
        } catch (Exception ignored) {
            // Best effort cleanup for temporary files
        }
    }

    /**
     * Aggregates summary statistics from pipeline results and inventory metadata.
     */
    private AnalysisSummary buildSummary(List<CryptoFinding> findings,
                                         List<CryptoAsset> assets,
                                         List<RiskAssessment> assessments,
                                         List<PQCRecommendation> recommendations) {
        int lowRisk = 0;
        int mediumRisk = 0;
        int highRisk = 0;
        int criticalRisk = 0;
        int quantumHigh = 0;

        for (RiskAssessment assessment : assessments) {
            if (assessment.getRiskLevel() == RiskLevel.LOW) lowRisk++;
            else if (assessment.getRiskLevel() == RiskLevel.MEDIUM) mediumRisk++;
            else if (assessment.getRiskLevel() == RiskLevel.HIGH) highRisk++;
            else if (assessment.getRiskLevel() == RiskLevel.CRITICAL) criticalRisk++;

            if (assessment.getQuantumRisk() == QuantumRisk.HIGH) quantumHigh++;
        }

        int pqcRecommended = 0;
        int pqcConditional = 0;
        int pqcNeedsAnalysis = 0;
        int pqcNotRequired = 0;

        for (PQCRecommendation rec : recommendations) {
            if (rec.getRecommendationStatus() == PQCRecommendationStatus.RECOMMENDED) pqcRecommended++;
            else if (rec.getRecommendationStatus() == PQCRecommendationStatus.CONDITIONAL) pqcConditional++;
            else if (rec.getRecommendationStatus() == PQCRecommendationStatus.NEEDS_ANALYSIS) pqcNeedsAnalysis++;
            else if (rec.getRecommendationStatus() == PQCRecommendationStatus.NOT_REQUIRED) pqcNotRequired++;
        }

        int activeCount = 0;
        int deprecatedCount = 0;
        int unknownLifecycleCount = 0;
        int directUsageCount = 0;

        if (assets != null) {
            for (CryptoAsset asset : assets) {
                if (asset.getLifecycleStatus() == LifecycleStatus.ACTIVE) activeCount++;
                else if (asset.getLifecycleStatus() == LifecycleStatus.DEPRECATED) deprecatedCount++;
                else unknownLifecycleCount++;

                if (asset.getUsageCategory() == CryptoUsageCategory.DIRECT_USAGE) directUsageCount++;
            }
        }

        return new AnalysisSummary(
                findings.size(),
                lowRisk,
                mediumRisk,
                highRisk,
                criticalRisk,
                quantumHigh,
                pqcRecommended,
                pqcConditional,
                pqcNeedsAnalysis,
                pqcNotRequired,
                activeCount,
                deprecatedCount,
                unknownLifecycleCount,
                directUsageCount
        );
    }
}
