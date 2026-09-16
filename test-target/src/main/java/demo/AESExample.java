package demo;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class AESExample {

    public void encryptData() throws Exception {
        // AES/GCM Authenticated Encryption
        Cipher gcmCipher = Cipher.getInstance("AES/GCM/NoPadding");

        // AES-CBC Encryption
        Cipher cbcCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Key Generator for AES
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        // Dynamic algorithm at runtime - Scanner MUST report UNKNOWN and LOW confidence without guessing
        String algorithm = getAlgorithm();
        Cipher dynamicCipher = Cipher.getInstance(algorithm);
    }

    private String getAlgorithm() {
        return System.getProperty("crypto.cipher.algo", "AES/GCM/NoPadding");
    }
}