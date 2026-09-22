package com.ecdat.backend.scanner;

import com.ecdat.backend.scanner.configuration.ConfigurationFinding;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Scanner for configuration files to detect cryptographic and security settings.
 * Supports properties, YAML, XML, and generic configuration formats.
 * Implements secret redaction and XML security (XXE protection).
 */
@Component
public class ConfigurationScanner {

    private static final List<String> CONFIGURATION_EXTENSIONS = List.of(
        ".properties", ".yml", ".yaml", ".xml", ".conf", ".cfg", ".ini"
    );

    // Secret patterns to redact
    private static final List<String> SECRET_KEY_PATTERNS = List.of(
        "password", "key-password", "trust-store-password", "secret", "token",
        "credential", "private-key", "api-key", "access-key", "secret-key"
    );

    // Cryptographic patterns
    private static final Pattern TLS_PROTOCOL_PATTERN = Pattern.compile(
        "(?i)(TLS|SSL)[vV]?[0-9.]*"
    );
    private static final Pattern CIPHER_SUITE_PATTERN = Pattern.compile(
        "(?i)(TLS_)[A-Z0-9_]+"
    );
    private static final Pattern ALGORITHM_PATTERN = Pattern.compile(
        "(?i)\\b(RSA|ECDSA|ECDH|EC|AES|SHA-[0-9]+|MD5|DES|3DES|Blowfish)\\b"
    );
    private static final Pattern KEY_SIZE_PATTERN = Pattern.compile(
        "(?i)(RSA|AES|ECDSA)[-_]?([0-9]+)"
    );
    private static final Pattern KEYSTORE_PATTERN = Pattern.compile(
        "(?i)(key-store|keystore|trust-store|truststore|keyStore|trustStore)"
    );

