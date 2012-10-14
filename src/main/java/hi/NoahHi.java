package hi;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class NoahHi implements Runnable {

    public static final String guid = Base36.encode(new Date().getTime());
    private DefaultHttpClient httpClient;
    private int sequence = 0;
    private String ack = "";
    private String pickUrl = "";

    public NoahHi() throws IOException {
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        httpClient = httpClientFactory.generateClient("group_test", "group123456789");

        login();
        welcome();
        init();
        // pick();
        //pickMessage();
    }

    public static void main(String[] args) throws IOException {
        NoahHi noahHi_1 = new NoahHi();
        Thread t = new Thread(noahHi_1);
        t.start();
        // noahHi.sendMessage();
        // noahHi.sendGroupMessage();
        // noahHi.queryInfo();
        // noahHi.deleteFriend("雁过无痕_LKQ");
        //noahHi.addFriend("雁过无痕_LKQ", 0, "");
        // noahHi.logout();
        InputStreamReader stdin = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(stdin);
        String command = "";
        NoahHi noahHi = new NoahHi();
        while (true) {
            System.out.println("请输入命令：");
            command = br.readLine();
            if ("logout".equalsIgnoreCase(command)) {
                noahHi.logout();
                System.exit(0);
            } else if ("queryInfo".equalsIgnoreCase(command)) {
                System.out.println("请输入需要查询的用户Id：");
                String userName = br.readLine();
                noahHi.queryInfo(userName);
            } else if ("deleteFriend".equalsIgnoreCase(command)) {
                System.out.println("请输入您需要删除的好友的Id：");
                String userName = br.readLine();
                noahHi.deleteFriend(userName);
            } else {
                System.out.println("没有该命令！");
            }

        }
    }

    public void login() throws IOException {
        String checkUrl = "http://web.im.baidu.com/check?callback=_nbc_.f1&v=30&time=" + guid;
        HttpGet getCheck = new HttpGet(checkUrl);
        HttpResponse res1 = httpClient.execute(getCheck);
        System.out.println("Check result: " + EntityUtils.toString(res1.getEntity()));
        EntityUtils.consume(res1.getEntity());
    }

    public void welcome() throws IOException {
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
    }

    public void init() throws IOException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("v", "30"));
        params.add(new BasicNameValuePair("seq", "1"));
        params.add(new BasicNameValuePair("session", ""));
        params.add(new BasicNameValuePair("source", "22"));
        params.add(new BasicNameValuePair("guid", guid));
        params.add(new BasicNameValuePair("status", "online"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "gb2312");
        String initUrl = "http://web.im.baidu.com/init";
        HttpPost initPost = new HttpPost(initUrl);
        initPost.setEntity(entity);
        HttpResponse initRes = httpClient.execute(initPost);
        System.out.println("Init result: " + EntityUtils.toString(initRes.getEntity()));
        EntityUtils.consume(initRes.getEntity());
    }

    public void pick() throws IOException {
        while (true) {
            pickUrl = String.format("http://web.im.baidu.com/pick?v=30&session=&source=22&type=23&flag=1&seq=%d&ack=%s&guid=%s", sequence, ack, guid);

            HttpGet getRequest = new HttpGet(pickUrl);

            HttpResponse pickRes = httpClient.execute(getRequest);
            String entityStr = EntityUtils.toString(pickRes.getEntity());
            System.out.println("Pick result:" + entityStr);
            EntityUtils.consume(pickRes.getEntity());

            JSONObject jsonObject = JSONObject.fromObject(entityStr);
            JSONObject content = jsonObject.getJSONObject("content");
            if (content != null && content.get("ack") != null) {
                ack = (String) content.get("ack");
                JSONArray fields = content.getJSONArray("fields");
                JSONObject o = fields.getJSONObject(0);
                String fromUser = (String) o.get("from");
                System.out.println("++++Message from: " + fromUser);
            }
            sequence++;
            if (sequence > 2)
                break;
        }
    }

    public void pickMessage() throws IOException, InterruptedException {
        while (true) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("v", "30");
            parameters.put("session", "");
            parameters.put("source", "22");
            parameters.put("type", "23");
            parameters.put("flag", "1");
            parameters.put("seq", sequence + "");
            parameters.put("ack", ack);
            parameters.put("guid", guid);
            /*pickUrl = String.format("http://web.im.baidu.com/pick?v=30&session=&source=22&type=23&flag=1&seq=%d&ack=%s&guid=%s", sequence, ack, guid);
            HttpGet getRequest = new HttpGet(pickUrl);
            HttpResponse pickRes = httpClient.execute(getRequest);
            String entityStr = EntityUtils.toString(pickRes.getEntity());*/
            String entityStr = requestAPi("get", "pick", parameters);
            System.out.println("Pick result:" + entityStr);
           // EntityUtils.consume(pickRes.getEntity());

            JSONObject jsonObject = JSONObject.fromObject(entityStr);
            JSONObject content = jsonObject.getJSONObject("content");
            if (content != null && content.get("ack") != null) {
                ack = (String) content.get("ack");
                JSONArray fields = content.getJSONArray("fields");
                JSONObject fieldObject = fields.getJSONObject(0);
                String command = fieldObject.getString("command");
                if ("message".equalsIgnoreCase(command)) {
                    String fromUser = fieldObject.getString("from");
                    JSONArray contentArray = fieldObject.getJSONArray("content");
                    String message = "";

                    for (int i = 0; i < contentArray.size(); i++) {
                        JSONObject messageType = contentArray.getJSONObject(i);
                        if ("text".equalsIgnoreCase(messageType.getString("type"))) {
                            message = messageType.getString("c");
                            break;
                        }
                    }
                    System.out.println("message from : " + fromUser + " , message : " + message);
                    sendMessage(fromUser, "已收到消息");
                } else if ("groupmessage".equalsIgnoreCase(command)) {
                    JSONObject fieldContent = fieldObject.getJSONObject("content");
                    String groupId = fieldContent.getString("gid");
                    String fromUser = fieldContent.getString("from");
                    JSONArray contentArray = fieldContent.getJSONArray("content");
                    String message = "";
                    for (int i = 0; i < contentArray.size(); i++) {
                        JSONObject messageType = contentArray.getJSONObject(i);
                        if ("text".equalsIgnoreCase(messageType.getString("type"))) {
                            message = messageType.getString("c");
                            break;
                        }
                    }
                    System.out.println("groupMessage from : " + groupId + " , fromUser : " + fromUser + " , message : " + message);
                    sendGroupMessage(groupId, "已收到群消息");
                } else if ("activity".equalsIgnoreCase(command)) {

                } else if ("friendaddnotify".equalsIgnoreCase(command)) {
                    JSONObject fieldContent = fieldObject.getJSONObject("content");
                    String userName = fieldContent.getString("username");
                    addFriend(userName, 0, "接受");
                } else if ("addgroupmembernotify".equalsIgnoreCase(command)) {

                } else if ("deletegroupmembernotify".equalsIgnoreCase(command)) {

                } else if ("friendstatus".equalsIgnoreCase(command)) {

                } else if ("friendinfonotify".equalsIgnoreCase(command)) {

                } else {

                }
            }
            sequence++;
            Thread.sleep(1);

        }
    }

    public void sendMessage(String to, String message) throws IOException {
        String messageUrl = "http://web.im.baidu.com/message";
        HttpPost post = new HttpPost(messageUrl);

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("v", "30"));
        qparams.add(new BasicNameValuePair("seq", sequence + ""));
        qparams.add(new BasicNameValuePair("session", ""));
        qparams.add(new BasicNameValuePair("source", "22"));
        qparams.add(new BasicNameValuePair("guid", guid));
        qparams.add(new BasicNameValuePair("from", "group_test"));
        qparams.add(new BasicNameValuePair("to", to));
        qparams.add(new BasicNameValuePair("body", "<msg><font n=\"宋体\" s=\"10\" b=\"0\" i=\"0\" ul=\"0\" c=\"0\"/><text c=\"" + message + " \"/></msg>"));
        qparams.add(new BasicNameValuePair("friend", "true"));
        UrlEncodedFormEntity e = new UrlEncodedFormEntity(qparams, "utf-8");
        post.setEntity(e);
        HttpResponse response1 = httpClient.execute(post);
        System.out.println(EntityUtils.toString(response1.getEntity()));
    }

    public void sendGroupMessage(String toGid, String message) throws IOException {
        String messageUrl = "http://web.im.baidu.com/groupmessage";
        HttpPost post = new HttpPost(messageUrl);

        List<NameValuePair> groupParameters = new ArrayList<NameValuePair>();
        groupParameters.add(new BasicNameValuePair("v", "30"));
        groupParameters.add(new BasicNameValuePair("seq", sequence + ""));
        groupParameters.add(new BasicNameValuePair("session", ""));
        groupParameters.add(new BasicNameValuePair("source", "22"));
        groupParameters.add(new BasicNameValuePair("messageid", ""));
        groupParameters.add(new BasicNameValuePair("from", "group_test"));
        groupParameters.add(new BasicNameValuePair("gid", toGid));
        groupParameters.add(new BasicNameValuePair("body", "<msg><font n=\"宋体\" s=\"10\" b=\"0\" i=\"0\" ul=\"0\" c=\"0\"/><text c=\"" + message + " \"/></msg>"));
        UrlEncodedFormEntity e = new UrlEncodedFormEntity(groupParameters, "utf-8");
        post.setEntity(e);
        HttpResponse response1 = httpClient.execute(post);
        System.out.println(EntityUtils.toString(response1.getEntity()));

    }

    public void logout() throws IOException {
        String logoutUrl = String.format("http://web.im.baidu.com/logout?v=30&session=&source=22&seq=%d&guid=%s", sequence, guid);
        HttpGet logout = new HttpGet(logoutUrl);
        HttpResponse res1 = httpClient.execute(logout);
        System.out.println("logout result: " + EntityUtils.toString(res1.getEntity()));
        EntityUtils.consume(res1.getEntity());
    }

    public void queryInfo(String userName) throws IOException {
        String field = "relationship,username,showname,showtype,status";
        String queryInfoUrl = String.format("http://web.im.baidu.com/queryinfo?username=%s&field=%s", userName, field);
        HttpGet getRequest = new HttpGet(queryInfoUrl);
        HttpResponse pickRes = httpClient.execute(getRequest);
        String entityStr = EntityUtils.toString(pickRes.getEntity());
        System.out.println("queryInfo Result:" + entityStr);
        EntityUtils.consume(pickRes.getEntity());
    }

    public void addFriend(String userName, Integer tid, String comment) throws IOException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", userName);
        parameters.put("agree", "1");
        parameters.put("comment", comment);
        requestAPi("post", "verifyfriend", parameters);
        //加对方为好友
        /*String validate = verifyCode("addfriend", userName);
        parameters.remove("agree");
        parameters.put("comment", "回加");
        parameters.put("tid", tid.toString());
        parameters.put("validate", validate);
        requestAPi("get", "addfriend", parameters);*/
    }


    public void deleteFriend(String userName) throws IOException {
        String validate = verifyCode("deletefriend", userName);
        String deleteFriendUrl = String.format("http://web.im.baidu.com/deletefriend?username=%s&validate=%s", userName, validate);
        HttpGet getRequest = new HttpGet(deleteFriendUrl);
        HttpResponse pickRes = httpClient.execute(getRequest);
        String entityStr = EntityUtils.toString(pickRes.getEntity());
        System.out.println("deleteFriend : " + entityStr);
    }

    public String verifyCode(String type, String userName) throws IOException {
        String verifyUrl = String.format("http://web.im.baidu.com/verifycode?type=%s&username=%s", type, userName);
        HttpGet getRequest = new HttpGet(verifyUrl);
        HttpResponse pickRes = httpClient.execute(getRequest);
        String entityStr = EntityUtils.toString(pickRes.getEntity());
        JSONObject jsonObject = JSONObject.fromObject(entityStr);
        if ("ok".equalsIgnoreCase(jsonObject.getString("result"))) {
            JSONObject content = jsonObject.getJSONObject("content");
            JSONObject validate = content.getJSONObject("validate");
            System.out.println("validateStr : " + validate.toString());
            if (!validate.containsKey("v_code")) {
                return "";
            }
            String validateStr = validate.getString("v_url") + "," + validate.getString("v_period") + "," + validate.getString("v_time") + "," + validate.getString("v_code");
            return validateStr;
        } else {
            System.out.println("verify Result:" + entityStr);
            EntityUtils.consume(pickRes.getEntity());
            return "";
        }
    }

    public String requestAPi(String method, String api, Map<String, String> parameters) throws IOException {
        String requestUrl = "http://web.im.baidu.com/" + api;
        if ("get".equalsIgnoreCase(method)) {
            List<String> parameterList = new ArrayList<String>();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String parameterName = entry.getKey();
                String parameterValue = entry.getValue();
                parameterList.add(parameterName + "=" + parameterValue);
            }
            String query = implodeArrayList(parameterList, "&");
            if (StringUtils.isNotEmpty(query)) {
                requestUrl = requestUrl + "?" + query;
            }
            HttpGet getRequest = new HttpGet(requestUrl);
            HttpResponse response = httpClient.execute(getRequest);
            String entityStr = EntityUtils.toString(response.getEntity());
            System.out.println("get Result : " + entityStr);
            return entityStr;
        } else {
            HttpPost post = new HttpPost(requestUrl);
            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String parameterName = entry.getKey();
                String parameterValue = entry.getValue();
                postParameters.add(new BasicNameValuePair(parameterName, parameterValue));
            }
            UrlEncodedFormEntity e = new UrlEncodedFormEntity(postParameters, "utf-8");
            post.setEntity(e);
            HttpResponse response = httpClient.execute(post);
            String entityStr = EntityUtils.toString(response.getEntity());
            System.out.println("post Result : " + entityStr);
            return entityStr;
        }
    }

    public String implodeArrayList(List<String> arrayList, String separator) {
        StringBuilder queryBuilder = new StringBuilder();
        if (arrayList.size() > 0) {
            queryBuilder.append(arrayList.get(0));
            for (int i = 1; i < arrayList.size(); i++) {
                queryBuilder.append(separator);
                queryBuilder.append(arrayList.get(i));
            }
        }
        return queryBuilder.toString();
    }


    public void run() {
        try {
            pickMessage();
        } catch (IOException e) {
            System.out.println("error:" + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("error:" + e.getMessage());
        }
    }
}
