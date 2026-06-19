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

public class ResultActivity extends AppCompatActivity {

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

        // 3. Analyze each service and build HTML text
        StringBuilder htmlBuilder = new StringBuilder();
        double worstMetric = -1;
        String worstService = null;

        for (int i = 0; i < selectedNames.size(); i++) {
            String name = selectedNames.get(i);
            int fee = selectedPrices.get(i);
            int days = selectedDays.get(i);

            int monthlyDays = days * 4;

            // Cost effectiveness metric: 0-day usage is treated as worst (scaled by price to cancel highest fee first)
            double metric;
            if (days == 0) {
                metric = 100000000.0 + fee; // Infinite cost, ordered by fee
                
                htmlBuilder.append("주 0회(한 달에 0번) 접속하시네요<br>")
                        .append("<b><font color='#E53935'>").append(name).append("</font></b> 앱 한 번 켤 때마다<br>")
                        .append("<font color='#E53935'><b>[기부 중]</b></font><br>")
                        .append("기부천사 납셨네요 👼<br>")
                        .append("<font color='#E53935'><b>지금 즉시 해지하세요!</b></font><br><br>");
            } else {
                int costPerDay = (int) Math.round((double) fee / monthlyDays);
                metric = (double) fee / monthlyDays;

                htmlBuilder.append("주 ").append(days).append("회(한 달에 ").append(monthlyDays).append("번) 접속하시네요<br>")
                        .append("<b><font color='#1A202C'>").append(name).append("</font></b> 앱 한 번 켤 때마다<br>")
                        .append("<b><font size='5' color='#2B6CB0'>").append(df.format(costPerDay)).append("원</font></b>씩 내고 계십니다<br>");

                if (days <= 2) {
                    htmlBuilder.append("차라리 영화관/공연장을 가세요 🎬<br><font color='#E53935'><b>당장 해지 추천!</b></font><br><br>");
                } else if (days <= 5) {
                    htmlBuilder.append("본전은 치고 있으나 🤔<br>밥 먹을 때 무조건 켜두세요!<br><br>");
                } else {
                    htmlBuilder.append("<font color='#388E3C'><b>아주 훌륭한 소비입니다! 👍</b></font><br><br>");
                }
            }

            if (metric > worstMetric) {
                worstMetric = metric;
                worstService = name;
            }
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

    // Helper method to retrieve unsubscription links
    private String getUnsubscribeUrl(String serviceName) {
        if (serviceName == null) return "https://www.google.com";
        switch (serviceName) {
            case "넷플릭스": return "https://www.netflix.com/CancelPlan";
            case "유튜브": return "https://www.youtube.com/paid_memberships";
            case "디즈니+": return "https://www.disneyplus.com/";
            case "티빙": return "https://www.tving.com/";
            case "웨이브": return "https://www.wavve.com/";
            case "왓챠": return "https://watcha.com/";
            case "쿠팡플레이": return "https://www.coupang.com";
            case "애플 TV+": return "https://support.apple.com/billing";
            case "멜론": return "https://www.melon.com";
            case "유튜브 뮤직": return "https://www.youtube.com/paid_memberships";
            case "지니뮤직": return "https://www.genie.co.kr";
            case "스포티파이": return "https://www.spotify.com";
            default: return "https://www.google.com";
        }
    }
}
