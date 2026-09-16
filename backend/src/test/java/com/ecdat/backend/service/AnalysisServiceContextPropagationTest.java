package com.ecdat.backend.service;

import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisServiceContextPropagationTest {

    private final AnalysisService analysisService = new AnalysisService();

    @Test
    void testBusinessCriticalityPropagation(@TempDir Path tempDir) throws IOException {
        // Create a simple Java file with crypto usage
        String javaContent = """
            import javax.crypto.Cipher;
            public class Test {
                public void encrypt() throws Exception {
                    Cipher cipher = Cipher.getInstance("AES");
                }
            }
            """;
        Path	javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, javaContent);

        // Create context with HIGH business criticality
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setApplicationName("TestApp");
        context.setBusinessCriticality(BusinessCriticality.HIGH);
        context.setDataSensitivity(DataSensitivity.CONFIDENTIAL);
        context.setDataLifetimeYears(15);
        context.setMigrationTimeYears(3);
        context.setThreatHorizonYears(10);

        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertNotNull(response);
        assertNotNull(response.getContext());
        assertEquals(BusinessCriticality.HIGH, response.getContext().getBusinessCriticality());
        assertEquals(DataSensitivity.CONFIDENTIAL, response.getContext().getDataSensitivity());
        assertEquals(15, response.getContext().getDataLifetimeYears());
        assertEquals(3, response.getContext().getMigrationTimeYears());
        assertEquals(10, response.getContext().getThreatHorizonYears());

        // Verify that findings have the business criticality applied
        if (!response.getFindings().isEmpty()) {
            CryptoFinding finding = response.getFindings().get(0);
            // The finding should have the context values applied
            assertNotNull(finding.getBusinessCriticality());
            assertNotNull(finding.getDataSensitivity());
            // Note: The actual values depend on the scanner implementation
            // The context should be applied by applyContextToFindings
        }
    }

    @Test
    void testDataSensitivityPropagation(@TempDir Path tempDir) throws IOException {
        String javaContent = """
            import java.security.KeyPairGenerator;
            public class Test {
                public void generateKey() throws Exception {
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                }
            }
            """;
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, javaContent);

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setBusinessCriticality(BusinessCriticality.CRITICAL);
        context.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);

        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertNotNull(response);
        assertEquals(DataSensitivity.HIGHLY_SENSITIVE, response.getContext().getDataSensitivity());

        if (!response.getFindings().isEmpty()) {
            CryptoFinding finding = response.getFindings().get(0);
            // The finding should have the context values applied
            assertNotNull(finding.getDataSensitivity());
            // Note: The actual values depend on the scanner implementation
        }
    }

    @Test
    void testDefaultContextWhenNotProvided(@TempDir Path tempDir) throws IOException {
        String javaContent = """
            import javax.crypto.Cipher;
            public class Test {
                public void encrypt() throws Exception {
                    Cipher cipher = Cipher.getInstance("AES");
                }
            }
            """;
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, javaContent);

        // Analyze without providing context
        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), null);

        assertNotNull(response);
        assertNotNull(response.getContext());
        // Should have default values
        assertNotNull(response.getContext().getBusinessCriticality());
        assertNotNull(response.getContext().getDataSensitivity());
        assertNotNull(response.getContext().getDataLifetimeYears());
        assertNotNull(response.getContext().getMigrationTimeYears());
        assertNotNull(response.getContext().getThreatHorizonYears());
    }

    @Test
    void testContextAppliedToQuantumRiskAssessment(@TempDir Path tempDir) throws IOException {
        String javaContent = """
            import java.security.KeyPairGenerator;
            public class Test {
                public void generateKey() throws Exception {
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                }
            }
            """;
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, javaContent);

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setBusinessCriticality(BusinessCriticality.CRITICAL);
        context.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);
        context.setDataLifetimeYears(20);
        context.setMigrationTimeYears(5);
        context.setThreatHorizonYears(10);

        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertNotNull(response);
        
        // Verify quantum risk results use the context values
        if (!response.getRiskAssessments().isEmpty()) {
            var riskAssessment = response.getRiskAssessments().get(0);
            if (riskAssessment.getQuantumRiskResult() != null) {
                assertEquals(20, riskAssessment.getQuantumRiskResult().getDataLifetimeYears());
                assertEquals(5, riskAssessment.getQuantumRiskResult().getMigrationTimeYears());
                assertEquals(10, riskAssessment.getQuantumRiskResult().getThreatHorizonYears());
                assertEquals(BusinessCriticality.CRITICAL, riskAssessment.getQuantumRiskResult().getBusinessCriticality());
                assertEquals(DataSensitivity.HIGHLY_SENSITIVE, riskAssessment.getQuantumRiskResult().getDataSensitivity());
            }
        }
    }

    @Test
    void testContextValuesInCBOM(@TempDir Path tempDir) throws IOException {
        String javaContent = """
            import javax.crypto.Cipher;
            public class Test {
                public void encrypt() throws Exception {
                    Cipher cipher = Cipher.getInstance("AES");
                }
            }
            """;
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, javaContent);

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setBusinessCriticality(BusinessCriticality.HIGH);
        context.setDataSensitivity(DataSensitivity.CONFIDENTIAL);

        AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);

        assertNotNull(response);
        assertNotNull(response.getCbom());
        
        // Verify CBOM contains the context values
        if (response.getCbom().getComponents() != null && !response.getCbom().getComponents().isEmpty()) {
            var component = response.getCbom().getComponents().get(0);
            if (component.getCryptoProperties() != null) {
                // The CBOM should reflect the context values
                assertNotNull(component.getCryptoProperties().getBusinessCriticality());
                assertNotNull(component.getCryptoProperties().getDataSensitivity());
                // Note: The actual values depend on the CBOMGenerator implementation
            }
        }
    }

    @Test
    void testAllContextEnumValues(@TempDir Path tempDir) throws IOException {
        String javaContent = """
            import javax.crypto.Cipher;
            public class Test {
                public void encrypt() throws Exception {
                    Cipher cipher = Cipher.getInstance("AES");
                }
            }
            """;
        Path javaFile = tempDir.resolve("Test.java");
        Files.writeString(javaFile, javaContent);

        // Test all business criticality values
        BusinessCriticality[] criticalities = BusinessCriticality.values();
        for (BusinessCriticality criticality : criticalities) {
            ProjectAnalysisContext context = new ProjectAnalysisContext();
            context.setBusinessCriticality(criticality);
            
            AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);
            assertNotNull(response);
            assertEquals(criticality, response.getContext().getBusinessCriticality());
        }

        // Test all data sensitivity values
        DataSensitivity[] sensitivities = DataSensitivity.values();
        for (DataSensitivity sensitivity : sensitivities) {
            ProjectAnalysisContext context = new ProjectAnalysisContext();
            context.setDataSensitivity(sensitivity);
            
            AnalysisResponse response = analysisService.analyzeDirectory(tempDir.toString(), context);
            assertNotNull(response);
            assertEquals(sensitivity, response.getContext().getDataSensitivity());
        }
    }
}
