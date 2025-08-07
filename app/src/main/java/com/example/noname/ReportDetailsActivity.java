package com.example.noname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;

import com.example.noname.database.AccountDAO;
import com.example.noname.database.TransactionDAO;
import com.example.noname.models.Account;
import com.example.noname.models.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ReportDetailsActivity";
    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;

    private TextView btnCloseReport;
    private TabLayout tabLayoutMonthsReport;
    private ImageButton btnCalendar;
    private LinearLayout walletButton;
    private ImageView ivWalletIcon;
    private TextView tvWalletName;
    private TextView tvOpeningBalance, tvClosingBalance;
    private TextView tvTotalExpense;

    private final ActivityResultLauncher<Intent> walletLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    long selectedAccountId = result.getData().getLongExtra("selected_account_id", -1);
                    if (selectedAccountId != -1) {
                        // Fix: Pass ID directly into the methods
                        loadWalletDetails(selectedAccountId);
                        loadReportData(selectedAccountId);
                    }
                }
            }
    );

    private long currentUserId = 1;
    private long currentAccountId = -1;
    private String currentReportDate = DB_DATE_FORMAT.format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        accountDAO = new AccountDAO(this);
        transactionDAO = new TransactionDAO(this);

        initializeViews();
        setupTabs();
        setupListeners();

        loadInitialData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        accountDAO.open();
        transactionDAO.open();
        loadReportData(currentAccountId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        accountDAO.close();
        transactionDAO.close();
    }

    private void initializeViews() {
        btnCloseReport = findViewById(R.id.btn_close_report);
        tabLayoutMonthsReport = findViewById(R.id.tab_layout_months_report);
        btnCalendar = findViewById(R.id.btn_calendar);
        walletButton = findViewById(R.id.wallet_button);
        ivWalletIcon = findViewById(R.id.iv_wallet_icon);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        tvOpeningBalance = findViewById(R.id.tv_opening_balance);
        tvClosingBalance = findViewById(R.id.tv_closing_balance);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
    }

    private void setupTabs() {
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("Last Month"));
        tabLayoutMonthsReport.addTab(tabLayoutMonthsReport.newTab().setText("This Month"), true);
    }

    private void setupListeners() {
        btnCloseReport.setOnClickListener(v -> finish());
        btnCalendar.setOnClickListener(v -> Toast.makeText(this, "Open Calendar", Toast.LENGTH_SHORT).show());
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReportDetailsActivity.this, ChooseWalletActivity.class);
            walletLauncher.launch(intent);
        });

        tabLayoutMonthsReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Calendar calendar = Calendar.getInstance();
                if (tab.getPosition() == 0) {
                    calendar.add(Calendar.MONTH, -1);
                } else if (tab.getPosition() == 2) {
                    calendar.add(Calendar.MONTH, 1);
                }
                currentReportDate = DB_DATE_FORMAT.format(calendar.getTime());
                loadReportData(currentAccountId);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadInitialData() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        long selectedAccountId = prefs.getLong("selected_account_id", -1);

        if (selectedAccountId != -1) {
            currentAccountId = selectedAccountId;
        } else {
            accountDAO.open();
            List<Account> accounts = accountDAO.getAllAccountsByUserId(currentUserId);
            accountDAO.close();
            if (!accounts.isEmpty()) {
                currentAccountId = accounts.get(0).getId();
            }
        }

        if (currentAccountId != -1) {
            loadWalletDetails(currentAccountId);
        } else {
            tvWalletName.setText("No Wallet");
            ivWalletIcon.setImageResource(R.drawable.ic_wallet);
            Toast.makeText(this, "No wallet to display report for", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWalletDetails(long accountId) {
        accountDAO.open();
        Account account = accountDAO.getAccountById(accountId);
        accountDAO.close();
        if (account != null) {
            tvWalletName.setText(account.getName());
            String iconName = getWalletIconName(account.getName());
            int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            ivWalletIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_wallet);
        } else {
            tvWalletName.setText("No Wallet");
            ivWalletIcon.setImageResource(R.drawable.ic_wallet);
        }
    }

    private void loadReportData(long accountId) {
        if (accountId == -1) {
            tvOpeningBalance.setText("0 đ");
            tvClosingBalance.setText("0 đ");
            tvTotalExpense.setText("0 đ");
            return;
        }

        String startDate = currentReportDate.substring(0, 7) + "-01";
        Calendar endCalendar = Calendar.getInstance();
        try {
            endCalendar.setTime(DB_DATE_FORMAT.parse(currentReportDate));
            endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        } catch (java.text.ParseException e) {
            Log.e(TAG, "Error parsing date: " + currentReportDate, e);
            endCalendar = Calendar.getInstance();
        }
        String endDate = DB_DATE_FORMAT.format(endCalendar.getTime());

        Log.d(TAG, "Loading report for Account ID: " + accountId + " from " + startDate + " to " + endDate);

        double openingBalance = transactionDAO.getAccountBalanceBeforeDate(accountId, startDate);
        double totalExpense = 0.0;

        List<Transaction> transactions = transactionDAO.getTransactionsByDateRange(currentUserId, accountId, startDate, endDate);

        for (Transaction transaction : transactions) {
            totalExpense += transaction.getAmount();
        }

        double closingBalance = openingBalance - totalExpense;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);

        tvOpeningBalance.setText(currencyFormat.format(openingBalance));
        tvClosingBalance.setText(currencyFormat.format(closingBalance));
        tvTotalExpense.setText(currencyFormat.format(totalExpense));
    }

    private String getWalletIconName(String walletName) {
        Map<String, String> walletIconMap = new HashMap<>();
        walletIconMap.put("Cash", "ic_wallet_cash");
        walletIconMap.put("Momo Wallet", "ic_wallet_momo");
        walletIconMap.put("Bank", "ic_wallet_bank");
        return walletIconMap.getOrDefault(walletName, "ic_wallet");
    }
}