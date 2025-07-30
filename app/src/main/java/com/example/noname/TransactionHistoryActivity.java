package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import com.example.noname.account.BaseActivity;

public class TransactionHistoryActivity extends BaseActivity {
    private CardView onboardingCard;
    private ImageButton btnCloseOnboarding;
    private Button btnOnboardingAdd, btnOnboardingCreate;
    private TextView tooltipAddTransaction, tooltipCreateBudget;
    private LinearLayout walletButton;
    private TabLayout tabLayoutMonths;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;
    private final Handler tooltipHandler = new Handler(Looper.getMainLooper());
    private Runnable hideTooltipRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupTabs();
        setupClickListeners();
    }

    private void initializeViews() {
        btnOnboardingAdd = findViewById(R.id.btn_onboarding_add);
        btnOnboardingCreate = findViewById(R.id.btn_onboarding_create);
        tooltipAddTransaction = findViewById(R.id.tooltip_add_transaction);
        tooltipCreateBudget = findViewById(R.id.tooltip_create_budget);
        onboardingCard = findViewById(R.id.onboarding_card);
        btnCloseOnboarding = findViewById(R.id.btn_close_onboarding);
        walletButton = findViewById(R.id.wallet_button);
        tabLayoutMonths = findViewById(R.id.tab_layout_months);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
    }

    private void setupTabs() {
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText(getString(R.string.tab_previous_month)));
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText(getString(R.string.tab_this_month)), true);
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText(getString(R.string.tab_future)));
    }

    private void setupClickListeners() {
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionHistoryActivity.this, MyWalletActivity.class);
            startActivity(intent);
        });

        btnCloseOnboarding.setOnClickListener(v -> onboardingCard.setVisibility(View.GONE));
        btnOnboardingAdd.setOnClickListener(v -> showTooltip(tooltipAddTransaction));
        btnOnboardingCreate.setOnClickListener(v -> showTooltip(tooltipCreateBudget));

        fabAddTransaction.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.open_add_transaction_screen), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to the actual "Add Transaction" screen
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_transactions);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                Toast.makeText(this, getString(R.string.switch_to_overview), Toast.LENGTH_SHORT).show();
                finish();
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                return true;
            } else if (itemId == R.id.navigation_budget) {
                Toast.makeText(this, getString(R.string.switch_to_budget), Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Budget screen
                return true;
            } else if (itemId == R.id.navigation_account) {
                Toast.makeText(this, getString(R.string.switch_to_account), Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Account screen
                return true;
            }
            return false;
        });
    }

    private void showTooltip(View tooltipView) {
        if (hideTooltipRunnable != null) {
            tooltipHandler.removeCallbacks(hideTooltipRunnable);
        }

        tooltipAddTransaction.setVisibility(View.GONE);
        tooltipCreateBudget.setVisibility(View.GONE);

        tooltipView.setVisibility(View.VISIBLE);
        tooltipView.setAlpha(0.0f);
        tooltipView.animate().alpha(1.0f).setDuration(300).start();

        hideTooltipRunnable = () -> {
            tooltipView.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .withEndAction(() -> tooltipView.setVisibility(View.GONE))
                    .start();
        };

        tooltipHandler.postDelayed(hideTooltipRunnable, 4000);
    }
}