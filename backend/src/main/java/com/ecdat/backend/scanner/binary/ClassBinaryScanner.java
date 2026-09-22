package com.ecdat.backend.scanner.binary;

import com.ecdat.backend.scanner.CryptoFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Scanner for standalone .class files that analyzes bytecode without execution.
 */
public class ClassBinaryScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassBinaryScanner.class);
    private final BytecodeAnalyzer bytecodeAnalyzer;

    public ClassBinaryScanner() {
        this.bytecodeAnalyzer = new BytecodeAnalyzer();
    }

    public ClassBinaryScanner(BytecodeAnalyzer bytecodeAnalyzer) {
        this.bytecodeAnalyzer = bytecodeAnalyzer != null ? bytecodeAnalyzer : new BytecodeAnalyzer();
    }

    /**
     * Scans a standalone .class file and extracts cryptographic findings from bytecode.
     *
     * @param classPath path to the .class file
     * @return list of crypto findings
     * @throws IOException if class file cannot be read or limits are exceeded
     */
    public List<CryptoFinding> scanClass(Path classPath) throws IOException {
        if (classPath == null || !Files.exists(classPath)) {
            throw new IllegalArgumentException("Class file does not exist: " + classPath);
        }

        String fileName = classPath.getFileName().toString();
        if (!fileName.toLowerCase().endsWith(".class")) {
            throw new IllegalArgumentException("File is not a .class file: " + fileName);
        }

        long fileSize = Files.size(classPath);
        if (fileSize > BinaryAnalysisLimits.MAX_CLASS_FILE_SIZE) {
            throw new SecurityException("Class file exceeds maximum size limit: " + fileSize);
        }

        String artifactName = fileName;
        List<CryptoFinding> findings;

        try {
            findings = bytecodeAnalyzer.analyzeClassFile(classPath, artifactName);
        } catch (Exception e) {
            logger.warn("Failed to analyze class file {}: {}", classPath, e.getMessage());
            // Return a low-confidence error finding instead of crashing
            CryptoFinding errorFinding = createErrorFinding(classPath, artifactName, e.getMessage());
            return List.of(errorFinding);
        }

        logger.info("Scanned class file: {}, findings: {}", fileName, findings.size());
        return findings;
    }

    /**
     * Creates an error finding for analysis failures.
     */
    private CryptoFinding createErrorFinding(Path classPath, String artifactName, String errorMessage) {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setFile(classPath.toString());
        finding.setLine(1);
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setSourceType("BINARY");
        finding.setEvidence("Bytecode analysis error: " + errorMessage);
        finding.setLibrary("Java Bytecode");
        return finding;
    }

    /**
     * Extracts class name from file path.
     */
    private String extractClassName(Path classPath) {
        String fileName = classPath.getFileName().toString();
        if (fileName.endsWith(".class")) {
            return fileName.substring(0, fileName.length() - 6);
        }
        return fileName;
    }
}
