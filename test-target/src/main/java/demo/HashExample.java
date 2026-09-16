package demo;

import java.security.MessageDigest;

public class HashExample {

    public void hashOperations() throws Exception {
        // Legacy weak hashes
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        // Approved modern hashes
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        MessageDigest sha3 = MessageDigest.getInstance("SHA3-256");
    }
}