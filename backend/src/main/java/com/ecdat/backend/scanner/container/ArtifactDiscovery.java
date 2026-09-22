package com.ecdat.backend.scanner.container;

import com.ecdat.backend.input.ArtifactType;
import com.ecdat.backend.input.DiscoveredArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Discovers cryptographic artifacts from extracted container filesystem layers.
 * Routes discovered artifacts to existing scanners (binary, certificate, configuration, etc.).
 */
public class ArtifactDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactDiscovery.class);

    /**
     * Discovers all supported artifacts from a directory.
     */
    public List<DiscoveredArtifact> discoverArtifacts(Path directory, String source) throws IOException {
        List<DiscoveredArtifact> artifacts = new ArrayList<>();

        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return artifacts;
        }

        logger.info("Discovering artifacts in: {}", directory);

        Files.walk(directory)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    String fileName = file.getFileName().toString();
                    ArtifactType type = determineArtifactType(fileName);
                    
                    if (type != ArtifactType.OTHER) {
                        DiscoveredArtifact artifact = DiscoveredArtifact.of(
                            type,
                            file,
                            directory,
                            source
                        );
                        artifacts.add(artifact);
                        logger.debug("Discovered artifact: {} ({})", fileName, type);
                    }
                } catch (Exception e) {
                    logger.warn("Error discovering artifact: {}", file, e);
                }
            });

        logger.info("Total artifacts discovered: {}", artifacts.size());
        return artifacts;
    }

    /**
     * Determines the artifact type based on file extension.
     */
    private ArtifactType determineArtifactType(String fileName) {
        String lowerName = fileName.toLowerCase();

        // Java binaries
        if (lowerName.endsWith(".jar")) {
            return ArtifactType.JAR;
        }
        if (lowerName.endsWith(".class")) {
            return ArtifactType.CLASS_FILE;
        }

        // Java source
        if (lowerName.endsWith(".java")) {
            return ArtifactType.JAVA_SOURCE;
        }

        // Configuration files
        if (lowerName.endsWith(".properties") || 
            lowerName.endsWith(".yml") || 
            lowerName.endsWith(".yaml") ||
            lowerName.endsWith(".xml") ||
            lowerName.endsWith(".conf") ||
            lowerName.endsWith(".cfg") ||
            lowerName.endsWith(".ini")) {
            return ArtifactType.CONFIGURATION;
        }

        // Certificates
        if (lowerName.endsWith(".crt") || 
            lowerName.endsWith(".pem") || 
            lowerName.endsWith(".der") ||
            lowerName.endsWith(".jks") ||
            lowerName.endsWith(".p12") ||
            lowerName.endsWith(".pfx")) {
            return ArtifactType.CERTIFICATE;
        }

        // Maven POM
        if (lowerName.equals("pom.xml")) {
            return ArtifactType.DEPENDENCY_DESCRIPTOR;
        }

        return ArtifactType.OTHER;
    }

    /**
     * Discovers artifacts from all layers and merges them.
     */
    public List<DiscoveredArtifact> discoverFromLayers(Path layersDir, String imageSource) throws IOException {
        List<DiscoveredArtifact> allArtifacts = new ArrayList<>();

        if (!Files.exists(layersDir) || !Files.isDirectory(layersDir)) {
            return allArtifacts;
        }

        Files.list(layersDir)
            .filter(Files::isDirectory)
            .forEach(layerDir -> {
                try {
                    List<DiscoveredArtifact> layerArtifacts = discoverArtifacts(layerDir, imageSource);
                    allArtifacts.addAll(layerArtifacts);
                } catch (Exception e) {
                    logger.warn("Error discovering artifacts in layer: {}", layerDir, e);
                }
            });

        return allArtifacts;
    }
}
