package hi;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Waiter extends Thread {
    public static final String BAIDU_PICK_URL = "http://web.im.baidu.com/pick?v=30&seq=2&session=&source=22&guid=h85sqv9o&type=23&flag=1&ack=";
    private HttpClient httpClient;

    public Waiter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void run() {
        HttpGet getRequest = new HttpGet(BAIDU_PICK_URL);
        HttpResponse response = null;
        try {
            response = httpClient.execute(getRequest);
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
