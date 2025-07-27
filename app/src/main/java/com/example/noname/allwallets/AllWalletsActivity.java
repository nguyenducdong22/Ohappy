package com.example.noname.allwallets;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout; // Thêm import này

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.MainActivity; // Thêm import này
import com.example.noname.R;

public class AllWalletsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_wallets);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_all_wallets);
        setSupportActionBar(toolbar);

        // Hiển thị nút Back và bỏ tiêu đề mặc định
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // --- PHẦN CẬP NHẬT ---
        // Bắt sự kiện click cho mục "Tổng cộng"
        LinearLayout totalWalletLayout = findViewById(R.id.layout_total_wallet);
        totalWalletLayout.setOnClickListener(v -> {
            // Tạo Intent để quay về MainActivity
            Intent intent = new Intent(AllWalletsActivity.this, TransactionsFragment.class);

            // Gửi tín hiệu yêu cầu mở TransactionsFragment
            intent.putExtra("NAVIGATE_TO", "TRANSACTIONS_FRAGMENT");

            // Cờ để dùng lại MainActivity đã có, không tạo mới
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            finish(); // Đóng màn hình hiện tại
        });
        // --- KẾT THÚC PHẦN CẬP NHẬT ---
    }

    // Xử lý sự kiện khi nhấn nút Back trên Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Đóng Activity hiện tại và quay lại màn hình trước
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}