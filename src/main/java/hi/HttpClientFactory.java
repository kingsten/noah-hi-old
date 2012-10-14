package hi;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClientFactory {

    public static final String BAIDU_STATIC_PAGE = "http://web.im.baidu.com/popup/src/v2Jump.html";
    public static final String BAIDU_ID_PAGE = "http://www.baidu.com";
    public static final String BAIDU_FETCH_TOKEN_PAGE = "https://passport.baidu.com/v2/api/?getapi&class=login&tpl=mn&tangram=false";
    public static final String BAIDU_PASSPORT_LOGIN_PAGE = "https://passport.baidu.com/v2/api/?login";
    public static final String BAIDU_PICK_URL = "http://web.im.baidu.com/pick?v=30&seq=2&session=&source=22&guid=h85sqv9o&type=23&flag=1&ack=";

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        DefaultHttpClient httpClient = httpClientFactory.generateClient("henryyu1983", "ecgroup");

        String guid = Base36.encode(new Date().getTime());

        //check login
        String checkUrl = "http://web.im.baidu.com/check?callback=_nbc_.f1&v=30&time=" + guid;
        HttpGet getCheck = new HttpGet(checkUrl);
        HttpResponse res1 = httpClient.execute(getCheck);
        System.out.println("Check result: " + EntityUtils.toString(res1.getEntity()));
        EntityUtils.consume(res1.getEntity());

        //welcome
        String welcomeUrl = "http://web.im.baidu.com/welcome";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("v", "30"));
        params.add(new BasicNameValuePair("seq", "0"));
        params.add(new BasicNameValuePair("session", ""));
        params.add(new BasicNameValuePair("source", "22"));
        params.add(new BasicNameValuePair("guid", guid));
        params.add(new BasicNameValuePair("force", "true"));
        params.add(new BasicNameValuePair("from", "0"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "gb2312");
        HttpPost postWelcome = new HttpPost(welcomeUrl);
        postWelcome.setEntity(entity);
        HttpResponse welcomeRes = httpClient.execute(postWelcome);
        System.out.println("Welcome: " + EntityUtils.toString(welcomeRes.getEntity()));
        EntityUtils.consume(welcomeRes.getEntity());

        //init
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("v", "30"));
        params.add(new BasicNameValuePair("seq", "1"));
        params.add(new BasicNameValuePair("session", ""));
        params.add(new BasicNameValuePair("source", "22"));
        params.add(new BasicNameValuePair("guid", guid));
        params.add(new BasicNameValuePair("status", "online"));
        entity = new UrlEncodedFormEntity(params, "gb2312");
        String initUrl = "http://web.im.baidu.com/init";
        HttpPost initPost = new HttpPost(initUrl);
        initPost.setEntity(entity);
        HttpResponse initRes = httpClient.execute(initPost);
        System.out.println("Init result: " + EntityUtils.toString(initRes.getEntity()));
        EntityUtils.consume(initRes.getEntity());

        //pick
        int sequence = 0;
        String ack = "";
        String pickUrl = "";
        String fromUser = "";

        while(true) {
            pickUrl = String.format("http://web.im.baidu.com/pick?v=30&session=&source=22&type=23&flag=1&seq=%d&ack=%s&guid=%s", sequence, ack, guid);
//            System.out.println(pickUrl);

            // "http://web.im.baidu.com/pick?v=30&seq=2&session=&source=22&guid=h85sqv9o&type=23&flag=1&ack=";
            HttpGet getRequest = new HttpGet(pickUrl);
            HttpResponse pickRes = httpClient.execute(getRequest);
            String entityStr = EntityUtils.toString(pickRes.getEntity());
            System.out.println("Pick result:" + entityStr);
            EntityUtils.consume(pickRes.getEntity());

            JSONObject jsonObject = JSONObject.fromObject(entityStr);
            JSONObject content = jsonObject.getJSONObject("content");
            if(content!=null && content.get("ack") != null) {
                ack = (String) content.get("ack");
                JSONArray fields = content.getJSONArray("fields");
                JSONObject o = fields.getJSONObject(0);
                fromUser = (String)o.get("from");
                System.out.println("++++Message from: " + fromUser);
            }
            sequence++;
            if(sequence > 2 )
                break;
        }

        String messageUrl= "http://web.im.baidu.com/message";
        HttpPost post = new HttpPost(messageUrl);

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("v", "30"));
        qparams.add(new BasicNameValuePair("seq", sequence + ""));
        qparams.add(new BasicNameValuePair("session", ""));
        qparams.add(new BasicNameValuePair("source", "22"));
        qparams.add(new BasicNameValuePair("guid", guid));
        qparams.add(new BasicNameValuePair("from", "henryyu1983"));
        qparams.add(new BasicNameValuePair("to", "于子元"));
        qparams.add(new BasicNameValuePair("body", "<msg><font n=\"宋体\" s=\"10\" b=\"0\" i=\"0\" ul=\"0\" c=\"0\"/><text c=\"中国人 \"/></msg>"));
        qparams.add(new BasicNameValuePair("friend", "true"));
