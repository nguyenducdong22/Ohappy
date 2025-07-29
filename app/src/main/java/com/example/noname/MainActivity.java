package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

// QUAN TRỌNG: Đảm bảo bạn đang import đúng BaseActivity
import com.example.noname.account.BaseActivity;

public class MainActivity extends BaseActivity {

    // Khai báo các biến UI
    private TextView tvHeaderMainText, tvSubHeaderText, tvReportSectionTitle,
            tvSeeReportDetails, tvReportTrendTitle, tvCurrentReportValue,
            tvTotalSpentPercentage;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction, fabChatbot;
    private TabLayout tabLayoutWeekMonthReport;
    private ImageButton btnReportPrev, btnReportNext;
    private LinearLayout reportSummaryView, reportTabView;
    private View reportDot1, reportDot2;

    private int currentReportGraphPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        updateUiWithLocalizedStrings();
        setupListeners();

        // Thiết lập trạng thái ban đầu
        updateReportGraphViewVisibility();
    }

    private void initializeViews() {
        // Khởi tạo tất cả các View
        tvHeaderMainText = findViewById(R.id.tv_header_main_text);
        tvSubHeaderText = findViewById(R.id.tv_sub_header_text);
        tvReportSectionTitle = findViewById(R.id.tv_report_section_title);
        tvSeeReportDetails = findViewById(R.id.tv_see_report_details);
        tvReportTrendTitle = findViewById(R.id.tv_report_trend_title);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabChatbot = findViewById(R.id.fab_chatbot);
        tabLayoutWeekMonthReport = findViewById(R.id.tab_layout_week_month_report);
        tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);
        btnReportPrev = findViewById(R.id.btn_report_prev);
        btnReportNext = findViewById(R.id.btn_report_next);
        reportSummaryView = findViewById(R.id.report_summary_view);
        reportTabView = findViewById(R.id.report_tab_view);
        reportDot1 = findViewById(R.id.report_dot1);
        reportDot2 = findViewById(R.id.report_dot2);
    }

    private void updateUiWithLocalizedStrings() {
        // Cập nhật các văn bản từ tệp strings.xml
        tvSubHeaderText.setText(getString(R.string.total_balance));
        tvReportSectionTitle.setText(getString(R.string.report_this_month));
        tvSeeReportDetails.setText(getString(R.string.see_report));

        // Cập nhật văn bản cho BottomNavigationView
        bottomNavigationView.getMenu().findItem(R.id.navigation_overview).setTitle(getString(R.string.title_overview));
        bottomNavigationView.getMenu().findItem(R.id.navigation_transactions).setTitle(getString(R.string.title_transactions));
        bottomNavigationView.getMenu().findItem(R.id.navigation_budget).setTitle(getString(R.string.title_budget));
        bottomNavigationView.getMenu().findItem(R.id.navigation_account).setTitle(getString(R.string.title_account));

        updateReportGraphViewText();
    }

    private void setupListeners() {
        btnReportPrev.setOnClickListener(v -> {
            currentReportGraphPage = (currentReportGraphPage + 1) % 2;
            updateReportGraphViewVisibility();
            updateReportGraphViewText();
        });

        btnReportNext.setOnClickListener(v -> {
            currentReportGraphPage = (currentReportGraphPage + 1) % 2;
            updateReportGraphViewVisibility();
            updateReportGraphViewText();
        });

        // =================== LOGIC ĐIỀU HƯỚNG ĐÃ ĐƯỢC KHÔI PHỤC ===================
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                // Đã ở màn hình chính, không cần làm gì
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                // TODO: Tạo và mở màn hình TransactionHistoryActivity
                // Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
                // startActivity(intent);
                Toast.makeText(this, "Mở Lịch sử Giao dịch", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // TODO: Tạo và mở màn hình BudgetOverviewActivity
                // Intent budgetIntent = new Intent(MainActivity.this, BudgetOverviewActivity.class);
                // startActivity(budgetIntent);
                Toast.makeText(this, "Mở Ngân sách", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_account) {
                // Mở màn hình Tài khoản
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        // =================== KẾT THÚC PHẦN KHÔI PHỤC ===================

        fabAddTransaction.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, getString(R.string.add_new_transaction), Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình AddTransaction
        });

        fabChatbot.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, getString(R.string.chatbot), Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình Chatbot
        });
    }

    private void updateReportGraphViewVisibility() {
        reportSummaryView.setVisibility(currentReportGraphPage == 0 ? View.VISIBLE : View.GONE);
        reportTabView.setVisibility(currentReportGraphPage == 1 ? View.VISIBLE : View.GONE);
        reportDot1.setBackgroundResource(currentReportGraphPage == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
        reportDot2.setBackgroundResource(currentReportGraphPage == 1 ? R.drawable.dot_active : R.drawable.dot_inactive);
    }

    private void updateReportGraphViewText() {
        if (currentReportGraphPage == 0) {
            tvReportTrendTitle.setText(getString(R.string.this_month));
        } else {
            tvReportTrendTitle.setText(getString(R.string.last_3_months_avg));
        }
    }
}