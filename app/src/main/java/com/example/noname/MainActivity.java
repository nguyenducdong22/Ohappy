package com.example.noname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import com.example.noname.Budget.BudgetOverviewActivity;
import com.example.noname.database.AccountDAO;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.TransactionDAO;
import com.example.noname.database.UserDAO;
import com.example.noname.models.Account;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // DAOs
    private UserDAO userDAO;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;

    private long currentUserId = 1;

    private long displayedAccountId;
    private SharedPreferences sharedPreferences;
    private static final String PREF_SELECTED_ACCOUNT_ID = "selected_account_id";

    // Views
    private TextView tvHeaderMainText;
    private LinearLayout subHeaderBalanceDetails;
    private CardView walletSummaryCard;
    private CardView dealCard;
    private CardView topExpenseCard;
    private TextView tvSeeAllWallets;
    private TextView tvAccountName;
    private TextView tvAccountBalance;
    private TextView tvTopExpense1, tvTopExpense2, tvTopExpense3;
    private ProgressBar pbTopExpense1, pbTopExpense2, pbTopExpense3;

    // Report Card Views
    private LinearLayout reportSummaryView; // Chứa LineChart
    private LinearLayout reportTabView;     // Chứa BarChart và TabLayout
    private TextView tvSeeReportDetails;
    private TextView tvEmptyReportPrompt;
    private TextView tvReportTrendTitle;
    private ImageButton btnReportPrev, btnReportNext;
    private LinearLayout reportPageIndicators;
    private LinearLayout lineChartContainer;
    private TextView tvCurrentReportValue;
    private TextView tvTotalSpentPercentage;
    private TextView tvTotalSpent;
    private LineChart lineChartReport;
    private BarChart barChartReport;
    private TabLayout tabLayoutWeekMonthReport;

    private int currentReportPage = 0; // 0 for LineChart, 1 for BarChart

    private final ActivityResultLauncher<Intent> chooseWalletLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    long newAccountId = result.getData().getLongExtra("selected_account_id", -1);
                    if (newAccountId != -1) {
                        displayedAccountId = newAccountId;
                        saveSelectedAccountId(displayedAccountId);
                    }
                    loadDashboardData();
                    Toast.makeText(this, "Đã cập nhật ví chính.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userDAO = new UserDAO(this);
        accountDAO = new AccountDAO(this);
        transactionDAO = new TransactionDAO(this);

        userDAO.open();
        accountDAO.open();
        transactionDAO.open();

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        displayedAccountId = sharedPreferences.getLong(PREF_SELECTED_ACCOUNT_ID, -1);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        // Header & Wallet
        tvHeaderMainText = findViewById(R.id.tv_header_main_text);
        subHeaderBalanceDetails = findViewById(R.id.sub_header_balance_details);
        walletSummaryCard = findViewById(R.id.wallet_summary_card);
        tvSeeAllWallets = findViewById(R.id.tv_see_all_wallets);
        tvAccountName = findViewById(R.id.tv_account_name);
        tvAccountBalance = findViewById(R.id.tv_account_balance);

        // Report Card
        reportSummaryView = findViewById(R.id.report_summary_view);
        reportTabView = findViewById(R.id.report_tab_view);
        tvSeeReportDetails = findViewById(R.id.tv_see_report_details);
        tvEmptyReportPrompt = findViewById(R.id.tv_empty_report_prompt);
        btnReportPrev = findViewById(R.id.btn_report_prev);
        btnReportNext = findViewById(R.id.btn_report_next);
        reportPageIndicators = findViewById(R.id.report_page_indicators);
        tvReportTrendTitle = findViewById(R.id.tv_report_trend_title);
        tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        lineChartContainer = findViewById(R.id.line_chart_container);
        lineChartReport = findViewById(R.id.line_chart_report);
        barChartReport = findViewById(R.id.bar_chart_report);
        tabLayoutWeekMonthReport = findViewById(R.id.tab_layout_week_month_report);

        // Other Cards
        dealCard = findViewById(R.id.deal_card);
        topExpenseCard = findViewById(R.id.top_expense_card);
        tvTopExpense1 = findViewById(R.id.tv_top_expense_1);
        tvTopExpense2 = findViewById(R.id.tv_top_expense_2);
        tvTopExpense3 = findViewById(R.id.tv_top_expense_3);
        pbTopExpense1 = findViewById(R.id.progress_top_expense_1);
        pbTopExpense2 = findViewById(R.id.progress_top_expense_2);
        pbTopExpense3 = findViewById(R.id.progress_top_expense_3);
    }


    private void setupListeners() {
        tvSeeAllWallets.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChooseWalletActivity.class);
            chooseWalletLauncher.launch(intent);
        });

        tabLayoutWeekMonthReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    setupAndLoadWeeklyBarChart();
                } else {
                    setupAndLoadMonthlyBarChart();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnReportPrev.setOnClickListener(v -> {
            currentReportPage = 0; // Go to Line Chart
            updateReportView();
        });

        btnReportNext.setOnClickListener(v -> {
            currentReportPage = 1; // Go to Bar Chart
            updateReportView();
        });

        tvSeeReportDetails.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportDetailsActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                startActivity(new Intent(MainActivity.this, TransactionHistoryActivity.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                startActivity(new Intent(this, BudgetOverviewActivity.class));
                return true;
            } else if (itemId == R.id.navigation_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });

        FloatingActionButton fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Addtransaction.class));
        });

        FloatingActionButton fabChatbot = findViewById(R.id.fab_chatbot);
        fabChatbot.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatbotActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(accountDAO != null) accountDAO.close();
        if(transactionDAO != null) transactionDAO.close();
        if(userDAO != null) userDAO.close();
    }


    private void loadDashboardData() {
        // --- 1. Load Wallet and Total Balance ---
        List<Account> accounts = accountDAO.getAllAccountsByUserId(currentUserId);
        double totalBalance = 0.0;
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                totalBalance += account.getBalance();
            }
            tvHeaderMainText.setText(String.format(Locale.GERMANY, "%,.0f đ", totalBalance));

            Account displayedAccount;
            if (displayedAccountId == -1) {
                displayedAccount = accounts.stream()
                        .filter(acc -> "Tiền mặt".equals(acc.getName()))
                        .findFirst()
                        .orElse(accounts.get(0));
                displayedAccountId = displayedAccount.getId();
                saveSelectedAccountId(displayedAccountId);
            } else {
                displayedAccount = accountDAO.getAccountById(displayedAccountId);
                if (displayedAccount == null) {
                    displayedAccount = accounts.get(0);
                    displayedAccountId = displayedAccount.getId();
                    saveSelectedAccountId(displayedAccountId);
                }
            }
            tvAccountName.setText(displayedAccount.getName());
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvAccountBalance.setText(currencyFormat.format(displayedAccount.getBalance()));
        } else {
            tvHeaderMainText.setText("0 đ");
            tvAccountName.setText("Chưa có ví");
            tvAccountBalance.setText("0 đ");
            displayedAccountId = -1;
            saveSelectedAccountId(-1);
        }

        // --- 2. Load Report Data ---
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(cal.getTime());

        // Load data for Line Chart (Page 0)
        double totalMonthlyExpense = transactionDAO.getTotalAmountByTypeAndDateRange(currentUserId, "Expense", startDate, endDate);
        tvTotalSpent.setText(new DecimalFormat("#,### đ").format(totalMonthlyExpense));
        Cursor dailyExpenseCursor = transactionDAO.getDailyExpensesByDateRange(currentUserId, startDate, endDate);
        setupLineChart(dailyExpenseCursor);


        // --- 3. Load Top Expenses ---
        loadTopExpenses(startDate, endDate);

        // --- 4. Set initial state of report view
        updateReportView();
    }

    private void saveSelectedAccountId(long accountId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_SELECTED_ACCOUNT_ID, accountId);
        editor.apply();
    }

    private void loadTopExpenses(String startDate, String endDate) {
        Cursor cursor = transactionDAO.getTopExpensesByCategory(currentUserId, startDate, endDate, 3);
        List<TextView> textViews = List.of(tvTopExpense1, tvTopExpense2, tvTopExpense3);
        List<ProgressBar> progressBars = List.of(pbTopExpense1, pbTopExpense2, pbTopExpense3);

        if (cursor != null && cursor.moveToFirst()) {
            double totalExpenseInCursor = 0;
            List<Map<String, Object>> topExpenses = new ArrayList<>();
            do {
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                topExpenses.add(Map.of(
                        "name", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME)),
                        "amount", amount
                ));
                totalExpenseInCursor += amount;
            } while (cursor.moveToNext());

            for (int i = 0; i < 3; i++) {
                if (i < topExpenses.size()) {
                    Map<String, Object> expense = topExpenses.get(i);
                    double amount = (double) expense.get("amount");
                    String name = (String) expense.get("name");
                    int percentage = totalExpenseInCursor > 0 ? (int) ((amount / totalExpenseInCursor) * 100) : 0;

                    textViews.get(i).setText(name + " (" + percentage + "%)");
                    progressBars.get(i).setProgress(percentage);
                    textViews.get(i).setVisibility(View.VISIBLE);
                    progressBars.get(i).setVisibility(View.VISIBLE);
                } else {
                    textViews.get(i).setVisibility(View.GONE);
                    progressBars.get(i).setVisibility(View.GONE);
                }
            }
            cursor.close();
        } else {
            for (int i = 0; i < 3; i++) {
                textViews.get(i).setVisibility(View.GONE);
                progressBars.get(i).setVisibility(View.GONE);
            }
        }
    }

    private void updateReportView() {
        if (currentReportPage == 0) { // Show Line Chart
            reportSummaryView.setVisibility(View.VISIBLE);
            reportTabView.setVisibility(View.GONE);
            tvReportTrendTitle.setText("Xu hướng chi tiêu tháng này");
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_active);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_inactive);
        } else { // Show Bar Chart
            reportSummaryView.setVisibility(View.GONE);
            reportTabView.setVisibility(View.VISIBLE);
            tvReportTrendTitle.setText("So sánh chi tiêu");
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_inactive);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_active);

            if (tabLayoutWeekMonthReport.getSelectedTabPosition() == 0) {
                setupAndLoadWeeklyBarChart();
            } else {
                setupAndLoadMonthlyBarChart();
            }
        }
    }

    private void setupLineChart(Cursor dailyExpensesCursor) {
        // Bước 1: Đọc dữ liệu từ Cursor và đưa vào Map để dễ tra cứu
        Map<Integer, Float> dailyTotalsMap = new HashMap<>();
        if (dailyExpensesCursor != null) {
            if(dailyExpensesCursor.moveToFirst()) {
                do {
                    String dateString = dailyExpensesCursor.getString(dailyExpensesCursor.getColumnIndexOrThrow("transaction_day"));
                    int dayOfMonth = Integer.parseInt(dateString.substring(8, 10));
                    float amount = dailyExpensesCursor.getFloat(dailyExpensesCursor.getColumnIndexOrThrow("total_amount"));
                    dailyTotalsMap.put(dayOfMonth, amount);
                } while (dailyExpensesCursor.moveToNext());
            }
            dailyExpensesCursor.close();
        }

        if (dailyTotalsMap.isEmpty()) {
            tvEmptyReportPrompt.setVisibility(View.VISIBLE);
            lineChartContainer.setVisibility(View.GONE);
            return;
        }
        tvEmptyReportPrompt.setVisibility(View.GONE);
        lineChartContainer.setVisibility(View.VISIBLE);

        // Bước 2: Tạo dữ liệu LŨY KẾ cho cả tháng
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        float cumulativeTotal = 0f; // Biến lưu tổng chi tiêu tích lũy

        for (int day = 1; day <= daysInMonth; day++) {
            // Lấy chi tiêu của ngày hôm đó
            float dailySpending = dailyTotalsMap.getOrDefault(day, 0f);
            // Cộng dồn vào tổng tích lũy
            cumulativeTotal += dailySpending;
            // Thêm điểm dữ liệu mới với giá trị là tổng tích lũy
            entries.add(new Entry(day - 1, cumulativeTotal));
            // Tạo nhãn cho trục X
            labels.add(String.format(Locale.US, "%02d", day));
        }

        // Bước 3: Cấu hình và vẽ biểu đồ
        XAxis xAxis = lineChartReport.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextSize(8f);

        lineChartReport.getAxisLeft().setDrawGridLines(false);
        lineChartReport.getAxisLeft().setAxisMinimum(0f);
        lineChartReport.getAxisRight().setEnabled(false);

        lineChartReport.setTouchEnabled(true);
        lineChartReport.setDragEnabled(true);
        lineChartReport.setScaleEnabled(true);
        lineChartReport.setPinchZoom(true);

        lineChartReport.getLegend().setEnabled(false);
        lineChartReport.getDescription().setEnabled(false);

        LineDataSet dataSet = new LineDataSet(entries, "Chi tiêu hàng ngày");
        dataSet.setColor(ContextCompat.getColor(this, R.color.expense_item_red));
        dataSet.setLineWidth(2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(this, R.color.expense_item_red));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChartReport.setData(lineData);
        lineChartReport.invalidate();
        lineChartReport.moveViewToX(entries.size());
    }


    private String[] getWeekDateRange(Calendar cal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        String start = dateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        String end = dateFormat.format(cal.getTime());
        return new String[]{start, end};
    }

    private void setupAndLoadWeeklyBarChart() {
        Calendar cal = Calendar.getInstance();

        String[] currentWeekRange = getWeekDateRange((Calendar) cal.clone());
        double currentWeekTotal = transactionDAO.getTotalAmountByTypeAndDateRange(currentUserId, "Expense", currentWeekRange[0], currentWeekRange[1]);

        cal.add(Calendar.WEEK_OF_YEAR, -1);
        String[] previousWeekRange = getWeekDateRange((Calendar) cal.clone());
        double previousWeekTotal = transactionDAO.getTotalAmountByTypeAndDateRange(currentUserId, "Expense", previousWeekRange[0], previousWeekRange[1]);

        DecimalFormat formatter = new DecimalFormat("#,### đ");
        tvCurrentReportValue.setText(formatter.format(currentWeekTotal));

        if (previousWeekTotal > 0) {
            double change = ((currentWeekTotal - previousWeekTotal) / previousWeekTotal) * 100;
            String changeText = String.format(Locale.US, "%.1f%% so với tuần trước", change);
            tvTotalSpentPercentage.setText(changeText);
            tvTotalSpentPercentage.setTextColor(change >= 0 ? Color.RED : ContextCompat.getColor(this, R.color.primary_green));
        } else {
            tvTotalSpentPercentage.setText(currentWeekTotal > 0 ? "Bắt đầu chi tiêu tuần này" : "Chưa có dữ liệu");
            tvTotalSpentPercentage.setTextColor(Color.GRAY);
        }
        setupBarChart(previousWeekTotal, currentWeekTotal, new String[]{"Tuần trước", "Tuần này"});
    }

    private void setupAndLoadMonthlyBarChart() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        String currentMonthStart = dateFormat.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String currentMonthEnd = dateFormat.format(cal.getTime());
        double currentMonthTotal = transactionDAO.getTotalAmountByTypeAndDateRange(currentUserId, "Expense", currentMonthStart, currentMonthEnd);

        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String prevMonthStart = dateFormat.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String prevMonthEnd = dateFormat.format(cal.getTime());
        double previousMonthTotal = transactionDAO.getTotalAmountByTypeAndDateRange(currentUserId, "Expense", prevMonthStart, prevMonthEnd);

        DecimalFormat formatter = new DecimalFormat("#,### đ");
        tvCurrentReportValue.setText(formatter.format(currentMonthTotal));

        if (previousMonthTotal > 0) {
            double change = ((currentMonthTotal - previousMonthTotal) / previousMonthTotal) * 100;
            String changeText = String.format(Locale.US, "%.1f%% so với tháng trước", change);
            tvTotalSpentPercentage.setText(changeText);
            tvTotalSpentPercentage.setTextColor(change >= 0 ? Color.RED : ContextCompat.getColor(this, R.color.primary_green));
        } else {
            tvTotalSpentPercentage.setText(currentMonthTotal > 0 ? "Bắt đầu chi tiêu tháng này" : "Chưa có dữ liệu");
            tvTotalSpentPercentage.setTextColor(Color.GRAY);
        }

        setupBarChart(previousMonthTotal, currentMonthTotal, new String[]{"Tháng trước", "Tháng này"});
    }

    private void setupBarChart(double previousValue, double currentValue, String[] labels) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) previousValue));
        entries.add(new BarEntry(1, (float) currentValue));

        BarDataSet dataSet = new BarDataSet(entries, "So sánh chi tiêu");
        dataSet.setColors(ContextCompat.getColor(this, R.color.expense_item_orange), ContextCompat.getColor(this, R.color.primary_green));
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new DecimalFormat("#,###").format(value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChartReport.setData(barData);

        barChartReport.getDescription().setEnabled(false);
        barChartReport.getLegend().setEnabled(false);
        barChartReport.getAxisRight().setEnabled(false);
        barChartReport.getAxisLeft().setAxisMinimum(0f);
        barChartReport.getAxisLeft().setDrawAxisLine(false);
        barChartReport.getAxisLeft().setDrawGridLines(false);
        barChartReport.getAxisLeft().setDrawLabels(false);
        barChartReport.setFitBars(true);
        barChartReport.setTouchEnabled(false);

        XAxis xAxis = barChartReport.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        barChartReport.animateY(1000);
        barChartReport.invalidate();
    }
}