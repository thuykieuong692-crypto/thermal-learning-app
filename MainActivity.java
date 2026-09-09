package com.thermal.learning;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    // 与 assets/index.html 内置基线保持一致；每次发布新版 APK 时改这里
    private static final String API_BASE = "https://1483887786-cd9ayeq1pb.ap-guangzhou.tencentscf.com";
    private static final String BUILTIN_VER = "v2.3"; // 内置基线版本

    private WebView webView;
    private Handler ui;
    private volatile boolean loadedOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(0xFF0B1220);
        getWindow().setNavigationBarColor(0xFF0B1220);

        ui = new Handler(Looper.getMainLooper());
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString((s.getUserAgentString() == null ? "" : s.getUserAgentString())
                + " ThermalApp/" + BUILTIN_VER);

        // 外链桥接 + 检查更新接口
        webView.addJavascriptInterface(new Bridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("file://") || url.contains("tencentscf.com")) {
                    return false;
                }
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            }
        });

        setContentView(webView);

        // 优先加载已下载的在线版；否则加载内置版
        File cached = cachedFile();
        if (cached.exists()) {
            loadedOnline = true;
            webView.loadUrl(cachedFileUrl());
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }

        // 后台静默检查更新（App 自动更新，无需重装）
        checkUpdate(false);
    }

    private File appDir() {
        File d = new File(getFilesDir(), "app");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private File cachedFile() { return new File(appDir(), "index.html"); }
    private File verFile() { return new File(appDir(), "version.json"); }
    private String cachedFileUrl() { return "file://" + cachedFile().getAbsolutePath(); }

    private void toast(final String msg) {
        ui.post(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    /** 检查更新。manual=true 时给用户明确提示 */
    private void checkUpdate(final boolean manual) {
        new Thread(() -> {
            try {
                String ver = httpGet(API_BASE + "/api/version", 8000);
                JSONObject v = new JSONObject(ver);
                final String onlineMd5 = v.optString("md5", "");
                final String onlineVer = v.optString("ver", "?");
                String localMd5 = readLocalMd5();
                if (!onlineMd5.isEmpty() && !onlineMd5.equals(localMd5)) {
                    String html = httpGet(API_BASE + "/index.html", 15000);
                    if (html != null && html.length() > 1000) {
                        saveCache(html, onlineMd5, onlineVer);
                        ui.post(() -> {
                            if (!isFinishing()) {
                                loadedOnline = true;
                                webView.loadUrl(cachedFileUrl());
                            }
                        });
                        toast("已更新到 " + onlineVer + "，正在应用");
                    } else if (manual) {
                        toast("下载新版失败，继续使用当前版本");
                    }
                } else if (manual) {
                    toast("已是最新（" + onlineVer + "）");
                }
            } catch (Exception e) {
                if (manual) toast("检查更新失败：网络不可用");
            }
        }).start();
    }

    private String readLocalMd5() {
        try {
            File f = verFile();
            if (!f.exists()) return "";
            JSONObject j = new JSONObject(readFile(f));
            return j.optString("md5", "");
        } catch (Exception e) { return ""; }
    }

    private void saveCache(String html, String md5, String ver) {
        try {
            FileOutputStream out = new FileOutputStream(cachedFile());
            out.write(html.getBytes("utf-8"));
            out.close();
            JSONObject j = new JSONObject();
            j.put("md5", md5);
            j.put("ver", ver);
            FileOutputStream vout = new FileOutputStream(verFile());
            vout.write(j.toString().getBytes("utf-8"));
            vout.close();
        } catch (Exception e) { /* ignore */ }
    }

    private String httpGet(String url, int timeout) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(timeout);
        c.setReadTimeout(timeout);
        c.setRequestProperty("User-Agent", "ThermalApp/" + BUILTIN_VER);
        int code = c.getResponseCode();
        InputStream in = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));
        String line;
        while ((line = r.readLine()) != null) sb.append(line).append("\n");
        r.close();
        c.disconnect();
        return sb.toString();
    }

    private String readFile(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line).append("\n");
        r.close();
        return sb.toString();
    }

    private class Bridge {
        @JavascriptInterface
        public void openExternal(String url) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }

        @JavascriptInterface
        public void checkUpdate() {
            MainActivity.this.checkUpdate(true);
        }

        @JavascriptInterface
        public String versionInfo() {
            try {
                JSONObject j = new JSONObject(httpGet(API_BASE + "/api/version", 8000));
                JSONObject r = new JSONObject();
                r.put("builtin", BUILTIN_VER);
                r.put("online", j.optString("ver", "?"));
                r.put("current", loadedOnline ? j.optString("ver", "?") : BUILTIN_VER);
                r.put("source", loadedOnline ? "online" : "builtin");
                return r.toString();
            } catch (Exception e) {
                try {
                    JSONObject r = new JSONObject();
                    r.put("builtin", BUILTIN_VER);
                    r.put("online", "?");
                    r.put("current", loadedOnline ? "?" : BUILTIN_VER);
                    r.put("source", loadedOnline ? "online" : "builtin");
                    return r.toString();
                } catch (Exception ex) { return "{}"; }
            }
        }
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
