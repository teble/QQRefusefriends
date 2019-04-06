package cn.huasteble.refusefriends;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;
import static cn.huasteble.refusefriends.Constance.qzoneUrl;
import static cn.huasteble.refusefriends.Constance.postUrl;
import static cn.huasteble.refusefriends.Constance.postDataBase;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog dialog;
    private int bkn = 0;
    private String cookie;
    private WebView webView;
    private long firstTime = 0;

    @SuppressLint("HandlerLeak")
    final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingMainView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                toast("再按一次退出程序");
                firstTime = secondTime;
                return true;
            } else{
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void settingMainView() {
        setContentView(R.layout.activity_main);
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "请先登陆QQ！", LENGTH_LONG).show();
                setBkn(0);
                settingWebView();
            }
        });
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getBkn() == 0) {
                    Toast.makeText(getBaseContext(), "请先登陆QQ！", LENGTH_LONG).show();
                    return;
                }
                Map<String, String> map = new HashMap<>();
                map.put("Host", "ti.qq.com");
                map.put("Connection", "Keep-Alive");
                map.put("Cookie", getCookie());
                try {
                    final PostData postData = new PostData(postUrl, map, postDataBase + getBkn(getSKey(getCookie())));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                postData.post(mHandler);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void settingWebView() {
        setContentView(R.layout.web_view);
        init();
    }

    private void clearCookies(Context context) {
        CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(qzoneUrl);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse(url));
                    startActivity(intent);
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                //返回值是true的时候控制网页在WebView中去打开，如果为false调用系统浏览器或第三方浏览器去打开
                return true;
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                //newProgress 1-100之间的整数
                if (newProgress == 100) {
                    //网页加载完毕，关闭ProgressDialog
                    closeDialog();
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookies = cookieManager.getCookie(qzoneUrl);
//                    Log.d("Test", "onProgressChanged: " + webView.getUrl());
                    String sKey;
                    if (cookies != null && !webView.getUrl().equals(qzoneUrl)) {
                        Log.d("Test", "onProgressChanged: " + cookies);
                        setCookie(cookies);
                        sKey = getSKey(cookies);
                        Log.d("TEST", "onProgressChanged: "+ sKey);
                        if (sKey != null) {
                            setBkn(getBkn(sKey));
                            toast("登陆成功！");
                            settingMainView();
                        }
                    }
                } else {
                    //网页正在加载,打开ProgressDialog
                    openDialog(newProgress);
                }
            }

            private void closeDialog() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
            }

            private void openDialog(int newProgress) {

                if (dialog == null) {
                    dialog = new ProgressDialog(MainActivity.this);
                    dialog.setTitle("加载中……");
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.setProgress(newProgress);
                    dialog.show();
                } else {
                    dialog.setProgress(newProgress);
                }
            }
        });
    }

    private void setBkn(int num) {
        this.bkn = num;
    }

    private int getBkn() {
        return this.bkn;
    }

    private String getSKey(String cookies) {
        String sKey;
        String[] strings = cookies.split("; ");
        Map<String, String> map = new HashMap<>();
        for (String string : strings) {
            if (string.isEmpty()) continue;
            String[] str = string.split("=");
            if (str.length == 2) {
                map.put(str[0], str[1]);
            } else {
                map.put(str[0], "");
            }
        }
        sKey = map.get("skey");
        return sKey;
    }

    private int getBkn(String sKey) {
        int base = 5381;
        for (int i = 0; i < sKey.length(); i++) {
            base += (base << 5) + sKey.charAt(i);
        }
        return base & 2147483647;
    }

    private void toast(String string) {
        Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
    }

    private void setCookie(String cookie) {
        this.cookie = cookie;
    }

    private String getCookie() {
        return this.cookie;
    }
}
