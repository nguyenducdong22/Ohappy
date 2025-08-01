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
import com.example.noname.database.TransactionDAO;
import com.example.noname.database.UserDAO;
import com.example.noname.models.Account;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private LinearLayout subHeaderReportDots;
    private CardView walletSummaryCard;
    private CardView reportCardDynamicContent;
    private CardView dealCard;
    private CardView topExpenseCard;
    private TextView tvReportSectionTitle;
    private TextView tvSeeReportDetails;
    private TextView tvEmptyReportPrompt;
    private TextView tvReportTrendTitle;
    private ImageButton btnReportPrev, btnReportNext;
    private LinearLayout reportPageIndicators;
    private LinearLayout reportSummaryView;
    private LinearLayout reportTabView;
    private TextView tvSeeAllWallets;
    private TextView tvAccountName;
    private TextView tvAccountBalance;
    private TextView tvTopExpense1, tvTopExpense2, tvTopExpense3;
    private ProgressBar pbTopExpense1, pbTopExpense2, pbTopExpense3;

    private LineChart lineChart;
    private BarChart barChart;
    private TextView tvCurrentReportValue;
    private TextView tvTotalSpentPercentage;

    private int currentReportGraphPage = 0;

    private final ActivityResultLauncher<Intent> chooseWalletLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    long newAccountId = result.getData().getLongExtra("selected_account_id", -1);
                    if (newAccountId != -1) {
                        displayedAccountId = newAccountId;
                        saveSelectedAccountId(displayedAccountId);
                        Log.d("MainActivity", "Returned from ChooseWalletActivity. New displayed Account ID: " + displayedAccountId);
                    }
                    loadDashboardData();
                    Toast.makeText(this, "Đã cập nhật ví chính.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MainActivity", "Returned from ChooseWalletActivity with Canceled or no data.");
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
        Log.d("MainActivity", "Loaded stored account ID: " + displayedAccountId);

        tvHeaderMainText = findViewById(R.id.tv_header_main_text);
        subHeaderBalanceDetails = findViewById(R.id.sub_header_balance_details);
        subHeaderReportDots = findViewById(R.id.sub_header_report_dots);
        walletSummaryCard = findViewById(R.id.wallet_summary_card);
        reportCardDynamicContent = findViewById(R.id.report_card_dynamic_content);
        reportSummaryView = findViewById(R.id.report_summary_view);
        reportTabView = findViewById(R.id.report_tab_view);
        dealCard = findViewById(R.id.deal_card);
        topExpenseCard = findViewById(R.id.top_expense_card);
        tvReportSectionTitle = findViewById(R.id.tv_report_section_title);
        tvSeeReportDetails = findViewById(R.id.tv_see_report_details);
        tvEmptyReportPrompt = findViewById(R.id.tv_empty_report_prompt);
        btnReportPrev = findViewById(R.id.btn_report_prev);
        btnReportNext = findViewById(R.id.btn_report_next);
        reportPageIndicators = findViewById(R.id.report_page_indicators);
        tvReportTrendTitle = findViewById(R.id.tv_report_trend_title);
        tvSeeAllWallets = findViewById(R.id.tv_see_all_wallets);
        tvAccountName = findViewById(R.id.tv_account_name);
        tvAccountBalance = findViewById(R.id.tv_account_balance);
        tvTopExpense1 = findViewById(R.id.tv_top_expense_1);
        tvTopExpense2 = findViewById(R.id.tv_top_expense_2);
        tvTopExpense3 = findViewById(R.id.tv_top_expense_3);
        pbTopExpense1 = findViewById(R.id.progress_top_expense_1);
        pbTopExpense2 = findViewById(R.id.progress_top_expense_2);
        pbTopExpense3 = findViewById(R.id.progress_top_expense_3);

        lineChart = findViewById(R.id.line_chart);
        barChart = findViewById(R.id.bar_chart);

        tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);

        updateHeaderAndContentForOverview();

        tvSeeAllWallets.setOnClickListener(v -> {
            Log.d("MainActivity", "Clicked 'Xem tất cả' in Wallet section.");
            Intent intent = new Intent(MainActivity.this, ChooseWalletActivity.class);
            chooseWalletLauncher.launch(intent);
        });

        TabLayout tabLayoutWeekMonthReport = findViewById(R.id.tab_layout_week_month_report);
        tabLayoutWeekMonthReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Toast.makeText(MainActivity.this, "Biểu đồ tuần được chọn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Biểu đồ tháng được chọn", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnReportPrev.setOnClickListener(v -> {
            currentReportGraphPage = (currentReportGraphPage - 1 + 2) % 2;
            updateReportGraphView();
        });

        btnReportNext.setOnClickListener(v -> {
            currentReportGraphPage = (currentReportGraphPage + 1) % 2;
            updateReportGraphView();
        });

        tvSeeReportDetails.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportDetailsActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                updateHeaderAndContentForOverview();
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_budget) {
                Intent budgetIntent = new Intent(this, BudgetOverviewActivity.class);
                startActivity(budgetIntent);
                return true;
            } else if (itemId == R.id.navigation_account) {
                Intent intent = new Intent(this, AccountActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        FloatingActionButton fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabAddTransaction.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Thêm giao dịch mới!", Toast.LENGTH_SHORT).show();
            Intent addTransactionIntent = new Intent(MainActivity.this, Addtransaction.class);
            startActivity(addTransactionIntent);
        });

        FloatingActionButton fabChatbot = findViewById(R.id.fab_chatbot);
        fabChatbot.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Mở Chatbot AI!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accountDAO.close();
        transactionDAO.close();
        userDAO.close();
    }

    private void updateHeaderAndContentForOverview() {
        subHeaderBalanceDetails.setVisibility(View.VISIBLE);
        subHeaderReportDots.setVisibility(View.GONE);
        walletSummaryCard.setVisibility(View.VISIBLE);
        reportCardDynamicContent.setVisibility(View.VISIBLE);
        dealCard.setVisibility(View.VISIBLE);
        topExpenseCard.setVisibility(View.VISIBLE);

        tvReportSectionTitle.setText("Báo cáo tháng này");
        tvSeeReportDetails.setText("Xem báo cáo");
        reportPageIndicators.setVisibility(View.VISIBLE);
        updateReportGraphView();
    }

    private void loadDashboardData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        String startDate = currentMonth + "-01";
        String endDate = dateFormat.format(new Date());

        List<Account> accounts = accountDAO.getAllAccountsByUserId(currentUserId);
        double totalBalance = 0.0;

        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                totalBalance += account.getBalance();
            }
            tvHeaderMainText.setText(String.format("%,.0f đ", totalBalance));

            Account displayedAccount = null;

            if (displayedAccountId == -1) {
                for (Account acc : accounts) {
                    if ("Tiền mặt".equals(acc.getName())) {
                        displayedAccount = acc;
                        break;
                    }
                }
                if (displayedAccount == null) {
                    displayedAccount = accounts.get(0);
                }
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
            currencyFormat.setMinimumFractionDigits(0);
            currencyFormat.setMaximumFractionDigits(0);
            tvAccountBalance.setText(currencyFormat.format(displayedAccount.getBalance()));

        } else {
            tvHeaderMainText.setText("0 đ");
            tvAccountName.setText("Chưa có ví");
            tvAccountBalance.setText("0 đ");
            displayedAccountId = -1;
            saveSelectedAccountId(-1);
        }

        loadTopExpenses(startDate, endDate);
    }

    private void saveSelectedAccountId(long accountId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_SELECTED_ACCOUNT_ID, accountId);
        editor.apply();
    }

    private void loadTopExpenses(String startDate, String endDate) {
        Cursor cursor = transactionDAO.getTopExpensesByCategory(currentUserId, startDate, endDate, 3);
        List<TextView> textViews = new ArrayList<>();
        textViews.add(tvTopExpense1);
        textViews.add(tvTopExpense2);
        textViews.add(tvTopExpense3);

        List<ProgressBar> progressBars = new ArrayList<>();
        progressBars.add(pbTopExpense1);
        progressBars.add(pbTopExpense2);
        progressBars.add(pbTopExpense3);

        if (cursor != null && cursor.moveToFirst()) {
            double totalExpense = 0;
            cursor.moveToFirst();
            List<Map<String, Object>> topExpenses = new ArrayList<>();
            do {
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(TransactionDAO.COLUMN_CATEGORY_NAME));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                Map<String, Object> expense = new HashMap<>();
                expense.put("name", categoryName);
                expense.put("amount", totalAmount);
                topExpenses.add(expense);
                totalExpense += totalAmount;
            } while (cursor.moveToNext());
            cursor.close();

            for (int i = 0; i < 3; i++) {
                if (i < topExpenses.size()) {
                    Map<String, Object> expense = topExpenses.get(i);
                    double amount = (double) expense.get("amount");
                    String name = (String) expense.get("name");
                    int percentage = (int) ((amount / totalExpense) * 100);

                    textViews.get(i).setText(name + " (" + percentage + "%)");
                    progressBars.get(i).setProgress(percentage);
                    textViews.get(i).setVisibility(View.VISIBLE);
                    progressBars.get(i).setVisibility(View.VISIBLE);
                } else {
                    textViews.get(i).setVisibility(View.GONE);
                    progressBars.get(i).setVisibility(View.GONE);
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                textViews.get(i).setVisibility(View.GONE);
                progressBars.get(i).setVisibility(View.GONE);
            }
        }
    }

    private void updateReportGraphView() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        String startDateCurrentMonth = currentMonth + "-01";
        String endDateCurrentMonth = dateFormat.format(new Date());

        if (currentReportGraphPage == 0) {
            tvReportTrendTitle.setText("Xu hướng chi tiêu tháng này");
            reportSummaryView.setVisibility(View.VISIBLE);
            reportTabView.setVisibility(View.GONE);
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_active);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_inactive);

            Map<String, Double> dailyExpenses = getDailyExpensesForMonth(currentUserId, startDateCurrentMonth, endDateCurrentMonth);
            showLineChart(dailyExpenses);

        } else {
            tvReportTrendTitle.setText("So sánh chi tiêu");
            reportSummaryView.setVisibility(View.GONE);
            reportTabView.setVisibility(View.VISIBLE);
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_inactive);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_active);

            Map<String, Double> monthlyComparison = getMonthlyComparisonData(currentUserId);
            showBarChart(monthlyComparison);
        }
    }

    private Map<String, Double> getDailyExpensesForMonth(long userId, String startDate, String endDate) {
        Map<String, Double> dailyExpenses = new HashMap<>();
        Cursor cursor = transactionDAO.getDailyExpensesByDateRange(userId, startDate, endDate);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("transaction_day"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                dailyExpenses.put(date, amount);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return dailyExpenses;
    }

    private void showLineChart(Map<String, Double> dailyExpenses) {
        if (dailyExpenses.isEmpty()) {
            tvEmptyReportPrompt.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.GONE);
        } else {
            tvEmptyReportPrompt.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);

            // BƯỚC 3: Code để tạo và vẽ biểu đồ đường
            List<Entry> entries = new ArrayList<>();
            List<String> dates = new ArrayList<>();
            int i = 0;
            for (Map.Entry<String, Double> entry : dailyExpenses.entrySet()) {
                entries.add(new Entry(i++, entry.getValue().floatValue()));
                dates.add(entry.getKey().substring(5)); // Lấy định dạng MM-dd
            }

            // Tạo custom formatter cho trục X
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(true);

            // Tùy chỉnh trục Y
            lineChart.getAxisLeft().setDrawGridLines(false);
            lineChart.getAxisRight().setEnabled(false);

            // Vô hiệu hóa tương tác
            lineChart.setTouchEnabled(false);
            lineChart.setDragEnabled(false);
            lineChart.setScaleEnabled(false);
            lineChart.setPinchZoom(false);

            // Ẩn legend và description
            lineChart.getLegend().setEnabled(false);
            lineChart.getDescription().setEnabled(false);

            LineDataSet dataSet = new LineDataSet(entries, "Chi tiêu hàng ngày");
            dataSet.setColor(ContextCompat.getColor(this, R.color.expense_item_red));
            dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_dark));
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(ContextCompat.getColor(this, R.color.expense_item_red));
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            // Xóa các điểm tròn trên đường
            dataSet.setDrawCircles(false);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate();
        }
    }

    private Map<String, Double> getMonthlyComparisonData(long userId) {
        Map<String, Double> monthlyComparison = new HashMap<>();

        SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String currentMonth = yearMonthFormat.format(new Date());
        Date prevMonthDate = new Date(System.currentTimeMillis() - 2592000000L);
        String prevMonth = yearMonthFormat.format(prevMonthDate);

        Cursor cursorCurrent = transactionDAO.getMonthlyTotalExpense(userId, currentMonth + "-01", currentMonth + "-31");
        if (cursorCurrent != null && cursorCurrent.moveToFirst()) {
            monthlyComparison.put("Tháng này", cursorCurrent.getDouble(0));
            cursorCurrent.close();
        }

        Cursor cursorPrev = transactionDAO.getMonthlyTotalExpense(userId, prevMonth + "-01", prevMonth + "-31");
        if (cursorPrev != null && cursorPrev.moveToFirst()) {
            monthlyComparison.put("Tháng trước", cursorPrev.getDouble(0));
            cursorPrev.close();
        }

        return monthlyComparison;
    }

    private void showBarChart(Map<String, Double> monthlyComparison) {
        if (monthlyComparison.isEmpty()) {
            tvCurrentReportValue.setText("0 đ");
            tvTotalSpentPercentage.setText("Không có dữ liệu");
            barChart.setVisibility(View.GONE);
        } else {
            barChart.setVisibility(View.VISIBLE);
            double currentMonthExpense = monthlyComparison.getOrDefault("Tháng này", 0.0);
            tvCurrentReportValue.setText(String.format("%,.0f đ", currentMonthExpense));
            tvTotalSpentPercentage.setText("Tổng đã chi tháng này - 0%");

            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, monthlyComparison.getOrDefault("Tháng trước", 0.0).floatValue()));
            entries.add(new BarEntry(1, monthlyComparison.getOrDefault("Tháng này", 0.0).floatValue()));

            final ArrayList<String> xLabels = new ArrayList<>();
            xLabels.add("Tháng trước");
            xLabels.add("Tháng này");

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);

            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getAxisLeft().setDrawLabels(false);
            barChart.getAxisRight().setEnabled(false);

            barChart.setTouchEnabled(false);
            barChart.setDragEnabled(false);
            barChart.setScaleEnabled(false);
            barChart.setPinchZoom(false);

            barChart.getLegend().setEnabled(false);
            barChart.getDescription().setEnabled(false);

            BarDataSet dataSet = new BarDataSet(entries, "So sánh chi tiêu");
            dataSet.setColors(ContextCompat.getColor(this, R.color.expense_item_red),
                    ContextCompat.getColor(this, R.color.primary_green));
            dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_dark));
            dataSet.setDrawValues(true);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.5f);
            barChart.setData(barData);
            barChart.invalidate();
        }
    }
}