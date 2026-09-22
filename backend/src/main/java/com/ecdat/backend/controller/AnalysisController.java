package com.ecdat.backend.controller;

import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.AnalyzeRequest;
import com.ecdat.backend.dto.CapabilitiesResponse;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.input.AnalysisInput;
import com.ecdat.backend.service.AnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;
    
    @Value("${ecdat.allowed.analysis-directory:}")
    private String allowedAnalysisDirectory;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Returns the list of supported and planned input source capabilities.
     *
     * @return structured capabilities response
     */
    @GetMapping("/analyze/capabilities")
    public ResponseEntity<CapabilitiesResponse> getCapabilities() {
        return ResponseEntity.ok(new CapabilitiesResponse(analysisService.getProcessorRegistry().getCapabilities()));
    }

    /**
     * Analyzes a local or accessible filesystem project directory.
     * SECURITY: This endpoint is restricted to a configured safe directory.
     * If no allowed directory is configured, this endpoint returns an error.
     *
     * @param request JSON payload containing directory path and optional analysis context
     * @return integrated analysis response
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzePath(@RequestBody(required = false) AnalyzeRequest request) {
        // Security check: require allowed directory configuration
        if (allowedAnalysisDirectory == null || allowedAnalysisDirectory.trim().isEmpty()) {
            throw new SecurityException("Path-based analysis is disabled. Please use the ZIP upload endpoint instead.");
        }
        
        if (request == null || request.getPath() == null) {
            throw new IllegalArgumentException("Request body must include a valid 'path' or 'sourcePath'.");
        }
        
        // Security check: validate the path is within allowed directory
        Path requestedPath = Paths.get(request.getPath()).normalize().toAbsolutePath();
        Path allowedPath = Paths.get(allowedAnalysisDirectory).normalize().toAbsolutePath();
        
        if (!requestedPath.startsWith(allowedPath)) {
            throw new SecurityException("Access denied: requested path is outside the allowed analysis directory.");
        }
        
        ProjectAnalysisContext context = request.getContext();
        if (context == null) {
            context = new ProjectAnalysisContext(); // Use defaults
        }
        
        AnalysisResponse response = analysisService.analyzeDirectory(request.getPath(), context);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes an uploaded project archive (.zip) or direct source file (.java).
     *
     * @param file multipart zip archive or source file
     * @param context optional project analysis context parameters
     * @return integrated analysis response
     */
    @PostMapping(value = "/analyze/upload", consumes = "multipart/form-data")
    public ResponseEntity<AnalysisResponse> analyzeUpload(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "applicationName", required = false) String applicationName,
            @RequestParam(value = "businessCriticality", required = false) String businessCriticality,
            @RequestParam(value = "dataSensitivity", required = false) String dataSensitivity,
            @RequestParam(value = "dataLifetimeYears", required = false) Integer dataLifetimeYears,
            @RequestParam(value = "migrationTimeYears", required = false) Integer migrationTimeYears,
            @RequestParam(value = "threatHorizonYears", required = false) Integer threatHorizonYears) {
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file 'file' is required and must not be empty.");
        }
        
        ProjectAnalysisContext context = buildContext(
                applicationName, businessCriticality, dataSensitivity,
                dataLifetimeYears, migrationTimeYears, threatHorizonYears
        );
        
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".java")) {
            AnalysisResponse response = analysisService.analyzeSourceFile(file, context);
            return ResponseEntity.ok(response);
        }
        
        AnalysisResponse response = analysisService.analyzeArchive(file, context);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes an uploaded direct Java source file (.java).
     *
     * @param file multipart Java source file
     * @param context optional project analysis context parameters
     * @return integrated analysis response
     */
    @PostMapping(value = "/analyze/source", consumes = "multipart/form-data")
    public ResponseEntity<AnalysisResponse> analyzeSource(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "applicationName", required = false) String applicationName,
            @RequestParam(value = "businessCriticality", required = false) String businessCriticality,
            @RequestParam(value = "dataSensitivity", required = false) String dataSensitivity,
            @RequestParam(value = "dataLifetimeYears", required = false) Integer dataLifetimeYears,
            @RequestParam(value = "migrationTimeYears", required = false) Integer migrationTimeYears,
            @RequestParam(value = "threatHorizonYears", required = false) Integer threatHorizonYears) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file 'file' is required and must not be empty.");
        }

        ProjectAnalysisContext context = buildContext(
                applicationName, businessCriticality, dataSensitivity,
                dataLifetimeYears, migrationTimeYears, threatHorizonYears
        );

        AnalysisResponse response = analysisService.analyzeSourceFile(file, context);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes an uploaded configuration file (.properties, .yml, .yaml, .xml, .conf, .cfg, .ini).
     *
     * @param file multipart configuration file
     * @param context optional project analysis context parameters
     * @return integrated analysis response
     */
    @PostMapping(value = "/analyze/configuration", consumes = "multipart/form-data")
    public ResponseEntity<AnalysisResponse> analyzeConfiguration(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "applicationName", required = false) String applicationName,
            @RequestParam(value = "businessCriticality", required = false) String businessCriticality,
            @RequestParam(value = "dataSensitivity", required = false) String dataSensitivity,
            @RequestParam(value = "dataLifetimeYears", required = false) Integer dataLifetimeYears,
            @RequestParam(value = "migrationTimeYears", required = false) Integer migrationTimeYears,
            @RequestParam(value = "threatHorizonYears", required = false) Integer threatHorizonYears) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file 'file' is required and must not be empty.");
        }

        ProjectAnalysisContext context = buildContext(
                applicationName, businessCriticality, dataSensitivity,
                dataLifetimeYears, migrationTimeYears, threatHorizonYears
        );

        AnalysisInput input = AnalysisInput.forConfiguration(file, context);
        AnalysisResponse response = analysisService.analyze(input);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes an uploaded binary file (.jar, .class).
     * SECURITY: Binary files are analyzed statically without execution.
     *
     * @param file multipart binary file (JAR or CLASS)
     * @param context optional project analysis context parameters
     * @return integrated analysis response
     */
    @PostMapping(value = "/analyze/binary", consumes = "multipart/form-data")
    public ResponseEntity<AnalysisResponse> analyzeBinary(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "applicationName", required = false) String applicationName,
            @RequestParam(value = "businessCriticality", required = false) String businessCriticality,
            @RequestParam(value = "dataSensitivity", required = false) String dataSensitivity,
            @RequestParam(value = "dataLifetimeYears", required = false) Integer dataLifetimeYears,
            @RequestParam(value = "migrationTimeYears", required = false) Integer migrationTimeYears,
            @RequestParam(value = "threatHorizonYears", required = false) Integer threatHorizonYears) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file 'file' is required and must not be empty.");
        }

        ProjectAnalysisContext context = buildContext(
                applicationName, businessCriticality, dataSensitivity,
                dataLifetimeYears, migrationTimeYears, threatHorizonYears
        );

        AnalysisResponse response = analysisService.analyzeBinaryFile(file, context);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes an uploaded container image archive (.tar, .tar.gz, .tgz).
     * SECURITY: Container images are analyzed statically without execution.
     * NO Docker daemon is required. NO container code is executed.
     *
     * @param file multipart container image archive
     * @param context optional project analysis context parameters
     * @return integrated analysis response
     */
    @PostMapping(value = "/analyze/container", consumes = "multipart/form-data")
    public ResponseEntity<AnalysisResponse> analyzeContainer(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "applicationName", required = false) String applicationName,
            @RequestParam(value = "businessCriticality", required = false) String businessCriticality,
            @RequestParam(value = "dataSensitivity", required = false) String dataSensitivity,
            @RequestParam(value = "dataLifetimeYears", required = false) Integer dataLifetimeYears,
            @RequestParam(value = "migrationTimeYears", required = false) Integer migrationTimeYears,
            @RequestParam(value = "threatHorizonYears", required = false) Integer threatHorizonYears) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file 'file' is required and must not be empty.");
        }

        ProjectAnalysisContext context = buildContext(
                applicationName, businessCriticality, dataSensitivity,
                dataLifetimeYears, migrationTimeYears, threatHorizonYears
        );

        AnalysisInput input = AnalysisInput.forContainer(file, context);
        AnalysisResponse response = analysisService.analyze(input);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes a public Git repository URL.
     * SECURITY: Only public HTTPS Git repositories are supported.
     * Private repositories requiring authentication are not supported.
     *
     * @param request JSON payload containing repository URL and optional analysis context
     * @return integrated analysis response
     */
    @PostMapping("/analyze/repository")
    public ResponseEntity<AnalysisResponse> analyzeRepository(@RequestBody AnalyzeRequest request) {
        if (request == null || request.getRepositoryUrl() == null) {
            throw new IllegalArgumentException("Request body must include a valid 'repositoryUrl'.");
        }
        
        ProjectAnalysisContext context = request.getContext();
        if (context == null) {
            context = new ProjectAnalysisContext();
        }
        
        AnalysisInput input = AnalysisInput.forRepository(request.getRepositoryUrl(), context);
        if (request.getApplicationName() != null) {
            input.setProjectName(request.getApplicationName());
        }
        
        AnalysisResponse response = analysisService.analyze(input);
        return ResponseEntity.ok(response);
    }

    private ProjectAnalysisContext buildContext(
            String applicationName,
            String businessCriticality,
            String dataSensitivity,
            Integer dataLifetimeYears,
            Integer migrationTimeYears,
            Integer threatHorizonYears) {
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        if (applicationName != null) {
            context.setApplicationName(applicationName);
        }
        if (businessCriticality != null) {
            try {
                context.setBusinessCriticality(com.ecdat.backend.inventory.BusinessCriticality.valueOf(businessCriticality.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep default if invalid value
            }
        }
        if (dataSensitivity != null) {
            try {
                context.setDataSensitivity(com.ecdat.backend.inventory.DataSensitivity.valueOf(dataSensitivity.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep default if invalid value
            }
        }
        if (dataLifetimeYears != null) {
            context.setDataLifetimeYears(dataLifetimeYears);
        }
        if (migrationTimeYears != null) {
            context.setMigrationTimeYears(migrationTimeYears);
        }
        if (threatHorizonYears != null) {
            context.setThreatHorizonYears(threatHorizonYears);
        }
        return context;
    }
}
