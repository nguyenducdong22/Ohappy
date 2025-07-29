package com.example.noname.Budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.cardview.widget.CardView; // Import CardView
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Cần thiết để deserialize List

import com.example.noname.MainActivity;
import com.example.noname.R;

import java.lang.reflect.Type; // Cần thiết cho TypeToken
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetOverviewActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_BUDGET = 2;

    private ImageView btnBackBudgetOverview;
    private ImageView iconWorld;
    private ImageView iconRefresh;
    private ImageView iconMore;
    private Button btnCreateBudget;

    private TextView tvRemainingSpendableAmount;
    private TextView tvTotalBudgetAmount;
    private TextView tvTotalSpentAmount;
    private TextView tvDaysToEndOfMonth;

    // RecyclerView và Adapter cho danh sách ngân sách
    private RecyclerView recyclerViewBudgets;
    private BudgetAdapter budgetAdapter;
    private List<Budget> currentBudgetsList; // Danh sách các ngân sách hiện tại

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_overview);

        // Ánh xạ các View
        btnBackBudgetOverview = findViewById(R.id.btn_back_budget_overview);
        btnCreateBudget = findViewById(R.id.btn_create_budget);

        tvRemainingSpendableAmount = findViewById(R.id.tv_remaining_spendable_amount);
        tvTotalBudgetAmount = findViewById(R.id.tv_total_budget_amount);
        tvTotalSpentAmount = findViewById(R.id.tv_total_spent_amount);
        tvDaysToEndOfMonth = findViewById(R.id.tv_days_to_end_of_month);

        // Khởi tạo RecyclerView
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        currentBudgetsList = new ArrayList<>(); // Khởi tạo danh sách rỗng
        budgetAdapter = new BudgetAdapter(currentBudgetsList);
        recyclerViewBudgets.setAdapter(budgetAdapter);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // --- Thiết lập sự kiện click ---
        btnBackBudgetOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnCreateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BudgetOverviewActivity.this, AddBudgetActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET);
            }
        });

        // --- Cấu hình Bottom Navigation ---
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
                    Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình giao dịch!", Toast.LENGTH_SHORT).show();
                    // TODO: Điều hướng đến TransactionsActivity
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình tài khoản!", Toast.LENGTH_SHORT).show();
                    // TODO: Điều hướng đến AccountActivity
                    return true;
                }
                return false;
            });
        }

        // --- Cấu hình FAB "Thêm giao dịch" ---
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(v -> {
                Toast.makeText(BudgetOverviewActivity.this, "Thêm giao dịch mới từ màn hình Ngân sách!", Toast.LENGTH_SHORT).show();
                // TODO: Điều hướng đến màn hình Thêm giao dịch
            });
        }

        // Tải ngân sách khi Activity được tạo lần đầu
        loadBudgetsAndDisplay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_BUDGET) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Ngân sách mới đã được tạo!", Toast.LENGTH_SHORT).show();
                loadBudgetsAndDisplay(); // Tải lại danh sách ngân sách
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Tạo ngân sách đã bị hủy.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadBudgetsAndDisplay() {
        SharedPreferences sharedPref = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("all_budgets", null); // Lấy chuỗi JSON của TẤT CẢ ngân sách

        Type type = new TypeToken<ArrayList<Budget>>() {}.getType();
        List<Budget> loadedBudgets = gson.fromJson(json, type);

        if (loadedBudgets != null && !loadedBudgets.isEmpty()) {
            currentBudgetsList.clear();
            currentBudgetsList.addAll(loadedBudgets);
            budgetAdapter.setBudgetList(currentBudgetsList); // Cập nhật dữ liệu cho Adapter

            // Cập nhật phần tổng quan (hiển thị dữ liệu của ngân sách đầu tiên hoặc tổng hợp)
            // Ví dụ: Hiển thị tổng quan của ngân sách đầu tiên trong danh sách
            Budget firstBudget = currentBudgetsList.get(0);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            currencyFormat.setMinimumFractionDigits(0);
            currencyFormat.setMaximumFractionDigits(0);

            tvRemainingSpendableAmount.setText(currencyFormat.format(firstBudget.getAmount())); // Giả sử còn lại bằng tổng
            tvTotalBudgetAmount.setText(String.format("%,.0f M", firstBudget.getAmount() / 1000000.0));
            tvTotalSpentAmount.setText(String.format("%,.0f K", 50.0)); // Giá trị mẫu
            tvDaysToEndOfMonth.setText("3 ngày"); // Giá trị mẫu

            // Nút "Tạo Ngân sách" luôn hiển thị
            btnCreateBudget.setVisibility(View.VISIBLE);

        } else {
            // Chưa có ngân sách nào được tạo
            currentBudgetsList.clear();
            budgetAdapter.setBudgetList(currentBudgetsList); // Xóa dữ liệu cũ trên RecyclerView

            tvRemainingSpendableAmount.setText("0.00 đ");
            tvTotalBudgetAmount.setText("0 M");
            tvTotalSpentAmount.setText("0 K");
            tvDaysToEndOfMonth.setText("0 ngày");

            // Nút "Tạo Ngân sách" luôn hiển thị
            btnCreateBudget.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgetsAndDisplay();
    }
}