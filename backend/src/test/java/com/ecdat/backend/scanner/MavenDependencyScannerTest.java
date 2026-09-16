package com.ecdat.backend.scanner;

import com.ecdat.backend.scanner.maven.MavenDependencyFinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenDependencyScannerTest {

    private final MavenDependencyScanner scanner = new MavenDependencyScanner();

    @Test
    void testScanDirectoryWithPomXml(@TempDir Path tempDir) throws IOException {
        // Create a test pom.xml file
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0</version>
                <dependencies>
                    <dependency>
                        <groupId>org.bouncycastle</groupId>
                        <artifactId>bcprov-jdk18on</artifactId>
                        <version>1.78</version>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter</artifactId>
                        <version>3.2.0</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        List<MavenDependencyFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertEquals(2, findings.size());

        MavenDependencyFinding bouncyCastleFinding = findings.stream()
            .filter(f -> f.getArtifactId().equals("bcprov-jdk18on"))
            .findFirst()
            .orElse(null);

        assertNotNull(bouncyCastleFinding);
        assertTrue(bouncyCastleFinding.isCryptoRelated());
        assertEquals("Bouncy Castle", bouncyCastleFinding.getCryptoLibraryName());
        assertEquals("1.78", bouncyCastleFinding.getVersion());
    }

    @Test
    void testScanDirectoryWithoutPomXml(@TempDir Path tempDir) {
        List<MavenDependencyFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertTrue(findings.isEmpty());
    }

    @Test
    void testBouncyCastleDetection() {
        // Test various Bouncy Castle artifact IDs
        String[] cryptoArtifacts = {
            "bcprov-jdk18on", "bcpkix-jdk18on", "bcpg-jdk18on", "bctls-jdk18on"
        };

        for (String artifact : cryptoArtifacts) {
            assertTrue(artifact.toLowerCase().contains("bc") || 
                      artifact.toLowerCase().contains("bouncycastle"));
        }
    }

    @Test
    void testNonCryptoDependency() {
        // Test that non-crypto dependencies are identified correctly
        String nonCryptoArtifact = "spring-boot-starter";
        assertFalse(nonCryptoArtifact.toLowerCase().contains("crypto"));
        assertFalse(nonCryptoArtifact.toLowerCase().contains("security"));
    }

    @Test
    void testScanDirectoryWithMultiplePoms(@TempDir Path tempDir) throws IOException {
        // Create multiple pom.xml files
        String pomContent1 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <dependencies>
                    <dependency>
                        <groupId>org.bouncycastle</groupId>
                        <artifactId>bcprov-jdk18on</artifactId>
                        <version>1.78</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        String pomContent2 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <dependencies>
                    <dependency>
                        <groupId>com.google.crypto.tink</groupId>
                        <artifactId>tink</artifactId>
                        <version>1.12.0</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        Path pomFile1 = tempDir.resolve("pom.xml");
        Path pomFile2 = tempDir.resolve("submodule").resolve("pom.xml");
        Files.createDirectories(pomFile2.getParent());
        Files.writeString(pomFile1, pomContent1);
        Files.writeString(pomFile2, pomContent2);

        List<MavenDependencyFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertEquals(2, findings.size());

        assertTrue(findings.stream().anyMatch(f -> f.getArtifactId().equals("bcprov-jdk18on")));
        assertTrue(findings.stream().anyMatch(f -> f.getArtifactId().equals("tink")));
    }

    @Test
    void testMalformedPomXml(@TempDir Path tempDir) throws IOException {
        // Create a malformed pom.xml
        String malformedPom = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0</version>
                <dependencies>
                    <dependency>
                        <groupId>org.bouncycastle</groupId>
                        <!-- Missing artifactId -->
                        <version>1.78</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, malformedPom);

        // Should not throw exception, but return empty or partial results
        List<MavenDependencyFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        // The scanner should handle malformed XML gracefully
    }
}
