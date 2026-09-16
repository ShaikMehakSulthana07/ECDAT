package demo;

import java.security.Signature;
import javax.crypto.KeyAgreement;

public class AppB {
    public void runCrypto() throws Exception {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA");
        KeyAgreement ecdh = KeyAgreement.getInstance("ECDH");
    }
}
