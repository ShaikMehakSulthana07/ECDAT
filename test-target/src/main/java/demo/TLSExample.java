package demo;

import javax.net.ssl.SSLContext;

public class TLSExample {

    public void initTls() throws Exception {
        SSLContext tlsContext = SSLContext.getInstance("TLSv1.3");
    }
}