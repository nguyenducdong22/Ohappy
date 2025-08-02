package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.Budget.BudgetOverviewActivity;
import com.example.noname.adapters.TransactionAdapter;
import com.example.noname.database.AccountDAO;
import com.example.noname.database.TransactionDAO;
import com.example.noname.models.Account;
import com.example.noname.models.Transaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    private static final String TAG = "TransactionHistory";

    // Views
    private CardView onboardingCard;
    private ImageButton btnCloseOnboarding;
    private Button btnOnboardingAdd, btnOnboardingCreate;
    private TextView tooltipAddTransaction, tooltipCreateBudget;
    private LinearLayout walletButton;
    private TextView tvWalletName;
    private ImageView ivWalletIcon;
    private TextView tvBalance;
    private LinearLayout emptyStateView;

    private TabLayout tabLayoutMonths;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;
    private RecyclerView recyclerViewTransactions;

    // Adapters & DAOs
    private TransactionAdapter transactionAdapter;
    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;

    // Others
    private final Handler tooltipHandler = new Handler(Looper.getMainLooper());
    private Runnable hideTooltipRunnable;
    private ActivityResultLauncher<Intent> walletLauncher;
    private long currentUserId;
    private long currentAccountId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        transactionDAO = new TransactionDAO(this);
        accountDAO = new AccountDAO(this);

        walletLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        currentAccountId = result.getData().getLongExtra("selected_account_id", -1);
                        String selectedWalletName = result.getData().getStringExtra("selected_wallet_name");
                        int selectedWalletIconId = result.getData().getIntExtra("selected_wallet_icon", R.drawable.ic_wallet);

                        if (currentAccountId != -1) {
                            tvWalletName.setText(selectedWalletName);
                            ivWalletIcon.setImageResource(selectedWalletIconId);
                            loadTransactionsAndBalances();
                        }
                    }
                }
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();

        loadInitialWalletInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải dữ liệu cho tab đang được chọn
        loadTransactionsAndBalances();
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
        ivWalletIcon = findViewById(R.id.iv_wallet_icon);
        tvBalance = findViewById(R.id.tv_balance);
        emptyStateView = findViewById(R.id.empty_state_view);

        tabLayoutMonths = findViewById(R.id.tab_layout_months);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);
        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
    }

    private void setupRecyclerView() {
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(this);
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void loadInitialWalletInfo() {
        accountDAO.open();
        List<Account> accounts = accountDAO.getAllAccountsByUserId(currentUserId);
        accountDAO.close();

        if (!accounts.isEmpty()) {
            Account firstAccount = accounts.get(0);
            currentAccountId = firstAccount.getId();
            tvWalletName.setText(firstAccount.getName());
            int iconResId = getIconResIdForWallet(firstAccount.getName());
            ivWalletIcon.setImageResource(iconResId);
        } else {
            tvWalletName.setText("Không có ví");
            ivWalletIcon.setImageResource(R.drawable.ic_wallet);
            currentAccountId = -1;
        }
        // Sau khi có ví, tải dữ liệu giao dịch
        loadTransactionsAndBalances();
    }

    private void loadTransactionsAndBalances() {
        if (currentUserId == -1 || currentAccountId == -1) {
            tvBalance.setText(formatCurrency(0.0));
            transactionAdapter.setTransactions(new ArrayList<>());
            recyclerViewTransactions.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
            return;
        }

        // === THAY ĐỔI LỚN: TÍNH TOÁN NGÀY THÁNG DỰA VÀO TAB ĐÃ CHỌN ===
        Calendar cal = Calendar.getInstance();
        int selectedTabPosition = tabLayoutMonths.getSelectedTabPosition();

        // Vị trí 0 là "THÁNG TRƯỚC", vị trí 1 là "THÁNG NÀY"
        if (selectedTabPosition == 0) {
            cal.add(Calendar.MONTH, -1); // Lùi lại 1 tháng
        }
        // Nếu là tab "THÁNG NÀY" (vị trí 1), không cần thay đổi `cal`

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(cal.getTime());
        // ================================================================

        transactionDAO.open();
        List<Transaction> transactions = transactionDAO.getTransactionsByDateRange(currentUserId, currentAccountId, startDate, endDate);
        transactionDAO.close();

        if (!transactions.isEmpty()) {
            transactionAdapter.setTransactions(transactions);
            recyclerViewTransactions.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            transactionAdapter.setTransactions(new ArrayList<>());
            recyclerViewTransactions.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        }

        accountDAO.open();
        Account currentAccount = accountDAO.getAccountById(currentAccountId);
        accountDAO.close();

        if (currentAccount != null) {
            tvBalance.setText(formatCurrency(currentAccount.getBalance()));
        } else {
            tvBalance.setText(formatCurrency(0.0));
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

    private int getIconResIdForWallet(String walletName) {
        // (Giữ nguyên không đổi)
        switch (walletName) {
            case "Tiền mặt": return R.drawable.ic_wallet_cash;
            case "Ví Momo": return R.drawable.ic_wallet_momo;
            case "Ngân hàng": return R.drawable.ic_wallet_bank;
            default: return R.drawable.ic_wallet;
        }
    }

    private void setupTabs() {
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG TRƯỚC"));
        tabLayoutMonths.addTab(tabLayoutMonths.newTab().setText("THÁNG NÀY"), true);

        // === THAY ĐỔI LỚN: THÊM LISTENER ĐỂ XỬ LÝ KHI CHỌN TAB ===
        tabLayoutMonths.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Mỗi khi một tab được chọn, gọi lại hàm tải dữ liệu
                loadTransactionsAndBalances();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        // ==========================================================
    }

    private void setupClickListeners() {
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionHistoryActivity.this, ChooseWalletActivity.class);
            walletLauncher.launch(intent);
        });

        btnCloseOnboarding.setOnClickListener(v -> onboardingCard.setVisibility(View.GONE));
        btnOnboardingAdd.setOnClickListener(v -> showTooltip(tooltipAddTransaction));
        btnOnboardingCreate.setOnClickListener(v -> showTooltip(tooltipCreateBudget));

        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, Addtransaction.class);
            startActivity(intent);
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_transactions);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                Intent overviewIntent = new Intent(this, MainActivity.class);
                overviewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(overviewIntent);
                return true;
            } else if (itemId == R.id.navigation_transactions) {
                return true;
            } else if (itemId == R.id.navigation_budget) {
                Intent budgetIntent = new Intent(this, BudgetOverviewActivity.class);
                budgetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(budgetIntent);
                return true;
            } else if (itemId == R.id.navigation_account) {
                Intent accountIntent = new Intent(this, AccountActivity.class);
                accountIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(accountIntent);
                return true;
            }
            return false;
        });
    }

    private void showTooltip(View tooltipView) {
        // (Giữ nguyên không đổi)
        if (hideTooltipRunnable != null) tooltipHandler.removeCallbacks(hideTooltipRunnable);
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