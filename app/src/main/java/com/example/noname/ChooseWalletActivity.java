package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_wallet);

        LinearLayout walletCash = findViewById(R.id.wallet_cash);
        LinearLayout walletMomo = findViewById(R.id.wallet_momo);
        LinearLayout walletBank = findViewById(R.id.wallet_bank);

        View.OnClickListener selectWalletListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                finish(); // Quay lại Addtransaction
            }
        };

        walletCash.setOnClickListener(selectWalletListener);
        walletMomo.setOnClickListener(selectWalletListener);
        walletBank.setOnClickListener(selectWalletListener);
    }
}
