package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.database.ReportManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class ReportDetailsActivity extends AppCompatActivity {

    // --- Khai báo View ---
    private TextView btnCloseReport;
    private TabLayout tabLayoutMonthsReport;
    private ImageButton btnCalendar;
    private TextView tvOpeningBalance, tvClosingBalance;
    private TextView tvNetIncome, tvTotalIncome, tvTotalExpense;
    private PieChart pieChartIncome, pieChartExpense;

    // View cho bộ chọn ví
    private LinearLayout walletButton;
    private ImageView ivWalletIcon;
    private TextView tvWalletName;

    // Biến lưu trạng thái
    private String selectedMonthFilter = "";

    // Trình khởi chạy để nhận kết quả từ ChooseWalletActivity
    private final ActivityResultLauncher<Intent> walletLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String selectedWalletKey = result.getData().getStringExtra("selected_wallet_key");
                    if (selectedWalletKey != null) {
                        updateWalletView(selectedWalletKey);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupTabs();
        setupListeners();

        // Mặc định tải dữ liệu cho ví "Tiền mặt" và tháng hiện tại
        updateWalletView("cash");
    }

    private void initializeViews() {
        btnCloseReport = findViewById(R.id.btn_close_report);
        tabLayoutMonthsReport = findViewById(R.id.tab_layout_months_report);
        btnCalendar = findViewById(R.id.btn_calendar);
        tvOpeningBalance = findViewById(R.id.tv_opening_balance);
        tvClosingBalance = findViewById(R.id.tv_closing_balance);
        tvNetIncome = findViewById(R.id.tv_net_income);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        pieChartIncome = findViewById(R.id.pie_chart_income_category);
        pieChartExpense = findViewById(R.id.pie_chart_expense_category);
        walletButton = findViewById(R.id.wallet_button);
        ivWalletIcon = findViewById(R.id.iv_wallet_icon);
        tvWalletName = findViewById(R.id.tv_wallet_name);
    }

    private void setupTabs() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        selectedMonthFilter = monthFormat.format(cal.getTime());
        TabLayout.Tab thisMonthTab = tabLayoutMonthsReport.newTab().setText("Tháng này");
        thisMonthTab.setTag(selectedMonthFilter);
        tabLayoutMonthsReport.addTab(thisMonthTab, true);

        cal.add(Calendar.MONTH, -1);
        String lastMonthFilter = monthFormat.format(cal.getTime());
        TabLayout.Tab lastMonthTab = tabLayoutMonthsReport.newTab().setText("Tháng trước");
        lastMonthTab.setTag(lastMonthFilter);
        tabLayoutMonthsReport.addTab(lastMonthTab, 0);
    }

    private void setupListeners() {
        btnCloseReport.setOnClickListener(v -> finish());
        btnCalendar.setOnClickListener(v -> Toast.makeText(this, "Mở Lịch", Toast.LENGTH_SHORT).show());
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReportDetailsActivity.this, ChooseWalletActivity.class);
            walletLauncher.launch(intent);
        });

        tabLayoutMonthsReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getTag() != null) {
                    selectedMonthFilter = (String) tab.getTag();
                    loadReportDataForMonth(selectedMonthFilter);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateWalletView(String walletKey) {
        String walletName;
        int walletIconResId;
        switch (walletKey) {
            case "bank":
                walletName = "Ngân hàng";
                walletIconResId = R.drawable.ic_wallet_bank;
                break;
            case "cash":
            default:
                walletName = "Tiền mặt";
                walletIconResId = R.drawable.ic_wallet_cash;
                break;
        }
        tvWalletName.setText(walletName);
        ivWalletIcon.setImageResource(walletIconResId);
        loadReportDataForMonth(selectedMonthFilter);
    }

    private void loadReportDataForMonth(String month) {
        Map<String, Double> summary = ReportManager.getMonthlySummary(this, month);
        double totalIncome = summary.getOrDefault("totalIncome", 0.0);
        double totalExpense = summary.getOrDefault("totalExpense", 0.0);
        double netIncome = totalIncome - totalExpense;
        double openingBalance = ReportManager.getOpeningBalance(this, month);
        double closingBalance = openingBalance + netIncome;

        DecimalFormat formatter = new DecimalFormat("#,### đ");
        tvOpeningBalance.setText(formatter.format(openingBalance));
        tvClosingBalance.setText(formatter.format(closingBalance));
        tvTotalIncome.setText(formatter.format(totalIncome));
        tvTotalExpense.setText(formatter.format(totalExpense));
        tvNetIncome.setText("= " + formatter.format(netIncome));

        try (Cursor incomeCursor = ReportManager.getSummaryByCategory(this, month, "Income")) {
            setupCategoryPieChart(pieChartIncome, incomeCursor, "Thu nhập", totalIncome);
        }
        try (Cursor expenseCursor = ReportManager.getSummaryByCategory(this, month, "Expense")) {
            setupCategoryPieChart(pieChartExpense, expenseCursor, "Chi tiêu", totalExpense);
        }
    }

    private void setupCategoryPieChart(PieChart chart, Cursor cursor, String centerText, double totalValue) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));
                float totalAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("total_amount"));
                String colorCode = cursor.getString(cursor.getColumnIndexOrThrow("color_code"));

                entries.add(new PieEntry(totalAmount, categoryName));
                try {
                    colors.add(Color.parseColor(colorCode));
                } catch (Exception e) {
                    colors.add(Color.LTGRAY);
                }
            } while (cursor.moveToNext());
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Không có dữ liệu");
            chart.invalidate();
            return;
        }

        chart.setUsePercentValues(false); // Quan trọng: Không dùng % mặc định nữa
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setCenterText(centerText);
        chart.setCenterTextSize(14f);
        chart.setDrawEntryLabels(false); // Không vẽ tên danh mục trên lát cắt

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        // CẬP NHẬT: Ẩn giá trị phần trăm mặc định trên biểu đồ
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.invalidate(); // Refresh biểu đồ

        // CẬP NHẬT: Thêm listener để hiện thị thông tin khi chạm vào
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null) return;
                PieEntry pieEntry = (PieEntry) e;
                float percent = (pieEntry.getValue() / (float) totalValue) * 100;
                String selectedText = String.format(Locale.getDefault(), "%s\n%.1f%%", pieEntry.getLabel(), percent);
                chart.setCenterText(selectedText);
            }

            @Override
            public void onNothingSelected() {
                // Trả lại text ban đầu khi không chọn gì
                chart.setCenterText(centerText);
            }
        });
    }
}