    /**
     * Scans a directory for configuration files and extracts cryptographic findings.
     *
     * @param rootDir root directory to scan
     * @return list of configuration findings
     */
    public List<ConfigurationFinding> scanDirectory(String rootDir) {
        List<ConfigurationFinding> findings = new ArrayList<>();
        Path root = Paths.get(rootDir);

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isConfigurationFile)
                 .forEach(path -> {
                     try {
                         List<ConfigurationFinding> fileFindings = scanConfigurationFile(path);
                         findings.addAll(fileFindings);
                     } catch (Exception e) {
                         System.err.println("Failed to scan configuration file: " + path + " - " + e.getMessage());
                     }
                 });
        } catch (Exception e) {
            System.err.println("Failed to scan directory for configuration files: " + rootDir);
        }

        return findings;
    }

    /**
     * Converts configuration findings to CryptoFinding format for integration with existing pipeline.
     */
    public List<CryptoFinding> convertToCryptoFindings(List<ConfigurationFinding> configFindings) {
        List<CryptoFinding> cryptoFindings = new ArrayList<>();

        for (ConfigurationFinding configFinding : configFindings) {
            CryptoFinding cryptoFinding = new CryptoFinding();
            cryptoFinding.setSourceType("CONFIGURATION");
            cryptoFinding.setFile(configFinding.getFileName());
            cryptoFinding.setLine(configFinding.getLineNumber());
            cryptoFinding.setEvidence(configFinding.getEvidence());
            cryptoFinding.setConfidence(configFinding.getConfidence());

            // Map finding type to algorithm/variant
            switch (configFinding.getFindingType()) {
                case "TLS_PROTOCOL":
                    cryptoFinding.setProtocol(configFinding.getDetectedValue());
                    cryptoFinding.setAlgorithm(configFinding.getDetectedValue());
                    cryptoFinding.setPurpose(CryptoFinding.Purpose.PROTOCOL);
                    break;
                case "CIPHER_SUITE":
                    cryptoFinding.setVariant(configFinding.getDetectedValue());
                    cryptoFinding.setAlgorithm(extractAlgorithmFromCipher(configFinding.getDetectedValue()));
                    cryptoFinding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
                    break;
                case "ALGORITHM":
                    cryptoFinding.setAlgorithm(configFinding.getDetectedValue());
                    cryptoFinding.setPurpose(inferPurpose(configFinding.getDetectedValue()));
                    break;
                case "KEY_SIZE":
                    String[] parts = configFinding.getDetectedValue().split("-");
                    if (parts.length >= 2) {
                        cryptoFinding.setAlgorithm(parts[0]);
                        try {
                            cryptoFinding.setKeySize(Integer.parseInt(parts[1]));
                        } catch (NumberFormatException e) {
                            // Keep key size as null
                        }
                    }
                    cryptoFinding.setPurpose(inferPurpose(parts[0]));
                    break;
                case "KEYSTORE":
                    cryptoFinding.setAlgorithm("KEYSTORE");
                    cryptoFinding.setVariant(configFinding.getDetectedValue());
                    cryptoFinding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
                    break;
                default:
                    cryptoFinding.setAlgorithm(configFinding.getDetectedValue());
                    cryptoFinding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
            }

            cryptoFindings.add(cryptoFinding);
        }

        return cryptoFindings;
    }

    /**
     * Extracts base algorithm from cipher suite name.
     */
    private String extractAlgorithmFromCipher(String cipherSuite) {
        if (cipherSuite.contains("AES")) return "AES";
        if (cipherSuite.contains("RSA")) return "RSA";
        if (cipherSuite.contains("ECDSA")) return "ECDSA";
        if (cipherSuite.contains("ECDH")) return "ECDH";
        if (cipherSuite.contains("DES")) return "DES";
        if (cipherSuite.contains("3DES")) return "3DES";
        return cipherSuite;
    }

    /**
     * Infers purpose from algorithm name.
     */
    private CryptoFinding.Purpose inferPurpose(String algorithm) {
        if (algorithm == null) return CryptoFinding.Purpose.UNKNOWN;
        String upper = algorithm.toUpperCase();
        if (upper.contains("SHA") || upper.contains("MD5")) return CryptoFinding.Purpose.HASHING;
        if (upper.contains("RSA") || upper.contains("ECDSA")) return CryptoFinding.Purpose.DIGITAL_SIGNATURE;
        if (upper.contains("ECDH")) return CryptoFinding.Purpose.KEY_AGREEMENT;
        if (upper.contains("AES") || upper.contains("DES")) return CryptoFinding.Purpose.ENCRYPTION;
        return CryptoFinding.Purpose.UNKNOWN;
    }

    /**
     * Determines if a file is a configuration file based on extension.
     */
    private boolean isConfigurationFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (String ext : CONFIGURATION_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scans a single configuration file and extracts cryptographic findings.
     */
    private List<ConfigurationFinding> scanConfigurationFile(Path configPath) {
        String fileName = configPath.getFileName().toString();
        String fileExtension = getFileExtension(fileName);

        return switch (fileExtension.toLowerCase()) {
            case ".properties" -> scanPropertiesFile(configPath);
            case ".yml", ".yaml" -> scanYamlFile(configPath);
            case ".xml" -> scanXmlFile(configPath);
            case ".conf", ".cfg", ".ini" -> scanGenericConfigFile(configPath);
            default -> new ArrayList<>();
        };
    }

    /**
     * Scans a Java properties file.
     */
    private List<ConfigurationFinding> scanPropertiesFile(Path configPath) {
        List<ConfigurationFinding> findings = new ArrayList<>();
        String fileName = configPath.getFileName().toString();

        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }

                // Parse key=value
                int separatorIndex = line.indexOf('=');
                if (separatorIndex > 0) {
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();

                    // Redact secrets
                    String redactedValue = redactSecret(key, value);

                    // Extract cryptographic findings
                    findings.addAll(extractCryptoFindings(
                        fileName, lineNumber, key, redactedValue, "PROPERTIES"
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse properties file: " + configPath);
        }

        return findings;
    }

    /**
     * Scans a YAML file using SnakeYAML.
     */
    private List<ConfigurationFinding> scanYamlFile(Path configPath) {
        List<ConfigurationFinding> findings = new ArrayList<>();
        String fileName = configPath.getFileName().toString();

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(Files.newInputStream(configPath));

            // Flatten nested YAML and extract findings
            findings.addAll(extractFromYamlMap(data, fileName, "", 1));

        } catch (YAMLException e) {
            System.err.println("Failed to parse YAML file: " + configPath + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to read YAML file: " + configPath);
        }

        return findings;
    }

    /**
     * Recursively extracts findings from a YAML map.
     */
    private List<ConfigurationFinding> extractFromYamlMap(Map<String, Object> map, String fileName, String prefix, int depth) {
        List<ConfigurationFinding> findings = new ArrayList<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                // Recurse into nested map
                findings.addAll(extractFromYamlMap((Map<String, Object>) value, fileName, fullKey, depth + 1));
            } else if (value instanceof List) {
                // Handle list values (e.g., enabled-protocols)
                for (Object item : (List<?>) value) {
                    if (item instanceof String) {
                        String redactedValue = redactSecret(fullKey, (String) item);
                        findings.addAll(extractCryptoFindings(
                            fileName, depth, fullKey, redactedValue, "YAML"
                        ));
                    }
                }
            } else if (value instanceof String) {
                String redactedValue = redactSecret(fullKey, (String) value);
                findings.addAll(extractCryptoFindings(
                    fileName, depth, fullKey, redactedValue, "YAML"
                ));
            }
        }

        return findings;
    }

    /**
     * Scans an XML file with XXE protection.
     */
    private List<ConfigurationFinding> scanXmlFile(Path configPath) {
        List<ConfigurationFinding> findings = new ArrayList<>();
        String fileName = configPath.getFileName().toString();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // XXE Protection: Disable external entities
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(configPath.toFile());
            document.getDocumentElement().normalize();

            // Extract text content from XML elements
            findings.addAll(extractFromXmlNodes(document.getDocumentElement(), fileName, ""));

        } catch (Exception e) {
            System.err.println("Failed to parse XML file: " + configPath + " - " + e.getMessage());
        }

        return findings;
    }

    /**
     * Recursively extracts findings from XML nodes.
     */
    private List<ConfigurationFinding> extractFromXmlNodes(org.w3c.dom.Node node, String fileName, String prefix) {
        List<ConfigurationFinding> findings = new ArrayList<>();

        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            String nodeName = node.getNodeName();
            String fullKey = prefix.isEmpty() ? nodeName : prefix + "." + nodeName;

            // Get text content
            String textContent = node.getTextContent().trim();
            if (!textContent.isEmpty()) {
                String redactedValue = redactSecret(fullKey, textContent);
                findings.addAll(extractCryptoFindings(
                    fileName, 0, fullKey, redactedValue, "XML"
                ));
            }

            // Recurse into child nodes
            org.w3c.dom.NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                findings.addAll(extractFromXmlNodes(children.item(i), fileName, fullKey));
            }
        }

        return findings;
    }

    /**
     * Scans a generic configuration file (.conf, .cfg, .ini).
     */
    private List<ConfigurationFinding> scanGenericConfigFile(Path configPath) {
        List<ConfigurationFinding> findings = new ArrayList<>();
        String fileName = configPath.getFileName().toString();

        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";") || line.startsWith("//")) {
                    continue;
                }

                // Parse key=value or key: value
                int separatorIndex = Math.max(line.indexOf('='), line.indexOf(':'));
                if (separatorIndex > 0) {
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();

                    // Redact secrets
                    String redactedValue = redactSecret(key, value);

                    // Extract cryptographic findings (more conservative for generic config)
                    findings.addAll(extractCryptoFindingsConservative(
                        fileName, lineNumber, key, redactedValue, "GENERIC"
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse generic config file: " + configPath);
        }

        return findings;
    }

    /**
     * Extracts cryptographic findings from a key-value pair.
     */
    private List<ConfigurationFinding> extractCryptoFindings(String fileName, int line, String key, String value, String format) {
        List<ConfigurationFinding> findings = new ArrayList<>();

        // Check for TLS/SSL protocols
        Matcher tlsMatcher = TLS_PROTOCOL_PATTERN.matcher(value);
        while (tlsMatcher.find()) {
            String protocol = tlsMatcher.group();
            findings.add(new ConfigurationFinding(
                "TLS_PROTOCOL",
                protocol,
                fileName,
                line,
                key + "=" + value,
                CryptoFinding.Confidence.HIGH,
                format
            ));
        }

        // Check for cipher suites
        Matcher cipherMatcher = CIPHER_SUITE_PATTERN.matcher(value);
        while (cipherMatcher.find()) {
            String cipher = cipherMatcher.group();
            findings.add(new ConfigurationFinding(
                "CIPHER_SUITE",
                cipher,
                fileName,
                line,
                key + "=" + value,
                CryptoFinding.Confidence.HIGH,
                format
            ));
        }

        // Check for algorithms
        Matcher algoMatcher = ALGORITHM_PATTERN.matcher(value);
        while (algoMatcher.find()) {
            String algorithm = algoMatcher.group();
            findings.add(new ConfigurationFinding(
                "ALGORITHM",
                algorithm,
                fileName,
                line,
                key + "=" + value,
                CryptoFinding.Confidence.HIGH,
                format
            ));
        }

        // Check for key sizes
        Matcher keySizeMatcher = KEY_SIZE_PATTERN.matcher(value);
        while (keySizeMatcher.find()) {
            String algo = keySizeMatcher.group(1);
            String size = keySizeMatcher.group(2);
            findings.add(new ConfigurationFinding(
                "KEY_SIZE",
                algo + "-" + size,
                fileName,
                line,
                key + "=" + value,
                CryptoFinding.Confidence.HIGH,
                format
            ));
        }

        // Check for keystore/truststore configuration
        Matcher keystoreMatcher = KEYSTORE_PATTERN.matcher(key);
        if (keystoreMatcher.find()) {
            findings.add(new ConfigurationFinding(
                "KEYSTORE",
                value,
                fileName,
                line,
                key + "=" + (isSecretKey(key) ? "REDACTED" : value),
                CryptoFinding.Confidence.HIGH,
                format
            ));
        }

        return findings;
    }

    /**
     * Conservative extraction for generic config files (reduces false positives).
     */
    private List<ConfigurationFinding> extractCryptoFindingsConservative(String fileName, int line, String key, String value, String format) {
        List<ConfigurationFinding> findings = new ArrayList<>();

        // Only extract if key clearly indicates crypto context
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("ssl") || lowerKey.contains("tls") || lowerKey.contains("cipher") ||
            lowerKey.contains("protocol") || lowerKey.contains("algorithm")) {

            // Check for TLS/SSL protocols
            Matcher tlsMatcher = TLS_PROTOCOL_PATTERN.matcher(value);
            while (tlsMatcher.find()) {
                String protocol = tlsMatcher.group();
                findings.add(new ConfigurationFinding(
                    "TLS_PROTOCOL",
                    protocol,
                    fileName,
                    line,
                    key + "=" + value,
                    CryptoFinding.Confidence.MEDIUM,
                    format
                ));
            }

            // Check for algorithms (more conservative)
            Matcher algoMatcher = ALGORITHM_PATTERN.matcher(value);
            while (algoMatcher.find()) {
                String algorithm = algoMatcher.group();
                findings.add(new ConfigurationFinding(
                    "ALGORITHM",
                    algorithm,
                    fileName,
                    line,
                    key + "=" + value,
                    CryptoFinding.Confidence.MEDIUM,
                    format
                ));
            }
        }

        return findings;
    }

    /**
     * Redacts secret values based on key patterns.
     */
    private String redactSecret(String key, String value) {
        if (isSecretKey(key)) {
            return "REDACTED";
        }
        return value;
    }

    /**
     * Checks if a key likely contains a secret.
     */
    private boolean isSecretKey(String key) {
        String lowerKey = key.toLowerCase();
        for (String pattern : SECRET_KEY_PATTERNS) {
            if (lowerKey.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the file extension from a filename.
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }
}
