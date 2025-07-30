package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ChooseWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar_choose_wallet);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });


        LinearLayout walletCash = findViewById(R.id.wallet_cash);
        LinearLayout walletMomo = findViewById(R.id.wallet_momo);
        LinearLayout walletBank = findViewById(R.id.wallet_bank);

        View.OnClickListener selectWalletListener = v -> {
            // Dùng một "key" để định danh loại ví được chọn
            String selectedWalletKey = "";

            int viewId = v.getId();
            if (viewId == R.id.wallet_cash) {
                selectedWalletKey = "cash";
            } else if (viewId == R.id.wallet_momo) {
                selectedWalletKey = "momo";
            } else if (viewId == R.id.wallet_bank) {
                selectedWalletKey = "bank";
            }

            Intent resultIntent = new Intent();
            // Gửi "key" này về cho Activity trước đó
            resultIntent.putExtra("selected_wallet_key", selectedWalletKey);
            setResult(Activity.RESULT_OK, resultIntent);
            finish(); // Đóng Activity và trả về kết quả
        };

        walletCash.setOnClickListener(selectWalletListener);
        walletMomo.setOnClickListener(selectWalletListener);
        walletBank.setOnClickListener(selectWalletListener);
    }
}