package com.ecdat.backend.scanner;

import com.ecdat.backend.scanner.maven.MavenDependency;
import com.ecdat.backend.scanner.maven.MavenDependencyFinding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scanner for Maven pom.xml files to identify dependencies,
 * particularly crypto/security-related libraries.
 */
public class MavenDependencyScanner {

    private static final List<String> CRYPTO_RELATED_ARTIFACT_IDS = List.of(
        "bcpkix-jdk18on",       // Bouncy Castle PKIX
        "bcprov-jdk18on",       // Bouncy Castle Provider
        "bcpg-jdk18on",         // Bouncy Castle OpenPGP
        "bctls-jdk18on",        // Bouncy Castle TLS
        "commons-crypto",       // Apache Commons Crypto
        "cryptography",        // Python cryptography (if in polyglot projects)
        "jasypt",              // Java encryption
        "tink",                // Google Tink cryptography
        "conscrypt",           // Google Conscrypt
        "spongycastle",        // Spongy Castle (Android Bouncy Castle)
        "openssl",             // OpenSSL bindings
        "netty-tcnative",      // Netty OpenSSL
        "spring-security-crypto" // Spring Security Crypto
    );

    private static final List<String> CRYPTO_RELATED_GROUP_IDS = List.of(
        "org.bouncycastle",
        "commons-crypto",
        "com.google.crypto.tink",
        "org.conscrypt",
        "org.spongycastle",
        "org.apache.commons.crypto"
    );

    /**
     * Scans a directory for pom.xml files and extracts dependency information.
     * 
     * @param rootDir root directory to scan
     * @return list of Maven dependency findings
     */
    public List<MavenDependencyFinding> scanDirectory(String rootDir) {
        List<MavenDependencyFinding> findings = new ArrayList<>();
        Path root = Paths.get(rootDir);

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith("pom.xml"))
                 .forEach(path -> {
                     try {
                         List<MavenDependency> dependencies = parsePomXml(path);
                         for (MavenDependency dependency : dependencies) {
                             MavenDependencyFinding finding = createFinding(dependency, path);
                             findings.add(finding);
                         }
                     } catch (Exception e) {
                         System.err.println("Failed to parse pom.xml: " + path + " - " + e.getMessage());
                     }
                 });
        } catch (Exception e) {
            System.err.println("Failed to scan directory for pom.xml files: " + rootDir);
        }

        return findings;
    }

    /**
     * Parses a pom.xml file and extracts dependency information.
     * 
     * @param pomXmlPath path to pom.xml file
     * @return list of Maven dependencies
     */
    private List<MavenDependency> parsePomXml(Path pomXmlPath) throws Exception {
        List<MavenDependency> dependencies = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(pomXmlPath.toFile());
        document.getDocumentElement().normalize();

        // Get all dependency elements
        NodeList dependencyNodes = document.getElementsByTagName("dependency");

        for (int i = 0; i < dependencyNodes.getLength(); i++) {
            Element dependencyElement = (Element) dependencyNodes.item(i);
            
            String groupId = getElementText(dependencyElement, "groupId");
            String artifactId = getElementText(dependencyElement, "artifactId");
            String version = getElementText(dependencyElement, "version");
            String scope = getElementText(dependencyElement, "scope");

            if (groupId != null && artifactId != null) {
                MavenDependency dependency = new MavenDependency(
                    groupId, artifactId, version, scope, pomXmlPath.toString()
                );
                dependencies.add(dependency);
            }
        }

        return dependencies;
    }

    /**
     * Gets text content of a child element.
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    /**
     * Creates a dependency finding from a Maven dependency.
     */
    private MavenDependencyFinding createFinding(MavenDependency dependency, Path pomPath) {
        boolean isCryptoRelated = isCryptoRelated(dependency);
        
        return new MavenDependencyFinding(
            dependency.getGroupId(),
            dependency.getArtifactId(),
            dependency.getVersion(),
            dependency.getScope(),
            pomPath.toString(),
            isCryptoRelated,
            determineCryptoLibraryName(dependency),
            CryptoFinding.Confidence.HIGH
        );
    }

    /**
     * Determines if a dependency is crypto-related based on groupId and artifactId.
     */
    private boolean isCryptoRelated(MavenDependency dependency) {
        String groupId = dependency.getGroupId().toLowerCase();
        String artifactId = dependency.getArtifactId().toLowerCase();

        // Check artifact IDs
        for (String cryptoArtifact : CRYPTO_RELATED_ARTIFACT_IDS) {
            if (artifactId.contains(cryptoArtifact.toLowerCase())) {
                return true;
            }
        }

        // Check group IDs
        for (String cryptoGroup : CRYPTO_RELATED_GROUP_IDS) {
            if (groupId.contains(cryptoGroup.toLowerCase())) {
                return true;
            }
        }

        // Check for crypto-related keywords
        if (artifactId.contains("crypto") || artifactId.contains("security") ||
            artifactId.contains("cipher") || artifactId.contains("encrypt")) {
            return true;
        }

        return false;
    }

    /**
     * Determines the crypto library name if applicable.
     */
    private String determineCryptoLibraryName(MavenDependency dependency) {
        if (!isCryptoRelated(dependency)) {
            return null;
        }

        String artifactId = dependency.getArtifactId().toLowerCase();
        String groupId = dependency.getGroupId().toLowerCase();

        if (groupId.contains("bouncycastle") || artifactId.contains("bc")) {
            return "Bouncy Castle";
        }
        if (artifactId.contains("commons-crypto")) {
            return "Apache Commons Crypto";
        }
        if (artifactId.contains("tink")) {
            return "Google Tink";
        }
        if (artifactId.contains("conscrypt")) {
            return "Google Conscrypt";
        }
        if (artifactId.contains("spongycastle")) {
            return "Spongy Castle";
        }
        if (artifactId.contains("jasypt")) {
            return "Jasypt";
        }
        if (artifactId.contains("spring-security-crypto")) {
            return "Spring Security Crypto";
        }

        return "UNKNOWN_CRYPTO_LIBRARY";
    }
}
