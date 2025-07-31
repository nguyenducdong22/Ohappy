package com.example.noname;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.core.content.ContextCompat;

import com.example.noname.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    // Views
    private CardView onboardingCard;
    private ImageButton btnCloseOnboarding;
    private Button btnOnboardingAdd, btnOnboardingCreate;
    private TextView tooltipAddTransaction, tooltipCreateBudget;
    private LinearLayout walletButton;
    private TextView tvWalletName;
    private ImageView ivWalletIcon;
    private TextView tvBalance;
    private TabLayout tabLayoutMonths;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;
    private LinearLayout transactionsContainer;
    private View emptyStateView;

    // Others
    private final Handler tooltipHandler = new Handler(Looper.getMainLooper());
    private Runnable hideTooltipRunnable;
    private ActivityResultLauncher<Intent> walletLauncher;
    private DatabaseHelper dbHelper;
    private long currentAccountId = 1; // ID mặc định cho ví "Tiền mặt" (dữ liệu mẫu)
    private String selectedMonthFilter = ""; // Để lọc theo tháng, ví dụ "2025-08"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

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

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi quay lại màn hình để đảm bảo thông tin luôn mới nhất
        loadAndDisplayTransactions();
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
        ivWalletIcon = findViewById(R.id.iv_wallet_icon);
        tvBalance = findViewById(R.id.tv_balance);

        // Tabs
        tabLayoutMonths = findViewById(R.id.tab_layout_months);

        // Bottom Navigation and FAB
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // Transaction List Container
        transactionsContainer = findViewById(R.id.transactions_container);
        emptyStateView = findViewById(R.id.empty_state_view);
    }

    private void setupTabs() {
        // Lấy tháng hiện tại và tháng trước
        Calendar cal = Calendar.getInstance();

        // Tháng này (Tháng 8)
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        selectedMonthFilter = monthFormat.format(cal.getTime());
        String thisMonthLabel = "THÁNG " + (cal.get(Calendar.MONTH) + 1); // THÁNG 8
        TabLayout.Tab thisMonthTab = tabLayoutMonths.newTab().setText(thisMonthLabel);
        thisMonthTab.setTag(selectedMonthFilter);
        tabLayoutMonths.addTab(thisMonthTab, true);

        // Tháng trước (Tháng 7)
        cal.add(Calendar.MONTH, -1);
        String lastMonthFilter = monthFormat.format(cal.getTime());
        String lastMonthLabel = "THÁNG " + (cal.get(Calendar.MONTH) + 1); // THÁNG 7
        TabLayout.Tab lastMonthTab = tabLayoutMonths.newTab().setText(lastMonthLabel);
        lastMonthTab.setTag(lastMonthFilter);
        tabLayoutMonths.addTab(lastMonthTab, 0); // Thêm vào vị trí đầu tiên

        tabLayoutMonths.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Lấy bộ lọc tháng từ tag và tải lại dữ liệu
                selectedMonthFilter = (String) tab.getTag();
                loadAndDisplayTransactions();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionHistoryActivity.this, ChooseWalletActivity.class);
            walletLauncher.launch(intent);
        });

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
            } else if (itemId == R.id.navigation_budget || itemId == R.id.navigation_account) {
                Toast.makeText(this, "Chuyển đến " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void loadAndDisplayTransactions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Lấy và cập nhật số dư của tài khoản
        try (Cursor balanceCursor = db.query(DatabaseHelper.TABLE_ACCOUNTS,
                new String[]{DatabaseHelper.COLUMN_BALANCE},
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(currentAccountId)}, null, null, null)) {
            if (balanceCursor != null && balanceCursor.moveToFirst()) {
                double balance = balanceCursor.getDouble(balanceCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BALANCE));
                DecimalFormat formatter = new DecimalFormat("#,### đ");
                tvBalance.setText(formatter.format(balance));
            }
        } catch (Exception e) {
            Log.e("TransactionHistory", "Error loading balance", e);
        }

        // 2. Lấy và hiển thị danh sách giao dịch theo tháng đã chọn
        final String GET_TRANSACTIONS_QUERY =
                "SELECT T.id, T.amount, T.type, T.transaction_date, T.description, C.name AS category_name, C.icon_name " +
                        "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " T " +
                        "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON T.category_id = C.id " +
                        "WHERE T.account_id = ? AND strftime('%Y-%m', T.transaction_date) = ? " +
                        "ORDER BY T.transaction_date DESC";

        try (Cursor transactionCursor = db.rawQuery(GET_TRANSACTIONS_QUERY, new String[]{String.valueOf(currentAccountId), selectedMonthFilter})) {
            transactionsContainer.removeViews(1, transactionsContainer.getChildCount() - 1);

            if (transactionCursor != null && transactionCursor.getCount() > 0) {
                emptyStateView.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(this);
                while (transactionCursor.moveToNext()) {
                    View itemView = inflater.inflate(R.layout.list_item_transaction, transactionsContainer, false);
                    ImageView ivItemIcon = itemView.findViewById(R.id.iv_category_icon);
                    TextView tvItemCategory = itemView.findViewById(R.id.tv_category_name);
                    TextView tvItemDesc = itemView.findViewById(R.id.tv_transaction_description);
                    TextView tvItemAmount = itemView.findViewById(R.id.tv_transaction_amount);
                    TextView tvItemDate = itemView.findViewById(R.id.tv_transaction_date);

                    String type = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_TYPE));
                    double amount = transactionCursor.getDouble(transactionCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
                    String categoryName = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow("category_name"));
                    String description = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                    String dateString = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DATE));
                    String iconName = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME));

                    tvItemCategory.setText(categoryName);
                    tvItemDesc.setText(description);

                    DecimalFormat amountFormatter = new DecimalFormat("#,### đ");
                    String formattedAmount = amountFormatter.format(amount);
                    if ("Expense".equalsIgnoreCase(type)) {
                        tvItemAmount.setText("-" + formattedAmount);
                        tvItemAmount.setTextColor(Color.parseColor("#FF6B6B")); // Màu đỏ
                    } else {
                        tvItemAmount.setText("+" + formattedAmount);
                        tvItemAmount.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh
                    }

                    try {
                        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date = dbFormat.parse(dateString);
                        tvItemDate.setText(displayFormat.format(date));
                    } catch (ParseException e) {
                        tvItemDate.setText(dateString.split(" ")[0]);
                    }

                    int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                    ivItemIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_help);

                    transactionsContainer.addView(itemView);
                }
            } else {
                emptyStateView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("TransactionHistory", "Error loading transactions", e);
            emptyStateView.setVisibility(View.VISIBLE);
        }
    }

    private void updateWalletInfo(String walletKey) {
        String walletName;
        int walletIconResId;
        switch (walletKey) {
            case "bank":
                walletName = "Ngân hàng";
                walletIconResId = R.drawable.ic_wallet_bank;
                currentAccountId = 2; // ID của Ngân hàng trong dữ liệu mẫu
                break;
            case "cash":
            default:
                walletName = "Tiền mặt";
                walletIconResId = R.drawable.ic_wallet_cash;
                currentAccountId = 1; // ID của Tiền mặt trong dữ liệu mẫu
                break;
        }
        tvWalletName.setText(walletName);
        ivWalletIcon.setImageResource(walletIconResId);
        loadAndDisplayTransactions();
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
        hideTooltipRunnable = () -> tooltipView.animate()
                .alpha(0.0f)
                .setDuration(300)
                .withEndAction(() -> tooltipView.setVisibility(View.GONE))
                .start();
        tooltipHandler.postDelayed(hideTooltipRunnable, 4000);
    }
}