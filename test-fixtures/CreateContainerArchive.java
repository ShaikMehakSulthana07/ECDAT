import java.io.*;
import java.nio.file.*;
import java.util.jar.*;

/**
 * Creates a minimal Docker-compatible container archive for testing.
 * This approach uses only Java standard libraries to avoid external tool dependencies.
 */
public class CreateContainerArchive {
    
    public static void main(String[] args) throws IOException {
        String appDir = args.length > 0 ? args[0] : "D:\\ECDAT\\test-fixtures\\cryptoguard-crypto-app";
        String outputDir = args.length > 1 ? args[1] : "D:\\ECDAT\\test-fixtures";
        
        Path jarPath = Paths.get(appDir, "target", "cryptoguard-crypto-fixture-1.0.0.jar");
        Path outputPath = Paths.get(outputDir, "cryptoguard-crypto-image.tar");
        
        if (!Files.exists(jarPath)) {
            System.err.println("JAR file not found: " + jarPath);
            System.err.println("Please run 'mvn clean package' in the fixture directory first.");
            System.exit(1);
        }
        
        createDockerArchive(jarPath, outputPath);
        System.out.println("Container archive created: " + outputPath);
    }
    
    private static void createDockerArchive(Path jarPath, Path outputPath) throws IOException {
        // Create a temporary directory for the archive structure
        Path tempDir = Files.createTempDirectory("docker-archive-");
        Path imageDir = tempDir.resolve("cryptoguard-crypto-image");
        Files.createDirectories(imageDir);
        
        // Create minimal Docker structure
        Path layersDir = imageDir.resolve("layers");
        Files.createDirectories(layersDir);
        
        Path layer1Dir = layersDir.resolve("layer1");
        Files.createDirectories(layer1Dir);
        
        // Copy the JAR to the layer directory
        Files.copy(jarPath, layer1Dir.resolve("cryptoguard-crypto-fixture.jar"));
        
        // Create manifest.json
        String manifest = "[{\"Config\":\"config.json\",\"RepoTags\":[\"cryptoguard-crypto-fixture:1.0\"],\"Layers\":[\"layer1\"]}]";
        Files.writeString(imageDir.resolve("manifest.json"), manifest);
        
        // Create config.json
        String config = "{\"config\":{\"Cmd\":[\"java\",\"-jar\",\"/app/cryptoguard-crypto-fixture.jar\"]},\"architecture\":\"amd64\",\"os\":\"linux\"}";
        Files.writeString(imageDir.resolve("config.json"), config);
        
        // Create TAR archive (using Java's JarOutputStream as a simple TAR-like format)
        try (OutputStream fos = Files.newOutputStream(outputPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             JarOutputStream jos = new JarOutputStream(bos)) {
            
            // Add manifest.json
            addFileToArchive(jos, imageDir.resolve("manifest.json"), "manifest.json");
            
            // Add config.json
            addFileToArchive(jos, imageDir.resolve("config.json"), "config.json");
            
            // Add layer contents (as a directory structure)
            addDirectoryToArchive(jos, layer1Dir, "layer1/");
        }
        
        // Cleanup
        deleteDirectory(tempDir);
    }
    
    private static void addFileToArchive(JarOutputStream jos, Path filePath, String entryName) throws IOException {
        if (!Files.exists(filePath)) {
            return;
        }
        
        byte[] content = Files.readAllBytes(filePath);
        JarEntry entry = new JarEntry(entryName);
        entry.setSize(content.length);
        jos.putNextEntry(entry);
        jos.write(content);
        jos.closeEntry();
    }
    
    private static void addDirectoryToArchive(JarOutputStream jos, Path dir, String prefix) throws IOException {
        Files.walk(dir)
            .filter(path -> !path.equals(dir))
            .forEach(path -> {
                try {
                    String entryName = prefix + dir.relativize(path).toString().replace("\\", "/");
                    if (Files.isDirectory(path)) {
                        JarEntry entry = new JarEntry(entryName + "/");
                        jos.putNextEntry(entry);
                        jos.closeEntry();
                    } else {
                        addFileToArchive(jos, path, entryName);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to add " + path + ": " + e.getMessage());
                }
            });
    }
    
    private static void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
            .sorted((a, b) -> -a.compareTo(b)) // Delete files before directories
            .forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    System.err.println("Failed to delete " + file + ": " + e.getMessage());
                }
            });
    }
}
