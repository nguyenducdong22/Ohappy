package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    // Top Bar elements
    private LinearLayout headerTitleSection;
    private TextView tvHeaderMainText;
    private LinearLayout subHeaderBalanceDetails;
    private TextView tvSubHeaderText; // "Tổng số dư"
    private LinearLayout subHeaderReportDots; // Page indicators for report
    private View dot1, dot2, dot3; // Individual dots for top header
    private ImageButton btnSearch;
    private ImageButton btnNotifications;

    // Main Content Cards
    private CardView walletSummaryCard;
    private CardView reportCardDynamicContent;
    private LinearLayout reportSummaryView; // "Tổng đã chi / Tổng thu"
    private LinearLayout reportTabView;     // "Tuần / Tháng" tab and chart
    private CardView dealCard;
    private CardView topExpenseCard;
    private CardView recentTransactionsCard;

    // Report Card Dynamic Elements
    private TextView tvReportSectionTitle; // "Báo cáo tháng này"
    private TextView tvSeeReportDetails;
    private TabLayout tabLayoutWeekMonthReport; // For "Tuần" / "Tháng" in report card
    private TextView tvCurrentReportValue;
    private TextView tvTotalSpentPercentage;
    private ImageButton btnReportPrev, btnReportNext;
    private LinearLayout reportPageIndicators; // Dots for report trend

    // Bottom Navigation
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;

    private int currentReportGraphPage = 0; // 0 for Tổng đã chi/Tổng thu, 1 for Tuần/Tháng graph

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Top Bar elements
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

        // Initialize Main Content Cards
        walletSummaryCard = findViewById(R.id.wallet_summary_card);
        reportCardDynamicContent = findViewById(R.id.report_card_dynamic_content);
        reportSummaryView = findViewById(R.id.report_summary_view);
        reportTabView = findViewById(R.id.report_tab_view);
        dealCard = findViewById(R.id.deal_card);
        topExpenseCard = findViewById(R.id.top_expense_card); // Now named top_expense_card
        recentTransactionsCard = findViewById(R.id.recent_transactions_card);

        // Initialize Dynamic Report Card elements
        tvReportSectionTitle = findViewById(R.id.tv_report_section_title);
        tvSeeReportDetails = findViewById(R.id.tv_see_report_details);
        tabLayoutWeekMonthReport = findViewById(R.id.tab_layout_week_month_report);
        tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);
        btnReportPrev = findViewById(R.id.btn_report_prev);
        btnReportNext = findViewById(R.id.btn_report_next);
        reportPageIndicators = findViewById(R.id.report_page_indicators);

        // Initialize Bottom Navigation and FAB
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // --- Set up initial state of the UI (Default to Overview Screen - image_ba3ced.jpg) ---
        updateHeaderAndContentForOverview();

        // Set up tab selected listener for Report Card (Tuần/Tháng)
        tabLayoutWeekMonthReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Toast.makeText(MainActivity.this, "Tuần Selected", Toast.LENGTH_SHORT).show();
                    // Update content for "Tuần" here
                    tvCurrentReportValue.setText("500.000 đ"); // Example value
                    tvTotalSpentPercentage.setText("Tổng đã chi tuần này - 25%"); // Example text
                } else {
                    Toast.makeText(MainActivity.this, "Tháng Selected", Toast.LENGTH_SHORT).show();
                    // Update content for "Tháng" here
                    tvCurrentReportValue.setText("1.500.000 đ"); // Example value
                    tvTotalSpentPercentage.setText("Tổng đã chi tháng này - 15%"); // Example text
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* Do nothing */ }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { /* Do nothing */ }
        });

        // Set up Listeners for report navigation arrows
        btnReportPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentReportGraphPage = (currentReportGraphPage - 1 + 2) % 2; // Cycle between 0 and 1
                updateReportGraphView();
            }
        });

        btnReportNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentReportGraphPage = (currentReportGraphPage + 1) % 2; // Cycle between 0 and 1
                updateReportGraphView();
            }
        });


        // Set up Bottom Navigation Listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_overview) {
                    updateHeaderAndContentForOverview();
                    return true;
                } else if (itemId == R.id.navigation_transactions) {
                    // This might be a separate screen/activity or a different view in MainActivity
                    Toast.makeText(MainActivity.this, "Số giao dịch", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    // This might be a separate screen/activity or a different view in MainActivity
                    Toast.makeText(MainActivity.this, "Ngân sách", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    // This might be a separate screen/activity or a different view in MainActivity (e.g., for Logout)
                    Toast.makeText(MainActivity.this, "Tài khoản", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        // Set up FAB Listener
        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Addtransaction.class);
                startActivity(intent);
            }
        });

    }

    // Call this method to update the header and content based on current view
    private void updateHeaderAndContentForOverview() {
        // Top Bar
        tvHeaderMainText.setText("0.00 đ");
        subHeaderBalanceDetails.setVisibility(View.VISIBLE);
        subHeaderReportDots.setVisibility(View.GONE); // No dots for balance header
        // btnBackHome.setVisibility(View.GONE); // No back button for overview header

        // Main Content Cards visibility
        walletSummaryCard.setVisibility(View.VISIBLE);
        reportCardDynamicContent.setVisibility(View.VISIBLE);
        dealCard.setVisibility(View.VISIBLE); // Deal card is visible when scrolling down
        topExpenseCard.setVisibility(View.VISIBLE);
        recentTransactionsCard.setVisibility(View.VISIBLE);

        // Inside reportCardDynamicContent
        tvReportSectionTitle.setText("Báo cáo tháng này"); // "Báo cáo tháng này"
        tvSeeReportDetails.setText("Xem báo cáo");
        reportSummaryView.setVisibility(View.VISIBLE); // Show Tổng đã chi/Tổng thu
        reportTabView.setVisibility(View.GONE); // Hide Tuần/Tháng tab content
        reportPageIndicators.setVisibility(View.VISIBLE); // Show dots for report trend navigation
        updateReportGraphView(); // Update initial state of report graph navigation
    }

    // This method handles switching between report graph views within reportCardDynamicContent
    private void updateReportGraphView() {
        if (currentReportGraphPage == 0) { // First screenshot (image_ba3ced.jpg) - Default
            reportSummaryView.setVisibility(View.VISIBLE);
            reportTabView.setVisibility(View.GONE);
            ((TextView)findViewById(R.id.tv_report_trend_title)).setText("Tháng này"); // Thay đổi tiêu đề báo cáo xu hướng
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_active);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_inactive);
        } else { // Second screenshot (IMG_1613.png) - Tuần/Tháng tabs
            reportSummaryView.setVisibility(View.GONE);
            reportTabView.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tv_report_trend_title)).setText("Trung bình 3 tháng trước"); // Thay đổi tiêu đề báo cáo xu hướng
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_inactive);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_active);
            tabLayoutWeekMonthReport.getTabAt(0).select(); // Select "Tuần" tab by default
        }
    }
}