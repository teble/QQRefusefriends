package cn.huasteble.refusefriends.utils;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author teble
 * @date 2019/9/1 14:46
 */
public class HttpUtils {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static String get(String url) {
        Request request = new Request.Builder().get().url(url).build();
        Response response = null;
        try {
            response = CLIENT.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Objects.requireNonNull(response).isSuccessful()) {
            return "请求失败";
        }
        String result = null;
        try {
            result = Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String post(String url, Map<String, String> headersMap, String data) {
        RequestBody requestBody = RequestBody.create(data, MediaType.get("text/plain;charset=utf-8"));
        Headers headers = Headers.of(headersMap);
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .addHeader("Accept-Encoding", "")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Content-Length", data.length() + "")
                .post(requestBody)
                .build();
        Response response = null;
        try {
            response = CLIENT.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Objects.requireNonNull(response).isSuccessful()) {
            return "请求失败";
        }
        String result = null;
        try {
            result = Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