//
        UrlEncodedFormEntity e = new UrlEncodedFormEntity(qparams, "utf-8");
        post.setEntity(e);
        HttpResponse response1 = httpClient.execute(post);
        System.out.println(EntityUtils.toString(response1.getEntity()));

//        HttpGet get = new HttpGet("http://passport.baidu.com/?logout&tpl=mn&u=http%3A%2F%2Fweb.im.baidu.com%2F");
//        HttpResponse res2 = httpClient.execute(get);
//        System.out.println(EntityUtils.toString(res2.getEntity()));
    }

    public DefaultHttpClient generateClient(String username, String password) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        initBAIDUID(httpClient);
        initBDUSS(httpClient, username, password);
        return httpClient;
    }

    private void initBDUSS(DefaultHttpClient httpClient, String username, String password) throws IOException {
        CookieStore cookieStore = httpClient.getCookieStore();
        String token = fetchToken(httpClient);

        String url = BAIDU_PASSPORT_LOGIN_PAGE;
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> qparams = initParam(username, password, token);

        UrlEncodedFormEntity e = new UrlEncodedFormEntity(qparams, "gb2312");
        httppost.setEntity(e);
        HttpResponse response = httpClient.execute(httppost);
        httpClient.getCookieStore().addCookie(cookieStore.getCookies().get(0));

        EntityUtils.consume(response.getEntity());
    }


    private String fetchToken(DefaultHttpClient httpClient) throws IOException {
        HttpGet get = new HttpGet(BAIDU_FETCH_TOKEN_PAGE);
        HttpResponse response = httpClient.execute(get);
        HttpEntity entity = response.getEntity();
        String token = "";
        if (entity != null) {
            String str = EntityUtils.toString(entity);
            Pattern pattern = Pattern.compile("bdPass.api.params.login_token='(.+)'");
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                token = matcher.group(1);
            }
        }
        return token;
    }

    private void initBAIDUID(DefaultHttpClient httpClient) throws IOException {
        HttpGet get = new HttpGet(BAIDU_ID_PAGE);
        HttpResponse response = httpClient.execute(get);
        CookieStore cookieStore = httpClient.getCookieStore();
        EntityUtils.consume(response.getEntity());
    }

    private static List<NameValuePair> initParam(String username, String password, String token) {
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("ppui_logintime", "100000"));
        qparams.add(new BasicNameValuePair("charset", "utf-8"));
        qparams.add(new BasicNameValuePair("codestring", ""));
        qparams.add(new BasicNameValuePair("token", token));
        qparams.add(new BasicNameValuePair("isPhone", "false"));
        qparams.add(new BasicNameValuePair("index", "0"));
        qparams.add(new BasicNameValuePair("u", ""));  //
        qparams.add(new BasicNameValuePair("safeflg", "0"));
        qparams.add(new BasicNameValuePair("staticpage", BAIDU_STATIC_PAGE));  //
        qparams.add(new BasicNameValuePair("loginType", "1"));
        qparams.add(new BasicNameValuePair("tpl", "mn"));
        qparams.add(new BasicNameValuePair("callback", "parent.bdPass.api.login._postCallback"));

        qparams.add(new BasicNameValuePair("username", username));
        qparams.add(new BasicNameValuePair("password", password));
        qparams.add(new BasicNameValuePair("verifycode", ""));
        qparams.add(new BasicNameValuePair("mem_pass", "on"));
        return qparams;
    }


}
