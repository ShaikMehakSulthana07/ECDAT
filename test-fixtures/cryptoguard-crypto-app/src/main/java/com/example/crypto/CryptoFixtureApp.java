package com.example.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.net.ssl.SSLContext;

/**
 * Deterministic crypto fixture for CryptoGuard testing.
 * Contains deliberate cryptographic API usage for static analysis.
 * NO REAL SECRETS - TEST DATA ONLY.
 */
public class CryptoFixtureApp {

    public static void main(String[] args) throws Exception {
        System.out.println("CryptoGuard Test Fixture - DO NOT USE IN PRODUCTION");
        
        // Initialize all crypto services
        testRSA2048();
        testECDSA();
        testECDH();
        testAESGCM();
        testAESCBC();
        testSHA256();
        testSHA1();
        testMD5();
        testRSASignature();
        testTLSv12();
        testDynamicCipher();
        
        System.out.println("Crypto fixture initialized successfully");
    }

    /**
     * RSA 2048-bit key generation.
     */
    public static void testRSA2048() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        System.out.println("RSA 2048-bit key pair generated");
    }

    /**
     * ECDSA signature with secp256r1 curve.
     */
    public static void testECDSA() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        kpg.initialize(ecSpec);
        KeyPair keyPair = kpg.generateKeyPair();
        
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(keyPair.getPrivate());
        System.out.println("ECDSA with secp256r1 initialized");
    }

    /**
     * ECDH key agreement.
     */
    public static void testECDH() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        kpg.initialize(ecSpec);
        KeyPair keyPair = kpg.generateKeyPair();
        
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(keyPair.getPrivate());
        System.out.println("ECDH key agreement initialized");
    }

    /**
     * AES-GCM encryption.
     */
    public static void testAESGCM() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, new byte[12]);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        System.out.println("AES-GCM encryption initialized");
    }

    /**
     * AES-CBC encryption.
     */
    public static void testAESCBC() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        System.out.println("AES-CBC encryption initialized");
    }

    /**
     * SHA-256 hash.
     */
    public static void testSHA256() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest("test data".getBytes());
        System.out.println("SHA-256 hash computed");
    }

    /**
     * SHA-1 hash (weak, for testing).
     */
    public static void testSHA1() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest("test data".getBytes());
        System.out.println("SHA-1 hash computed (weak algorithm)");
    }

    /**
     * MD5 hash (weak, for testing).
     */
    public static void testMD5() throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest("test data".getBytes());
        System.out.println("MD5 hash computed (weak algorithm)");
    }

    /**
     * RSA signature with SHA-256.
     */
    public static void testRSASignature() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        System.out.println("RSA signature with SHA-256 initialized");
    }

    /**
     * TLSv1.2 SSL context.
     */
    public static void testTLSv12() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, null);
        System.out.println("TLSv1.2 SSL context initialized");
    }

    /**
     * Dynamic cipher selection (should remain unresolved in static analysis).
     */
    public static void testDynamicCipher() throws Exception {
        String algorithm = getAlgorithmFromConfiguration();
        // This dynamic call should not be resolved by static analysis
        // Cipher.getInstance(algorithm); // Commented to prevent runtime error
        System.out.println("Dynamic cipher selection: " + algorithm);
    }

    /**
     * Simulates reading configuration for dynamic algorithm selection.
     */
    private static String getAlgorithmFromConfiguration() {
        // Simulate reading from config file
        return "AES/GCM/NoPadding";
    }
}
