package demo;

import java.security.KeyPairGenerator;
import java.security.Signature;
import javax.crypto.Cipher;

public class RSAExample {

    public void generateKeyPair() throws Exception {
        // RSA-2048 key generation
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
    }

    public void signData() throws Exception {
        // RSA Digital Signature
        Signature signature = Signature.getInstance("SHA256withRSA");
    }

    public void encryptData() throws Exception {
        // RSA Encryption / Key Transport
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    }
}