package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class TransactionHistoryActivity extends AppCompatActivity {
    private CardView onboardingCard;
    private ImageButton btnCloseOnboarding;
    // Onboarding and Tooltip Views
    private Button btnOnboardingAdd, btnOnboardingCreate;
    private TextView tooltipAddTransaction, tooltipCreateBudget;

    // Wallet selector
    private LinearLayout walletButton;

    // Tabs
    private TabLayout tabLayoutMonths;

    // Bottom Navigation
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;

    // Handler for hiding tooltips
    private final Handler tooltipHandler = new Handler(Looper.getMainLooper());
    private Runnable hideTooltipRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // Hide the default action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Views
        initializeViews();

        // Setup Tabs
        setupTabs();

        // Setup Listeners
        setupClickListeners();
    }

    private void initializeViews() {
        // Onboarding and Tooltips
        btnOnboardingAdd = findViewById(R.id.btn_onboarding_add);
        btnOnboardingCreate = findViewById(R.id.btn_onboarding_create);
        tooltipAddTransaction = findViewById(R.id.tooltip_add_transaction);
        tooltipCreateBudget = findViewById(R.id.tooltip_create_budget);

        onboardingCard = findViewById(R.id.onboarding_card);
        btnCloseOnboarding = findViewById(R.id.btn_close_onboarding);

        // Wallet Button from Top Bar
        walletButton = findViewById(R.id.wallet_button);

        // Tabs
        tabLayoutMonths = findViewById(R.id.tab_layout_months);

        // Bottom Navigation and FAB
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
    }

    private void setupTabs() {
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG TRƯỚC"));
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG NÀY"), true); // Select "This Month"
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("TƯƠNG LAI"));
    }

    private void setupClickListeners() {
        // Wallet button to navigate to MyWalletActivity
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionHistoryActivity.this, MyWalletActivity.class);
            startActivity(intent);
        });

        // Onboarding close button
        btnCloseOnboarding.setOnClickListener(v -> {
            onboardingCard.setVisibility(View.GONE);
        });

        // Onboarding "Thêm" (Add) button
        btnOnboardingAdd.setOnClickListener(v -> {
            showTooltip(tooltipAddTransaction);
        });

        // Onboarding "Tạo" (Create) button
        btnOnboardingCreate.setOnClickListener(v -> {
            showTooltip(tooltipCreateBudget);
        });

        // FAB to add a transaction
        fabAddTransaction.setOnClickListener(v -> {
            // TODO: Navigate to the actual "Add Transaction" screen
            Toast.makeText(this, "Mở màn hình Thêm Giao Dịch", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.navigation_transactions); // Highlight the correct item
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                // TODO: Navigate back to MainActivity
                Toast.makeText(this, "Chuyển về Tổng quan", Toast.LENGTH_SHORT).show();
                finish(); // Example: close this activity to go back
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                // Already here
                return true;
            } else if (itemId == R.id.navigation_budget) {
                // TODO: Navigate to Budget screen
                Toast.makeText(this, "Chuyển đến Ngân sách", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_account) {
                // TODO: Navigate to Account screen
                Toast.makeText(this, "Chuyển đến Tài khoản", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    /**
     * Shows a tooltip view and schedules it to be hidden after a delay.
     * @param tooltipView The tooltip view (TextView) to display.
     */
    private void showTooltip(View tooltipView) {
        // Cancel any pending hide command to prevent it from hiding the new tooltip
        if (hideTooltipRunnable != null) {
            tooltipHandler.removeCallbacks(hideTooltipRunnable);
        }

        // Ensure all tooltips are hidden before showing the new one
        tooltipAddTransaction.setVisibility(View.GONE);
        tooltipCreateBudget.setVisibility(View.GONE);

        // Make the selected tooltip visible
        tooltipView.setVisibility(View.VISIBLE);
        tooltipView.setAlpha(0.0f);
        tooltipView.animate().alpha(1.0f).setDuration(300).start();


        // Define the action to hide the tooltip
        hideTooltipRunnable = () -> {
            tooltipView.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .withEndAction(() -> tooltipView.setVisibility(View.GONE))
                    .start();
        };

        // Post the hide action with a 4-second delay
        tooltipHandler.postDelayed(hideTooltipRunnable, 4000);
    }
}