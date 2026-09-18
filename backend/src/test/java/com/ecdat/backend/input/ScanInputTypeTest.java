package com.ecdat.backend.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScanInputTypeTest {

    @Test
    void testSupportedInputTypes() {
        assertTrue(ScanInputType.ZIP_ARCHIVE.isSupported(), "ZIP_ARCHIVE must be supported in Phase 4");
        assertTrue(ScanInputType.DIRECTORY.isSupported(), "DIRECTORY must be supported in Phase 4");
    }

    @Test
    void testPlannedInputTypesAreNotSupportedYet() {
        assertFalse(ScanInputType.GIT_REPOSITORY.isSupported(), "GIT_REPOSITORY is roadmap Phase 5");
        assertFalse(ScanInputType.FILES.isSupported(), "FILES is roadmap Phase 6");
        assertFalse(ScanInputType.JAR.isSupported(), "JAR is roadmap Phase 7");
        assertFalse(ScanInputType.CLASS.isSupported(), "CLASS is roadmap Phase 7");
        assertFalse(ScanInputType.CONFIGURATION.isSupported(), "CONFIGURATION is roadmap Phase 6");
        assertFalse(ScanInputType.CONTAINER_IMAGE.isSupported(), "CONTAINER_IMAGE is roadmap Phase 8");
    }

    @Test
    void testEnumDisplayNamesAndDescriptions() {
        for (ScanInputType type : ScanInputType.values()) {
            assertNotNull(type.getDisplayName(), "Display name must not be null for " + type);
            assertFalse(type.getDisplayName().isEmpty(), "Display name must not be empty for " + type);
            assertNotNull(type.getDescription(), "Description must not be null for " + type);
            assertFalse(type.getDescription().isEmpty(), "Description must not be empty for " + type);
        }
    }
}
