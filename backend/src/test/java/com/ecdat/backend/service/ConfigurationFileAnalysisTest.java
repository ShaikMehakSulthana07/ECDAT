package com.ecdat.backend.service;

import com.ecdat.backend.dto.AnalysisResponse;
import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import com.ecdat.backend.scanner.CryptoFinding;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for Phase 6 Configuration File Scanning.
 * Verifies that configuration files are properly analyzed and produce findings.
 */
class ConfigurationFileAnalysisTest {

    private final AnalysisService analysisService = new AnalysisService();

    @Test
    void testConfigurationFileAnalysisProducesFindings() {
        String propertiesContent = """
                server.ssl.enabled=true
                server.ssl.protocol=TLSv1.2
                server.ssl.ciphers=TLS_RSA_WITH_AES_128_CBC_SHA
                crypto.algorithm=RSA
                crypto.key-size=1024
                crypto.hash=SHA-1
                """;

        MockMultipartFile configFile = new MockMultipartFile(
                "file", "application.properties", "text/plain", propertiesContent.getBytes()
        );

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setApplicationName("Test Application");
        context.setBusinessCriticality(BusinessCriticality.HIGH);
        context.setDataSensitivity(DataSensitivity.CONFIDENTIAL);

        AnalysisResponse response = analysisService.analyzeConfigurationFile(configFile, context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getFindings());
        assertNotNull(response.getCbom());
        assertNotNull(response.getSummary());

        // Verify that configuration findings were converted to CryptoFindings
        // The properties file contains TLSv1.2, RSA, AES, SHA-1 which should be detected
        assertTrue(response.getFindings().size() > 0, 
                "Configuration file analysis should produce at least one finding");

        // Verify input type is correctly set
        assertEquals("CONFIGURATION_FILE", response.getInputType());
        assertEquals("application.properties", response.getInputName());
    }

    @Test
    void testConfigurationFileWithYamlFormat() {
        String yamlContent = """
                server:
                  ssl:
                    enabled: true
                    protocol: TLSv1.2
                crypto:
                  algorithm: RSA
                  key-size: 2048
                """;

        MockMultipartFile yamlFile = new MockMultipartFile(
                "file", "application.yml", "text/yaml", yamlContent.getBytes()
        );

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeConfigurationFile(yamlFile, context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("CONFIGURATION_FILE", response.getInputType());
    }

    @Test
    void testConfigurationFileWithXmlFormat() {
        String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <configuration>
                    <server>
                        <ssl>
                            <protocol>TLSv1.2</protocol>
                        </ssl>
                    </server>
                    <crypto>
                        <algorithm>RSA</algorithm>
                    </crypto>
                </configuration>
                """;

        MockMultipartFile xmlFile = new MockMultipartFile(
                "file", "config.xml", "text/xml", xmlContent.getBytes()
        );

        ProjectAnalysisContext context = new ProjectAnalysisContext();
        AnalysisResponse response = analysisService.analyzeConfigurationFile(xmlFile, context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("CONFIGURATION_FILE", response.getInputType());
    }
}
