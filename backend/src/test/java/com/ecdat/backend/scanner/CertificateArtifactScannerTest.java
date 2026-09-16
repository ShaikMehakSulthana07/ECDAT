package com.ecdat.backend.scanner;

import com.ecdat.backend.scanner.certificate.CertificateArtifactFinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CertificateArtifactScannerTest {

    private final CertificateArtifactScanner scanner = new CertificateArtifactScanner();

    @Test
    void testScanDirectoryWithCertificateFiles(@TempDir Path tempDir) throws IOException {
        // Create a mock PEM certificate file
        String pemContent = """
            -----BEGIN CERTIFICATE-----
            MIIBkTCB+wIJAKH1g6Z8p0+9MA0GCSqGSIb3DQEBCwUAMBExDzANBgNVBAMMBnRl
            c3RjYTAeFw0yNDAxMDEwMDAwMDBaFw0yNTAxMDEwMDAwMDBaMBExDzANBgNVBAMM
            BnRlc3RjYTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAuX5x8t8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            CAwEAATANBgkqhkiG9w0BAQsFAAOBgQBAuX5x8t8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8p8
            -----END CERTIFICATE-----
            """;

        Path pemFile = tempDir.resolve("test-cert.pem");
        Files.writeString(pemFile, pemContent);

        List<CertificateArtifactFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertFalse(findings.isEmpty());
        
        CertificateArtifactFinding finding = findings.get(0);
        assertEquals("test-cert.pem", finding.getFileName());
        assertNotNull(finding.getFileType());
        // Since the mock certificate is not valid X.509, confidence should be LOW
        assertEquals(com.ecdat.backend.scanner.CryptoFinding.Confidence.LOW, finding.getConfidence());
    }

    @Test
    void testScanDirectoryWithoutCertificates(@TempDir Path tempDir) {
        List<CertificateArtifactFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertTrue(findings.isEmpty());
    }

    @Test
    void testMultipleCertificateExtensions(@TempDir Path tempDir) throws IOException {
        // Create files with different certificate extensions
        Path crtFile = tempDir.resolve("cert.crt");
        Path cerFile = tempDir.resolve("cert.cer");
        Path derFile = tempDir.resolve("cert.der");
        
        Files.writeString(crtFile, "mock crt content");
        Files.writeString(cerFile, "mock cer content");
        Files.writeString(derFile, "mock der content");

        List<CertificateArtifactFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertEquals(3, findings.size());
        
        assertTrue(findings.stream().anyMatch(f -> f.getFileName().equals("cert.crt")));
        assertTrue(findings.stream().anyMatch(f -> f.getFileName().equals("cert.cer")));
        assertTrue(findings.stream().anyMatch(f -> f.getFileName().equals("cert.der")));
    }

    @Test
    void testKeystoreFiles(@TempDir Path tempDir) throws IOException {
        // Create keystore files
        Path jksFile = tempDir.resolve("keystore.jks");
        Path p12File = tempDir.resolve("keystore.p12");
        
        Files.writeString(jksFile, "mock jks content");
        Files.writeString(p12File, "mock p12 content");

        List<CertificateArtifactFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertEquals(2, findings.size());
        
        // Keystores should be detected but with LOW confidence since we can't parse them safely
        assertTrue(findings.stream().allMatch(f -> f.getConfidence() == com.ecdat.backend.scanner.CryptoFinding.Confidence.LOW));
    }

    @Test
    void testNonCertificateFilesIgnored(@TempDir Path tempDir) throws IOException {
        // Create non-certificate files
        Path javaFile = tempDir.resolve("Test.java");
        Path txtFile = tempDir.resolve("readme.txt");
        Path xmlFile = tempDir.resolve("pom.xml");
        
        Files.writeString(javaFile, "public class Test {}");
        Files.writeString(txtFile, "readme content");
        Files.writeString(xmlFile, "<project></project>");

        List<CertificateArtifactFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertTrue(findings.isEmpty());
    }

    @Test
    void testSubdirectoryScanning(@TempDir Path tempDir) throws IOException {
        // Create certificate in subdirectory
        Path subDir = tempDir.resolve("certs");
        Files.createDirectories(subDir);
        
        Path certFile = subDir.resolve("server.crt");
        Files.writeString(certFile, "mock crt content");

        List<CertificateArtifactFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertEquals(1, findings.size());
        assertEquals("server.crt", findings.get(0).getFileName());
    }

    @Test
    void testMalformedCertificateFile(@TempDir Path tempDir) throws IOException {
        // Create a malformed PEM file
        Path malformedPem = tempDir.resolve("malformed.pem");
        Files.writeString(malformedPem, "this is not a valid certificate");

        List<CertificateArtifactFinding> findings = scanner.scanDirectory(tempDir.toString());

        assertNotNull(findings);
        assertEquals(1, findings.size());
        // Should not crash, but return LOW confidence finding
        assertEquals(com.ecdat.backend.scanner.CryptoFinding.Confidence.LOW, findings.get(0).getConfidence());
    }
}
