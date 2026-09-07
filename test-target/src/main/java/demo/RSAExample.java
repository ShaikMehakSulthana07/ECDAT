package demo;
import java.security.KeyPairGenerator;

public class RSAExample {
    public void generate() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
    }
}