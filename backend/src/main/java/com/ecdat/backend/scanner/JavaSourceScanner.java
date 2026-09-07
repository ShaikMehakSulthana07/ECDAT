package com.ecdat.backend.scanner;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JavaSourceScanner {

    public List<CryptoFinding> scanDirectory(String rootDirPath) {
        List<CryptoFinding> allFindings = new ArrayList<>();
        Path root = Paths.get(rootDirPath);

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(path -> {
                     try {
                         CompilationUnit cu = StaticJavaParser.parse(path);
                         CryptoScanner visitor = new CryptoScanner(root.relativize(path).toString().replace("\\", "/"));
                         visitor.visit(cu, allFindings);
                     } catch (Exception e) {
                         System.err.println("Malformed or unparseable Java file skipped: " + path + " - " + e.getMessage());
                     }
                 });
        } catch (Exception e) {
            System.err.println("Failed to scan directory: " + rootDirPath);
        }
        return allFindings;
    }
}