package com.example.choosefood;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class SlotMachineActivity extends AppCompatActivity {
    TextSwitcher slot;
    private Handler handler;
    private int slotIndexCurrent = 0;
    private int slotCountCurrent = 0;
    private Runnable runnable;
    private Button start;
    private ArrayList<String> food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_machine);

        initView();
    }

    private void initView() {
        handler = new Handler(getMainLooper());
        slot = findViewById(R.id.slot_text);
        slot.setFactory(() -> {
            TextView t = new TextView(SlotMachineActivity.this);
            t.setGravity(Gravity.CENTER);
            t.setTextSize(30);
            return t;
        });
        slot.setCurrentText("Tap start to scroll");

        start = findViewById(R.id.start_button);

        food = getIntent().getStringArrayListExtra("Food");
        start.setOnClickListener(v -> startSlots());
    }

    /**
     * 開始拉霸
     */
    private void startSlots() {
        start.setVisibility(View.INVISIBLE);
        handler.removeCallbacks(runnable);

        runnable = () -> switchSlot(food, new Random().nextInt(food.size()));
        slotCountCurrent = Math.max(Math.min(food.size(), 30), 15);
        slotIndexCurrent = 0;
        handler.postDelayed(runnable, 100);
    }

    /**
     * 拉霸轉動
     */
    private void switchSlot(ArrayList<String> info, int index) {
        // 預設文字
        slot.setText(info.get(slotIndexCurrent));
        slotIndexCurrent = (slotIndexCurrent + 1) % info.size();
        slotCountCurrent--;
        if (slotCountCurrent > 0) {
            handler.postDelayed(runnable, 100);
        } else {
            showResult(info, index);
        }
    }

    /**
     * 顯示結果
     */
    @SuppressLint("SetTextI18n")
    private void showResult(ArrayList<String> info, int index) {
        slot.setText(info.get(index));
        start.setText("ReStart");
        start.setVisibility(View.VISIBLE);

        start.setOnClickListener(v -> {
            info.remove(index);
            startSlots();
        });
    }
}