package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback; // Import này
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import này

import com.example.noname.account.BaseActivity;

public class ChooseWalletActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar_choose_wallet); // Ánh xạ toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Bật nút back
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Xử lý nút back trên toolbar
        toolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED); // Trả về kết quả hủy
            finish();
        });

        // Xử lý nút back của thiết bị
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED); // Trả về kết quả hủy
                finish();
            }
        });


        LinearLayout walletCash = findViewById(R.id.wallet_cash);
        LinearLayout walletMomo = findViewById(R.id.wallet_momo);
        LinearLayout walletBank = findViewById(R.id.wallet_bank);

        View.OnClickListener selectWalletListener = v -> { // Sử dụng lambda cho gọn
            String selectedWallet = "";

            if (v.getId() == R.id.wallet_cash) {
                selectedWallet = "Tiền mặt";
            } else if (v.getId() == R.id.wallet_momo) {
                selectedWallet = "Ví Momo";
            } else if (v.getId() == R.id.wallet_bank) {
                selectedWallet = "Ngân hàng";
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_wallet", selectedWallet);
            setResult(Activity.RESULT_OK, resultIntent);
            finish(); // Quay lại AddtransactionActivity
        };

        walletCash.setOnClickListener(selectWalletListener);
        walletMomo.setOnClickListener(selectWalletListener);
        walletBank.setOnClickListener(selectWalletListener);
    }
}