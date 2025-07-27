package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
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

    // Các phần tử thanh trên cùng (Top Bar)
    private LinearLayout headerTitleSection;
    private TextView tvHeaderMainText;
    private LinearLayout subHeaderBalanceDetails;
    private TextView tvSubHeaderText; // "Tổng số dư"
    private LinearLayout subHeaderReportDots; // Chỉ báo trang cho báo cáo
    private View dot1, dot2, dot3; // Các chấm riêng lẻ cho tiêu đề trên cùng
    private ImageButton btnSearch;
    private ImageButton btnNotifications;

    // Các thẻ nội dung chính (Main Content Cards)
    private CardView walletSummaryCard;
    private CardView reportCardDynamicContent;
    private LinearLayout reportSummaryView; // "Tổng đã chi / Tổng thu"
    private LinearLayout reportTabView;     // Tab "Tuần / Tháng" và biểu đồ
    private CardView dealCard;
    private CardView topExpenseCard;
    private CardView recentTransactionsCard;

    // Các phần tử động của thẻ báo cáo (Report Card Dynamic Elements)
    private TextView tvReportSectionTitle; // "Báo cáo tháng này"
    private TextView tvSeeReportDetails;
    private TabLayout tabLayoutWeekMonthReport; // Dành cho "Tuần" / "Tháng" trong thẻ báo cáo
    private TextView tvCurrentReportValue;
    private TextView tvTotalSpentPercentage;
    private ImageButton btnReportPrev, btnReportNext;
    private LinearLayout reportPageIndicators; // Các chấm cho xu hướng báo cáo

    // Thanh điều hướng dưới cùng và FABs
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;
    private FloatingActionButton fabChatbot;

    private int currentReportGraphPage = 0; // 0 cho Tổng đã chi/Tổng thu, 1 cho biểu đồ Tuần/Tháng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Đảm bảo đây là R.layout.main cho layout dashboard chính

        // Ẩn ActionBar nếu có
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Khởi tạo các phần tử thanh trên cùng
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

        // Khởi tạo các thẻ nội dung chính
        walletSummaryCard = findViewById(R.id.wallet_summary_card);
        reportCardDynamicContent = findViewById(R.id.report_card_dynamic_content);
        reportSummaryView = findViewById(R.id.report_summary_view);
        reportTabView = findViewById(R.id.report_tab_view);
        dealCard = findViewById(R.id.deal_card);
        topExpenseCard = findViewById(R.id.top_expense_card);
        recentTransactionsCard = findViewById(R.id.recent_transactions_card);

        // Khởi tạo các phần tử động của thẻ báo cáo
        tvReportSectionTitle = findViewById(R.id.tv_report_section_title);
        tvSeeReportDetails = findViewById(R.id.tv_see_report_details);
        tabLayoutWeekMonthReport = findViewById(R.id.tab_layout_week_month_report);
        tvCurrentReportValue = findViewById(R.id.tv_current_report_value);
        tvTotalSpentPercentage = findViewById(R.id.tv_total_spent_percentage);
        btnReportPrev = findViewById(R.id.btn_report_prev);
        btnReportNext = findViewById(R.id.btn_report_next);
        reportPageIndicators = findViewById(R.id.report_page_indicators);

        // Khởi tạo thanh điều hướng dưới cùng và FABs
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        fabChatbot = findViewById(R.id.fab_chatbot);

        // Cập nhật giao diện cho chế độ tổng quan khi khởi động
        updateHeaderAndContentForOverview();

        // Lắng nghe sự kiện chọn tab trong thẻ báo cáo
        tabLayoutWeekMonthReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Toast.makeText(MainActivity.this, "Tuần được chọn", Toast.LENGTH_SHORT).show();
                    tvCurrentReportValue.setText("500.000 đ");
                    tvTotalSpentPercentage.setText("Tổng đã chi tuần này - 25%");
                } else {
                    Toast.makeText(MainActivity.this, "Tháng được chọn", Toast.LENGTH_SHORT).show();
                    tvCurrentReportValue.setText("1.500.000 đ");
                    tvTotalSpentPercentage.setText("Tổng đã chi tháng này - 15%");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* Không làm gì */ }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { /* Không làm gì */ }
        });

        // Lắng nghe sự kiện nhấp nút "Trước" trong báo cáo
        btnReportPrev.setOnClickListener(v -> {
            currentReportGraphPage = (currentReportGraphPage - 1 + 2) % 2;
            updateReportGraphView();
        });

        // Lắng nghe sự kiện nhấp nút "Tiếp theo" trong báo cáo
        btnReportNext.setOnClickListener(v -> {
            currentReportGraphPage = (currentReportGraphPage + 1) % 2;
            updateReportGraphView();
        });

        // Thiết lập trình lắng nghe cho thanh điều hướng dưới cùng
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                updateHeaderAndContentForOverview();
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                Toast.makeText(MainActivity.this, "Mở màn hình giao dịch!", Toast.LENGTH_SHORT).show();
                // TODO: Nếu có TransactionsActivity, hãy bỏ comment dòng dưới
                // Intent transactionsIntent = new Intent(MainActivity.this, TransactionsActivity.class);
                // startActivity(transactionsIntent);
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // Chuyển sang BudgetActivity khi chọn mục "Ngân sách"
                Intent budgetIntent = new Intent(MainActivity.this, BudgetActivity.class);
                startActivity(budgetIntent);
                return true;
            }
            return false;
        });

        // Lắng nghe sự kiện nhấp FAB "Thêm giao dịch"
        fabAddTransaction.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Thêm giao dịch mới!", Toast.LENGTH_SHORT).show();
            // TODO: Điều hướng đến màn hình Thêm giao dịch
        });

        // Lắng nghe sự kiện nhấp FAB "Chatbot"
        fabChatbot.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Mở Chatbot AI!", Toast.LENGTH_SHORT).show();
            // TODO: Nếu có ChatbotActivity, hãy bỏ comment dòng dưới
            // Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            // startActivity(intent);
        });
    }

    // Cập nhật giao diện header và nội dung cho chế độ tổng quan
    private void updateHeaderAndContentForOverview() {
        // Thanh trên cùng
        tvHeaderMainText.setText("0.00 đ");
        subHeaderBalanceDetails.setVisibility(View.VISIBLE);
        subHeaderReportDots.setVisibility(View.GONE);

        // Hiển thị các thẻ nội dung chính
        walletSummaryCard.setVisibility(View.VISIBLE);
        reportCardDynamicContent.setVisibility(View.VISIBLE);
        dealCard.setVisibility(View.VISIBLE);
        topExpenseCard.setVisibility(View.VISIBLE);
        recentTransactionsCard.setVisibility(View.VISIBLE);

        // Bên trong reportCardDynamicContent
        tvReportSectionTitle.setText("Báo cáo tháng này");
        tvSeeReportDetails.setText("Xem báo cáo");
        reportSummaryView.setVisibility(View.VISIBLE);
        reportTabView.setVisibility(View.GONE);
        reportPageIndicators.setVisibility(View.VISIBLE);
        updateReportGraphView();
    }

    // Cập nhật chế độ xem biểu đồ báo cáo
    private void updateReportGraphView() {
        if (currentReportGraphPage == 0) {
            reportSummaryView.setVisibility(View.VISIBLE);
            reportTabView.setVisibility(View.GONE);
            ((TextView)findViewById(R.id.tv_report_trend_title)).setText("Tháng này");
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_active);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_inactive);
        } else {
            reportSummaryView.setVisibility(View.GONE);
            reportTabView.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tv_report_trend_title)).setText("Trung bình 3 tháng trước");
            findViewById(R.id.report_dot1).setBackgroundResource(R.drawable.dot_inactive);
            findViewById(R.id.report_dot2).setBackgroundResource(R.drawable.dot_active);
            if (tabLayoutWeekMonthReport.getTabCount() > 0) {
                tabLayoutWeekMonthReport.getTabAt(0).select();
            }
        }
    }
}