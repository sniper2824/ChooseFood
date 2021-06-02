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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Step 1 : 檢查用戶的網路連線是否正常 */
        boolean netAvailable = verifyNetAvailable(this);
        if (!netAvailable) {
            Toast.makeText(this, "Please Checking your Net is working", Toast.LENGTH_LONG).show();
        }

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

        walking.setOnClickListener(this);
        foodPanda.setOnClickListener(this);
        uberEat.setOnClickListener(this);
    }
}