package com.ecdat.backend.scanner.binary;

import com.ecdat.backend.scanner.CryptoFinding;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static bytecode analyzer using ASM to detect cryptographic API usage.
 * NEVER executes bytecode - only parses and analyzes statically.
 */
public class BytecodeAnalyzer {

    private static final Set<String> CRYPTO_API_CLASSES = new HashSet<>();
    private static final Set<String> CRYPTO_ALGORITHM_METHODS = new HashSet<>();

    static {
        // Cryptographic API classes to detect
        CRYPTO_API_CLASSES.add("javax/crypto/Cipher");
        CRYPTO_API_CLASSES.add("javax/crypto/KeyGenerator");
        CRYPTO_API_CLASSES.add("javax/crypto/KeyAgreement");
        CRYPTO_API_CLASSES.add("javax/crypto/SecretKey");
        CRYPTO_API_CLASSES.add("javax/crypto/spec/SecretKeySpec");
        CRYPTO_API_CLASSES.add("javax/crypto/spec/IvParameterSpec");
        CRYPTO_API_CLASSES.add("javax/crypto/spec/PBEKeySpec");
        CRYPTO_API_CLASSES.add("java/security/Signature");
        CRYPTO_API_CLASSES.add("java/security/MessageDigest");
        CRYPTO_API_CLASSES.add("java/security/KeyPairGenerator");
        CRYPTO_API_CLASSES.add("java/security/KeyFactory");
        CRYPTO_API_CLASSES.add("java/security/KeyStore");
        CRYPTO_API_CLASSES.add("javax/net/ssl/SSLContext");
        CRYPTO_API_CLASSES.add("javax/net/ssl/TrustManagerFactory");
        CRYPTO_API_CLASSES.add("javax/net/ssl/KeyManagerFactory");

        // Methods that take algorithm strings as parameters
        CRYPTO_ALGORITHM_METHODS.add("getInstance");
        CRYPTO_ALGORITHM_METHODS.add("init");
        CRYPTO_ALGORITHM_METHODS.add("initialize");
    }

    /**
     * Analyzes a CLASS file and extracts cryptographic findings.
     *
     * @param classFile path to the .class file
     * @param artifactName name of the containing artifact (JAR or standalone)
     * @return list of crypto findings
     * @throws IOException if file cannot be read
     */
    public List<CryptoFinding> analyzeClassFile(Path classFile, String artifactName) throws IOException {
        if (classFile == null || !Files.exists(classFile)) {
            throw new IllegalArgumentException("Class file does not exist: " + classFile);
        }

        long fileSize = Files.size(classFile);
        if (fileSize > BinaryAnalysisLimits.MAX_CLASS_FILE_SIZE) {
            throw new SecurityException("Class file exceeds maximum size limit: " + fileSize);
        }

        List<CryptoFinding> findings = new ArrayList<>();

        try (InputStream is = Files.newInputStream(classFile)) {
            ClassReader reader = new ClassReader(is);
            CryptoClassVisitor visitor = new CryptoClassVisitor(classFile, artifactName);
            reader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            findings.addAll(visitor.getFindings());
        } catch (Exception e) {
            // Malformed bytecode should not crash the analysis
            // Return low-confidence findings or empty list
            CryptoFinding errorFinding = createErrorFinding(classFile, artifactName, e.getMessage());
            findings.add(errorFinding);
        }

        return findings;
    }

    /**
     * Creates a finding for analysis errors (malformed bytecode, etc.).
     */
    private CryptoFinding createErrorFinding(Path classFile, String artifactName, String errorMessage) {
        CryptoFinding finding = new CryptoFinding();
        finding.setAlgorithm("UNKNOWN");
        finding.setFile(classFile.toString());
        finding.setLine(1);
        finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
        finding.setConfidence(CryptoFinding.Confidence.LOW);
        finding.setSourceType("BINARY");
        finding.setEvidence("Bytecode analysis error: " + errorMessage);
        finding.setLibrary("Java Bytecode");
        return finding;
    }

    /**
     * Extracts class name from file path.
     */
    private String extractClassName(Path classFile) {
        String fileName = classFile.getFileName().toString();
        if (fileName.endsWith(".class")) {
            return fileName.substring(0, fileName.length() - 6);
        }
        return fileName;
    }

