package cn.huasteble.refusefriends.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author teble
 * @date 2019/11/12 16:48
 * @description
 */
public class OkHttpUtils {
    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static final String TAG = "OkHttpUtils";

    private static StringBuffer getQueryString(String url, Map<String, String> queries) {
        StringBuffer sb = new StringBuffer(url);
        if (queries != null && queries.keySet().size() > 0) {
            boolean firstFlag = !url.contains("?");
            for (Map.Entry entry : queries.entrySet()) {
                if (firstFlag) {
                    sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }
        return sb;
    }

    private static String execNewCall(Request request) {
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body()).string();
            }
        } catch (Exception e) {
            Log.d(TAG, "okHttp3 put error >> ex = ", e);
        }
        return "";
    }

    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return execNewCall(request);
    }

    public static String get(String url, Map<String, String> queries) {
        StringBuffer sb = getQueryString(url, queries);
        Request request = new Request.Builder()
                .url(sb.toString())
                .build();
        return execNewCall(request);
    }

    public static String postForm(String url, Map<String, String> params, Map<String, String> headers) {
        final FormBody.Builder builder = new FormBody.Builder();
        if (headers == null) {
            headers = new HashMap<>();
        }
        if (params != null && params.keySet().size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, Objects.requireNonNull(params.get(key)));
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(builder.build())
                .build();
        return execNewCall(request);
    }

    public static String post(String url, String data, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(data, MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"));
        if (headers == null) {
            headers = new HashMap<>();
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(requestBody)
                .build();
        return execNewCall(request);
    }

    public static String postJson(String url, String jsonString, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(jsonString, MediaType.parse("application/json; charset=utf-8"));
        if (headers == null) {
            headers = new HashMap<>();
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(requestBody)
                .build();
        return execNewCall(request);
    }
}
