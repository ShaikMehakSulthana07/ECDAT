package com.ecdat.backend.service;

import com.ecdat.backend.cbom.CBOMDocument;
import com.ecdat.backend.cbom.CBOMGenerator;
import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.AnalysisSummary;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.input.InputAdapter;
import com.ecdat.backend.input.InputAdapterRegistry;
import com.ecdat.backend.input.ScanRequest;
import com.ecdat.backend.input.ScanWorkspace;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private final InputAdapterRegistry inputAdapterRegistry;

    public AnalysisService() {
        this.scanner = new JavaSourceScanner();
        this.inventoryClassifier = new InventoryClassifier();
        this.riskEngine = new RiskEngine();
        this.pqcEngine = new PQCRecommendationEngine();
        this.cbomGenerator = new CBOMGenerator();
        this.quantumRiskEngine = new QuantumRiskEngine();
        this.mavenDependencyScanner = new MavenDependencyScanner();
        this.certificateArtifactScanner = new CertificateArtifactScanner();
        this.inputAdapterRegistry = new InputAdapterRegistry();
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
        this.inputAdapterRegistry = new InputAdapterRegistry();
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
        this.inputAdapterRegistry = new InputAdapterRegistry();
    }

    public AnalysisService(JavaSourceScanner scanner, InventoryClassifier inventoryClassifier,
                           RiskEngine riskEngine, PQCRecommendationEngine pqcEngine,
                           CBOMGenerator cbomGenerator, QuantumRiskEngine quantumRiskEngine,
                           MavenDependencyScanner mavenDependencyScanner,
                           CertificateArtifactScanner certificateArtifactScanner,
                           InputAdapterRegistry inputAdapterRegistry) {
        this.scanner = scanner;
        this.inventoryClassifier = inventoryClassifier;
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
        this.quantumRiskEngine = quantumRiskEngine;
        this.mavenDependencyScanner = mavenDependencyScanner;
        this.certificateArtifactScanner = certificateArtifactScanner;
        this.inputAdapterRegistry = inputAdapterRegistry != null ? inputAdapterRegistry : new InputAdapterRegistry();
    }

    /**
     * Executes the multi-input discovery pipeline for a given ScanRequest.
     * Routes the request to the matching InputAdapter, sets up the normalized ScanWorkspace,
     * runs analysis, and cleans up resources.
     *
     * @param request the scan request specifying input type and source details
     * @return integrated AnalysisResponse
     */
    public AnalysisResponse analyze(ScanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Scan request must not be null.");
        }

        InputAdapter adapter = inputAdapterRegistry.getAdapter(request.getInputType());
        ProjectAnalysisContext context = request.getContext();
        if (context == null) {
            context = new ProjectAnalysisContext();
        }

        try (ScanWorkspace workspace = adapter.prepareWorkspace(request)) {
            return executePipeline(workspace.getSourcePath().toString(), request.getSourceIdentifier(), context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process scan workspace: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the end-to-end cryptographic analysis pipeline on a target source directory.
     *
     * @param sourcePath directory path containing source files
     * @param context project analysis context for quantum risk assessment
     * @return integrated AnalysisResponse containing findings, risk assessments, PQC recommendations, inventory, and CBOM
     */
    public AnalysisResponse analyzeDirectory(String sourcePath, ProjectAnalysisContext context) {
        ScanRequest request = ScanRequest.forDirectory(sourcePath, context);
        return analyze(request);
    }

    /**
     * Extracts an uploaded zip archive safely and executes the cryptographic analysis pipeline.
     *
     * @param file uploaded zip archive
     * @param context project analysis context for quantum risk assessment
     * @return integrated AnalysisResponse
     */
    public AnalysisResponse analyzeArchive(MultipartFile file, ProjectAnalysisContext context) {
        ScanRequest request = ScanRequest.forZip(file, context);
        return analyze(request);
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

    public InputAdapterRegistry getInputAdapterRegistry() {
        return inputAdapterRegistry;
    }
}
