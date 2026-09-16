package com.ecdat.backend.service;

import com.ecdat.backend.cbom.CBOMDocument;
import com.ecdat.backend.cbom.CBOMGenerator;
import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.AnalysisSummary;
import com.ecdat.backend.dto.ProjectAnalysisContext;
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
import com.ecdat.backend.risk.quantum.QuantumRiskEngine;
import com.ecdat.backend.risk.quantum.QuantumRiskInput;
import com.ecdat.backend.risk.quantum.QuantumRiskResult;
import com.ecdat.backend.scanner.CertificateArtifactScanner;
import com.ecdat.backend.scanner.MavenDependencyScanner;
import com.ecdat.backend.scanner.certificate.CertificateArtifactFinding;
import com.ecdat.backend.scanner.CryptoFinding;
import com.ecdat.backend.scanner.JavaSourceScanner;
import com.ecdat.backend.scanner.maven.MavenDependencyFinding;
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
    private final QuantumRiskEngine quantumRiskEngine;
    private final MavenDependencyScanner mavenDependencyScanner;
    private final CertificateArtifactScanner certificateArtifactScanner;

    public AnalysisService() {
        this.scanner = new JavaSourceScanner();
        this.inventoryClassifier = new InventoryClassifier();
        this.riskEngine = new RiskEngine();
        this.pqcEngine = new PQCRecommendationEngine();
        this.cbomGenerator = new CBOMGenerator();
        this.quantumRiskEngine = new QuantumRiskEngine();
        this.mavenDependencyScanner = new MavenDependencyScanner();
        this.certificateArtifactScanner = new CertificateArtifactScanner();
    }

    public AnalysisService(JavaSourceScanner scanner, RiskEngine riskEngine,
                           PQCRecommendationEngine pqcEngine, CBOMGenerator cbomGenerator,
                           QuantumRiskEngine quantumRiskEngine, MavenDependencyScanner mavenDependencyScanner,
                           CertificateArtifactScanner certificateArtifactScanner) {
        this.scanner = scanner;
        this.inventoryClassifier = new InventoryClassifier();
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
        this.quantumRiskEngine = quantumRiskEngine;
        this.mavenDependencyScanner = mavenDependencyScanner;
        this.certificateArtifactScanner = certificateArtifactScanner;
    }

    public AnalysisService(JavaSourceScanner scanner, InventoryClassifier inventoryClassifier,
                           RiskEngine riskEngine, PQCRecommendationEngine pqcEngine,
                           CBOMGenerator cbomGenerator, QuantumRiskEngine quantumRiskEngine,
                           MavenDependencyScanner mavenDependencyScanner,
                           CertificateArtifactScanner certificateArtifactScanner) {
        this.scanner = scanner;
        this.inventoryClassifier = inventoryClassifier;
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
        this.quantumRiskEngine = quantumRiskEngine;
        this.mavenDependencyScanner = mavenDependencyScanner;
        this.certificateArtifactScanner = certificateArtifactScanner;
    }

    /**
     * Executes the end-to-end cryptographic analysis pipeline on a target source directory.
     *
     * @param sourcePath directory path containing source files
     * @param context project analysis context for quantum risk assessment
     * @return integrated AnalysisResponse containing findings, risk assessments, PQC recommendations, inventory, and CBOM
     */
    public AnalysisResponse analyzeDirectory(String sourcePath, ProjectAnalysisContext context) {
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

        // Use default context if not provided
        if (context == null) {
            context = new ProjectAnalysisContext();
        }

        return executePipeline(path.toString(), sourcePath, context);
    }

    /**
     * Extracts an uploaded zip archive safely and executes the cryptographic analysis pipeline.
     *
     * @param file uploaded zip archive
     * @param context project analysis context for quantum risk assessment
     * @return integrated AnalysisResponse
     */
    public AnalysisResponse analyzeArchive(MultipartFile file, ProjectAnalysisContext context) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded archive file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only .zip archive files are supported for upload.");
        }

        // Use default context if not provided
        if (context == null) {
            context = new ProjectAnalysisContext();
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("ecdat-upload-");
            extractZipSafely(file, tempDir);
            return executePipeline(tempDir.toString(), originalFilename, context);
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
    private AnalysisResponse executePipeline(String scanDirectory, String displayPath, ProjectAnalysisContext context) {
        // Phase 1 — Crypto Discovery
        List<CryptoFinding> findings = scanner.scanDirectory(scanDirectory);
        if (findings == null) {
            findings = new ArrayList<>();
        }

        // Phase 1.5 — Maven Dependency Analysis
        List<MavenDependencyFinding> dependencyFindings = mavenDependencyScanner.scanDirectory(scanDirectory);
        
        // Enrich source code findings with library information from dependencies
        enrichFindingsWithLibraryInfo(findings, dependencyFindings);

        // Phase 1.6 — Certificate/Key Artifact Discovery
        List<CertificateArtifactFinding> certificateFindings = certificateArtifactScanner.scanDirectory(scanDirectory);

        // Apply business context to findings
        applyContextToFindings(findings, context);

        // Phase 7 Layer — Inventory Classification
        List<CryptoAsset> cryptoAssets = inventoryClassifier.classifyAll(findings);

        // Phase 2 — Risk Assessment
        List<RiskAssessment> riskAssessments = new ArrayList<>();
        for (CryptoFinding finding : findings) {
            RiskAssessment assessment = riskEngine.assessRisk(finding);
            riskAssessments.add(assessment);
        }

        // Phase 2.5 — Quantum Risk Assessment (Mosca-style)
        List<QuantumRiskResult> quantumRiskResults = new ArrayList<>();
        for (int i = 0; i < findings.size(); i++) {
            CryptoFinding finding = findings.get(i);
            QuantumRiskInput quantumInput = createQuantumRiskInput(finding, context);
            QuantumRiskResult quantumResult = quantumRiskEngine.assessQuantumRisk(quantumInput);
            quantumRiskResults.add(quantumResult);
            // Attach quantum risk result to the corresponding risk assessment
            if (i < riskAssessments.size()) {
                riskAssessments.get(i).setQuantumRiskResult(quantumResult);
            }
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

        AnalysisResponse response = new AnalysisResponse(
                "SUCCESS",
                displayPath,
                findings,
                riskAssessments,
                pqcRecommendations,
                cryptoAssets,
                inventory,
                cbom,
                summary
        ).withContext(context);
        response.setCertificateFindings(certificateFindings);
        return response;
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

                if (entry.isDirectory() || entry.getName().endsWith("/") || entry.getName().endsWith("\\")) {
                    if (!Files.exists(entryDestination)) {
                        Files.createDirectories(entryDestination);
                    }
                } else {
                    Path parent = entryDestination.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    if (!Files.isDirectory(entryDestination)) {
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
     * Enriches source code findings with library information from Maven dependencies.
     */
    private void enrichFindingsWithLibraryInfo(List<CryptoFinding> findings, List<MavenDependencyFinding> dependencyFindings) {
        if (dependencyFindings == null || dependencyFindings.isEmpty()) {
            return;
        }

        // Create a map of crypto library names for quick lookup
        for (CryptoFinding finding : findings) {
            if (finding.getLibrary() == null || finding.getLibrary().isEmpty()) {
                // Try to match with known crypto libraries from dependencies
                for (MavenDependencyFinding depFinding : dependencyFindings) {
                    if (depFinding.isCryptoRelated() && depFinding.getCryptoLibraryName() != null) {
                        // Associate the finding with the crypto library
                        // This is a heuristic - in a real implementation, you'd need more sophisticated matching
                        finding.setLibrary(depFinding.getCryptoLibraryName());
                        finding.setSourceType("JAVA_AST_MAVEN");
                        break;
                    }
                }
                
                // If no match found, mark as unknown
                if (finding.getLibrary() == null || finding.getLibrary().isEmpty()) {
                    finding.setLibrary("UNKNOWN");
                }
            }
        }
    }

    /**
     * Applies project-level context to crypto findings.
     */
    private void applyContextToFindings(List<CryptoFinding> findings, ProjectAnalysisContext context) {
        if (context == null) {
            return;
        }
        
        for (CryptoFinding finding : findings) {
            // Always apply context values if context is provided
            finding.setBusinessCriticality(context.getBusinessCriticality());
            finding.setDataSensitivity(context.getDataSensitivity());
        }
    }

    /**
     * Creates quantum risk input from a crypto finding and project context.
     */
    private QuantumRiskInput createQuantumRiskInput(CryptoFinding finding, ProjectAnalysisContext context) {
        QuantumRiskInput input = new QuantumRiskInput();
        input.setAlgorithm(finding.getAlgorithm());
        input.setKeySize(finding.getKeySize());
        input.setCryptographicPurpose(finding.getPurpose() != null ? finding.getPurpose().name() : "UNKNOWN");
        input.setDataLifetimeYears(context.getDataLifetimeYears());
        input.setMigrationTimeYears(context.getMigrationTimeYears());
        input.setThreatHorizonYears(context.getThreatHorizonYears());
        input.setBusinessCriticality(context.getBusinessCriticality());
        input.setDataSensitivity(context.getDataSensitivity());
        return input;
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
