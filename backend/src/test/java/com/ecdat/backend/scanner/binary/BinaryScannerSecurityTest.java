package com.ecdat.backend.scanner.binary;

import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security tests for binary scanning (JAR/CLASS files).
 * Ensures no code execution, proper resource limits, and safe error handling.
 */
class BinaryScannerSecurityTest {

    @TempDir
    Path tempDir;

    @Test
    void testOversizedJarIsRejected() throws IOException {
        // Create a fake JAR file that exceeds size limit
        Path oversizedJar = tempDir.resolve("oversized.jar");
        byte[] data = new byte[(int) (BinaryAnalysisLimits.MAX_BINARY_FILE_SIZE + 1)];
        Files.write(oversizedJar, data);

        JarBinaryScanner scanner = new JarBinaryScanner();
        
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            scanner.scanJar(oversizedJar);
        });
        
        assertTrue(exception.getMessage().contains("exceeds maximum size limit"));
    }

    @Test
    void testOversizedClassIsRejected() throws IOException {
        // Create a fake CLASS file that exceeds size limit
        Path oversizedClass = tempDir.resolve("oversized.class");
        byte[] data = new byte[(int) (BinaryAnalysisLimits.MAX_CLASS_FILE_SIZE + 1)];
        Files.write(oversizedClass, data);

        ClassBinaryScanner scanner = new ClassBinaryScanner();
        
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            scanner.scanClass(oversizedClass);
        });
        
        assertTrue(exception.getMessage().contains("exceeds maximum size limit"));
    }

    @Test
    void testMalformedClassDoesNotCrash() throws IOException {
        // Create a malformed CLASS file (random bytes)
        Path malformedClass = tempDir.resolve("malformed.class");
        byte[] garbage = new byte[100];
        for (int i = 0; i < garbage.length; i++) {
            garbage[i] = (byte) (i % 256);
        }
        Files.write(malformedClass, garbage);

        ClassBinaryScanner scanner = new ClassBinaryScanner();
        
        // Should not crash, should return error finding
        assertDoesNotThrow(() -> {
            var findings = scanner.scanClass(malformedClass);
            assertNotNull(findings);
            assertFalse(findings.isEmpty());
            assertEquals("UNKNOWN", findings.get(0).getAlgorithm());
            assertEquals(CryptoFinding.Confidence.LOW, findings.get(0).getConfidence());
        });
    }

    @Test
    void testBinaryScannerRejectsUnsupportedFileType() throws IOException {
        // Create a file that's not JAR or CLASS
        Path unsupportedFile = tempDir.resolve("unsupported.txt");
        Files.writeString(unsupportedFile, "Not a binary file");

        BinaryScanner scanner = new BinaryScanner();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scanner.scanBinary(unsupportedFile);
        });
        
        assertTrue(exception.getMessage().contains("Unsupported binary file type"));
    }

    @Test
    void testAnalysisHandlesNullFile() {
        JarBinaryScanner scanner = new JarBinaryScanner();
        
        assertThrows(IllegalArgumentException.class, () -> {
            scanner.scanJar(null);
        });
    }

    @Test
    void testAnalysisHandlesNonExistentFile() {
        JarBinaryScanner scanner = new JarBinaryScanner();
        Path nonExistent = tempDir.resolve("does-not-exist.jar");
        
        assertThrows(IllegalArgumentException.class, () -> {
            scanner.scanJar(nonExistent);
        });
    }

    @Test
    void testNonClassFileRejectedByClassScanner() throws IOException {
        Path textFile = tempDir.resolve("fake.class");
        Files.writeString(textFile, "This is not a real class file");

        ClassBinaryScanner scanner = new ClassBinaryScanner();
        
        // Should handle gracefully (likely malformed bytecode)
        assertDoesNotThrow(() -> {
            var findings = scanner.scanClass(textFile);
            assertNotNull(findings);
        });
    }

    @Test
    void testEmptyJarDoesNotCrash() throws IOException {
        // Create an empty JAR file (which is actually invalid)
        Path emptyJar = tempDir.resolve("empty.jar");
        Files.write(emptyJar, new byte[0]);

        JarBinaryScanner scanner = new JarBinaryScanner();
        
        // Should handle gracefully - empty JAR is invalid and will throw
        assertThrows(Exception.class, () -> {
            scanner.scanJar(emptyJar);
        });
    }

    @Test
    void testBinaryScannerHandlesNullDirectory() {
        BinaryScanner scanner = new BinaryScanner();
        
        assertThrows(IllegalArgumentException.class, () -> {
            scanner.scanDirectory(null);
        });
    }

    @Test
    void testBinaryScannerHandlesNonExistentDirectory() {
        BinaryScanner scanner = new BinaryScanner();
        Path nonExistent = tempDir.resolve("does-not-exist");
        
        assertThrows(IllegalArgumentException.class, () -> {
            scanner.scanDirectory(nonExistent);
        });
    }

    @Test
    void testBinaryScannerHandlesFileInsteadOfDirectory() throws IOException {
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "content");

        BinaryScanner scanner = new BinaryScanner();
        
        assertThrows(IllegalArgumentException.class, () -> {
            scanner.scanDirectory(file);
        });
    }

    @Test
    void testSecurityLimitsAreRespected() {
        // Verify that security limits are defined and reasonable
        assertTrue(BinaryAnalysisLimits.MAX_BINARY_FILE_SIZE > 0);
        assertTrue(BinaryAnalysisLimits.MAX_JAR_ENTRIES > 0);
        assertTrue(BinaryAnalysisLimits.MAX_CLASSES_PER_JAR > 0);
        assertTrue(BinaryAnalysisLimits.MAX_NESTED_JAR_DEPTH >= 0);
        assertTrue(BinaryAnalysisLimits.MAX_TOTAL_UNCOMPRESSED_SIZE > 0);
        assertTrue(BinaryAnalysisLimits.MAX_CLASS_FILE_SIZE > 0);
        assertTrue(BinaryAnalysisLimits.MAX_METHODS_PER_CLASS > 0);
        assertTrue(BinaryAnalysisLimits.MAX_INSTRUCTIONS_PER_METHOD > 0);
    }

    @Test
    void testFindingsDoNotExposeSecrets() throws IOException {
        // Create a fake class file with potential secret-like strings
        Path fakeClass = tempDir.resolve("SecretHandler.class");
        byte[] data = new byte[200];
        // Add some strings that look like secrets
        String secretContent = "password=secret123\0apikey=abc123\0token=xyz789";
        System.arraycopy(secretContent.getBytes(), 0, data, 0, secretContent.length());
        Files.write(fakeClass, data);

        ClassBinaryScanner scanner = new ClassBinaryScanner();
        
        var findings = scanner.scanClass(fakeClass);
        
        // Findings should not contain the secret strings
        for (CryptoFinding finding : findings) {
            String evidence = finding.getEvidence();
            if (evidence != null) {
                assertFalse(evidence.contains("secret123"));
                assertFalse(evidence.contains("abc123"));
                assertFalse(evidence.contains("xyz789"));
            }
        }
    }
}
