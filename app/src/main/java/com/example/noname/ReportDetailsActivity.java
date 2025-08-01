package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;

public class ReportDetailsActivity extends AppCompatActivity {

    // --- Khai báo View ---
    private TextView btnCloseReport;
    private TabLayout tabLayoutMonthsReport;
    private ImageButton btnCalendar;

    // View cho bộ chọn ví
    private LinearLayout walletButton;
    private ImageView ivWalletIcon;
    private TextView tvWalletName;

    // Trình khởi chạy để nhận kết quả từ ChooseWalletActivity
    private final ActivityResultLauncher<Intent> walletLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Nhận key của ví đã chọn (ví dụ: "cash", "momo", "bank")
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

        // Thiết lập trạng thái ban đầu cho ví là "Tiền mặt"
        updateWalletView("cash");
    }

    private void initializeViews() {
        btnCloseReport = findViewById(R.id.btn_close_report);
        tabLayoutMonthsReport = findViewById(R.id.tab_layout_months_report);
        btnCalendar = findViewById(R.id.btn_calendar);

        // Ánh xạ các view của bộ chọn ví
        walletButton = findViewById(R.id.wallet_button);
        ivWalletIcon = findViewById(R.id.iv_wallet_icon);
        tvWalletName = findViewById(R.id.tv_wallet_name);
    }

    private void setupTabs() {
        // Vì thời điểm hiện tại là cuối tháng 7/2025
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("Tháng trước"));
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("Tháng này"), true);
    }

    private void setupListeners() {
        // Sự kiện đóng màn hình
        btnCloseReport.setOnClickListener(v -> finish());

        // Sự kiện cho nút lịch
        btnCalendar.setOnClickListener(v -> Toast.makeText(this, "Mở Lịch", Toast.LENGTH_SHORT).show());

        // Sự kiện cho nút chọn ví: Mở màn hình ChooseWalletActivity
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReportDetailsActivity.this, ChooseWalletActivity.class);
            walletLauncher.launch(intent);
        });

        // Sự kiện cho các tab
        tabLayoutMonthsReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(ReportDetailsActivity.this, "Xem báo cáo cho: " + tab.getText(), Toast.LENGTH_SHORT).show();
                // TODO: Tải lại dữ liệu báo cáo cho tháng được chọn
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Cập nhật tên và icon ví dựa trên key nhận được từ ChooseWalletActivity.
     * @param walletKey Key định danh của ví ("cash", "momo", "bank")
     */
    private void updateWalletView(String walletKey) {
        String walletName;
        int walletIconResId;

        switch (walletKey) {
            case "momo":
                walletName = "Ví Momo";
                walletIconResId = R.drawable.ic_wallet_momo;
                break;
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

        Toast.makeText(this, "Đang xem báo cáo cho: " + walletName, Toast.LENGTH_SHORT).show();
        // TODO: Gọi hàm tải dữ liệu báo cáo cho ví tương ứng tại đây
    }
}