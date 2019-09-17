package cn.huasteble.refusefriends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.huasteble.refusefriends.config.Constance;
import cn.huasteble.refusefriends.handler.CrashHandler;
import cn.huasteble.refusefriends.utils.Calculation;
import cn.huasteble.refusefriends.utils.HttpUtils;
import cn.huasteble.refusefriends.utils.ToastMessageThread;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import static cn.huasteble.refusefriends.utils.Calculation.getBkn;
import static cn.huasteble.refusefriends.utils.Calculation.getSKey;

/**
 * @author teble
 */
@Setter
@Getter
public class MainActivity extends AppCompatActivity {
    final private static String TAG = "main";
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private ToastMessageThread messageThread;
    private long firstTime = 0;
    private int bkn = 0;
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.getInstance().init();
        context = getApplicationContext();
        messageThread = new ToastMessageThread();
        messageThread.start();
        setMainView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.findViewById(android.R.id.content) == findViewById(R.id.webView)) {
            Log.d(TAG, "onKeyDown: ");
            setMainView();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                messageThread.sendMessage("再按一次退出程序");
                firstTime = secondTime;
                return true;
            } else{
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setMainView() {
        setContentView(R.layout.activity_main);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                messageThread.sendMessage("请先登陆QQ！");
                setWebView();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bkn == 0) {
                    messageThread.sendMessage("请先登陆QQ");
                    setMainView();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> map = new HashMap<>();
                        map.put("Host", "ti.qq.com");
                        map.put("Connection", "Keep-Alive");
                        map.put("Cookie", cookie);
                        String response = HttpUtils.post(Constance.POST_URL, map, Constance.POST_DATA + bkn);
                        messageThread.sendMessage(response);
                    }
                });
            }
        });
    }

    private void setWebView() {
        setContentView(R.layout.activity_web);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        final WebView webView = findViewById(R.id.webView);
        webView.loadUrl(Constance.QZONE_URL);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest webResourceRequest) {
                Uri uri = webResourceRequest.getUrl();
                Log.d(TAG, "====Uri: " + JSON.toJSONString(uri));
                if (Objects.requireNonNull(uri.getScheme()).startsWith("http")) {
                    view.loadUrl(uri.getPath());
                } else {
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookies = cookieManager.getCookie(Constance.QZONE_URL);
                    String sKey;
                    if (cookies != null && !view.getUrl().equals(Constance.QZONE_URL)) {
                        Log.d(TAG, "onProgressChanged: " + cookies);
                        setCookie(cookies);
                        sKey = Calculation.getSKey(cookies);
                        Log.d(TAG, "onProgressChanged: "+ sKey);
                        if (sKey != null) {
                            setBkn(Calculation.getBkn(sKey));
                            messageThread.sendMessage("登陆成功！");
                            setMainView();
                        }
                    }
                } else {
                    //网页正在加载,打开ProgressDialog
                    openDialog(newProgress);
                }
            }

            private void closeDialog() {
            }

            private void openDialog(int newProgress) {
            }
        });
    }

    public static Context getContext() {
        return context;
    }
}
