package kr.ac.kopo.ottdiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class UsageActivity extends AppCompatActivity {

    private ArrayList<String> selectedNames;
    private ArrayList<Integer> selectedPrices;
    private ArrayList<String> selectedEmojis;
    private List<SeekBar> seekBars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        // 1. Retrieve passed data
        Intent intent = getIntent();
        selectedNames = intent.getStringArrayListExtra("selectedNames");
        selectedPrices = intent.getIntegerArrayListExtra("selectedPrices");
        selectedEmojis = intent.getStringArrayListExtra("selectedEmojis");

        if (selectedNames == null || selectedPrices == null || selectedEmojis == null) {
            Toast.makeText(this, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LinearLayout usageContainer = findViewById(R.id.usage_container);
        seekBars = new ArrayList<>();

        // 2. Dynamically inflate layout for each selected service
        for (int i = 0; i < selectedNames.size(); i++) {
            String name = selectedNames.get(i);
            int price = selectedPrices.get(i);
            String emoji = selectedEmojis.get(i);

            View itemView = getLayoutInflater().inflate(R.layout.item_usage_ott, usageContainer, false);

            TextView tvName = itemView.findViewById(R.id.tv_item_name);
            TextView tvDays = itemView.findViewById(R.id.tv_item_days);
            SeekBar sbUsage = itemView.findViewById(R.id.sb_item_usage);

            // Display format: Emoji + Name (e.g. 🍿 넷플릭스)
            tvName.setText(emoji + " " + name);
            tvDays.setText("0일");

            sbUsage.setMax(7);
            sbUsage.setProgress(0);

            // Update text dynamically as user drags the slider
            sbUsage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvDays.setText(progress + "일");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            seekBars.add(sbUsage);
            usageContainer.addView(itemView);
        }

        // 3. Handle 'Analyze My Wallet' Button click
        Button btnAnalyze = findViewById(R.id.btn_analyze);
        btnAnalyze.setOnClickListener(v -> {
            ArrayList<Integer> usageDays = new ArrayList<>();
            for (SeekBar sb : seekBars) {
                usageDays.add(sb.getProgress());
            }

            // Transfer data to ResultActivity
            Intent resultIntent = new Intent(UsageActivity.this, ResultActivity.class);
            resultIntent.putStringArrayListExtra("selectedNames", selectedNames);
            resultIntent.putIntegerArrayListExtra("selectedPrices", selectedPrices);
            resultIntent.putIntegerArrayListExtra("selectedDays", usageDays);
            startActivity(resultIntent);
        });
    }
}
