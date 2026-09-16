import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class MakeZip {
    public static void main(String[] args) throws Exception {
        zipFolder(Paths.get("d:/ECDAT/test-target"), Paths.get("d:/ECDAT/test-target.zip"));
        zipFolder(Paths.get("d:/ECDAT/projectA"), Paths.get("d:/ECDAT/projectA.zip"));
        zipFolder(Paths.get("d:/ECDAT/projectB"), Paths.get("d:/ECDAT/projectB.zip"));

        // Malicious zip with path traversal
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("d:/ECDAT/malicious.zip"))) {
            ZipEntry entry = new ZipEntry("../../evil.txt");
            zos.putNextEntry(entry);
            zos.write("malicious payload for path traversal".getBytes());
            zos.closeEntry();
        }

        // Malicious zip with excessive entry count (>10000)
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("d:/ECDAT/too_many_entries.zip"))) {
            for (int i = 0; i < 10005; i++) {
                ZipEntry entry = new ZipEntry("file_" + i + ".txt");
                zos.putNextEntry(entry);
                zos.write("hello".getBytes());
                zos.closeEntry();
            }
        }

        System.out.println("All zip files created successfully.");
    }

    private static void zipFolder(Path sourceDirPath, Path zipPath) throws IOException {
        Files.deleteIfExists(zipPath);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Files.walk(sourceDirPath).forEach(path -> {
                if (!Files.isDirectory(path)) {
                    String relativePath = sourceDirPath.relativize(path).toString().replace("\\", "/");
                    ZipEntry zipEntry = new ZipEntry(relativePath);
                    try {
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
