package com.ecdat.backend.scanner;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JavaSourceScannerTest {

    @Test
    public void testScannerFindings() {
        JavaSourceScanner scanner = new JavaSourceScanner();
        
        // Ensure path resolves correctly whether run from parent IDE or backend module
        File testTarget = new File("../test-target");
        if (!testTarget.exists()) {
            testTarget = new File("test-target");
        }
        
        List<CryptoFinding> findings = scanner.scanDirectory(testTarget.getAbsolutePath());
        
        assertFalse(findings.isEmpty(), "Scanner should produce findings.");
        
        // AES Detection
        assertTrue(findings.stream().anyMatch(f -> 
            "AES".equals(f.getAlgorithm()) && 
            "GCM".equals(f.getVariant()) && 
            CryptoFinding.Confidence.HIGH == f.getConfidence()
        ), "Failed to detect AES/GCM");

        // RSA Detection with Key Size
        assertTrue(findings.stream().anyMatch(f -> 
            "RSA".equals(f.getAlgorithm()) && 
            Integer.valueOf(2048).equals(f.getKeySize()) &&
            "RSA-2048".equals(f.getVariant())
        ), "Failed to detect RSA and extract key size");

        // ECDSA Detection
        assertTrue(findings.stream().anyMatch(f -> 
            "ECDSA".equals(f.getAlgorithm()) && 
            "SHA256withECDSA".equals(f.getVariant())
        ), "Failed to detect ECDSA");

        // Hashing Detection
        assertTrue(findings.stream().anyMatch(f -> "SHA-256".equals(f.getAlgorithm())));
        assertTrue(findings.stream().anyMatch(f -> "MD5".equals(f.getAlgorithm())));

        // TLS Detection
        assertTrue(findings.stream().anyMatch(f -> "TLS".equals(f.getAlgorithm()) && "TLSv1.3".equals(f.getVariant())));

        // Non-Literal Unknown Detection
        assertTrue(findings.stream().anyMatch(f -> 
            "UNKNOWN".equals(f.getAlgorithm()) && 
            CryptoFinding.Confidence.LOW == f.getConfidence()
        ), "Failed to flag non-literal string as UNKNOWN");

        // Path Verification
        assertTrue(findings.get(0).getFile().replace("\\", "/").contains("src/main/java/demo/"), 
            "File paths should be relative and clean");
    }
}