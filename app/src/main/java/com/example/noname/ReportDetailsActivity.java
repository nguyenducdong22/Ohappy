package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

    // <<< CẬP NHẬT BIẾN VIEW >>>
    private TextView btnCloseReport;
    private TabLayout tabLayoutMonthsReport;
    private LinearLayout walletButton; // Thêm mới
    private TextView tvWalletName;     // Thêm mới
    private ImageView ivWalletIcon;    // Thêm mới
    private ImageButton btnCalendar;   // Thêm mới (để có thể xử lý sự kiện nếu cần)


    // <<< THÊM MỚI: Trình khởi chạy để nhận kết quả từ MyWalletActivity >>>
    private final ActivityResultLauncher<Intent> walletSelectorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("SELECTED_WALLET_NAME")) {
                        String selectedWallet = data.getStringExtra("SELECTED_WALLET_NAME");
                        // Cập nhật giao diện với ví vừa chọn
                        updateWalletSelectorUI(selectedWallet);
                        Toast.makeText(this, "Đang xem báo cáo cho ví: " + selectedWallet, Toast.LENGTH_SHORT).show();
                        // TODO: Thêm logic để tải lại dữ liệu báo cáo theo ví đã chọn
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ánh xạ View
        initializeViews();

        // Cài đặt các Tab
        setupTabs();

        // Cài đặt sự kiện click
        setupListeners();

        // <<< THÊM MỚI: Cập nhật giao diện ví lần đầu khi mở activity >>>
        // Lấy tên ví mặc định từ layout và cập nhật icon
        updateWalletSelectorUI(tvWalletName.getText().toString());
    }

    // <<< THÊM MỚI: Phương thức khởi tạo view >>>
    private void initializeViews() {
        btnCloseReport = findViewById(R.id.btn_close_report);
        tabLayoutMonthsReport = findViewById(R.id.tab_layout_months_report);
        walletButton = findViewById(R.id.wallet_button);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        ivWalletIcon = findViewById(R.id.iv_wallet_icon);
        btnCalendar = findViewById(R.id.btn_calendar);
    }

    private void setupTabs() {
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("04/2025"));
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("05/2025"));
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("THÁNG TRƯỚC"));
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("THÁNG NÀY"), true);
    }

    // <<< CẬP NHẬT: Phương thức cài đặt listener >>>
    private void setupListeners() {
        // Sự kiện đóng màn hình
        btnCloseReport.setOnClickListener(v -> finish());

        // <<< THÊM MỚI: Sự kiện mở màn hình chọn ví >>>
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReportDetailsActivity.this, MyWalletActivity.class);
            // Gửi tên ví hiện tại sang để MyWalletActivity biết cần đánh dấu tick
            intent.putExtra("CURRENT_WALLET_NAME", tvWalletName.getText().toString());
            // Khởi chạy activity và chờ kết quả trả về
            walletSelectorLauncher.launch(intent);
        });

        btnCalendar.setOnClickListener(v -> {
            // TODO: Thêm sự kiện cho nút lịch ở đây
            Toast.makeText(this, "Mở Lịch", Toast.LENGTH_SHORT).show();
        });

        // Sự kiện cho các tab
        tabLayoutMonthsReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // TODO: Tải lại dữ liệu báo cáo cho tháng được chọn
                Toast.makeText(ReportDetailsActivity.this, "Đã chọn: " + tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // <<< THÊM MỚI: Phương thức cập nhật giao diện bộ chọn ví >>>
    private void updateWalletSelectorUI(String walletName) {
        tvWalletName.setText(walletName);
        if ("Tổng cộng".equals(walletName)) {
            ivWalletIcon.setImageResource(R.drawable.ic_globe);
        } else {
            // Mặc định là icon ví tiền cho các ví khác (ví dụ: Tiền mặt)
            ivWalletIcon.setImageResource(R.drawable.ic_wallet_filled); // Giả sử bạn có icon này
        }
    }
}