    /**
     * ASM ClassVisitor that detects cryptographic API usage.
     */
    private static class CryptoClassVisitor extends ClassVisitor {

        private final Path classFile;
        private final String artifactName;
        private final List<CryptoFinding> findings;
        private String className;

        public CryptoClassVisitor(Path classFile, String artifactName) {
            super(Opcodes.ASM9);
            this.classFile = classFile;
            this.artifactName = artifactName;
            this.findings = new ArrayList<>();
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name.replace('/', '.');
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new CryptoMethodVisitor(className, name, classFile, artifactName, findings);
        }

        public List<CryptoFinding> getFindings() {
            return findings;
        }
    }

    /**
     * ASM MethodVisitor that detects cryptographic method calls and constant strings.
     */
    private static class CryptoMethodVisitor extends MethodVisitor {

        private final String className;
        private final String methodName;
        private final Path classFile;
        private final String artifactName;
        private final List<CryptoFinding> findings;
        private int methodCount = 0;

        public CryptoMethodVisitor(String className, String methodName, Path classFile, String artifactName, List<CryptoFinding> findings) {
            super(Opcodes.ASM9);
            this.className = className;
            this.methodName = methodName;
            this.classFile = classFile;
            this.artifactName = artifactName;
            this.findings = findings;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            methodCount++;
            if (methodCount > BinaryAnalysisLimits.MAX_METHODS_PER_CLASS) {
                return; // Skip excessive methods
            }

            // Check if this is a cryptographic API call
            if (CRYPTO_API_CLASSES.contains(owner)) {
                handleCryptoApiCall(owner, name, descriptor);
            }

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitLdcInsn(Object value) {
            // Check for constant string arguments that might be algorithm names
            if (value instanceof String) {
                String strValue = (String) value;
                String algorithm = detectAlgorithmFromString(strValue);
                if (algorithm != null) {
                    createFindingFromConstantString(algorithm, strValue);
                }
            }
            super.visitLdcInsn(value);
        }

        /**
         * Handles calls to cryptographic API methods.
         */
        private void handleCryptoApiCall(String owner, String methodName, String descriptor) {
            String apiClass = owner.replace('/', '.');
            String algorithm = inferAlgorithmFromApi(apiClass, methodName);
            
            if (algorithm != null) {
                CryptoFinding finding = new CryptoFinding();
                finding.setAlgorithm(algorithm);
                finding.setFile(classFile.toString());
                finding.setLine(1);
                finding.setPurpose(inferPurpose(apiClass, methodName));
                finding.setConfidence(CryptoFinding.Confidence.HIGH);
                finding.setSourceType("BINARY");
                finding.setEvidence(apiClass + "." + methodName + "()");
                finding.setLibrary("Java Cryptography Architecture (JCA)");
                findings.add(finding);
            }
        }

        /**
         * Infers algorithm from API class and method name.
         */
        private String inferAlgorithmFromApi(String apiClass, String methodName) {
            if (apiClass.contains("Cipher")) {
                return "AES"; // Default assumption, may be refined from constant strings
            } else if (apiClass.contains("KeyPairGenerator")) {
                return "RSA"; // Default assumption, may be refined
            } else if (apiClass.contains("Signature")) {
                return "RSA"; // Default assumption, may be refined
            } else if (apiClass.contains("MessageDigest")) {
                return "SHA-256"; // Default assumption, may be refined
            } else if (apiClass.contains("SSLContext")) {
                return "TLS";
            } else if (apiClass.contains("KeyAgreement")) {
                return "ECDH";
            }
            return "UNKNOWN";
        }

        /**
         * Infers cryptographic purpose from API class and method.
         */
        private CryptoFinding.Purpose inferPurpose(String apiClass, String methodName) {
            if (apiClass.contains("Cipher") || apiClass.contains("KeyGenerator")) {
                return CryptoFinding.Purpose.ENCRYPTION;
            } else if (apiClass.contains("Signature")) {
                return CryptoFinding.Purpose.DIGITAL_SIGNATURE;
            } else if (apiClass.contains("MessageDigest")) {
                return CryptoFinding.Purpose.HASHING;
            } else if (apiClass.contains("KeyPairGenerator") || apiClass.contains("KeyAgreement")) {
                return CryptoFinding.Purpose.KEY_GENERATION;
            } else if (apiClass.contains("SSLContext")) {
                return CryptoFinding.Purpose.PROTOCOL;
            }
            return CryptoFinding.Purpose.UNKNOWN;
        }

        /**
         * Detects algorithm from constant string arguments.
         */
        private String detectAlgorithmFromString(String strValue) {
            String upper = strValue.toUpperCase();
            
            // Symmetric algorithms
            if (upper.contains("AES")) {
                if (upper.contains("GCM")) {
                    return "AES/GCM/NoPadding";
                } else if (upper.contains("CBC")) {
                    return "AES/CBC/PKCS5Padding";
                }
                return "AES";
            } else if (upper.contains("DES")) {
                if (upper.contains("DESede") || upper.contains("3DES")) {
                    return "DESede";
                }
                return "DES";
            }
            
            // Asymmetric algorithms
            else if (upper.contains("RSA")) {
                return "RSA";
            } else if (upper.contains("ECDSA")) {
                return "ECDSA";
            } else if (upper.contains("ECDH")) {
                return "ECDH";
            } else if (upper.contains("EC")) {
                return "EC";
            }
            
            // Hash algorithms
            else if (upper.contains("SHA-256") || upper.contains("SHA256")) {
                return "SHA-256";
            } else if (upper.contains("SHA-384") || upper.contains("SHA384")) {
                return "SHA-384";
            } else if (upper.contains("SHA-512") || upper.contains("SHA512")) {
                return "SHA-512";
            } else if (upper.contains("SHA-1") || upper.contains("SHA1")) {
                return "SHA-1";
            } else if (upper.contains("SHA-3") || upper.contains("SHA3")) {
                return "SHA-3";
            } else if (upper.contains("MD5")) {
                return "MD5";
            }
            
            // TLS versions
            else if (upper.contains("TLSV1.3") || upper.contains("TLS 1.3")) {
                return "TLSv1.3";
            } else if (upper.contains("TLSV1.2") || upper.contains("TLS 1.2")) {
                return "TLSv1.2";
            } else if (upper.contains("TLSV1.1") || upper.contains("TLS 1.1")) {
                return "TLSv1.1";
            } else if (upper.contains("TLSV1") || upper.contains("TLS 1")) {
                return "TLSv1";
            } else if (upper.contains("TLS")) {
                return "TLS";
            }
            
            return null;
        }

        /**
         * Creates a finding from a detected constant string.
         */
        private void createFindingFromConstantString(String algorithm, String rawString) {
            CryptoFinding finding = new CryptoFinding();
            finding.setAlgorithm(algorithm);
            finding.setFile(classFile.toString());
            finding.setLine(1);
            finding.setPurpose(inferPurposeFromAlgorithm(algorithm));
            finding.setConfidence(CryptoFinding.Confidence.HIGH);
            finding.setSourceType("BINARY");
            finding.setEvidence("Constant string: \"" + rawString + "\"");
            finding.setLibrary("Java Cryptography Architecture (JCA)");
            
            // Extract variant/mode if present
            if (algorithm.contains("/")) {
                String[] parts = algorithm.split("/");
                finding.setAlgorithm(parts[0]);
                finding.setVariant(parts.length > 1 ? parts[1] : null);
            }
            
            findings.add(finding);
        }

        /**
         * Infers purpose from algorithm name.
         */
        private CryptoFinding.Purpose inferPurposeFromAlgorithm(String algorithm) {
            String upper = algorithm.toUpperCase();
            if (upper.contains("AES") || upper.contains("DES")) {
                return CryptoFinding.Purpose.ENCRYPTION;
            } else if (upper.contains("RSA") || upper.contains("ECDSA")) {
                return CryptoFinding.Purpose.DIGITAL_SIGNATURE;
            } else if (upper.contains("ECDH")) {
                return CryptoFinding.Purpose.KEY_AGREEMENT;
            } else if (upper.contains("SHA") || upper.contains("MD5")) {
                return CryptoFinding.Purpose.HASHING;
            } else if (upper.contains("TLS")) {
                return CryptoFinding.Purpose.PROTOCOL;
            }
            return CryptoFinding.Purpose.UNKNOWN;
        }
    }
}
