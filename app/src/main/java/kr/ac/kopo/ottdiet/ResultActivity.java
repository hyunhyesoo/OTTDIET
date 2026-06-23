package kr.ac.kopo.ottdiet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    // Helper class to sort and display results in order of worst cost-effectiveness
    private static class AnalysisItem implements Comparable<AnalysisItem> {
        String name;
        int fee;
        int days;
        int monthlyDays;
        double metric;
        String htmlComment;

        @Override
        public int compareTo(AnalysisItem o) {
            // Sort in descending order (highest metric i.e. worst cost-effectiveness first)
            return Double.compare(o.metric, this.metric);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 1. Retrieve passed data
        Intent intent = getIntent();
        ArrayList<String> selectedNames = intent.getStringArrayListExtra("selectedNames");
        ArrayList<Integer> selectedPrices = intent.getIntegerArrayListExtra("selectedPrices");
        ArrayList<Integer> selectedDays = intent.getIntegerArrayListExtra("selectedDays");

        if (selectedNames == null || selectedPrices == null || selectedDays == null) {
            Toast.makeText(this, "분석 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTotalPrice = findViewById(R.id.tv_total_price);
        TextView tvPriceComment = findViewById(R.id.tv_price_comment);
        TextView tvAnalysisResult = findViewById(R.id.tv_analysis_result);
        Button btnUnsubscribe = findViewById(R.id.btn_unsubscribe);
        Button btnRetry = findViewById(R.id.btn_retry);

        DecimalFormat df = new DecimalFormat("#,###");

        // 2. Calculate total fee and set header comments
        int totalFee = 0;
        for (int price : selectedPrices) {
            totalFee += price;
        }

        tvTotalPrice.setText("- " + df.format(totalFee) + "원");

        if (totalFee == 0) {
            tvPriceComment.setText("(구독 중인 서비스가 없습니다)");
        } else if (totalFee < 10000) {
            tvPriceComment.setText("(커피 두 잔 값이 사라졌어요! ☕)");
        } else if (totalFee < 25000) {
            tvPriceComment.setText("(햄버거 세트 여러 개가 날아갔어요! 🍔)");
        } else if (totalFee < 40000) {
            tvPriceComment.setText("(치킨 한 마리가 사라졌어요! 🍗)");
        } else if (totalFee < 70000) {
            tvPriceComment.setText("(족발 세트 하나가 사라졌어요! 🐷)");
        } else {
            tvPriceComment.setText("(내 텅장이 완전히 털렸어요! 💸)");
        }

        // 3. Process each service and save to list for sorting (Top 10 logic: no emojis, bold names, colored amounts)
        List<AnalysisItem> analysisList = new ArrayList<>();

        for (int i = 0; i < selectedNames.size(); i++) {
            String name = selectedNames.get(i);
            int fee = selectedPrices.get(i);
            int days = selectedDays.get(i);

            int monthlyDays = days * 4;

            double metric;
            StringBuilder itemHtml = new StringBuilder();

            if (days == 0) {
                metric = 100000000.0 + fee; // Infinite cost, ordered by fee
                
                itemHtml.append("주 0회(한 달에 0번) 접속하시네요<br>")
                        .append("<b>").append(name).append("</b> 앱 한 번 켤 때마다<br>")
                        .append("<b><font color='#E53935'>기부 중</font></b><br>")
                        .append("기부천사 납셨네요<br>")
                        .append("<b><font color='#E53935'>지금 즉시 해지하세요!</font></b><br><br>");
            } else {
                int costPerDay = (int) Math.round((double) fee / monthlyDays);
                metric = (double) fee / monthlyDays;

                itemHtml.append("주 ").append(days).append("회(한 달에 ").append(monthlyDays).append("번) 접속하시네요<br>")
                        .append("<b>").append(name).append("</b> 앱 한 번 켤 때마다<br>");

                // Dynamic color based on thresholds (>3000 Red, 1000~3000 Orange, <1000 Green)
                if (costPerDay > 3000) {
                    itemHtml.append("<b><font size='5' color='#E53935'>").append(df.format(costPerDay)).append("원</font></b>씩 내고 계십니다<br>")
                            .append("차라리 영화관/공연장을 가세요<br><b><font color='#E53935'>당장 해지 추천!</font></b><br><br>");
                } else if (costPerDay >= 1000) {
                    itemHtml.append("<b><font size='5' color='#DD6B20'>").append(df.format(costPerDay)).append("원</font></b>씩 내고 계십니다<br>")
                            .append("본전은 치고 있으나<br><b><font color='#DD6B20'>밥 먹을 때 무조건 켜두세요!</font></b><br><br>");
                } else {
                    itemHtml.append("<b><font size='5' color='#388E3C'>").append(df.format(costPerDay)).append("원</font></b>씩 내고 계십니다<br>")
                            .append("<b><font color='#388E3C'>아주 훌륭한 소비입니다!</font></b><br><br>");
                }
            }

            AnalysisItem item = new AnalysisItem();
            item.name = name;
            item.fee = fee;
            item.days = days;
            item.monthlyDays = monthlyDays;
            item.metric = metric;
            item.htmlComment = itemHtml.toString();

            analysisList.add(item);
        }

        // Sort results: worst cost-effectiveness first
        Collections.sort(analysisList);

        // Build sorted HTML analysis output
        StringBuilder htmlBuilder = new StringBuilder();
        String worstService = null;

        if (!analysisList.isEmpty()) {
            worstService = analysisList.get(0).name; // The first item after sorting is the worst
            for (AnalysisItem item : analysisList) {
                htmlBuilder.append(item.htmlComment);
            }
        } else {
            htmlBuilder.append("분석 결과가 여기에 나타납니다.");
        }

        // Set HTML text based on Android SDK version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvAnalysisResult.setText(Html.fromHtml(htmlBuilder.toString(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvAnalysisResult.setText(Html.fromHtml(htmlBuilder.toString()));
        }

        // 4. Configure Unsubscribe Button dynamically
        if (worstService != null) {
            btnUnsubscribe.setText("[" + worstService + "] 해지하러 가기");
            final String targetService = worstService;
            btnUnsubscribe.setOnClickListener(v -> {
                String url = getUnsubscribeUrl(targetService);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            });
        } else {
            btnUnsubscribe.setText("해지할 서비스 없음");
            btnUnsubscribe.setEnabled(false);
        }

        // 5. Configure 'Analyze Again' button to restart MainActivity
        btnRetry.setOnClickListener(v -> {
            Intent mainIntent = new Intent(ResultActivity.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainIntent);
            finish();
        });
    }

    // Helper method to retrieve unsubscription links (Top 10 mapping)
    private String getUnsubscribeUrl(String serviceName) {
        if (serviceName == null) return "https://www.google.com";
        switch (serviceName) {
            case "넷플릭스": return "https://www.netflix.com";
            case "유튜브": return "https://www.youtube.com";
            case "디즈니+": return "https://www.disneyplus.com";
            case "티빙": return "https://www.tving.com";
            case "웨이브": return "https://www.wavve.com";
            case "왓챠": return "https://watcha.com";
            case "쿠팡플레이": return "https://www.coupangplay.com";
            case "멜론": return "https://www.melon.com";
            case "지니뮤직": return "https://www.genie.co.kr";
            case "스포티파이": return "https://www.spotify.com";
            default: return "https://www.google.com";
        }
    }
}
