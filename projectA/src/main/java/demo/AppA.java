package demo;

import java.security.KeyPairGenerator;
import javax.crypto.Cipher;

public class AppA {
    public void runCrypto() throws Exception {
        KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
        rsaGen.initialize(2048);

        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
    }
}
