package com.ecdat.backend.test.fixtures;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility to create a minimal Docker image archive for testing.
 * This creates a simple TAR file with manifest.json and layer structure.
 */
public class CreateTestContainerArchive {
    
    public static void main(String[] args) throws IOException {
        createTestContainerArchive("test-container.tar");
    }
    
    public static void createTestContainerArchive(String outputPath) throws IOException {
        Path outputDir = Paths.get("D:\\ECDAT\\backend\\src\\test\\resources\\fixtures");
        Files.createDirectories(outputDir);
        
        Path outputPathFile = outputDir.resolve(outputPath);
        
        // Create a simple Docker image structure
        try (OutputStream fos = Files.newOutputStream(outputPathFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(bos)) {
            
            // Create manifest.json
            String manifest = "[{\"Config\":\"sha256.json\",\"RepoTags\":[\"test:latest\"],\"Layers\":[\"layer1/layer.tar\"]}]";
            addEntry(jos, "manifest.json", manifest.getBytes());
            
            // Create config file
            String config = "{\"config\":{\"Cmd\":[\"java\",\"-version\"]},\"rootfs\":{\"type\":\"layers\",\"diff_ids\":[]}}";
            addEntry(jos, "sha256.json", config.getBytes());
            
            // Create a simple layer directory structure
            // For simplicity, we'll just add a text file representing the layer
            String layerContent = "This represents a container layer with filesystem content";
            addEntry(jos, "layer1/layer.tar", layerContent.getBytes());
            
            System.out.println("Created test container archive: " + outputPathFile);
        }
    }
    
    private static void addEntry(java.util.jar.JarOutputStream jos, String name, byte[] content) throws IOException {
        java.util.jar.JarEntry entry = new java.util.jar.JarEntry(name);
        entry.setSize(content.length);
        jos.putNextEntry(entry);
        jos.write(content);
        jos.closeEntry();
    }
}
