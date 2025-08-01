package com.example.noname;



import android.content.Intent;

import android.os.Bundle;

import android.os.Handler;

import android.os.Looper;

import android.view.View;

import android.widget.Button;

import android.widget.ImageButton;

import android.widget.ImageView; // Import thư viện ImageView

import android.widget.LinearLayout;

import android.widget.Toast;

import android.widget.TextView;



import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;



import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.tabs.TabLayout;



public class TransactionHistoryActivity extends AppCompatActivity {

// Views

    private CardView onboardingCard;

    private ImageButton btnCloseOnboarding;

    private Button btnOnboardingAdd, btnOnboardingCreate;

    private TextView tooltipAddTransaction, tooltipCreateBudget;

    private LinearLayout walletButton;

    private TextView tvWalletName;

    private ImageView ivWalletIcon; // Biến cho icon ví

    private TabLayout tabLayoutMonths;

    private BottomNavigationView bottomNavigationView;

    private FloatingActionButton fabAddTransaction;



// Others

    private final Handler tooltipHandler = new Handler(Looper.getMainLooper());

    private Runnable hideTooltipRunnable;

    private ActivityResultLauncher<Intent> walletLauncher;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transaction_history);



// Đăng ký launcher để nhận "key" và cập nhật giao diện

        walletLauncher = registerForActivityResult(

                new ActivityResultContracts.StartActivityForResult(),

                result -> {

                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {

                        String selectedWalletKey = result.getData().getStringExtra("selected_wallet_key");

                        if (selectedWalletKey != null) {

                            updateWalletInfo(selectedWalletKey);

                        }

                    }

                }

        );



        if (getSupportActionBar() != null) {

            getSupportActionBar().hide();

        }



        initializeViews();

        setupTabs();

        setupClickListeners();

    }



    private void initializeViews() {

        onboardingCard = findViewById(R.id.onboarding_card);

        btnCloseOnboarding = findViewById(R.id.btn_close_onboarding);

        btnOnboardingAdd = findViewById(R.id.btn_onboarding_add);

        btnOnboardingCreate = findViewById(R.id.btn_onboarding_create);

        tooltipAddTransaction = findViewById(R.id.tooltip_add_transaction);

        tooltipCreateBudget = findViewById(R.id.tooltip_create_budget);



// Wallet Button from Top Bar

        walletButton = findViewById(R.id.wallet_button);

        tvWalletName = findViewById(R.id.tv_wallet_name);

        ivWalletIcon = findViewById(R.id.iv_wallet_icon); // Ánh xạ ImageView



// Tabs

        tabLayoutMonths = findViewById(R.id.tab_layout_months);



// Bottom Navigation and FAB

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        fabAddTransaction = findViewById(R.id.fab_add_transaction);

    }



    private void setupTabs() {

        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG TRƯỚC"));

        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG NÀY"), true);

    }



    private void setupClickListeners() {

// Wallet button to navigate to ChooseWalletActivity

        walletButton.setOnClickListener(v -> {

            Intent intent = new Intent(TransactionHistoryActivity.this, ChooseWalletActivity.class);

            walletLauncher.launch(intent);

        });



// Onboarding close button

        btnCloseOnboarding.setOnClickListener(v -> onboardingCard.setVisibility(View.GONE));



// Onboarding "Thêm" (Add) button

        btnOnboardingAdd.setOnClickListener(v -> showTooltip(tooltipAddTransaction));



// Onboarding "Tạo" (Create) button

        btnOnboardingCreate.setOnClickListener(v -> showTooltip(tooltipCreateBudget));



// FAB to add a transaction

        fabAddTransaction.setOnClickListener(v -> Toast.makeText(this, "Mở màn hình Thêm Giao Dịch", Toast.LENGTH_SHORT).show());



// Bottom Navigation

        bottomNavigationView.setSelectedItemId(R.id.navigation_transactions);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.navigation_overview) {

// TODO: Navigate back to MainActivity

                Toast.makeText(this, "Chuyển về Tổng quan", Toast.LENGTH_SHORT).show();

                finish();

                return true;

            } else if (itemId == R.id.navigation_transactions) {

// Already here

                return true;

            } else if (itemId == R.id.navigation_budget || itemId == R.id.navigation_account) {

// TODO: Navigate to other screens

                Toast.makeText(this, "Chuyển đến " + item.getTitle(), Toast.LENGTH_SHORT).show();

                return true;

            }

            return false;

        });

    }



    /**

     * Cập nhật tên và icon ví dựa trên key nhận được.

     * @param walletKey Key định danh của ví ("cash", "momo", "bank")

     */

    private void updateWalletInfo(String walletKey) {

        String walletName;

        int walletIconResId;



        switch (walletKey) {

            case "momo":

                walletName = "Ví Momo";

                walletIconResId = R.drawable.ic_wallet_momo;

                break;

            case "bank":

                walletName = "Ngân hàng";

                walletIconResId = R.drawable.ic_wallet_bank;

                break;

            case "cash":

            default:

                walletName = "Tiền mặt";

                walletIconResId = R.drawable.ic_wallet_cash;

                break;

        }



        tvWalletName.setText(walletName);

        ivWalletIcon.setImageResource(walletIconResId);

    }



    /**

     * Shows a tooltip view and schedules it to be hidden after a delay.

     * @param tooltipView The tooltip view (TextView) to display.

     */

    private void showTooltip(View tooltipView) {

        if (hideTooltipRunnable != null) {

            tooltipHandler.removeCallbacks(hideTooltipRunnable);

        }



        tooltipAddTransaction.setVisibility(View.GONE);

        tooltipCreateBudget.setVisibility(View.GONE);



        tooltipView.setVisibility(View.VISIBLE);

        tooltipView.setAlpha(0.0f);

        tooltipView.animate().alpha(1.0f).setDuration(300).start();



        hideTooltipRunnable = () -> tooltipView.animate()

                .alpha(0.0f)

                .setDuration(300)

                .withEndAction(() -> tooltipView.setVisibility(View.GONE))

                .start();



        tooltipHandler.postDelayed(hideTooltipRunnable, 4000);

    }

}