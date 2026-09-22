import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/**
 * Creates an advanced Docker-compatible container archive with proper structure.
 * Uses only Java standard libraries to avoid external dependencies.
 */
public class CreateAdvancedContainerArchive {
    
    public static void main(String[] args) throws IOException {
        String appDir = args.length > 0 ? args[0] : "D:\\ECDAT\\test-fixtures\\cryptoguard-crypto-app";
        String outputDir = args.length > 1 ? args[1] : "D:\\ECDAT\\test-fixtures";
        
        Path jarPath = Paths.get(appDir, "target", "cryptoguard-crypto-fixture-1.0.0.jar");
        Path outputPath = Paths.get(outputDir, "cryptoguard-crypto-image-advanced.tar");
        
        if (!Files.exists(jarPath)) {
            System.err.println("JAR file not found: " + jarPath);
            System.err.println("Please run 'mvn clean package' in the fixture directory first.");
            System.exit(1);
        }
        
        createAdvancedDockerArchive(jarPath, outputPath);
        System.out.println("Advanced container archive created: " + outputPath);
    }
    
    private static void createAdvancedDockerArchive(Path jarPath, Path outputPath) throws IOException {
        // Create temporary directory for the archive structure
        Path tempDir = Files.createTempDirectory("docker-advanced-");
        Path imageDir = tempDir.resolve("cryptoguard-crypto-image");
        Files.createDirectories(imageDir);
        
        // Create proper Docker structure
        Path blobsDir = imageDir.resolve("blobs/sha256");
        Files.createDirectories(blobsDir);
        
        // Create layer content directory
        Path layerContentDir = tempDir.resolve("layer-content");
        Files.createDirectories(layerContentDir);
        
        // Create a simple filesystem for the layer
        Path appDir = layerContentDir.resolve("app");
        Files.createDirectories(appDir);
        
        // Copy the JAR to the layer
        Files.copy(jarPath, appDir.resolve("cryptoguard-crypto-fixture.jar"));
        
        // Create a simple application.properties in the layer
        String appProps = "server.port=8080\nserver.ssl.enabled=true\ncrypto.test=true\n";
        Files.writeString(appDir.resolve("application.properties"), appProps);
        
        // Create the layer TAR file using JAR format as TAR substitute
        Path layerTarPath = tempDir.resolve("layer.tar");
        createJarFromDirectory(layerContentDir, layerTarPath);
        
        // Calculate SHA256 hash of the layer
        String layerSha256 = calculateSHA256(layerTarPath);
        Path layerBlobPath = blobsDir.resolve(layerSha256);
        Files.copy(layerTarPath, layerBlobPath);
        
        // Create config blob
        String configContent = String.format("""
            {
              "config": {
                "Cmd": ["java", "-jar", "/app/cryptoguard-crypto-fixture.jar"],
                "WorkingDir": "/app",
                "Env": ["JAVA_HOME=/usr/lib/jvm/java-21-openjdk"]
              },
              "rootfs": {
                "type": "layers",
                "diff_ids": ["sha256:%s"]
              },
              "architecture": "amd64",
              "os": "linux"
            }
            """, layerSha256);
        
        Path configTarPath = tempDir.resolve("config.tar");
        createJarFromString(configContent, "config.json", configTarPath);
        String configSha256 = calculateSHA256(configTarPath);
        Path configBlobPath = blobsDir.resolve(configSha256);
        Files.copy(configTarPath, configBlobPath);
        
        // Create manifest
        String manifest = String.format("""
            [{
              "Config": "sha256:%s",
              "RepoTags": ["cryptoguard-crypto-fixture:1.0"],
              "Layers": ["sha256/%s"],
              "Architecture": "amd64",
              "OS": "linux"
            }]
            """, configSha256, layerSha256);
        
        Files.writeString(imageDir.resolve("manifest.json"), manifest);
        
        // Create repositories file
        String repositories = """
            {
              "cryptoguard-crypto-fixture": {
                "1.0": "sha256:%s"
              }
            }
            """.replace("%s", configSha256);
        Files.writeString(imageDir.resolve("repositories"), repositories);
        
        // Create the final TAR archive (using JAR format as TAR substitute)
        createJarFromDirectory(imageDir, outputPath);
        
        // Cleanup
        deleteDirectory(tempDir);
        
        System.out.println("Layer SHA256: " + layerSha256);
        System.out.println("Config SHA256: " + configSha256);
    }
    
    private static void createJarFromDirectory(Path sourceDir, Path outputPath) throws IOException {
        try (OutputStream fos = Files.newOutputStream(outputPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             JarOutputStream jos = new JarOutputStream(bos)) {
            
            Files.walk(sourceDir)
                .filter(path -> !path.equals(sourceDir))
                .forEach(path -> {
                    try {
                        String entryName = sourceDir.relativize(path).toString().replace("\\", "/");
                        
                        if (Files.isDirectory(path)) {
                            JarEntry entry = new JarEntry(entryName + "/");
                            jos.putNextEntry(entry);
                            jos.closeEntry();
                        } else {
                            JarEntry entry = new JarEntry(entryName);
                            entry.setSize(Files.size(path));
                            jos.putNextEntry(entry);
                            Files.copy(path, jos);
                            jos.closeEntry();
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to add " + path + ": " + e.getMessage());
                    }
                });
        }
    }
    
    private static void createJarFromString(String content, String entryName, Path outputPath) throws IOException {
        try (OutputStream fos = Files.newOutputStream(outputPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             JarOutputStream jos = new JarOutputStream(bos)) {
            
            byte[] contentBytes = content.getBytes();
            JarEntry entry = new JarEntry(entryName);
            entry.setSize(contentBytes.length);
            jos.putNextEntry(entry);
            jos.write(contentBytes);
            jos.closeEntry();
        }
    }
    
    private static String calculateSHA256(Path filePath) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 algorithm not available", e);
        }
    }
    
    private static void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
            .sorted((a, b) -> -a.compareTo(b))
            .forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    System.err.println("Failed to delete " + file + ": " + e.getMessage());
                }
            });
    }
}
