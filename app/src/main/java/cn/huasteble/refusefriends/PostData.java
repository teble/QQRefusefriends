package cn.huasteble.refusefriends;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Scanner;

import static android.widget.Toast.LENGTH_LONG;

public class PostData {
    private URL url;
    private String data;
    private Map<String, String> headers;
    PostData(String url, Map<String, String> headers, String data) throws MalformedURLException {
        this.url = new URL(url);
        this.headers = headers;
        this.data = data;
//        Log.d("TEST", "PostData: 构造完毕！");
    }
    public void post(Handler handler) throws IOException {
        Message msg = new Message();
        msg.obj = data;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoInput(true);                  //打开输入流，以便从服务器获取数据
        conn.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
        conn.setRequestMethod("POST");          //设置以Post方式提交数据
        conn.setUseCaches(false);               //使用Post方式不能使用缓存
        conn.setRequestProperty( "Accept-Encoding", "" );
        conn.setReadTimeout(5000);
        conn.setConnectTimeout(5000);

        for (Map.Entry<String, String> entry : headers.entrySet()){
            Log.d("Test", "post: "+entry.getKey() +", "+entry.getValue());
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", data.length() + "");
        conn.setDoOutput(true);
        conn.connect();
        OutputStream os = conn.getOutputStream();
        os.write(data.getBytes());
        //handler.sendMessage(msg);
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            msg.obj = "成功";
            InputStream inputStream = conn.getInputStream();
            String resource = new Scanner(inputStream).useDelimiter("\\Z").next();

            Map<String, Object> res = JSON.getMapForJson(resource);
            if(res != null) {
                msg.obj =(String) res.get("msg");
                handler.sendMessage(msg);
            }
        }
        else {
            msg.obj = "网络错误";
            handler.sendMessage(msg);
        }
    }
}
