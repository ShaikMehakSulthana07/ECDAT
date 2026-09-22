package com.ecdat.backend.scanner.container;

import com.ecdat.backend.input.DiscoveredArtifact;
import com.ecdat.backend.scanner.CertificateArtifactScanner;
import com.ecdat.backend.scanner.CryptoFinding;
import com.ecdat.backend.scanner.JavaSourceScanner;
import com.ecdat.backend.scanner.MavenDependencyScanner;
import com.ecdat.backend.scanner.binary.BinaryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main scanner for container image static analysis.
 * Orchestrates manifest reading, layer extraction, artifact discovery, and scanner routing.
 * NEVER executes container code or requires a Docker daemon.
 */
@Component
public class ContainerImageScanner {

    private static final Logger logger = LoggerFactory.getLogger(ContainerImageScanner.class);

    private final DockerManifestReader manifestReader;
    private final ArtifactDiscovery artifactDiscovery;
    private final BinaryScanner binaryScanner;
    private final JavaSourceScanner javaSourceScanner;

    public ContainerImageScanner() {
        this.manifestReader = new DockerManifestReader();
        this.artifactDiscovery = new ArtifactDiscovery();
        this.binaryScanner = new BinaryScanner();
        this.javaSourceScanner = new JavaSourceScanner();
    }

    /**
     * Scans a container image archive and returns crypto findings.
     */
    public List<CryptoFinding> scanContainerImage(Path imageArchivePath, String imageSource) throws IOException {
        logger.info("Starting container image scan: {}", imageArchivePath);

        // Verify the image is a valid Docker image archive
        if (!manifestReader.isValidDockerImage(imageArchivePath)) {
            throw new IOException("Invalid Docker image archive: manifest.json not found");
        }

        // Read the manifest
        DockerManifestReader.ContainerManifest manifest = manifestReader.readManifest(imageArchivePath);
        logger.info("Image manifest loaded: {} layers, architecture: {}", 
            manifest.getLayerDigests().size(), manifest.getArchitecture());

        // Check layer count limit
        if (manifest.getLayerDigests().size() > ContainerAnalysisLimits.MAX_LAYER_COUNT) {
            throw new SecurityException("Image layer count exceeds maximum limit of " + 
                ContainerAnalysisLimits.MAX_LAYER_COUNT);
        }

        // Extract layers
        LayerExtractor extractor = new LayerExtractor(imageArchivePath);
        try {
            for (String layerDigest : manifest.getLayerDigests()) {
                Path layerTarPath = imageArchivePath.resolve(layerDigest);
                if (Files.exists(layerTarPath)) {
                    extractor.extractLayer(layerTarPath, layerDigest);
                } else {
                    logger.warn("Layer file not found: {}", layerDigest);
                }
            }

            // Discover artifacts from extracted layers
            List<DiscoveredArtifact> artifacts = artifactDiscovery.discoverFromLayers(
                extractor.getLayersDir(), 
                imageSource
            );

            // Route artifacts to existing scanners
            List<CryptoFinding> findings = routeArtifactsToScanners(artifacts, imageSource);

            logger.info("Container image scan complete: {} artifacts, {} findings", artifacts.size(), findings.size());
            return findings;

        } finally {
            // Cleanup happens via ScanWorkspace AutoCloseable
            logger.info("Layer extraction complete: {} files, {} bytes", 
                extractor.getTotalExtractedFiles(), extractor.getTotalExtractedSize());
        }
    }

    /**
     * Scans a directory containing an extracted container image.
     */
    public List<CryptoFinding> scanExtractedImage(Path extractedDir, String imageSource) throws IOException {
        logger.info("Scanning extracted container image: {}", extractedDir);

        if (!manifestReader.isValidDockerImage(extractedDir)) {
            throw new IOException("Invalid Docker image directory: manifest.json not found");
        }

        return scanContainerImage(extractedDir, imageSource);
    }

    /**
     * Routes discovered artifacts to the appropriate existing scanners.
     * Preserves container provenance metadata in findings.
     */
    private List<CryptoFinding> routeArtifactsToScanners(List<DiscoveredArtifact> artifacts, String imageSource) {
        List<CryptoFinding> allFindings = new ArrayList<>();

        for (DiscoveredArtifact artifact : artifacts) {
            try {
                switch (artifact.getArtifactType()) {
                    case JAR:
                    case CLASS_FILE:
                        // Use existing binary scanner
                        List<CryptoFinding> binaryFindings = binaryScanner.scanFile(artifact.getPath());
                        // Update source type to indicate container origin and preserve provenance
                        for (CryptoFinding finding : binaryFindings) {
                            finding.setSourceType("CONTAINER_IMAGE");
                            // Preserve additional container provenance
                            finding.setFile(artifact.getRelativePath());
                            // Add container metadata
                            finding.setContainerImage(imageSource);
                            finding.setLayerId(artifact.getOrigin());
                            finding.setArtifactName(artifact.getName());
                        }
                        allFindings.addAll(binaryFindings);
                        break;

                    case CERTIFICATE:
                        // Certificate scanning returns CertificateArtifactFinding, not CryptoFinding
                        // Skip for now to maintain type safety
                        logger.debug("Skipping certificate artifact (requires separate handling)");
                        break;

                    case DEPENDENCY_DESCRIPTOR:
                        // Maven scanning returns MavenDependencyFinding, not CryptoFinding
                        // Skip for now to maintain type safety
                        logger.debug("Skipping Maven descriptor (requires separate handling)");
                        break;

                    case JAVA_SOURCE:
                        // Use existing Java source scanner
                        List<CryptoFinding> javaFindings = javaSourceScanner.scanFile(artifact.getPath());
                        // Update source type to indicate container origin and preserve provenance
                        for (CryptoFinding finding : javaFindings) {
                            finding.setSourceType("CONTAINER_IMAGE");
                            finding.setFile(artifact.getRelativePath());
                            finding.setContainerImage(imageSource);
                            finding.setLayerId(artifact.getOrigin());
                            finding.setArtifactName(artifact.getName());
                        }
                        allFindings.addAll(javaFindings);
                        break;

                    case CONFIGURATION:
                    case OTHER:
                        // For now, skip these or implement later
                        logger.debug("Skipping artifact type: {}", artifact.getArtifactType());
                        break;

                    default:
                        logger.debug("Unknown artifact type: {}", artifact.getArtifactType());
                        break;
                }
            } catch (Exception e) {
                logger.warn("Error scanning artifact: {}", artifact.getPath(), e);
            }
        }

        return allFindings;
    }
}
