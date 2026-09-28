package com.ecdat.backend.service;

import com.ecdat.backend.cbom.CBOMDocument;
import com.ecdat.backend.cbom.CBOMGenerator;
import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.AnalysisSummary;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.input.AnalysisInput;
import com.ecdat.backend.input.AnalysisInputProcessor;
import com.ecdat.backend.input.AnalysisInputProcessorRegistry;
import com.ecdat.backend.input.AnalysisInputType;
import com.ecdat.backend.input.InputAdapter;
import com.ecdat.backend.input.InputAdapterRegistry;
import com.ecdat.backend.input.ScanRequest;
import com.ecdat.backend.input.ScanWorkspace;
import com.ecdat.backend.input.UnsupportedInputException;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.CryptoAsset;
import com.ecdat.backend.inventory.CryptoInventory;
import com.ecdat.backend.inventory.CryptoUsageCategory;
import com.ecdat.backend.inventory.DataSensitivity;
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
import com.ecdat.backend.scanner.ConfigurationScanner;
import com.ecdat.backend.scanner.CryptoFinding;
import com.ecdat.backend.scanner.JavaSourceScanner;
import com.ecdat.backend.scanner.MavenDependencyScanner;
import com.ecdat.backend.scanner.binary.BinaryScanner;
import com.ecdat.backend.scanner.configuration.ConfigurationFinding;
import com.ecdat.backend.scanner.container.ContainerImageScanner;
import com.ecdat.backend.scanner.certificate.CertificateArtifactFinding;
import com.ecdat.backend.scanner.maven.MavenDependencyFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    private final JavaSourceScanner scanner;
    private final InventoryClassifier inventoryClassifier;
    private final RiskEngine riskEngine;
    private final PQCRecommendationEngine pqcEngine;
    private final CBOMGenerator cbomGenerator;
    private final QuantumRiskEngine quantumRiskEngine;
    private final MavenDependencyScanner mavenDependencyScanner;
    private final CertificateArtifactScanner certificateArtifactScanner;
    private final ConfigurationScanner configurationScanner;
    private final BinaryScanner binaryScanner;
    private final ContainerImageScanner containerImageScanner;
    private final AnalysisInputProcessorRegistry processorRegistry;
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
        this.configurationScanner = new ConfigurationScanner();
        this.binaryScanner = new BinaryScanner();
        this.containerImageScanner = new ContainerImageScanner();
        this.processorRegistry = new AnalysisInputProcessorRegistry();
        this.inputAdapterRegistry = new InputAdapterRegistry();
    }

    public AnalysisService(JavaSourceScanner scanner, RiskEngine riskEngine,
                           PQCRecommendationEngine pqcEngine, CBOMGenerator cbomGenerator,
                           QuantumRiskEngine quantumRiskEngine, MavenDependencyScanner mavenDependencyScanner,
                           CertificateArtifactScanner certificateArtifactScanner,
                           ConfigurationScanner configurationScanner) {
        this.scanner = scanner;
        this.inventoryClassifier = new InventoryClassifier();
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
        this.quantumRiskEngine = quantumRiskEngine;
        this.mavenDependencyScanner = mavenDependencyScanner;
        this.certificateArtifactScanner = certificateArtifactScanner;
        this.configurationScanner = configurationScanner != null ? configurationScanner : new ConfigurationScanner();
        this.binaryScanner = new BinaryScanner();
        this.containerImageScanner = new ContainerImageScanner();
        this.processorRegistry = new AnalysisInputProcessorRegistry();
        this.inputAdapterRegistry = new InputAdapterRegistry();
    }

    public AnalysisService(JavaSourceScanner scanner, InventoryClassifier inventoryClassifier,
                           RiskEngine riskEngine, PQCRecommendationEngine pqcEngine,
                           CBOMGenerator cbomGenerator, QuantumRiskEngine quantumRiskEngine,
                           MavenDependencyScanner mavenDependencyScanner,
                           CertificateArtifactScanner certificateArtifactScanner,
                           ConfigurationScanner configurationScanner) {
        this.scanner = scanner;
        this.inventoryClassifier = inventoryClassifier;
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
        this.quantumRiskEngine = quantumRiskEngine;
        this.mavenDependencyScanner = mavenDependencyScanner;
        this.certificateArtifactScanner = certificateArtifactScanner;
        this.configurationScanner = configurationScanner != null ? configurationScanner : new ConfigurationScanner();
        this.binaryScanner = new BinaryScanner();
        this.containerImageScanner = new ContainerImageScanner();
        this.processorRegistry = new AnalysisInputProcessorRegistry();
        this.inputAdapterRegistry = new InputAdapterRegistry();
    }

    public AnalysisService(JavaSourceScanner scanner, InventoryClassifier inventoryClassifier,
                           RiskEngine riskEngine, PQCRecommendationEngine pqcEngine,
                           CBOMGenerator cbomGenerator, QuantumRiskEngine quantumRiskEngine,
                           MavenDependencyScanner mavenDependencyScanner,
                           CertificateArtifactScanner certificateArtifactScanner,
                           ConfigurationScanner configurationScanner,
                           AnalysisInputProcessorRegistry processorRegistry) {
        this.scanner = scanner;
        this.inventoryClassifier = inventoryClassifier;
        this.riskEngine = riskEngine;
        this.pqcEngine = pqcEngine;
        this.cbomGenerator = cbomGenerator;
        this.quantumRiskEngine = quantumRiskEngine;
        this.mavenDependencyScanner = mavenDependencyScanner;
        this.certificateArtifactScanner = certificateArtifactScanner;
        this.configurationScanner = configurationScanner != null ? configurationScanner : new ConfigurationScanner();
        this.binaryScanner = new BinaryScanner();
        this.containerImageScanner = new ContainerImageScanner();
        this.processorRegistry = processorRegistry != null ? processorRegistry : new AnalysisInputProcessorRegistry();
        this.inputAdapterRegistry = new InputAdapterRegistry();
    }

    /**
     * Executes the multi-input discovery pipeline for a given unified AnalysisInput.
     *
     * @param input the analysis input specifying input type, source, and context
     * @return integrated AnalysisResponse
     */
    public AnalysisResponse analyze(AnalysisInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Analysis input must not be null.");
        }

        if (input.getInputType() == null) {
            throw new IllegalArgumentException("Analysis input must include an input type.");
        }

        AnalysisInputProcessor processor = processorRegistry.getProcessor(input.getInputType());
        ProjectAnalysisContext context = input.getContext();
        if (context == null) {
            context = new ProjectAnalysisContext();
        }

        try (ScanWorkspace workspace = processor.process(input)) {
            AnalysisResponse response = executePipeline(
                    workspace.getSourcePath().toString(),
                    input.getSourceIdentifier(),
                    context
            );
            response.setInputType(input.getInputType().name());
            response.setInputName(input.getOriginalName());
            response.setInputSource(input.getSourceIdentifier());
            return response;
        } catch (UnsupportedInputException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Failed to process scan workspace: " + e.getMessage(), e);
        }
    }

    /**
     * Legacy adapter method for ScanRequest.
     */
    public AnalysisResponse analyze(ScanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Scan request must not be null.");
        }

        if (request.getInputType() == null) {
            throw new IllegalArgumentException("Scan request must include an input type.");
        }

        AnalysisInputType analysisType = request.getInputType().toAnalysisInputType();
        AnalysisInput input = new AnalysisInput(analysisType, request.getSourceIdentifier());
        input.setArchiveFile(request.getArchiveFile());
        input.setSourceFile(request.getArchiveFile());
        input.setConfigurationFile(request.getArchiveFile());
        input.setBinaryFile(request.getArchiveFile());
        input.setDirectoryPath(request.getDirectoryPath());
        input.setRepositoryUrl(request.getRepositoryUrl());
        input.setProjectName(request.getProjectName());
        input.setContext(request.getContext());
        input.setScopes(request.getScopes());

        return analyze(input);
    }

    /**
     * Executes the end-to-end cryptographic analysis pipeline on a target source directory.
     */
    public AnalysisResponse analyzeDirectory(String sourcePath, ProjectAnalysisContext context) {
        AnalysisInput input = AnalysisInput.forDirectory(sourcePath, context);
        return analyze(input);
    }

    /**
     * Extracts an uploaded zip archive safely and executes the cryptographic analysis pipeline.
     */
    public AnalysisResponse analyzeArchive(MultipartFile file, ProjectAnalysisContext context) {
        AnalysisInput input = AnalysisInput.forZip(file, context);
        return analyze(input);
    }

    /**
     * Analyzes an uploaded single source file (.java) safely and executes the Java AST scanner.
     */
    public AnalysisResponse analyzeSourceFile(MultipartFile file, ProjectAnalysisContext context) {
        AnalysisInput input = AnalysisInput.forSourceFile(file, context);
        return analyze(input);
    }

    /**
     * Analyzes an uploaded configuration file (.properties, .yml, .yaml, .xml, .conf, .cfg, .ini).
     */
    public AnalysisResponse analyzeConfigurationFile(MultipartFile file, ProjectAnalysisContext context) {
        AnalysisInput input = AnalysisInput.forConfiguration(file, context);
        return analyze(input);
    }

    /**
     * Analyzes an uploaded binary file (.jar, .class).
     */
    public AnalysisResponse analyzeBinaryFile(MultipartFile file, ProjectAnalysisContext context) {
        AnalysisInput input = AnalysisInput.forBinary(file, context);
        return analyze(input);
    }

    /**
     * Analyzes an uploaded container image archive (.tar, .tar.gz, .tgz).
     */
    public AnalysisResponse analyzeContainerFile(MultipartFile file, ProjectAnalysisContext context) {
        AnalysisInput input = AnalysisInput.forContainer(file, context);
        return analyze(input);
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

        // Phase 1.7 — Configuration File Discovery
        List<ConfigurationFinding> configurationFindings = configurationScanner.scanDirectory(scanDirectory);
        List<CryptoFinding> configurationCryptoFindings = configurationScanner.convertToCryptoFindings(configurationFindings);
        findings.addAll(configurationCryptoFindings);

        // Phase 1.8 — Binary File Discovery (JAR/CLASS)
        try {
            Path scanPath = Paths.get(scanDirectory);
            if (Files.isDirectory(scanPath)) {
                List<CryptoFinding> binaryFindings = binaryScanner.scanDirectory(scanPath);
                findings.addAll(binaryFindings);
            }
        } catch (IOException e) {
            // Log but don't fail the entire analysis if binary scanning fails
            logger.warn("Binary file scanning failed: {}", e.getMessage());
        }

        // Phase 1.9 — Container Image Discovery (if input is container)
        try {
            Path scanPath = Paths.get(scanDirectory);
            if (Files.isDirectory(scanPath)) {
                // Check if this is a container image by looking for manifest.json
                Path manifestPath = scanPath.resolve("manifest.json");
                if (Files.exists(manifestPath)) {
                    logger.info("Container image detected, scanning artifacts...");
                    List<CryptoFinding> containerFindings = containerImageScanner.scanExtractedImage(scanPath, displayPath);
                    findings.addAll(containerFindings);
                    logger.info("Container image scan added {} findings", containerFindings.size());
                }
            }
        } catch (IOException e) {
            // Log but don't fail the entire analysis if container scanning fails
            logger.warn("Container image scanning failed: {}", e.getMessage());
        }

        // Apply business context to findings
        applyContextToFindings(findings, context);

        // Phase 7 Layer — Inventory Classification
        List<CryptoAsset> cryptoAssets = inventoryClassifier.classifyAll(findings);

        // Phase 2 — Risk Assessment
        List<RiskAssessment> riskAssessments = new ArrayList<>();
        for (CryptoFinding finding : findings) {
            // Pass business context to risk engine for more accurate scoring
            BusinessCriticality businessCriticality = context != null ? context.getBusinessCriticality() : null;
            DataSensitivity dataSensitivity = context != null ? context.getDataSensitivity() : null;
            RiskAssessment assessment = riskEngine.assessRisk(finding, businessCriticality, dataSensitivity);
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
        response.setConfigurationFindings(configurationFindings);
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
            if (finding.getLibrary() == null || finding.getLibrary().isEmpty() || "Java Cryptography Architecture (JCA)".equals(finding.getLibrary())) {
                // Try to match with known crypto libraries from dependencies
                for (MavenDependencyFinding depFinding : dependencyFindings) {
                    if (depFinding.isCryptoRelated() && depFinding.getCryptoLibraryName() != null) {
                        // Associate the finding with the crypto library
                        finding.setLibrary(depFinding.getCryptoLibraryName());
                        finding.setSourceType("JAVA_AST_MAVEN");
                        break;
                    }
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

    public AnalysisInputProcessorRegistry getProcessorRegistry() {
        return processorRegistry;
    }

    public InputAdapterRegistry getInputAdapterRegistry() {
        return inputAdapterRegistry;
    }
}
