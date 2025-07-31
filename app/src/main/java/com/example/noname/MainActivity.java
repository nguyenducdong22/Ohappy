package com.example.noname;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.noname.Budget.BudgetOverviewActivity;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.ReportManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Views
    private TextView tvHeaderMainText;
    private PieChart pieChartReport;
    private TextView tvTotalSpent, tvTotalIncome;
    private TextView tvSeeReportDetails;
    private TextView tvEmptyReportPrompt;
    private LinearLayout recentTransactionsContainer;
    private TextView tvEmptyTransactions;
    private TextView tvSeeAllTransactions;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction, fabChatbot;
    // Biến để lưu trạng thái trang báo cáo hiện tại (0 = PieChart, 1 = Tab View)
    private int currentReportPage = 0;
    // Các View trong Report Card cần để điều khiển
    private LinearLayout reportSummaryView; // View chứa PieChart
    private LinearLayout reportTabView;     // View chứa TabLayout Tuần/Tháng
    private TextView tvReportTrendTitle;
    private ImageButton btnReportPrev, btnReportNext;
    private View reportDot1, reportDot2;
    private TabLayout tabLayoutWeekMonthReport; // TabLayout để xử lý logic bên trong
    private LinearLayout topExpensesContainer;
    private TextView tvEmptyTopExpenses;
    private BarChart barChartReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupListeners();
        updateReportView(); // <<< GỌI Ở ĐÂY để set trạng thái ban đầu
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Luôn tải lại dữ liệu khi quay lại màn hình chính
        loadDashboardData();
    }

    private void initializeViews() {
        // Header
        tvHeaderMainText = findViewById(R.id.tv_header_main_text);

        // Report Card
        pieChartReport = findViewById(R.id.pie_chart_report);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvSeeReportDetails = findViewById(R.id.tv_see_report_details);
        tvEmptyReportPrompt = findViewById(R.id.tv_empty_report_prompt);

        // Recent Transactions Card
        recentTransactionsContainer = findViewById(R.id.recent_transactions_container);
        tvEmptyTransactions = findViewById(R.id.tv_empty_transactions);
        tvSeeAllTransactions = findViewById(R.id.tv_see_all_transactions);

        // Bottom Navigation & FABs
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabChatbot = findViewById(R.id.fab_chatbot);

        reportSummaryView = findViewById(R.id.report_summary_view);
        reportTabView = findViewById(R.id.report_tab_view);

        tvReportTrendTitle = findViewById(R.id.tv_report_trend_title);
        btnReportPrev = findViewById(R.id.btn_report_prev);
        btnReportNext = findViewById(R.id.btn_report_next);
        reportDot1 = findViewById(R.id.report_dot1);
        reportDot2 = findViewById(R.id.report_dot2);
        tabLayoutWeekMonthReport = findViewById(R.id.tab_layout_week_month_report);

        topExpensesContainer = findViewById(R.id.top_expenses_container);
        tvEmptyTopExpenses = findViewById(R.id.tv_empty_top_expenses);
        barChartReport = findViewById(R.id.bar_chart_report);
    }

    private void updateReportView() {
        if (currentReportPage == 0) {
            // --- Hiển thị chế độ xem Pie Chart ---
            reportSummaryView.setVisibility(View.VISIBLE);
            pieChartReport.setVisibility(View.VISIBLE);
            tvEmptyReportPrompt.setVisibility(pieChartReport.isEmpty() ? View.VISIBLE : View.GONE);

            reportTabView.setVisibility(View.GONE);

            // Cập nhật tiêu đề và dấu chấm chỉ thị
            tvReportTrendTitle.setText("Báo cáo xu hướng");
            reportDot1.setBackgroundResource(R.drawable.dot_active);
            reportDot2.setBackgroundResource(R.drawable.dot_inactive);

        } else { // currentReportPage == 1
            // --- Hiển thị chế độ xem Tab (Tuần/Tháng) ---
            reportSummaryView.setVisibility(View.GONE);
            pieChartReport.setVisibility(View.GONE);
            tvEmptyReportPrompt.setVisibility(View.GONE);

            reportTabView.setVisibility(View.VISIBLE);

            // Cập nhật tiêu đề và dấu chấm chỉ thị
            tvReportTrendTitle.setText("Báo cáo chi tiêu");
            reportDot1.setBackgroundResource(R.drawable.dot_inactive);
            reportDot2.setBackgroundResource(R.drawable.dot_active);

            // ▼▼▼ PHẦN ĐƯỢC CẬP NHẬT ▼▼▼
            // Tự động tải dữ liệu cho tab đang được chọn (mặc định là "Tuần")
            // ngay khi người dùng chuyển sang chế độ xem này.
            if (tabLayoutWeekMonthReport.getSelectedTabPosition() == 0) {
                setupAndLoadWeeklyBarChart();
            } else {
                setupAndLoadMonthlyBarChart();
            }
        }
    }


    private void setupListeners() {
        tvSeeReportDetails.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportDetailsActivity.class);
            startActivity(intent);
        });

        tvSeeAllTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_transactions) {
                startActivity(new Intent(MainActivity.this, TransactionHistoryActivity.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                startActivity(new Intent(MainActivity.this, BudgetOverviewActivity.class));
                return true;
            } else if (itemId == R.id.navigation_account) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                return true;
            }
            return itemId == R.id.navigation_overview;
        });

        fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Addtransaction.class));
        });

        fabChatbot.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatbotActivity.class));
        });

        // === THÊM CÁC LISTENER MỚI DƯỚI ĐÂY ===
        btnReportPrev.setOnClickListener(v -> {
            currentReportPage = 0; // Luôn quay về trang 0
            updateReportView();
        });

        btnReportNext.setOnClickListener(v -> {
            currentReportPage = 1; // Luôn đi tới trang 1
            updateReportView();
        });

        // (Tùy chọn) Thêm listener cho TabLayout nếu bạn cần xử lý logic Tuần/Tháng
        tabLayoutWeekMonthReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Logic cho tab "Tuần"
                    setupAndLoadWeeklyBarChart(); // <<< GỌI PHƯƠNG THỨC MỚI
                } else {
                    // Logic cho tab "Tháng"
                    setupAndLoadMonthlyBarChart();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void loadDashboardData() {
        // 1. Tải tổng số dư
        double totalBalance = ReportManager.getTotalBalance(this);
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        tvHeaderMainText.setText(formatter.format(totalBalance));

        // 2. Tải báo cáo tháng này
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String currentMonth = monthFormat.format(Calendar.getInstance().getTime());
        Map<String, Double> summary = ReportManager.getMonthlySummary(this, currentMonth);

        double totalIncome = summary.getOrDefault("totalIncome", 0.0);
        double totalExpense = summary.getOrDefault("totalExpense", 0.0);

        tvTotalIncome.setText(formatter.format(totalIncome));
        tvTotalSpent.setText(formatter.format(totalExpense));

        if (totalIncome > 0 || totalExpense > 0) {
            pieChartReport.setVisibility(View.VISIBLE);
            tvEmptyReportPrompt.setVisibility(View.GONE);
            setupPieChart(totalIncome, totalExpense);
        } else {
            pieChartReport.setVisibility(View.GONE);
            tvEmptyReportPrompt.setVisibility(View.VISIBLE);
        }

        // 3. Tải và hiển thị các giao dịch gần đây
        loadRecentTransactions();

        // 4. GỌI PHƯƠNG THỨC MỚI ĐỂ TẢI TOP CHI TIÊU
        loadTopExpenses(totalExpense, currentMonth);
    }

    private void setupPieChart(double totalIncome, double totalExpense) {
        pieChartReport.setUsePercentValues(true);
        pieChartReport.getDescription().setEnabled(false);
        pieChartReport.setExtraOffsets(5, 10, 5, 5);
        pieChartReport.setDragDecelerationFrictionCoef(0.95f);
        pieChartReport.setDrawHoleEnabled(true);
        pieChartReport.setHoleColor(Color.WHITE);
        pieChartReport.setTransparentCircleRadius(61f);
        pieChartReport.getLegend().setEnabled(false);

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (totalExpense > 0) entries.add(new PieEntry((float) totalExpense, "Chi tiêu"));
        if (totalIncome > 0) entries.add(new PieEntry((float) totalIncome, "Thu nhập"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        if (totalExpense > 0) colors.add(Color.parseColor("#EF5350")); // Màu đỏ
        if (totalIncome > 0) colors.add(Color.parseColor("#66BB6A")); // Màu xanh
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartReport));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);

        pieChartReport.setData(data);
        pieChartReport.animateY(1000);
        pieChartReport.invalidate();
    }

    private void loadRecentTransactions() {
        // Xóa các giao dịch cũ trước khi thêm mới
        // Bắt đầu từ index 1 để giữ lại LinearLayout chứa title "Giao dịch gần đây"
        if (recentTransactionsContainer.getChildCount() > 1) {
            recentTransactionsContainer.removeViews(1, recentTransactionsContainer.getChildCount() - 1);
        }

        try (Cursor cursor = ReportManager.getRecentTransactions(this, 3)) {
            if (cursor != null && cursor.getCount() > 0) {
                tvEmptyTransactions.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(this);
                while (cursor.moveToNext()) {
                    View itemView = inflater.inflate(R.layout.list_item_transaction, recentTransactionsContainer, false);

                    // Lấy các view từ item layout
                    ImageView ivItemIcon = itemView.findViewById(R.id.iv_category_icon);
                    TextView tvItemCategory = itemView.findViewById(R.id.tv_category_name);
                    TextView tvItemDesc = itemView.findViewById(R.id.tv_transaction_description);
                    TextView tvItemAmount = itemView.findViewById(R.id.tv_transaction_amount);
                    TextView tvItemDate = itemView.findViewById(R.id.tv_transaction_date);

                    // Lấy dữ liệu từ Cursor
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                    String dateString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DATE));
                    String iconName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME));

                    // Điền dữ liệu vào view
                    tvItemCategory.setText(categoryName);
                    tvItemDesc.setText(description);

                    DecimalFormat amountFormatter = new DecimalFormat("#,### đ");
                    String formattedAmount = amountFormatter.format(amount);

                    if ("Expense".equalsIgnoreCase(type)) {
                        tvItemAmount.setText("-" + formattedAmount);
                        tvItemAmount.setTextColor(Color.parseColor("#EF5350"));
                    } else {
                        tvItemAmount.setText("+" + formattedAmount);
                        tvItemAmount.setTextColor(Color.parseColor("#66BB6A"));
                    }

                    try {
                        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date = dbFormat.parse(dateString);
                        tvItemDate.setText(displayFormat.format(date));
                    } catch (ParseException e) {
                        tvItemDate.setText(dateString.split(" ")[0]);
                    }

                    int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                    ivItemIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_help);

                    recentTransactionsContainer.addView(itemView);
                }
            } else {
                tvEmptyTransactions.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading recent transactions", e);
            tvEmptyTransactions.setVisibility(View.VISIBLE);
        } finally {
            // Đảm bảo database được đóng nếu ReportManager không tự đóng
            new DatabaseHelper(this).close();
        }
    }

    private void loadTopExpenses(double totalExpense, String currentMonth) {
        // Xóa các view cũ trước khi thêm mới
        topExpensesContainer.removeAllViews();

        if (totalExpense == 0) {
            tvEmptyTopExpenses.setVisibility(View.VISIBLE);
            topExpensesContainer.setVisibility(View.GONE);
            return;
        }

        tvEmptyTopExpenses.setVisibility(View.GONE);
        topExpensesContainer.setVisibility(View.VISIBLE);

        try (Cursor cursor = ReportManager.getTopExpensesForMonth(this, currentMonth)) {
            if (cursor != null && cursor.getCount() > 0) {
                LayoutInflater inflater = LayoutInflater.from(this);
                while (cursor.moveToNext()) {
                    View itemView = inflater.inflate(R.layout.list_item_top_expense, topExpensesContainer, false);

                    // Lấy view từ item layout
                    ImageView ivIcon = itemView.findViewById(R.id.iv_top_expense_icon);
                    TextView tvCategory = itemView.findViewById(R.id.tv_top_expense_category);
                    TextView tvPercent = itemView.findViewById(R.id.tv_top_expense_percent);
                    ProgressBar progressBar = itemView.findViewById(R.id.pb_top_expense_progress);

                    // Lấy dữ liệu từ Cursor
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                    String iconName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME));
                    double categoryAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));

                    // Tính toán phần trăm
                    int percentage = (int) ((categoryAmount / totalExpense) * 100);

                    // Điền dữ liệu vào view
                    tvCategory.setText(categoryName);
                    tvPercent.setText(percentage + "%");
                    progressBar.setProgress(percentage);

                    int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                    ivIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_help);

                    topExpensesContainer.addView(itemView);
                }
            } else {
                tvEmptyTopExpenses.setVisibility(View.VISIBLE);
                topExpensesContainer.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading top expenses", e);
            tvEmptyTopExpenses.setVisibility(View.VISIBLE);
            topExpensesContainer.setVisibility(View.GONE);
        }
    }

    private void setupAndLoadWeeklyBarChart() {
        // 1. Lấy tổng chi tuần này và tuần trước từ ReportManager
        double previousWeekTotal = ReportManager.getTotalForPreviousWeek(this);
        double currentWeekTotal = ReportManager.getTotalForCurrentWeek(this);

        // 2. Cập nhật các TextView (logic này giữ nguyên)
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        TextView tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        TextView tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);

        tvCurrentReportValue.setText(formatter.format(currentWeekTotal));

        if (previousWeekTotal > 0) {
            double change = ((currentWeekTotal - previousWeekTotal) / previousWeekTotal) * 100;
            String changeText = String.format(Locale.US, "%.1f%% so với tuần trước", change);
            tvTotalSpentPercentage.setText(changeText);
            tvTotalSpentPercentage.setTextColor(change >= 0 ? Color.RED : Color.parseColor("#66BB6A"));
        } else {
            tvTotalSpentPercentage.setText(currentWeekTotal > 0 ? "Bắt đầu chi tiêu tuần này" : "Chưa có dữ liệu");
        }

        // 3. Chuẩn bị dữ liệu cho biểu đồ (CHỈ 2 CỘT)
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) previousWeekTotal));
        entries.add(new BarEntry(1, (float) currentWeekTotal));

        BarDataSet dataSet = new BarDataSet(entries, "So sánh chi tiêu");

        // Đặt màu khác nhau cho 2 cột
        dataSet.setColors(Color.parseColor("#FFC107"), Color.parseColor("#4CAF50")); // Vàng & Xanh
        dataSet.setDrawValues(true); // Hiển thị giá trị trên cột
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new DecimalFormat("#,###").format(value);
            }
        });


        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f); // Làm cho cột nhỏ hơn
        barChartReport.setData(barData);

        // 4. Tùy chỉnh giao diện biểu đồ
        barChartReport.getDescription().setEnabled(false);
        barChartReport.getLegend().setEnabled(false);
        barChartReport.getAxisRight().setEnabled(false);
        barChartReport.getAxisLeft().setAxisMinimum(0f);
        barChartReport.getAxisLeft().setDrawAxisLine(false); // Ẩn đường kẻ trục Y
        barChartReport.setFitBars(true); // Căn cột vào giữa

        // Đặt nhãn cho trục X
        String[] labels = new String[]{"Tuần trước", "Tuần này"};
        barChartReport.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        barChartReport.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        barChartReport.getXAxis().setGranularity(1f);
        barChartReport.getXAxis().setDrawGridLines(false);
        barChartReport.getXAxis().setDrawAxisLine(false);

        // Làm mới biểu đồ
        barChartReport.animateY(1000);
        barChartReport.invalidate();
    }

    private void setupAndLoadMonthlyBarChart() {
        // 1. Lấy tổng chi tháng này và tháng trước
        double previousMonthTotal = ReportManager.getTotalForPreviousMonth(this);
        double currentMonthTotal = ReportManager.getTotalForCurrentMonth(this);

        // 2. Cập nhật các TextView
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        TextView tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        TextView tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);

        tvCurrentReportValue.setText(formatter.format(currentMonthTotal));

        if (previousMonthTotal > 0) {
            double change = ((currentMonthTotal - previousMonthTotal) / previousMonthTotal) * 100;
            String changeText = String.format(Locale.US, "%.1f%% so với tháng trước", change);
            tvTotalSpentPercentage.setText(changeText);
            tvTotalSpentPercentage.setTextColor(change >= 0 ? Color.RED : Color.parseColor("#66BB6A"));
        } else {
            tvTotalSpentPercentage.setText(currentMonthTotal > 0 ? "Bắt đầu chi tiêu tháng này" : "Chưa có dữ liệu");
        }

        // 3. Chuẩn bị dữ liệu cho biểu đồ (2 cột)
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) previousMonthTotal));
        entries.add(new BarEntry(1, (float) currentMonthTotal));

        BarDataSet dataSet = new BarDataSet(entries, "So sánh chi tiêu tháng");
        dataSet.setColors(Color.parseColor("#FFC107"), Color.parseColor("#4CAF50"));
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new DecimalFormat("#,###").format(value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChartReport.setData(barData);

        // 4. Tùy chỉnh giao diện biểu đồ
        barChartReport.getDescription().setEnabled(false);
        barChartReport.getLegend().setEnabled(false);
        barChartReport.getAxisRight().setEnabled(false);
        barChartReport.getAxisLeft().setAxisMinimum(0f);
        barChartReport.setFitBars(true);

        // Đặt nhãn cho trục X (QUAN TRỌNG)
        String[] labels = new String[]{"Tháng trước", "Tháng này"};
        barChartReport.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        barChartReport.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        barChartReport.getXAxis().setGranularity(1f);
        barChartReport.getXAxis().setDrawGridLines(false);

        barChartReport.animateY(1000);
        barChartReport.invalidate();
    }
}