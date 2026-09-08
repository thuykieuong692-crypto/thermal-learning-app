package com.thermal.learning;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 沉浸式：状态栏 / 导航栏融入深色背景
        getWindow().setStatusBarColor(0xFF0B1220);
        getWindow().setNavigationBarColor(0xFF0B1220);

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 让页面里的资料外链（B站 / YouTube 等）用系统浏览器打开
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void openExternal(String url) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            }
        }, "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            // Android 7+ (API 24+) 走这个
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            // Android 5/6 (API 22/23) 走这个
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 本地资源与我们的接口域名留在应用内
                if (url.startsWith("file://") || url.contains("tencentscf.com")) {
                    return false;
                }
                // 其余外部链接用系统浏览器打开
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            }
        });

        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
