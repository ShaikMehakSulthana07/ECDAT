import javax.crypto.Cipher;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;

/**
 * Test fixture with various cryptographic API calls for binary scanning tests.
 * This file will be compiled to create test JAR/CLASS artifacts.
 */
public class CryptoFixture {
    
    void testAES() throws Exception {
        Cipher.getInstance("AES/GCM/NoPadding");
    }
    
    void testAES_CBC() throws Exception {
        Cipher.getInstance("AES/CBC/PKCS5Padding");
    }
    
    void testRSA() throws Exception {
        KeyPairGenerator.getInstance("RSA");
    }
    
    void testSHA256() throws Exception {
        MessageDigest.getInstance("SHA-256");
    }
    
    void testSHA1() throws Exception {
        MessageDigest.getInstance("SHA-1");
    }
    
    void testSHA512() throws Exception {
        MessageDigest.getInstance("SHA-512");
    }
    
    void testMD5() throws Exception {
        MessageDigest.getInstance("MD5");
    }
    
    void testRSASignature() throws Exception {
        Signature.getInstance("SHA256withRSA");
    }
    
    void testTLS() throws Exception {
        javax.net.ssl.SSLContext.getInstance("TLSv1.2");
    }
    
    void testDynamicAlgorithm(String algorithm) throws Exception {
        // This should produce UNKNOWN/LOW confidence
        Cipher.getInstance(algorithm);
    }
}
