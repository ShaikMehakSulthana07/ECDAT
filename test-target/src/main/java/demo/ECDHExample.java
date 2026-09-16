package demo;

import javax.crypto.KeyAgreement;

public class ECDHExample {

    public void agreeKey() throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
    }
}