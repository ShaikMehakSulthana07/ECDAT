package demo;

import java.security.KeyPairGenerator;
import java.security.Signature;

public class ECDSAExample {

    public void generateECKey() throws Exception {
        KeyPairGenerator ecGenerator = KeyPairGenerator.getInstance("EC");
    }

    public void signWithECDSA() throws Exception {
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
    }
}