package com.ecdat.backend.scanner;

import com.ecdat.backend.scanner.certificate.CertificateArtifactFinding;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scanner for certificate and key artifacts in project directories.
 * Discovers and analyzes certificate files without exposing private key material.
 */
public class CertificateArtifactScanner {

    private static final List<String> CERTIFICATE_EXTENSIONS = List.of(
        ".pem", ".crt", ".cer", ".der", ".p12", ".pfx", ".jks", ".keystore"
    );

    /**
     * Scans a directory for certificate and key artifacts.
     * 
     * @param rootDir root directory to scan
     * @return list of certificate artifact findings
     */
    public List<CertificateArtifactFinding> scanDirectory(String rootDir) {
        List<CertificateArtifactFinding> findings = new ArrayList<>();
        Path root = Paths.get(rootDir);

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isCertificateFile)
                 .forEach(path -> {
                     try {
                         CertificateArtifactFinding finding = analyzeCertificateFile(path);
                         if (finding != null) {
                             findings.add(finding);
                         }
                     } catch (Exception e) {
                         System.err.println("Failed to analyze certificate file: " + path + " - " + e.getMessage());
                     }
                 });
        } catch (Exception e) {
            System.err.println("Failed to scan directory for certificate files: " + rootDir);
        }

        return findings;
    }

    /**
     * Determines if a file is a certificate/key file based on extension.
     */
    private boolean isCertificateFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (String ext : CERTIFICATE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Analyzes a certificate file and extracts metadata.
     * 
     * @param certPath path to certificate file
     * @return certificate artifact finding
     */
    private CertificateArtifactFinding analyzeCertificateFile(Path certPath) {
        String fileName = certPath.getFileName().toString();
        String fileExtension = getFileExtension(fileName);
        
        // Determine file type
        String fileType = determineFileType(fileExtension);
        
        // Try to parse as X.509 certificate
        try {
            if (fileExtension.equals(".pem") || fileExtension.equals(".crt") || 
                fileExtension.equals(".cer") || fileExtension.equals(".der")) {
                
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Certificate cert = certFactory.generateCertificate(Files.newInputStream(certPath));
                
                return new CertificateArtifactFinding(
                    fileName,
                    certPath.toString(),
                    fileType,
                    "X.509",
                    extractPublicKeyAlgorithm(cert),
                    extractKeySize(cert),
                    extractSignatureAlgorithm(cert),
                    extractValidityDates(cert),
                    CryptoFinding.Confidence.HIGH
                );
            }
        } catch (Exception e) {
            // Fall through to low-confidence finding
        }
        
        // For other formats (keystores, PKCS12), we can identify the file but not parse safely
        return new CertificateArtifactFinding(
            fileName,
            certPath.toString(),
            fileType,
            "UNKNOWN",
            null,
            null,
            null,
            null,
            CryptoFinding.Confidence.LOW
        );
    }

    /**
     * Determines the file type based on extension.
     */
    private String determineFileType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".pem" -> "PEM";
            case ".crt", ".cer" -> "X.509 Certificate";
            case ".der" -> "DER-encoded Certificate";
            case ".p12", ".pfx" -> "PKCS12 Keystore";
            case ".jks", ".keystore" -> "Java Keystore";
            default -> "UNKNOWN";
        };
    }

    /**
     * Extracts the public key algorithm from a certificate.
     */
    private String extractPublicKeyAlgorithm(Certificate cert) {
        try {
            return cert.getPublicKey().getAlgorithm();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Extracts the key size from a certificate's public key.
     */
    private Integer extractKeySize(Certificate cert) {
        try {
            if (cert.getPublicKey() instanceof java.security.interfaces.RSAPublicKey rsaKey) {
                return rsaKey.getModulus().bitLength();
            }
            if (cert.getPublicKey() instanceof java.security.interfaces.ECPublicKey ecKey) {
                return ecKey.getParams().getOrder().bitLength();
            }
            if (cert.getPublicKey() instanceof java.security.interfaces.DSAKey dsaKey) {
                return dsaKey.getParams().getP().bitLength();
            }
            return cert.getPublicKey().getEncoded().length * 8; // Fallback approximate
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the signature algorithm from a certificate.
     */
    private String extractSignatureAlgorithm(Certificate cert) {
        try {
            if (cert instanceof java.security.cert.X509Certificate x509) {
                return x509.getSigAlgName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "UNKNOWN";
    }

    /**
     * Extracts validity dates from a certificate.
     */
    private String extractValidityDates(Certificate cert) {
        try {
            if (cert instanceof java.security.cert.X509Certificate x509) {
                return x509.getNotBefore() + " to " + x509.getNotAfter();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
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
