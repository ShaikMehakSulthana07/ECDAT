package com.ecdat.backend.input;

import com.ecdat.backend.dto.ProjectAnalysisContext;
import com.ecdat.backend.inventory.BusinessCriticality;
import com.ecdat.backend.inventory.DataSensitivity;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ScanRequestTest {

    @Test
    void testForZipFactory() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "project.zip", "application/zip", "dummy content".getBytes()
        );
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setApplicationName("PaymentService");

        ScanRequest request = ScanRequest.forZip(file, context);

        assertEquals(ScanInputType.ZIP_ARCHIVE, request.getInputType());
        assertEquals("project.zip", request.getSourceIdentifier());
        assertEquals("PaymentService", request.getProjectName());
        assertSame(file, request.getArchiveFile());
        assertNotNull(request.getContext());
        assertEquals(6, request.getScopes().size());
        assertTrue(request.isScopeEnabled(AnalysisScope.CRYPTO_APIS));
        assertTrue(request.isScopeEnabled(AnalysisScope.CBOM));
    }

    @Test
    void testForDirectoryFactory() {
        ProjectAnalysisContext context = new ProjectAnalysisContext();
        context.setBusinessCriticality(BusinessCriticality.CRITICAL);
        context.setDataSensitivity(DataSensitivity.HIGHLY_SENSITIVE);

        ScanRequest request = ScanRequest.forDirectory("/app/src", context);

        assertEquals(ScanInputType.DIRECTORY, request.getInputType());
        assertEquals("/app/src", request.getSourceIdentifier());
        assertEquals("/app/src", request.getDirectoryPath());
        assertEquals(BusinessCriticality.CRITICAL, request.getContext().getBusinessCriticality());
    }

    @Test
    void testForGitRepositoryFactory() {
        ScanRequest request = ScanRequest.forGitRepository("https://github.com/org/repo.git", null);

        assertEquals(ScanInputType.GIT_REPOSITORY, request.getInputType());
        assertEquals("https://github.com/org/repo.git", request.getRepositoryUrl());
        assertNotNull(request.getContext());
    }

    @Test
    void testForContainerFactory() {
        ScanRequest request = ScanRequest.forContainer("docker.io/library/app:latest", null);

        assertEquals(ScanInputType.CONTAINER_IMAGE, request.getInputType());
        assertEquals("docker.io/library/app:latest", request.getSourceIdentifier());
    }

    @Test
    void testCustomScopes() {
        ScanRequest request = new ScanRequest(ScanInputType.ZIP_ARCHIVE, "test.zip");
        request.setScopes(Set.of(AnalysisScope.CRYPTO_APIS, AnalysisScope.QUANTUM_RISK));

        assertTrue(request.isScopeEnabled(AnalysisScope.CRYPTO_APIS));
        assertTrue(request.isScopeEnabled(AnalysisScope.QUANTUM_RISK));
        assertFalse(request.isScopeEnabled(AnalysisScope.CERTIFICATES));
    }
}
