package com.ecdat.backend.controller;

import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.AnalyzeRequest;
import com.ecdat.backend.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Analyzes a local or accessible filesystem project directory.
     *
     * @param request JSON payload containing directory path
     * @return integrated analysis response
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzePath(@RequestBody(required = false) AnalyzeRequest request) {
        if (request == null || request.getPath() == null) {
            throw new IllegalArgumentException("Request body must include a valid 'path' or 'sourcePath'.");
        }
        AnalysisResponse response = analysisService.analyzeDirectory(request.getPath());
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes an uploaded project archive (.zip).
     *
     * @param file multipart zip archive
     * @return integrated analysis response
     */
    @PostMapping(value = "/analyze/upload", consumes = "multipart/form-data")
    public ResponseEntity<AnalysisResponse> analyzeUpload(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart file 'file' is required and must not be empty.");
        }
        AnalysisResponse response = analysisService.analyzeArchive(file);
        return ResponseEntity.ok(response);
    }
}
