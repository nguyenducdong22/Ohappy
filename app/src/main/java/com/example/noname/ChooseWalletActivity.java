package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.account.BaseActivity;

public class ChooseWalletActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_wallet);

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
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
    }

    private void setupListeners() {
        LinearLayout walletCash = findViewById(R.id.wallet_cash);
        LinearLayout walletMomo = findViewById(R.id.wallet_momo);
        LinearLayout walletBank = findViewById(R.id.wallet_bank);

        View.OnClickListener selectWalletListener = v -> {
            String selectedWallet = "";
            int viewId = v.getId();

            if (viewId == R.id.wallet_cash) {
                selectedWallet = getString(R.string.cash);
            } else if (viewId == R.id.wallet_momo) {
                selectedWallet = getString(R.string.wallet_momo);
            } else if (viewId == R.id.wallet_bank) {
                selectedWallet = getString(R.string.wallet_bank);
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_wallet", selectedWallet);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        };

        walletCash.setOnClickListener(selectWalletListener);
        walletMomo.setOnClickListener(selectWalletListener);
        walletBank.setOnClickListener(selectWalletListener);
    }
}