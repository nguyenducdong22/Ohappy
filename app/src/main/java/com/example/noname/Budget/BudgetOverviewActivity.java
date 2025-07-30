package com.example.noname.Budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.MainActivity;
import com.example.noname.R;
import com.example.noname.account.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetOverviewActivity extends BaseActivity {

    private static final int REQUEST_CODE_ADD_BUDGET = 2;

    private TextView tvRemainingSpendableAmount;
    private TextView tvTotalBudgetAmount;
    private TextView tvTotalSpentAmount;
    private TextView tvDaysToEndOfMonth;
    private RecyclerView recyclerViewBudgets;
    private BudgetAdapter budgetAdapter;
    private List<Budget> currentBudgetsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_overview);

        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi quay lại màn hình này
        loadBudgetsAndDisplay();
    }

    private void initializeViews() {
        tvRemainingSpendableAmount = findViewById(R.id.tv_remaining_spendable_amount);
        tvTotalBudgetAmount = findViewById(R.id.tv_total_budget_amount);
        tvTotalSpentAmount = findViewById(R.id.tv_total_spent_amount);
        tvDaysToEndOfMonth = findViewById(R.id.tv_days_to_end_of_month);
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);

        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        currentBudgetsList = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(currentBudgetsList);
        recyclerViewBudgets.setAdapter(budgetAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.btn_back_budget_overview).setOnClickListener(v -> finish());
        findViewById(R.id.btn_create_budget).setOnClickListener(v -> {
            Intent intent = new Intent(BudgetOverviewActivity.this, AddBudgetActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_budget);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_overview) {
                    Intent overviewIntent = new Intent(BudgetOverviewActivity.this, MainActivity.class);
                    overviewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(overviewIntent);
                    return true;
                } else if (itemId == R.id.navigation_transactions) {
                    Toast.makeText(this, getString(R.string.open_transactions_screen), Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    Toast.makeText(this, getString(R.string.open_account_screen), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }

        FloatingActionButton fabAddTransaction = findViewById(R.id.fab_add_transaction);
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(v -> {
                Toast.makeText(this, getString(R.string.add_transaction_from_budget_screen), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_BUDGET) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, getString(R.string.new_budget_created), Toast.LENGTH_SHORT).show();
                loadBudgetsAndDisplay();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.create_budget_cancelled), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadBudgetsAndDisplay() {
        SharedPreferences sharedPref = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("all_budgets", null);
        Type type = new TypeToken<ArrayList<Budget>>() {}.getType();
        List<Budget> loadedBudgets = gson.fromJson(json, type);

        if (loadedBudgets != null && !loadedBudgets.isEmpty()) {
            currentBudgetsList.clear();
            currentBudgetsList.addAll(loadedBudgets);
            budgetAdapter.setBudgetList(currentBudgetsList);
            updateOverviewCard(loadedBudgets);
        } else {
            currentBudgetsList.clear();
            budgetAdapter.setBudgetList(currentBudgetsList);
            resetOverviewCard();
        }
    }

    private void updateOverviewCard(List<Budget> budgets) {
        double totalBudget = 0;
        for (Budget budget : budgets) {
            totalBudget += budget.getAmount();
        }

        // Giả sử các giá trị khác là mẫu
        double remaining = totalBudget - 50000;
        int days = 3;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);

        tvRemainingSpendableAmount.setText(currencyFormat.format(remaining));
        tvTotalBudgetAmount.setText(String.format(Locale.getDefault(), "%,.0f M", totalBudget / 1000000.0));
        tvTotalSpentAmount.setText(String.format(Locale.getDefault(), "%,.0f K", 50.0));
        tvDaysToEndOfMonth.setText(getString(R.string.days_unit_format, days));
    }

    private void resetOverviewCard() {
        tvRemainingSpendableAmount.setText("0 đ");
        tvTotalBudgetAmount.setText("0 M");
        tvTotalSpentAmount.setText("0 K");
        tvDaysToEndOfMonth.setText(getString(R.string.days_unit_format, 0));
    }
}