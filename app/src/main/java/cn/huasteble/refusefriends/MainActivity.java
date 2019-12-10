package cn.huasteble.refusefriends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.huasteble.refusefriends.config.Constance;
import cn.huasteble.refusefriends.utils.Calculation;
import cn.huasteble.refusefriends.utils.OkHttpUtils;
import cn.huasteble.refusefriends.utils.ProgressView;
import cn.huasteble.refusefriends.utils.ToastMessageThread;
import lombok.Getter;
import lombok.Setter;

/**
 * @author teble
 */
@Getter
@Setter
public class MainActivity extends AppCompatActivity {
    final private static String TAG = "main";
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private ToastMessageThread messageThread;
    private long firstTime = 0;
    private int bkn = 0;
    private String cookie;
    private boolean startView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        messageThread = new ToastMessageThread();
        messageThread.start();
        setMainView();

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                String url = uri.toString();
                Log.d(TAG, "onCreate: " + JSON.toJSONString(uri));
                Log.d(TAG, "====callbackUrl: " + url);
                setWebView(url);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isStartView()) {
            setMainView();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                messageThread.sendMessage("再按一次退出程序");
                firstTime = secondTime;
                return true;
            } else {
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setMainView() {
        setStartView(true);
        setContentView(R.layout.activity_main);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        button1.setOnClickListener(v -> setWebView(null));
        button2.setOnClickListener(v -> {
            if (bkn == 0) {
                messageThread.sendMessage("请先登陆QQ");
                setMainView();
                return;
            }
            new Thread(() -> {
                Map<String, String> headers = new HashMap<String, String>() {
                    {
                        put("Host", "ti.qq.com");
                        put("Connection", "Keep-Alive");
                        put("Cookie", cookie);
                    }
                };
                String response = OkHttpUtils.post(Constance.POST_URL, Constance.POST_DATA + bkn, headers);
                Map<String, Object> res = JSON.parseObject(response);
                String msg = (String) res.get("msg");
                messageThread.sendMessage(msg);
            }).start();
        });
    }

    private void setWebView(String url) {
        setStartView(false);
        setContentView(R.layout.activity_web);
        init(url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(String url) {
        ProgressView progressView;
        final WebView webView = findViewById(R.id.webView);
        if (url == null) {
            webView.loadUrl(Constance.QZONE_URL);
        } else {
            webView.loadUrl(url);
        }

        progressView = new ProgressView(context);
        progressView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(context)));
        progressView.setColor(Color.parseColor("#FFFF00CC"));
        progressView.setProgress(10);

        webView.addView(progressView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(Constance.QZONE_URL);
                String sKey;
                if (cookies != null && !Constance.QZONE_URL.equals(url)) {
                    Log.d(TAG, "onProgressChanged: " + cookies);
                    setCookie(cookies);
                    sKey = Calculation.getSKey(cookies);
                    Log.d(TAG, "onProgressChanged: " + sKey);
                    if (sKey != null) {
                        setBkn(Calculation.getBkn(sKey));
                        messageThread.sendMessage("登陆成功！");
                        setMainView();
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest webResourceRequest) {
                Uri uri = webResourceRequest.getUrl();
                Log.d(TAG, "====Uri: " + JSON.toJSONString(uri));
                if (Objects.requireNonNull(uri.getScheme()).startsWith("http")) {
                    view.loadUrl(uri.getPath());
                } else {
                    try {
                        String url = uri.toString()
                                .replace("googlechrome", "refusefriends")
                                .replace("Chrome", "refusefriends");
                        Uri uri1 = Uri.parse(url);
                        Log.d(TAG, "====Replace: " + JSON.toJSONString(uri1));
                        Log.d(TAG, "shouldOverrideUrlLoading: " + uri1.toString());
                        final Intent intent = new Intent(Intent.ACTION_VIEW, uri1);
                        Log.d(TAG, "====Intent: " + JSON.toJSONString(intent));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        messageThread.sendMessage("未安装QQ，请输入账号密码登陆！");
                    }
                }
                return true;
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if (newProgress == Constance.LOADING_COMPLETED) {
                    closeDialog();
                } else {
                    openDialog(newProgress);
                }
            }

            private void closeDialog() {
                progressView.setVisibility(View.GONE);
            }

            private void openDialog(int newProgress) {
                progressView.setProgress(newProgress);
            }
        });
    }

    private int dp2px(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((float) 4 * scale + 0.5f);
    }

    public static Context getContext() {
        return context;
    }
}
