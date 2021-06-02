package com.example.choosefood;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.choosefood.model.FoodPandaInfo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebCrawlerActivity extends AppCompatActivity {

    private static final String TAG = WebCrawlerActivity.class.getName();

    TextView webText;

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
                Elements vendors = docs.getElementsByTag("script");
                runOnUiThread(() -> {
                    Log.d(TAG, ">>>>>>> " + vendors.toString());
                    webText.setText(vendors.toString());
                });
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        };
        new Thread(runnable).start();
    }

    private List<FoodPandaInfo> analyzeData(Elements elements) {
        List<FoodPandaInfo> vendors = new ArrayList<>();
        for (Element domElement : elements) {
            FoodPandaInfo foodPandaInfo = new FoodPandaInfo();
//            foodPandaInfo.setUrl();
//            foodPandaInfo.setTitle();
            vendors.add(foodPandaInfo);
        }
        return vendors;
    }

    private void uberEat() {

    }
}