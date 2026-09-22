package com.ecdat.backend.scanner.binary;

import com.ecdat.backend.scanner.CryptoFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified binary scanner that handles both JAR and CLASS files.
 * Delegates to JarBinaryScanner or ClassBinaryScanner based on file type.
 */
public class BinaryScanner {

    private static final Logger logger = LoggerFactory.getLogger(BinaryScanner.class);
    private final JarBinaryScanner jarScanner;
    private final ClassBinaryScanner classScanner;

    public BinaryScanner() {
        this.jarScanner = new JarBinaryScanner();
        this.classScanner = new ClassBinaryScanner();
    }

    public BinaryScanner(JarBinaryScanner jarScanner, ClassBinaryScanner classScanner) {
        this.jarScanner = jarScanner != null ? jarScanner : new JarBinaryScanner();
        this.classScanner = classScanner != null ? classScanner : new ClassBinaryScanner();
    }

    /**
     * Scans a binary file (JAR or CLASS) and extracts cryptographic findings.
     *
     * @param binaryPath path to the binary file
     * @return list of crypto findings
     * @throws IOException if file cannot be read or limits are exceeded
     */
    public List<CryptoFinding> scanBinary(Path binaryPath) throws IOException {
        return scanFile(binaryPath);
    }

    /**
     * Scans a single binary file (JAR or CLASS) and extracts cryptographic findings.
     *
     * @param filePath path to the binary file
     * @return list of crypto findings
     * @throws IOException if file cannot be read or limits are exceeded
     */
    public List<CryptoFinding> scanFile(Path filePath) throws IOException {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("Binary file does not exist: " + filePath);
        }

        String fileName = filePath.getFileName().toString();
        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".jar")) {
            logger.info("Scanning JAR file: {}", fileName);
            return jarScanner.scanJar(filePath);
        } else if (lowerName.endsWith(".class")) {
            logger.info("Scanning CLASS file: {}", fileName);
            return classScanner.scanClass(filePath);
        } else {
            throw new IllegalArgumentException("Unsupported binary file type: " + fileName + 
                ". Only .jar and .class files are supported.");
        }
    }

    /**
     * Scans a directory for binary files (JAR and CLASS) and aggregates findings.
     *
     * @param directoryPath path to the directory to scan
     * @return list of crypto findings from all binary files
     * @throws IOException if directory cannot be read
     */
    public List<CryptoFinding> scanDirectory(Path directoryPath) throws IOException {
        if (directoryPath == null || !Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("Directory does not exist or is not a directory: " + directoryPath);
        }

        List<CryptoFinding> allFindings = new ArrayList<>();

        try (var stream = Files.walk(directoryPath)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                return fileName.endsWith(".jar") || fileName.endsWith(".class");
            }).forEach(path -> {
                try {
                    List<CryptoFinding> findings = scanBinary(path);
                    allFindings.addAll(findings);
                } catch (IOException e) {
                    logger.warn("Failed to scan binary file {}: {}", path, e.getMessage());
                    // Continue scanning other files
                }
            });
        }

        logger.info("Scanned directory for binaries: {}, total findings: {}", directoryPath, allFindings.size());
        return allFindings;
    }
}
