package com.example.choosefood;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity {

    private static final String TAG = LocationPickerActivity.class.getName();

    private static final String ID_PIN = "pin";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermission;

    // 地圖
    private MapView mapView;

    // 預設經緯度
    private double latitude = 0.0;
    private double longitude = 0.0;

    // 地圖標記
    private Symbol symbol;
    private SymbolManager symbolManager;

    private static final int VIEW_CITY = 15;

    FusedLocationProviderClient fusedLocationProviderClient;
    PlacesClient placesClient;

    Button chooseLocation;
    Button confirm;
    Button cancel;
    EditText addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // need initial mapbox instance first
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_location_pick);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /* Step 1 : 取得使用者地區權限 */
        getLocationPermission();

        /* Step 2 : 產生地圖及按鈕 */
        if (locationPermission) {
            initMapView();
            confirmLocation();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 處理要求權限後的結果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermission = requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (locationPermission) {
            initMapView();
        } else {
            Toast.makeText(this, "Please Open Location Permission", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 向使用者要求地址權限
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * 拿取特定圖檔
     */
    private void addDropPinImage(Style style) {
        style.addImage(ID_PIN, BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_geolocate)), true);
    }

    /**
     * 產生地圖
     */
    private void initMapView() {
        // 取得使用者本身經緯度
        @SuppressLint("MissingPermission") Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Location lastKnownLocation = task.getResult();
                if (lastKnownLocation != null) {
                    latitude = lastKnownLocation.getLatitude();
                    longitude = lastKnownLocation.getLongitude();
                }
            }
        });

        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            // 調整鏡頭至街道高度
            mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(VIEW_CITY));
            // 調整鏡頭至使用者位置
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));

            addDropPinImage(style);

            symbolManager = new SymbolManager(mapView, mapboxMap, style);
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);

            // 建立標記
            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(latitude, longitude))
                    .withIconImage(ID_PIN)
                    .withIconSize(1.3f)
                    .withSymbolSortKey(10.0f)
                    .withDraggable(true);
            symbol = symbolManager.create(symbolOptions);

            // 當點擊地圖時移動標記
            mapboxMap.addOnMapClickListener(point -> {
                symbol.setLatLng(point);
                symbolManager.update(symbol);
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                return true;
            });
        }));
    }

    /**
     * 取得地址資料
     *
     * @return 地址
     */
    private String getAddressText() {
        try {
            List<Address> addresses = new Geocoder(getBaseContext(), Locale.TAIWAN)
                    .getFromLocation(latitude, longitude, 1);
            return addresses.isEmpty() ? "" : addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    /**
     * 確認按鈕
     */
    private void confirmLocation() {
        // 初始化所需要的按鈕及文字介面
        chooseLocation = findViewById(R.id.chooseButton);
        addressText = findViewById(R.id.addressText);
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel);

        // 隱藏確認及取消按鈕
        confirm.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);

        chooseLocation.setOnClickListener(v -> {
            String address = getAddressText();
            addressText.setText(address);

            chooseLocation.setVisibility(View.INVISIBLE);
            confirm.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);

            // TODO 取得使用者輸入的地址後將鏡頭導至該處
            confirm.setOnClickListener(v1 -> {
                Intent intent = new Intent(v1.getContext(), WebCrawlerActivity.class);
                intent.putExtra("type", getIntent().getStringExtra("type"));
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("address", address);
                startActivity(intent);
            });

            cancel.setOnClickListener(v1 -> {
                chooseLocation.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.INVISIBLE);
                cancel.setVisibility(View.INVISIBLE);
            });
        });
    }
}