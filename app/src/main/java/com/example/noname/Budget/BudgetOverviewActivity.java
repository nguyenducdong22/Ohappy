package com.example.noname.Budget; // Đảm bảo package này đúng

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.noname.MainActivity;
import com.example.noname.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Đảm bảo các import này đã đúng đường dẫn sau khi refactor
import com.example.noname.Budget.BudgetDao;
import com.example.noname.Budget.AppDatabase;


public class BudgetOverviewActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_BUDGET = 2;

    private ImageView btnBackBudgetOverview;
    private ImageView iconWorld; // Nếu có trong layout
    private ImageView iconRefresh; // Nếu có trong layout
    private ImageView iconMore; // Nếu có trong layout
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

    // Thêm tham chiếu đến BudgetDao của bạn
    private BudgetDao budgetDao;

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

        // Khởi tạo BudgetDao
        budgetDao = AppDatabase.getDatabase(this).budgetDao();

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
        // TẢI NGÂN SÁCH TỪ ROOM DATABASE
        List<Budget> loadedBudgets = budgetDao.getAllBudgets(); // Lấy tất cả ngân sách từ DB

        if (loadedBudgets != null && !loadedBudgets.isEmpty()) {
            currentBudgetsList.clear();
            currentBudgetsList.addAll(loadedBudgets);
            budgetAdapter.setBudgetList(currentBudgetsList); // Cập nhật dữ liệu cho Adapter

            // Cập nhật phần tổng quan
            double totalBudgetAmountValue = 0.0;
            for (Budget budget : loadedBudgets) {
                totalBudgetAmountValue += budget.getAmount();
            }

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            currencyFormat.setMinimumFractionDigits(0);
            currencyFormat.setMaximumFractionDigits(0);

            // TODO: Logic tính toán số tiền đã chi và còn lại thực tế
            // Bạn sẽ cần một bảng giao dịch và liên kết chúng với các ngân sách để tính toán chính xác
            double totalSpent = 0.0; // Giá trị ví dụ, bạn cần lấy từ dữ liệu giao dịch
            double remainingSpendable = totalBudgetAmountValue - totalSpent;

            tvRemainingSpendableAmount.setText(currencyFormat.format(remainingSpendable));
            tvTotalBudgetAmount.setText(String.format("%,.0f Đ", totalBudgetAmountValue)); // Hiển thị số tiền đầy đủ
            tvTotalSpentAmount.setText(String.format("%,.0f Đ", totalSpent)); // Giá trị ví dụ
            tvDaysToEndOfMonth.setText("3 ngày"); // Giá trị ví dụ, bạn cần tính toán ngày thực tế

            // Nút "Tạo Ngân sách" luôn hiển thị
            btnCreateBudget.setVisibility(View.VISIBLE);

        } else {
            // Chưa có ngân sách nào được tạo
            currentBudgetsList.clear();
            budgetAdapter.setBudgetList(currentBudgetsList); // Xóa dữ liệu cũ trên RecyclerView

            tvRemainingSpendableAmount.setText("0 đ");
            tvTotalBudgetAmount.setText("0 đ");
            tvTotalSpentAmount.setText("0 đ");
            tvDaysToEndOfMonth.setText("0 ngày");

            // Nút "Tạo Ngân sách" luôn hiển thị
            btnCreateBudget.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgetsAndDisplay(); // Tải lại dữ liệu khi quay lại Activity này để cập nhật trạng thái
    }
}