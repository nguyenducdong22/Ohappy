package com.example.noname;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MyWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        // Hide the default action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup listeners for all buttons on the screen
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Find views
        TextView btnCloseWallet = findViewById(R.id.btn_close_wallet);
        TextView btnEditWallet = findViewById(R.id.btn_edit_wallet);
        LinearLayout btnAddWallet = findViewById(R.id.btn_add_wallet);
        LinearLayout btnLinkService = findViewById(R.id.btn_link_service);

        // Close button: Closes this activity and returns to the previous one
        btnCloseWallet.setOnClickListener(v -> {
            finish();
        });

        // Edit button
        btnEditWallet.setOnClickListener(v -> {
            Toast.makeText(this, "Mở màn hình chỉnh sửa ví", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to the Edit Wallet screen
        });

        // Add Wallet button
        btnAddWallet.setOnClickListener(v -> {
            Toast.makeText(this, "Mở màn hình thêm ví mới", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to the Add Wallet screen
        });

        // Link Service button
        btnLinkService.setOnClickListener(v -> {
            Toast.makeText(this, "Mở màn hình liên kết dịch vụ", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to the Link Service screen
        });
    }
}