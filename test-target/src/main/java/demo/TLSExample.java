package demo;
import javax.net.ssl.SSLContext;

public class TLSExample {
    public void connect() throws Exception {
        SSLContext context = SSLContext.getInstance("TLSv1.3");
    }
}