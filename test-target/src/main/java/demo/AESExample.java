package demo;
import javax.crypto.Cipher;

public class AESExample {
    public void encrypt() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        
        String runtimeGenerated = getAlgorithm();
        Cipher unknownCipher = Cipher.getInstance(runtimeGenerated);
    }
    private String getAlgorithm() { return "AES/CBC/PKCS5Padding"; }
}