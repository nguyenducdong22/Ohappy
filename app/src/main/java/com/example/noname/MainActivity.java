package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.noname.Budget.BudgetOverviewActivity;
import com.example.noname.account.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;


// QUAN TRỌNG: Kế thừa từ BaseActivity
public class MainActivity extends BaseActivity {

    // Khai báo biến UI
    private LinearLayout headerTitleSection;
    private TextView tvHeaderMainText;
    private LinearLayout subHeaderBalanceDetails;
    private TextView tvSubHeaderText;
    private LinearLayout subHeaderReportDots;
    private View dot1, dot2, dot3;
    private ImageButton btnSearch;
    private ImageButton btnNotifications;
    private CardView walletSummaryCard;
    private CardView reportCardDynamicContent;
    private LinearLayout reportSummaryView;
    private LinearLayout reportTabView;
    private CardView dealCard;
    private CardView topExpenseCard;
    private CardView recentTransactionsCard;
    private TextView tvReportSectionTitle;
    private TextView tvSeeReportDetails;
    private TabLayout tabLayoutWeekMonthReport;
    private TextView tvCurrentReportValue;
    private TextView tvTotalSpentPercentage;
    private ImageButton btnReportPrev, btnReportNext;
    private LinearLayout reportPageIndicators;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;
    private FloatingActionButton fabChatbot;

    private int currentReportGraphPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupListeners();
        updateUiWithLocalizedStrings(); // Cập nhật ngôn ngữ
    }

    private void initializeViews() {
        // Khởi tạo tất cả các View
        headerTitleSection = findViewById(R.id.header_title_section);
        tvHeaderMainText = findViewById(R.id.tv_header_main_text);
        subHeaderBalanceDetails = findViewById(R.id.sub_header_balance_details);
        tvSubHeaderText = findViewById(R.id.tv_sub_header_text);
        subHeaderReportDots = findViewById(R.id.sub_header_report_dots);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        btnSearch = findViewById(R.id.btn_search);
        btnNotifications = findViewById(R.id.btn_notifications);
        walletSummaryCard = findViewById(R.id.wallet_summary_card);
        reportCardDynamicContent = findViewById(R.id.report_card_dynamic_content);
        reportSummaryView = findViewById(R.id.report_summary_view);
        reportTabView = findViewById(R.id.report_tab_view);
        dealCard = findViewById(R.id.deal_card);
        topExpenseCard = findViewById(R.id.top_expense_card);
        recentTransactionsCard = findViewById(R.id.recent_transactions_card);
        tvReportSectionTitle = findViewById(R.id.tv_report_section_title);
        tvSeeReportDetails = findViewById(R.id.tv_see_report_details);
        tabLayoutWeekMonthReport = findViewById(R.id.tab_layout_week_month_report);
        tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);
        btnReportPrev = findViewById(R.id.btn_report_prev);
        btnReportNext = findViewById(R.id.btn_report_next);
        reportPageIndicators = findViewById(R.id.report_page_indicators);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabChatbot = findViewById(R.id.fab_chatbot);
    }

    private void setupListeners() {
        tabLayoutWeekMonthReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String percentage = (tab.getPosition() == 0) ? "25" : "15";
                String textToShow = (tab.getPosition() == 0) ?
                        getString(R.string.total_spent_this_week, percentage) :
                        getString(R.string.total_spent_this_month_format, percentage);

                Toast.makeText(MainActivity.this, (tab.getPosition() == 0) ? getString(R.string.week_selected) : getString(R.string.month_selected), Toast.LENGTH_SHORT).show();
                tvTotalSpentPercentage.setText(textToShow);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* Do nothing */ }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { /* Do nothing */ }
        });

        btnReportPrev.setOnClickListener(v -> updateReportGraphView());
        btnReportNext.setOnClickListener(v -> updateReportGraphView());

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                updateUiWithLocalizedStrings();
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                // TODO: Tạo và mở màn hình TransactionHistoryActivity
                startActivity(new Intent(MainActivity.this, TransactionHistoryActivity.class));
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // TODO: Tạo và mở màn hình BudgetOverviewActivity
                startActivity(new Intent(MainActivity.this, BudgetOverviewActivity.class));
                return true;
            } else if (itemId == R.id.navigation_account) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                return true;
            }
            return false;
        });

        fabAddTransaction.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, getString(R.string.add_new_transaction), Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình AddTransactionActivity
            startActivity(new Intent(MainActivity.this, Addtransaction.class));
        });

        fabChatbot.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, getString(R.string.chatbot), Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình ChatbotActivity
            startActivity(new Intent(MainActivity.this, ChatbotActivity.class));


        });
    }

    private void updateUiWithLocalizedStrings() {
        // Cập nhật các chuỗi từ strings.xml
        tvReportSectionTitle.setText(getString(R.string.report_this_month));
        tvSeeReportDetails.setText(getString(R.string.see_report));

        // Cập nhật Bottom Nav
        bottomNavigationView.getMenu().findItem(R.id.navigation_overview).setTitle(getString(R.string.title_overview));
        bottomNavigationView.getMenu().findItem(R.id.navigation_transactions).setTitle(getString(R.string.title_transactions));
        bottomNavigationView.getMenu().findItem(R.id.navigation_budget).setTitle(getString(R.string.title_budget));
        bottomNavigationView.getMenu().findItem(R.id.navigation_account).setTitle(getString(R.string.title_account));

        // Gọi lại các hàm cập nhật giao diện để chúng cũng dùng ngôn ngữ mới
        updateHeaderAndContentForOverview();
    }

    private void updateHeaderAndContentForOverview() {
        tvHeaderMainText.setText("0.00 đ"); // Giữ nguyên nếu đây là dữ liệu số
        subHeaderBalanceDetails.setVisibility(View.VISIBLE);
        subHeaderReportDots.setVisibility(View.GONE);

        // Hiển thị các thẻ
        findViewById(R.id.wallet_summary_card).setVisibility(View.VISIBLE);
        findViewById(R.id.report_card_dynamic_content).setVisibility(View.VISIBLE);
        findViewById(R.id.deal_card).setVisibility(View.VISIBLE);
        findViewById(R.id.top_expense_card).setVisibility(View.VISIBLE);
        findViewById(R.id.recent_transactions_card).setVisibility(View.VISIBLE);

        // Đảm bảo văn bản trong thẻ báo cáo cũng được cập nhật
        updateReportGraphView();
    }

    private void updateReportGraphView() {
        currentReportGraphPage = (currentReportGraphPage + 1) % 2;

        reportSummaryView.setVisibility(currentReportGraphPage == 0 ? View.VISIBLE : View.GONE);
        reportTabView.setVisibility(currentReportGraphPage == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.report_dot1).setBackgroundResource(currentReportGraphPage == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
        findViewById(R.id.report_dot2).setBackgroundResource(currentReportGraphPage == 1 ? R.drawable.dot_active : R.drawable.dot_inactive);

        TextView reportTrendTitle = findViewById(R.id.tv_report_trend_title);
        if (currentReportGraphPage == 0) {
            reportTrendTitle.setText(getString(R.string.this_month));
        } else {
            reportTrendTitle.setText(getString(R.string.last_3_months_avg));
            if (tabLayoutWeekMonthReport.getTabCount() > 0) {
                tabLayoutWeekMonthReport.getTabAt(0).select();
            }
        }
    }

}