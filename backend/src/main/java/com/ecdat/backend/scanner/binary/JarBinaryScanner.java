package com.ecdat.backend.scanner.binary;

import com.ecdat.backend.scanner.CryptoFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Scanner for JAR files that analyzes bytecode without execution.
 * Applies archive safety limits and ZIP Slip protection.
 */
public class JarBinaryScanner {

    private static final Logger logger = LoggerFactory.getLogger(JarBinaryScanner.class);
    private final BytecodeAnalyzer bytecodeAnalyzer;

    public JarBinaryScanner() {
        this.bytecodeAnalyzer = new BytecodeAnalyzer();
    }

    public JarBinaryScanner(BytecodeAnalyzer bytecodeAnalyzer) {
        this.bytecodeAnalyzer = bytecodeAnalyzer != null ? bytecodeAnalyzer : new BytecodeAnalyzer();
    }

    /**
     * Scans a JAR file and extracts cryptographic findings from bytecode.
     *
     * @param jarPath path to the JAR file
     * @return list of crypto findings
     * @throws IOException if JAR cannot be read or limits are exceeded
     */
    public List<CryptoFinding> scanJar(Path jarPath) throws IOException {
        if (jarPath == null || !Files.exists(jarPath)) {
            throw new IllegalArgumentException("JAR file does not exist: " + jarPath);
        }

        long fileSize = Files.size(jarPath);
        if (fileSize > BinaryAnalysisLimits.MAX_BINARY_FILE_SIZE) {
            throw new SecurityException("JAR file exceeds maximum size limit: " + fileSize);
        }

        String artifactName = jarPath.getFileName().toString();
        List<CryptoFinding> findings = new ArrayList<>();
        int classCount = 0;
        long totalUncompressedSize = 0;

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // Skip directories
                if (entry.isDirectory()) {
                    continue;
                }

                // Check entry count limit
                if (classCount > BinaryAnalysisLimits.MAX_JAR_ENTRIES) {
                    logger.warn("JAR exceeds maximum entry count: {}", classCount);
                    break;
                }

                // Check total uncompressed size
                totalUncompressedSize += entry.getSize();
                if (totalUncompressedSize > BinaryAnalysisLimits.MAX_TOTAL_UNCOMPRESSED_SIZE) {
                    logger.warn("JAR exceeds maximum total uncompressed size: {}", totalUncompressedSize);
                    break;
                }

                String entryName = entry.getName();

                // Skip META-INF/MANIFEST.MF and other non-class resources
                if (entryName.equals("META-INF/MANIFEST.MF") || !entryName.endsWith(".class")) {
                    continue;
                }

                // Check class count limit
                classCount++;
                if (classCount > BinaryAnalysisLimits.MAX_CLASSES_PER_JAR) {
                    logger.warn("JAR exceeds maximum class count: {}", classCount);
                    break;
                }

                // Analyze the class file
                try {
                    List<CryptoFinding> classFindings = analyzeClassEntry(jarFile, entry, artifactName);
                    findings.addAll(classFindings);
                } catch (Exception e) {
                    // Malformed bytecode should not crash the entire scan
                    logger.warn("Failed to analyze class entry {}: {}", entryName, e.getMessage());
                    // Create a low-confidence finding for the error
                    CryptoFinding errorFinding = createErrorFinding(artifactName, entryName, e.getMessage());
                    findings.add(errorFinding);
                }
            }
        }

        logger.info("Scanned JAR: {}, classes: {}, findings: {}", artifactName, classCount, findings.size());
        return findings;
    }

    /**
     * Analyzes a single class entry within a JAR.
     */
    private List<CryptoFinding> analyzeClassEntry(JarFile jarFile, JarEntry entry, String artifactName) throws IOException {
        List<CryptoFinding> findings = new ArrayList<>();

        try (InputStream is = jarFile.getInputStream(entry)) {
            // Read the class bytes and analyze
            // Note: We extract to a temporary file for BytecodeAnalyzer which expects a Path
            // This is safe because we're within the JAR's already-validated structure
            Path tempClassFile = extractToTempFile(is, entry.getName());
            try {
                findings = bytecodeAnalyzer.analyzeClassFile(tempClassFile, artifactName);
            } finally {
                Files.deleteIfExists(tempClassFile);
            }
        }

        return findings;
    }

    /**
     * Extracts a class entry to a temporary file for analysis.
     */
    private Path extractToTempFile(InputStream is, String entryName) throws IOException {
        Path tempFile = Files.createTempFile("ecdat-class-", ".class");
        Files.copy(is, tempFile);
        return tempFile;
    }

    /**
     * Creates an error finding for analysis failures.
     */
    private CryptoFinding createErrorFinding(String artifactName, String entryName, String errorMessage) {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setFile(artifactName + "!" + entryName);
        finding.setLine(1);
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setSourceType("BINARY");
        finding.setEvidence("Bytecode analysis error: " + errorMessage);
        finding.setLibrary("Java Bytecode");
        return finding;
    }

    /**
     * Extracts class name from entry path.
     */
    private String extractClassName(String entryName) {
        if (entryName.endsWith(".class")) {
            String name = entryName.substring(0, entryName.length() - 6);
            return name.replace('/', '.');
        }
        return entryName;
    }
}
