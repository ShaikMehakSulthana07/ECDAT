package demo;
import java.security.MessageDigest;

public class HashExample {
    public void hash() throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
    }
}