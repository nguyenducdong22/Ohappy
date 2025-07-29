package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    // Các biến view
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
    private final Handler tooltipHandler = new Handler(Looper.getMainLooper());
    private Runnable hideTooltipRunnable;

    // Trình khởi chạy để nhận kết quả từ MyWalletActivity
    private final ActivityResultLauncher<Intent> walletSelectorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("SELECTED_WALLET_NAME")) {
                        String selectedWallet = data.getStringExtra("SELECTED_WALLET_NAME");
                        updateWalletSelectorUI(selectedWallet);
                        Toast.makeText(this, "Đã chọn ví: " + selectedWallet, Toast.LENGTH_SHORT).show();
                    }
                }
            });

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

        // Cập nhật giao diện ví lần đầu khi activity mở ra
        updateWalletSelectorUI(tvWalletName.getText().toString());
    }

    private void initializeViews() {
        onboardingCard = findViewById(R.id.onboarding_card);
        btnCloseOnboarding = findViewById(R.id.btn_close_onboarding);
        btnOnboardingAdd = findViewById(R.id.btn_onboarding_add);
        btnOnboardingCreate = findViewById(R.id.btn_onboarding_create);
        tooltipAddTransaction = findViewById(R.id.tooltip_add_transaction);
        tooltipCreateBudget = findViewById(R.id.tooltip_create_budget);

        walletButton = findViewById(R.id.wallet_button);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        ivWalletIcon = findViewById(R.id.iv_wallet_icon); // Khởi tạo ImageView

        tabLayoutMonths = findViewById(R.id.tab_layout_months);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
    }

    private void setupClickListeners() {
        // Sự kiện quan trọng: Mở màn hình chọn ví
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionHistoryActivity.this, MyWalletActivity.class);

            // Lấy tên ví hiện tại từ TextView
            String currentWallet = tvWalletName.getText().toString();

            // **Gửi tên ví này sang MyWalletActivity để nó biết cần hiển thị dấu tích ở đâu**
            intent.putExtra("CURRENT_WALLET_NAME", currentWallet);

            // Khởi chạy activity để chờ kết quả
            walletSelectorLauncher.launch(intent);
        });

        // Các listeners khác
        btnCloseOnboarding.setOnClickListener(v -> onboardingCard.setVisibility(View.GONE));
        btnOnboardingAdd.setOnClickListener(v -> showTooltip(tooltipAddTransaction));
        btnOnboardingCreate.setOnClickListener(v -> showTooltip(tooltipCreateBudget));
        fabAddTransaction.setOnClickListener(v -> Toast.makeText(this, "Mở màn hình Thêm Giao Dịch", Toast.LENGTH_SHORT).show());
        bottomNavigationView.setSelectedItemId(R.id.navigation_transactions);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                finish();
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                return true;
            } else if (itemId == R.id.navigation_budget) {
                return true;
            } else if (itemId == R.id.navigation_account) {
                return true;
            }
            return false;
        });
    }

    /**
     * Cập nhật giao diện của bộ chọn ví (cả icon và text).
     * @param walletName Tên của ví được chọn.
     */
    private void updateWalletSelectorUI(String walletName) {
        tvWalletName.setText(walletName);
        if ("Tổng cộng".equals(walletName)) {
            ivWalletIcon.setImageResource(R.drawable.ic_globe);
        } else {
            // Mặc định là icon ví tiền cho các trường hợp khác
            ivWalletIcon.setImageResource(R.drawable.ic_wallet);
        }
    }

    private void setupTabs() {
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG TRƯỚC"));
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG NÀY"), true);
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("TƯƠNG LAI"));
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