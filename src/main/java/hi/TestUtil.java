package hi;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {
    public static void main(String[] args) throws IOException {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("name", "于子元"));
        UrlEncodedFormEntity gb2312 = new UrlEncodedFormEntity(param, Charset.defaultCharset());
        //utf-8: %E4%BA%8E%E5%AD%90%E5%85%83
        //gb2312: %D3%DA%D7%D3%D4%AA

        System.out.println(EntityUtils.toString(gb2312));
    }
}
