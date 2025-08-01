package com.example.noname.Budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;
import com.example.noname.database.BudgetDAO;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BudgetOverviewActivity extends AppCompatActivity implements BudgetAdapter.OnBudgetActionListener {

    private static final int REQUEST_CODE_ADD_BUDGET = 2;
    private static final String TAG = "BudgetOverviewActivity";

    // --- UI Elements ---
    private ImageView btnBackBudgetOverview;
    private Button btnCreateBudget;
    private TextView tvRemainingSpendableAmount;
    private TextView tvTotalBudgetAmount;
    private TextView tvTotalSpentAmount;
    private TextView tvDaysToEndOfMonth;
    private TextView tvOverviewTitle;
    private RecyclerView recyclerViewBudgets;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;

    // --- Data and Logic ---
    private BudgetAdapter budgetAdapter;
    private List<Budget> currentBudgetsList;
    private BudgetDAO budgetDAO;
    private Budget selectedBudgetForOverview = null;
    private long currentUserId;

    // Các biến để lưu giá trị tổng đã tính toán
    private double totalAllBudgetsAmount = 0.0;
    private double totalAllSpentAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_overview);

        // Lấy User ID từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // TODO: Chuyển hướng về màn hình đăng nhập
            finish();
            return;
        }

        budgetDAO = new BudgetDAO(this);

        // --- Ánh xạ các thành phần UI ---
        btnBackBudgetOverview = findViewById(R.id.btn_back_budget_overview);
        btnCreateBudget = findViewById(R.id.btn_create_budget);
        tvRemainingSpendableAmount = findViewById(R.id.tv_remaining_spendable_amount);
        tvTotalBudgetAmount = findViewById(R.id.tv_total_budget_amount);
        tvTotalSpentAmount = findViewById(R.id.tv_total_spent_amount);
        tvDaysToEndOfMonth = findViewById(R.id.tv_days_to_end_of_month);
        tvOverviewTitle = findViewById(R.id.tv_screen_title);
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // --- Thiết lập RecyclerView và Adapter ---
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        currentBudgetsList = new ArrayList<>(); // Khởi tạo danh sách
        budgetAdapter = new BudgetAdapter(new ArrayList<>(), this);
        recyclerViewBudgets.setAdapter(budgetAdapter);

        // --- Thiết lập các trình lắng nghe sự kiện click ---
        btnBackBudgetOverview.setOnClickListener(v -> finish());
        btnCreateBudget.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetOverviewActivity.this, AddBudgetActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET);
        });

        // Tải dữ liệu ban đầu
        loadBudgetsAndDisplay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_BUDGET && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Ngân sách đã được lưu!", Toast.LENGTH_SHORT).show();
            loadBudgetsAndDisplay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgetsAndDisplay();
    }

    private void loadBudgetsAndDisplay() {
        if (currentUserId == -1) {
            Log.e(TAG, "Không thể tải ngân sách: User ID không hợp lệ.");
            return;
        }

        budgetDAO.open();
        List<Budget> loadedBudgets = budgetDAO.getAllBudgets(currentUserId);

        List<BudgetAdapter.BudgetItem> budgetItems = new ArrayList<>();
        totalAllBudgetsAmount = 0.0;
        totalAllSpentAmount = 0.0;

        for (Budget budget : loadedBudgets) {
            double spentAmount = budgetDAO.getTotalSpentForBudgetCategory(
                    budget.getCategoryId(),
                    budget.getStartDate(),
                    budget.getEndDate(),
                    currentUserId
            );
            budgetItems.add(new BudgetAdapter.BudgetItem(budget, spentAmount));
            totalAllBudgetsAmount += budget.getAmount();
            totalAllSpentAmount += spentAmount;
        }

        // Gán danh sách ngân sách cho biến thành viên
        currentBudgetsList = loadedBudgets;

        budgetAdapter.setBudgetItems(budgetItems);

        if (selectedBudgetForOverview != null) {
            boolean found = false;
            for (Budget budget : loadedBudgets) {
                if (budget.getId() == selectedBudgetForOverview.getId()) {
                    selectedBudgetForOverview = budget;
                    found = true;
                    break;
                }
            }
            if (!found) {
                selectedBudgetForOverview = null;
            }
        }

        // Cập nhật giao diện tổng quan lần đầu
        displayOverview(selectedBudgetForOverview);

        budgetDAO.close();
    }

    private void displayOverview(@Nullable Budget budget) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);

        if (budget == null) {
            tvOverviewTitle.setText("Ngân sách Đang áp dụng");
            // Hiển thị các giá trị tổng đã được tính toán sẵn
            tvRemainingSpendableAmount.setText(currencyFormat.format(totalAllBudgetsAmount - totalAllSpentAmount));
            tvTotalBudgetAmount.setText(String.format(Locale.getDefault(), "%,.0f Tr", totalAllBudgetsAmount / 1_000_000.0));
            tvTotalSpentAmount.setText(String.format(Locale.getDefault(), "%,.0f Ng", totalAllSpentAmount / 1_000.0));
            tvDaysToEndOfMonth.setText(getDaysToEndOfCurrentMonth() + " ngày");
        } else {
            tvOverviewTitle.setText("Ngân sách cho " + budget.getGroupName());

            double spentAmountForThisBudget = 0.0;
            // Tìm số tiền đã chi của ngân sách này trong danh sách items của adapter
            for (BudgetAdapter.BudgetItem item : budgetAdapter.getBudgetItems()) {
                if (item.budget.getId() == budget.getId()) {
                    spentAmountForThisBudget = item.spentAmount;
                    break;
                }
            }

            double remainingAmountForThisBudget = budget.getAmount() - spentAmountForThisBudget;
            int daysRemainingForThisBudget = getDaysBetweenDates(new Date(), parseDate(budget.getEndDate()));

            tvRemainingSpendableAmount.setText(currencyFormat.format(remainingAmountForThisBudget));
            tvTotalBudgetAmount.setText(currencyFormat.format(budget.getAmount()));
            tvTotalSpentAmount.setText(currencyFormat.format(spentAmountForThisBudget));
            tvDaysToEndOfMonth.setText(daysRemainingForThisBudget + " ngày");
        }
    }

    @Override
    public void onEditBudget(Budget budget) {
        Intent intent = new Intent(this, AddBudgetActivity.class);
        intent.putExtra("EXTRA_BUDGET_TO_EDIT", budget);
        startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET);
    }

    @Override
    public void onDeleteBudget(Budget budget) {
        budgetDAO.open();
        boolean success = budgetDAO.deleteBudget(budget.getId(), currentUserId);
        budgetDAO.close();
        if (success) {
            Toast.makeText(this, "Đã xóa ngân sách: " + budget.getGroupName(), Toast.LENGTH_SHORT).show();
            if (selectedBudgetForOverview != null && selectedBudgetForOverview.getId() == budget.getId()) {
                selectedBudgetForOverview = null;
            }
            loadBudgetsAndDisplay();
        } else {
            Toast.makeText(this, "Lỗi khi xóa ngân sách: " + budget.getGroupName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBudgetClick(Budget budget) {
        // Cập nhật giao diện tổng quan với ngân sách được chọn
        displayOverview(budget);
    }

    private int getDaysToEndOfCurrentMonth() {
        Calendar today = Calendar.getInstance();
        Calendar lastDayOfMonth = (Calendar) today.clone();
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastDayOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        lastDayOfMonth.set(Calendar.MINUTE, 59);
        lastDayOfMonth.set(Calendar.SECOND, 59);
        lastDayOfMonth.set(Calendar.MILLISECOND, 999);
        long diffMillis = lastDayOfMonth.getTimeInMillis() - today.getTimeInMillis();
        if (diffMillis < 0) return 0;
        return (int) TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
    }

    private int getDaysBetweenDates(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diffMillis = endDate.getTime() - startDate.getTime();
        if (diffMillis < 0) return 0;
        return (int) TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
    }

    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi phân tích chuỗi ngày: " + dateString + " - " + e.getMessage());
            return null;
        }
    }
}