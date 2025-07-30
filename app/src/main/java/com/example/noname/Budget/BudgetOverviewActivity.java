package com.example.noname.Budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.noname.MainActivity; // Used for navigation.
import com.example.noname.R;             // Used for resource IDs.
import com.example.noname.database.BudgetDAO; // New: Import BudgetDAO.

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BudgetOverviewActivity displays a summary of the user's budgets and a list of individual budgets.
 * It allows navigation to create new budgets and interacts with BudgetDAO to load budget data.
 */
public class BudgetOverviewActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_BUDGET = 2; // Request code for AddBudgetActivity.

    // UI elements for the overview section
    private ImageView btnBackBudgetOverview;
    private Button btnCreateBudget;

    private TextView tvRemainingSpendableAmount;
    private TextView tvTotalBudgetAmount;
    private TextView tvTotalSpentAmount;
    private TextView tvDaysToEndOfMonth;

    // RecyclerView and Adapter for the list of budgets
    private RecyclerView recyclerViewBudgets;
    private BudgetAdapter budgetAdapter;
    private List<Budget> currentBudgetsList; // The data source for the RecyclerView.

    // Bottom Navigation Bar and Floating Action Button
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;

    private BudgetDAO budgetDAO; // DAO for interacting with budget data.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_overview);

        // Initialize BudgetDAO.
        budgetDAO = new BudgetDAO(this);

        // Initialize UI components by finding their IDs.
        btnBackBudgetOverview = findViewById(R.id.btn_back_budget_overview);
        btnCreateBudget = findViewById(R.id.btn_create_budget);

        tvRemainingSpendableAmount = findViewById(R.id.tv_remaining_spendable_amount);
        tvTotalBudgetAmount = findViewById(R.id.tv_total_budget_amount);
        tvTotalSpentAmount = findViewById(R.id.tv_total_spent_amount);
        tvDaysToEndOfMonth = findViewById(R.id.tv_days_to_end_of_month);

        // Setup RecyclerView for displaying budget list.
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this)); // Vertical list layout.
        currentBudgetsList = new ArrayList<>(); // Initialize an empty list.
        budgetAdapter = new BudgetAdapter(currentBudgetsList); // Create adapter with the list.
        recyclerViewBudgets.setAdapter(budgetAdapter); // Set the adapter to the RecyclerView.

        // Initialize BottomNavigationView and FloatingActionButton.
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // --- Set up click listeners ---
        btnBackBudgetOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the current activity and go back.
            }
        });

        btnCreateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AddBudgetActivity to create a new budget.
                Intent intent = new Intent(BudgetOverviewActivity.this, AddBudgetActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET); // Expect a result back.
            }
        });

        // --- Configure Bottom Navigation ---
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_budget); // Highlight the budget icon.

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_overview) {
                    // Navigate to MainActivity (Overview screen).
                    Intent overviewIntent = new Intent(BudgetOverviewActivity.this, MainActivity.class);
                    overviewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(overviewIntent);
                    return true;
                } else if (itemId == R.id.navigation_transactions) {
                    Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình giao dịch!", Toast.LENGTH_SHORT).show();
                    // TODO: Implement navigation to TransactionsActivity.
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    return true; // Already on this screen.
                } else if (itemId == R.id.navigation_account) {
                    Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình tài khoản!", Toast.LENGTH_SHORT).show();
                    // TODO: Implement navigation to AccountActivity.
                    return true;
                }
                return false;
            });
        }

        // --- Configure FAB "Add Transaction" ---
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(v -> {
                Toast.makeText(BudgetOverviewActivity.this, "Thêm giao dịch mới từ màn hình Ngân sách!", Toast.LENGTH_SHORT).show();
                // TODO: Implement navigation to the "Add Transaction" screen.
            });
        }

        // Load budgets when the Activity is first created.
        loadBudgetsAndDisplay();
    }

    /**
     * Handles results from activities started with `startActivityForResult()`.
     * @param requestCode The integer request code.
     * @param resultCode The integer result code (e.g., Activity.RESULT_OK, Activity.RESULT_CANCELED).
     * @param data The Intent containing result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_BUDGET) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Ngân sách mới đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                loadBudgetsAndDisplay(); // Reload the budget list to show the newly added budget.
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Thao tác tạo ngân sách đã bị hủy bỏ.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Loads budget data from the database using BudgetDAO and updates the UI.
     * This method is responsible for populating the RecyclerView and updating the overview statistics.
     */
    private void loadBudgetsAndDisplay() {
        // Open the database connection before querying.
        budgetDAO.open();
        // Retrieve all budget records from the database.
        List<Budget> loadedBudgets = budgetDAO.getAllBudgets();
        // Close the database connection immediately after fetching data.
        budgetDAO.close();

        if (loadedBudgets != null && !loadedBudgets.isEmpty()) {
            // Update the adapter's data set and notify the RecyclerView.
            currentBudgetsList.clear();
            currentBudgetsList.addAll(loadedBudgets);
            budgetAdapter.setBudgetList(currentBudgetsList); // This calls notifyDataSetChanged().

            // Update the overview statistics at the top of the screen.
            // For demo, using data from the first budget. In a real app, calculate aggregates.
            Budget firstBudget = currentBudgetsList.get(0);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            currencyFormat.setMinimumFractionDigits(0);
            currencyFormat.setMaximumFractionDigits(0);

            // TODO: Implement actual calculation for these values by querying transactions.
            // You'll need to sum up `amount` from `transactions` table for `firstBudget.getCategoryId()`
            // within `firstBudget.getStartDate()` and `firstBudget.getEndDate()`.
            tvRemainingSpendableAmount.setText(currencyFormat.format(firstBudget.getAmount())); // Placeholder for "remaining".
            tvTotalBudgetAmount.setText(String.format("%,.0f M", firstBudget.getAmount() / 1000000.0)); // Placeholder for total budget.
            tvTotalSpentAmount.setText(String.format("%,.0f K", 50.0)); // Placeholder for total spent.
            tvDaysToEndOfMonth.setText("3 ngày"); // Placeholder for days remaining.

            // Ensure the "Create Budget" button is visible regardless of budget count.
            btnCreateBudget.setVisibility(View.VISIBLE);

        } else {
            // If no budgets are found, clear the RecyclerView and reset overview stats to zero.
            currentBudgetsList.clear();
            budgetAdapter.setBudgetList(currentBudgetsList);

            tvRemainingSpendableAmount.setText("0.00 đ");
            tvTotalBudgetAmount.setText("0 M");
            tvTotalSpentAmount.setText("0 K");
            tvDaysToEndOfMonth.setText("0 ngày");

            btnCreateBudget.setVisibility(View.VISIBLE); // Still allow creation.
        }
    }

    /**
     * Called when the activity will start interacting with the user.
     * This is a good place to refresh UI that might have changed while the activity was paused.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Reload budgets every time the activity comes to the foreground
        // to ensure the list is up-to-date (e.g., after returning from AddBudgetActivity).
        loadBudgetsAndDisplay();
    }
}