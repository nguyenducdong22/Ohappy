package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MyWalletActivity extends AppCompatActivity {

    private ImageView ivTotalCheckmark, ivCashCheckmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupClickListeners();

        // Nhận tên ví hiện tại được gửi từ TransactionHistoryActivity
        String currentWalletName = getIntent().getStringExtra("CURRENT_WALLET_NAME");
        updateCheckmarks(currentWalletName);
    }

    private void initializeViews() {
        ivTotalCheckmark = findViewById(R.id.iv_total_checkmark);
        ivCashCheckmark = findViewById(R.id.iv_cash_checkmark);
    }

    /**
     * Cập nhật hiển thị dấu tích dựa trên tên ví hiện tại.
     * @param currentWallet Tên ví đang được chọn ở màn hình trước.
     */
    private void updateCheckmarks(String currentWallet) {
        // Ẩn tất cả dấu tích trước khi bắt đầu
        ivTotalCheckmark.setVisibility(View.GONE);
        ivCashCheckmark.setVisibility(View.GONE);

        if (currentWallet == null) return;

        // Hiển thị dấu tích cho mục tương ứng
        if (currentWallet.equals("Tổng cộng")) {
            ivTotalCheckmark.setVisibility(View.VISIBLE);
        } else if (currentWallet.equals("Tiền mặt")) {
            ivCashCheckmark.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        // Tìm các View
        TextView btnCloseWallet = findViewById(R.id.btn_close_wallet);
        TextView btnEditWallet = findViewById(R.id.btn_edit_wallet);
        LinearLayout walletTotalContainer = findViewById(R.id.wallet_total_container);
        LinearLayout walletCashContainer = findViewById(R.id.wallet_cash_container);
        LinearLayout btnAddWallet = findViewById(R.id.btn_add_wallet);
        LinearLayout btnLinkService = findViewById(R.id.btn_link_service);


        // Khi chọn ví -> trả kết quả về và đóng activity
        walletTotalContainer.setOnClickListener(v -> returnSelectedWallet("Tổng cộng"));
        walletCashContainer.setOnClickListener(v -> returnSelectedWallet("Tiền mặt"));

        // Nút Đóng
        btnCloseWallet.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        // Các nút còn lại
        btnEditWallet.setOnClickListener(v -> Toast.makeText(this, "Mở màn hình chỉnh sửa ví", Toast.LENGTH_SHORT).show());
        btnAddWallet.setOnClickListener(v -> Toast.makeText(this, "Mở màn hình thêm ví mới", Toast.LENGTH_SHORT).show());
        btnLinkService.setOnClickListener(v -> Toast.makeText(this, "Mở màn hình liên kết dịch vụ", Toast.LENGTH_SHORT).show());
    }

    /**
     * Đóng gói tên ví đã chọn và gửi lại cho activity đã gọi nó.
     * @param walletName Tên của ví được người dùng nhấn chọn.
     */
    private void returnSelectedWallet(String walletName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SELECTED_WALLET_NAME", walletName);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}