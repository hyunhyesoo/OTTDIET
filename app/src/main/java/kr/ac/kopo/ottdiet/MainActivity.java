package kr.ac.kopo.ottdiet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Helper class to group OTT service data
    private static class ServiceItem {
        String name;
        int cbResId;
        int etResId;
        String prefKey;
        String emoji;

        ServiceItem(String name, int cbResId, int etResId, String prefKey, String emoji) {
            this.name = name;
            this.cbResId = cbResId;
            this.etResId = etResId;
            this.prefKey = prefKey;
            this.emoji = emoji;
        }
    }

    private ServiceItem[] services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Predefine the 10 OTT/Music services (replaced YouTube Music with Spotify)
        services = new ServiceItem[]{
            new ServiceItem("넷플릭스", R.id.cb_netflix, R.id.et_price_netflix, "price_netflix", "🍿"),
            new ServiceItem("유튜브", R.id.cb_youtube, R.id.et_price_youtube, "price_youtube", "📺"),
            new ServiceItem("왓챠", R.id.cb_watcha, R.id.et_price_watcha, "price_watcha", "🎞️"),
            new ServiceItem("디즈니+", R.id.cb_disney, R.id.et_price_disney, "price_disney", "🏰"),
            new ServiceItem("티빙", R.id.cb_tving, R.id.et_price_tving, "price_tving", "🎬"),
            new ServiceItem("쿠팡플레이", R.id.cb_coupang, R.id.et_price_coupang, "price_coupang", "🚀"),
            new ServiceItem("웨이브", R.id.cb_wavve, R.id.et_price_wavve, "price_wavve", "🌊"),
            new ServiceItem("멜론", R.id.cb_melon, R.id.et_price_melon, "price_melon", "🍈"),
            new ServiceItem("스포티파이", R.id.cb_spotify, R.id.et_price_spotify, "price_spotify", "🎧"),
            new ServiceItem("지니뮤직", R.id.cb_genie, R.id.et_price_genie, "price_genie", "🧞")
        };

        SharedPreferences prefs = getSharedPreferences("OTT_PREFS", MODE_PRIVATE);

        // Populate saved data and register listeners
        for (ServiceItem item : services) {
            CheckBox cb = findViewById(item.cbResId);
            EditText et = findViewById(item.etResId);

            int savedPrice = prefs.getInt(item.prefKey, -1);
            if (savedPrice != -1 && savedPrice > 0) {
                cb.setChecked(true);
                et.setVisibility(View.VISIBLE);
                et.setText(String.valueOf(savedPrice));
            } else {
                cb.setChecked(false);
                et.setVisibility(View.INVISIBLE); // Keep space reserved to maintain height
                et.setText("");
            }

            // Toggle visibility on checkbox change
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    et.setVisibility(View.VISIBLE);
                    et.requestFocus();
                } else {
                    et.setVisibility(View.INVISIBLE); // Keep space reserved to maintain height
                    et.setText("");
                }
            });
        }

        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            ArrayList<String> selectedNames = new ArrayList<>();
            ArrayList<Integer> selectedPrices = new ArrayList<>();
            ArrayList<String> selectedEmojis = new ArrayList<>();

            // 1. Validation: check if prices are entered for checked services
            boolean hasSelection = false;
            for (ServiceItem item : services) {
                CheckBox cb = findViewById(item.cbResId);
                EditText et = findViewById(item.etResId);

                if (cb.isChecked()) {
                    hasSelection = true;
                    String priceStr = et.getText().toString().trim();
                    if (priceStr.isEmpty()) {
                        Toast.makeText(MainActivity.this, item.name + "의 구독료를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        et.requestFocus();
                        return;
                    }
                }
            }

            if (!hasSelection) {
                Toast.makeText(MainActivity.this, "구독 중인 서비스를 하나 이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Save prices and collect data
            SharedPreferences.Editor editor = prefs.edit();
            for (ServiceItem item : services) {
                CheckBox cb = findViewById(item.cbResId);
                EditText et = findViewById(item.etResId);

                if (cb.isChecked()) {
                    int price = Integer.parseInt(et.getText().toString().trim());
                    editor.putInt(item.prefKey, price);

                    selectedNames.add(item.name);
                    selectedPrices.add(price);
                    selectedEmojis.add(item.emoji);
                } else {
                    editor.remove(item.prefKey);
                }
            }
            editor.apply();

            // 3. Navigate to UsageActivity
            Intent intent = new Intent(MainActivity.this, UsageActivity.class);
            intent.putStringArrayListExtra("selectedNames", selectedNames);
            intent.putIntegerArrayListExtra("selectedPrices", selectedPrices);
            intent.putStringArrayListExtra("selectedEmojis", selectedEmojis);
            startActivity(intent);
        });
    }
}