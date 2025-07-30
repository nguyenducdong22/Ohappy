package com.example.noname;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noname.account.BaseActivity;

public class MyWalletActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        TextView btnCloseWallet = findViewById(R.id.btn_close_wallet);
        TextView btnEditWallet = findViewById(R.id.btn_edit_wallet);
        LinearLayout btnAddWallet = findViewById(R.id.btn_add_wallet);
        LinearLayout btnLinkService = findViewById(R.id.btn_link_service);

        btnCloseWallet.setOnClickListener(v -> finish());

        btnEditWallet.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.open_edit_wallet_screen), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to the Edit Wallet screen
        });

        btnAddWallet.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.open_add_new_wallet_screen), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to the Add Wallet screen
        });

        btnLinkService.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.open_link_service_screen), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to the Link Service screen
        });
    }
}