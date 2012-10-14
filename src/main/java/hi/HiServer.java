package hi;

import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class HiServer {

    public static void main(String[] args) throws IOException {
        int count = 1;
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        DefaultHttpClient httpClient = httpClientFactory.generateClient("于子元", "ecgroup");
        for(int i = 0; i < count; i++) {
            new Waiter(httpClient).start();
        }
    }
}
