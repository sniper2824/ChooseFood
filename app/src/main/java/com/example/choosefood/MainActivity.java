package com.example.choosefood;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private TextView webText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Step 1 : 檢查用戶的網路連線是否正常 */
        boolean netAvailable = verifyNetAvailable(this);
        if (!netAvailable) {
            Toast.makeText(this, "Please Checking your Net is working", Toast.LENGTH_LONG).show();
        }

        webText = findViewById(R.id.webText);

        /* Step 2 : 初始化三個按鈕 */
        initView();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        String value = "";
        switch (v.getId()) {
            case R.id.walking:
                value = "Google";
                break;
            case R.id.foodpanda:
                value = "FoodPanda";
                break;
            case R.id.ubereat:
                value = "UberEat";
                break;
            default:
                Log.e(TAG, "Error");
        }
        intent.putExtra("type", value);
        startActivity(intent);
    }

    private void foodPanda() {
        Runnable runnable = () -> {
            try {
                Connection conn = Jsoup.connect("https://www.foodpanda.com.tw/restaurants/new?lat=24.749992&lng=121.0166887&vertical=restaurants");
                conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                final Document docs = conn.get();
                Elements scripts = docs.getElementsByTag("script");
                runOnUiThread(() -> {
                    for (Element script : scripts) {
                        if (script.toString().startsWith("<script>window.__PRELOADED_STATE__")) {
                            // 太長導致JSONObject轉換失敗
                            String vendorsText = new String(script.toString()
                                    .substring(0, script.toString().indexOf("window.__PROVIDER_PROPS__") - 1)
                                    .replace("<script>window.__PRELOADED_STATE__=", ""));
                            Log.d(TAG, vendorsText);
                            try {
                                JSONObject vendors = new JSONObject(vendorsText);
                                Log.d(TAG, vendors.toString());
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

    /**
     * 檢查網路連線
     *
     * @return boolean
     */
    private boolean verifyNetAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        }
        return false;
    }

    /**
     * 初始化按鈕及監聽事件
     */
    private void initView() {
        Button walking = findViewById(R.id.walking);
        Button foodPanda = findViewById(R.id.foodpanda);
        Button uberEat = findViewById(R.id.ubereat);
        Button test = findViewById(R.id.test);

        walking.setOnClickListener(this);
        foodPanda.setOnClickListener(this);
        uberEat.setOnClickListener(this);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodPanda();
            }
        });
    }
}