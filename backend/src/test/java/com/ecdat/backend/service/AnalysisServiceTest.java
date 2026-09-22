package com.ecdat.backend.service;

import com.ecdat.backend.cbom.CBOMComponent;
import com.ecdat.backend.cbom.CBOMDocument;
import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.inventory.AssetCategory;
import com.ecdat.backend.inventory.CryptoAsset;
import com.ecdat.backend.inventory.CryptoUsageCategory;
import com.ecdat.backend.inventory.LifecycleStatus;
import com.ecdat.backend.input.AnalysisInput;
import com.ecdat.backend.input.AnalysisInputType;
import com.ecdat.backend.input.ScanInputType;
import com.ecdat.backend.input.ScanRequest;
import com.ecdat.backend.input.UnsupportedInputException;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.pqc.PQCRecommendationStatus;
import com.ecdat.backend.risk.QuantumRisk;
import com.ecdat.backend.risk.RiskAssessment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisServiceTest {

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService();
    }

    private File getTestTargetDir() {
        File testTarget = new File("../test-target");
        if (!testTarget.exists()) {
            testTarget = new File("test-target");
        }
        return testTarget;
    }

    // 1. Successful analysis on real test-target
    @Test
    void testSuccessfulDirectoryAnalysis() {
        File testTarget = getTestTargetDir();
        assertTrue(testTarget.exists(), "test-target directory should exist.");

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeDirectory(testTarget.getAbsolutePath(), context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertFalse(response.getFindings().isEmpty(), "Findings should not be empty.");
        assertFalse(response.getRiskAssessments().isEmpty(), "Risk assessments should not be empty.");
        assertFalse(response.getPqcRecommendations().isEmpty(), "PQC recommendations should not be empty.");
        assertNotNull(response.getInventory(), "Inventory should not be null.");
        assertFalse(response.getCryptoAssets().isEmpty(), "CryptoAssets should not be empty.");
        assertEquals(response.getFindings().size(), response.getCryptoAssets().size());
        assertNotNull(response.getCbom(), "CBOM should not be null.");
        assertFalse(response.getCbom().getComponents().isEmpty(), "CBOM components should not be empty.");
        assertNotNull(response.getSummary(), "Summary should not be null.");
        assertEquals(response.getFindings().size(), response.getSummary().getTotalFindings());
    }

    // 2. Empty or missing input path
    @Test
    void testEmptyOrNullPathThrowsException() {
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        assertThrows(IllegalArgumentException.class, () -> analysisService.analyzeDirectory(null, context));
        assertThrows(IllegalArgumentException.class, () -> analysisService.analyzeDirectory("", context));
        assertThrows(IllegalArgumentException.class, () -> analysisService.analyzeDirectory("   ", context));
    }

    // 3. Nonexistent directory path
    @Test
    void testNonexistentPathThrowsException() {
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> analysisService.analyzeDirectory("non/existent/path/for/sure/12345", context));
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    // 4. Path is a file, not a directory
    @Test
    void testFilePathThrowsException(@TempDir Path tempDir) throws IOException {
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        Path tempFile = Files.createFile(tempDir.resolve("sample.java"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> analysisService.analyzeDirectory(tempFile.toString(), context));
        assertTrue(ex.getMessage().contains("is not a directory"));
    }

    // 5. Malformed source file is handled gracefully without terminating analysis
    @Test
    void testMalformedSourceFileHandling(@TempDir Path tempDir) throws IOException {
        // Valid file with crypto
        Path validFile = tempDir.resolve("ValidCrypto.java");
        Files.writeString(validFile, "import javax.crypto.Cipher;\npublic class ValidCrypto {\n" +
                "  public void test() throws Exception {\n" +
                "    Cipher c = Cipher.getInstance(\"AES/GCM/NoPadding\");\n" +
                "  }\n}\n");

        // Malformed unparseable java file
        Path malformedFile = tempDir.resolve("Malformed.java");
        Files.writeString(malformedFile, "public class Malformed { int x = ; void unclosed() {");

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getFindings().size());
        assertEquals("AES", response.getFindings().get(0).getAlgorithm());
        assertEquals(1, response.getCbom().getComponents().size());
    }

    // 6. Findings correctly reach the Risk Engine
    @Test
    void testFindingsReachRiskEngine(@TempDir Path tempDir) throws IOException {
        Path rsaFile = tempDir.resolve("RSASign.java");
        Files.writeString(rsaFile, "import java.security.Signature;\npublic class RSASign {\n" +
                "  public void sign() throws Exception {\n" +
                "    Signature s = Signature.getInstance(\"SHA256withECDSA\");\n" +
                "  }\n}\n");

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertEquals(1, response.getFindings().size());
        assertEquals(1, response.getRiskAssessments().size());

        RiskAssessment assessment = response.getRiskAssessments().get(0);
        assertEquals(QuantumRisk.HIGH, assessment.getQuantumRisk());
        assertEquals("ECDSA", assessment.getOriginalFinding().getAlgorithm());
        assertFalse(assessment.getReasons().isEmpty());
    }

    // 7. Risk Assessments reach the PQC Recommendation Engine
    @Test
    void testRiskReachesPQCEngine(@TempDir Path tempDir) throws IOException {
        Path rsaFile = tempDir.resolve("RSASign.java");
        Files.writeString(rsaFile, "import java.security.Signature;\npublic class RSASign {\n" +
                "  public void sign() throws Exception {\n" +
                "    Signature s = Signature.getInstance(\"SHA256withECDSA\");\n" +
                "  }\n}\n");

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertEquals(1, response.getPqcRecommendations().size());
        PQCRecommendation rec = response.getPqcRecommendations().get(0);
        assertEquals(PQCRecommendationStatus.RECOMMENDED, rec.getRecommendationStatus());
        assertEquals("ML-DSA", rec.getRecommendedAlgorithm());
        assertEquals(QuantumRisk.HIGH, rec.getQuantumRisk());
    }

    // 8. PQC Recommendations reach the CBOM Generator with inventory metadata
    @Test
    void testPQCReachesCBOM(@TempDir Path tempDir) throws IOException {
        Path rsaFile = tempDir.resolve("RSASign.java");
        Files.writeString(rsaFile, "import java.security.Signature;\npublic class RSASign {\n" +
                "  public void sign() throws Exception {\n" +
                "    Signature s = Signature.getInstance(\"SHA256withECDSA\");\n" +
                "  }\n}\n");

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        CBOMDocument cbom = response.getCbom();
        assertNotNull(cbom);
        assertEquals(1, cbom.getComponents().size());

        CBOMComponent component = cbom.getComponents().get(0);
        assertNotNull(component.getCryptoProperties());
        assertNotNull(component.getCryptoProperties().getPqcRecommendation());
        assertEquals("ML-DSA", component.getCryptoProperties().getPqcRecommendation().getRecommendedAlgorithm());
        assertEquals("RECOMMENDED", component.getCryptoProperties().getPqcRecommendation().getRecommendationStatus());
        assertEquals("DIGITAL_SIGNATURE", component.getCryptoProperties().getAssetCategory());
        assertEquals("ACTIVE", component.getCryptoProperties().getLifecycleStatus());
    }

    // 9. Zip archive analysis workflow
    @Test
    void testZipArchiveAnalysis() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("src/main/java/Demo.java");
            zos.putNextEntry(entry);
            String content = "import javax.crypto.Cipher;\npublic class Demo {\n" +
                    "  public void run() throws Exception {\n" +
                    "    Cipher c = Cipher.getInstance(\"AES/GCM/NoPadding\");\n" +
                    "  }\n}\n";
            zos.write(content.getBytes());
            zos.closeEntry();
        }

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", "project.zip", "application/zip", baos.toByteArray());

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeArchive(zipFile, context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("project.zip", response.getSourcePath());
        assertEquals(1, response.getFindings().size());
        assertEquals("AES", response.getFindings().get(0).getAlgorithm());
        assertEquals(1, response.getCbom().getComponents().size());
    }

    // 10. Zip Slip security vulnerability rejection
    @Test
    void testZipSlipRejection() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry maliciousEntry = new ZipEntry("../../evil.java");
            zos.putNextEntry(maliciousEntry);
            zos.write("public class Evil {}".getBytes());
            zos.closeEntry();
        }

        MockMultipartFile maliciousZip = new MockMultipartFile(
                "file", "malicious.zip", "application/zip", baos.toByteArray());

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        assertThrows(SecurityException.class, () -> analysisService.analyzeArchive(maliciousZip, context));
    }

    // 11. Pipeline correctly populates Enterprise Inventory and Summary
    @Test
    void testInventoryClassificationInPipeline(@TempDir Path tempDir) throws IOException {
        Path cryptoFile = tempDir.resolve("CryptoApp.java");
        Files.writeString(cryptoFile, "import javax.crypto.Cipher;\nimport java.security.MessageDigest;\n" +
                "public class CryptoApp {\n" +
                "  public void run() throws Exception {\n" +
                "    Cipher c = Cipher.getInstance(\"AES/GCM/NoPadding\");\n" +
                "    MessageDigest md = MessageDigest.getInstance(\"MD5\");\n" +
                "  }\n}\n");

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertNotNull(response.getInventory());
        assertEquals(2, response.getCryptoAssets().size());

        CryptoAsset aesAsset = response.getCryptoAssets().stream()
                .filter(a -> "AES".equals(a.getAlgorithm())).findFirst().orElseThrow();
        assertEquals(AssetCategory.ENCRYPTION, aesAsset.getAssetCategory());
        assertEquals(LifecycleStatus.ACTIVE, aesAsset.getLifecycleStatus());
        assertEquals(CryptoUsageCategory.DIRECT_USAGE, aesAsset.getUsageCategory());

        CryptoAsset md5Asset = response.getCryptoAssets().stream()
                .filter(a -> "MD5".equals(a.getAlgorithm())).findFirst().orElseThrow();
        assertEquals(AssetCategory.HASHING, md5Asset.getAssetCategory());
        assertEquals(LifecycleStatus.DEPRECATED, md5Asset.getLifecycleStatus());

        assertEquals(1, response.getSummary().getActiveAssetCount());
        assertEquals(1, response.getSummary().getDeprecatedAssetCount());
        assertEquals(2, response.getSummary().getDirectUsageCount());
    }

    @Test
    void testZipScanRequestRoutesThroughMultiInputArchitecture() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("src/main/java/Demo.java");
            zos.putNextEntry(entry);
            zos.write(("import javax.crypto.Cipher;\npublic class Demo {\n" +
                    "  public void run() throws Exception {\n" +
                    "    Cipher c = Cipher.getInstance(\"AES/GCM/NoPadding\");\n" +
                    "  }\n}\n").getBytes());
            zos.closeEntry();
        }

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", "project.zip", "application/zip", baos.toByteArray());
        ScanRequest request = ScanRequest.forZip(zipFile, new ProjectAnalysisContext());

        AnalysisResponse response = analysisService.analyze(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("project.zip", response.getSourcePath());
        assertEquals(1, response.getFindings().size());
        assertNotNull(response.getCbom());
        assertNotNull(response.getSummary());
    }

    @Test
    void testUnsupportedInputTypesDoNotReturnFakeAnalysisResults() {
        ProjectAnalysisContext context = new ProjectAnalysisContext();

        // All input types are now supported (ZIP_ARCHIVE, SOURCE_FILE, DIRECTORY, REPOSITORY_URL,
        // CONFIGURATION_FILE, BINARY_FILE, CONTAINER_IMAGE), so this test is now checking
        // only for truly invalid inputs
        assertThrows(IllegalArgumentException.class, () -> analysisService.analyze((AnalysisInput) null));
        assertThrows(IllegalArgumentException.class,
                () -> analysisService.analyze(new ScanRequest(null, "unknown")));
    }
}
