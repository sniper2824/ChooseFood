package com.example.choosefood;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class WebCrawlerActivity extends AppCompatActivity {

    private static final String TAG = WebCrawlerActivity.class.getName();

    TextView webText;

//    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_crawler);
        String type = getIntent().getStringExtra("type");

        webText = findViewById(R.id.webText);

        switch (type) {
            case "Google":
                google();
                break;
            case "FoodPanda":
                foodPanda();
                break;
            case "UberEat":
                uberEat();
                break;
            default:
        }
    }

    private void google() {

    }

    private void foodPanda() {
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        String url = "https://www.foodpanda.com.tw/restaurants/new?lat=" + latitude + "&lng=" + longitude + "&vertical=restaurants";
        Runnable runnable = () -> {
            try {
                Connection conn = Jsoup.connect(url);
                conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                final Document docs = conn.get();
                Elements scripts = docs.getElementsByTag("script");
                runOnUiThread(() -> {
                    for (Element script : scripts) {
                        if (script.toString().startsWith("<script>window.__PRELOADED_STATE__")) {
                            try {
                                JSONObject vendorsJson = new JSONObject(parsingFoodPanda(script.toString()));
                                JSONArray vendors = vendorsJson.getJSONArray("vendors");
                                for (int i = 0; i < vendors.length(); i++) {
//                                    nameList.add(vendors.getJSONObject(i).get("name").toString());
                                    Log.d(TAG, "Name = " + vendors.getJSONObject(i).get("name"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        };
        new Thread(runnable).start();
    }

    private String parsingFoodPanda(String s) {
        s = s.substring(s.indexOf("\"organicList\":{") + "\"organicList\":{".length() - 1);
        return s.substring(0, s.indexOf(",\"search\":{"));
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    private void uberEat() {
//        mWebView = new WebView(this);
//        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.addJavascriptInterface(new HtmlHandler(), "HtmlHandler");
//        mWebView.setWebViewClient(new WebViewClient(){
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                if (url == urlToLoad) {
//                    mWebView.loadUrl("javascript:HtmlHandler.handleHtml(document.documentElement.outerHTML);");
//                }
//            }
//        });
    }
//    class HtmlHandler {
//        @JavascriptInterface
//        @SuppressWarnings("unused")
//        public void handleHtml(String html) {
//            // scrape the content here
//
//        }
//    }
}