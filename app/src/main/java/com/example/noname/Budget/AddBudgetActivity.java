package com.example.noname.Budget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.example.noname.R;
import com.example.noname.database.DatabaseHelper; // Needed for getCategoryId.
import com.example.noname.database.BudgetDAO;     // New: For budget data operations.

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AddBudgetActivity allows users to create a new budget entry.
 * It handles input for budget amount, category selection, date range, and recurrence.
 * It uses BudgetDAO to save the new budget to the database.
 */
public class AddBudgetActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_GROUP = 1; // Request code for selecting a group/category.

    // UI elements
    private TextView tvGroupName;
    private ImageView ivGroupIcon;
    private EditText etAmount;
    private TextView tvDateRange;
    private SwitchMaterial switchRepeatBudget;

    // Data for the new budget
    private String selectedGroupName = "Chọn nhóm"; // Default display for category.
    private int selectedGroupIconResId = R.drawable.ic_circle; // Default icon.
    private String selectedDateRange = "Tháng này (01/07 - 31/07)"; // Placeholder for date range.
    private long selectedCategoryId = -1; // Stores the database ID of the chosen category.

    private DatabaseHelper dbHelper; // To get Category ID from its name.
    private BudgetDAO budgetDAO;     // To save budget data to the database.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        // Initialize DAOs.
        dbHelper = new DatabaseHelper(this);
        budgetDAO = new BudgetDAO(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_add_budget);
        setSupportActionBar(toolbar);

        // Initialize UI components
        tvGroupName = findViewById(R.id.tv_group_name);
        ivGroupIcon = findViewById(R.id.iv_group_icon);
        etAmount = findViewById(R.id.et_amount);
        tvDateRange = findViewById(R.id.tv_date_range);
        switchRepeatBudget = findViewById(R.id.switch_repeat_budget);

        // Set initial UI display
        tvGroupName.setText(selectedGroupName);
        ivGroupIcon.setImageResource(selectedGroupIconResId);
        tvDateRange.setText(selectedDateRange);

        // Set up "Cancel" button click listener
        TextView btnCancel = findViewById(R.id.btn_cancel_add_budget);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED); // Inform calling activity that operation was cancelled.
                finish(); // Close this activity.
            }
        });

        // Set up "Choose Group" (Category) click listener
        LinearLayout layoutChooseGroup = findViewById(R.id.layout_choose_group);
        layoutChooseGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddBudgetActivity.this, ChooseGroupActivity.class);
                // Pass current selection to ChooseGroupActivity.
                intent.putExtra("current_selected_group_name", selectedGroupName);
                intent.putExtra("current_selected_group_icon_res_id", selectedGroupIconResId);
                startActivityForResult(intent, REQUEST_CODE_SELECT_GROUP); // Start activity and wait for result.
            }
        });

        // Set up "Date Range" click listener (Placeholder for DatePicker)
        LinearLayout layoutDateRangeClick = findViewById(R.id.layout_date_range);
        layoutDateRangeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddBudgetActivity.this, "Mở Date Picker (chưa triển khai)", Toast.LENGTH_SHORT).show();
                // TODO: Implement a DatePickerDialog or navigate to a custom date range selection UI.
                // Upon selection, update `selectedDateRange` and `tvDateRange`.
            }
        });

        // Set up "Total" layout click listener (if needed)
        LinearLayout layoutTotal = findViewById(R.id.layout_total);
        if (layoutTotal != null) {
            layoutTotal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(AddBudgetActivity.this, "Mở thiết lập Tổng cộng (chưa triển khai)", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Set up "Save" button click listener
        Button btnSaveBudget = findViewById(R.id.btn_save_budget);
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountString = etAmount.getText().toString().trim();
                double amount = 0.0;

                // Input validation for amount.
                if (amountString.isEmpty() || amountString.equals("0")) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    amount = Double.parseDouble(amountString);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddBudgetActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Input validation for category selection.
                if (selectedGroupName.equals("Chọn nhóm") || selectedCategoryId == -1) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng chọn một nhóm hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Assume a fixed userId for now. In a real app, this would come from the logged-in user session.
                long userId = 1;
                boolean repeatBudget = switchRepeatBudget.isChecked();

                // Placeholder for start/end dates. Replace with actual selection logic from a DatePicker.
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String startDate = dateFormat.format(new Date());
                String endDate = dateFormat.format(new Date()); // For simplicity, assume same day if no range selected.

                // Save the budget to the database using BudgetDAO.
                // Always open and close the database connection explicitly.
                budgetDAO.open();
                boolean success = budgetDAO.addBudget(userId, selectedCategoryId, amount, startDate, endDate, repeatBudget);
                budgetDAO.close();

                if (success) {
                    Toast.makeText(AddBudgetActivity.this, "Đã lưu ngân sách cho: " + selectedGroupName, Toast.LENGTH_LONG).show();
                    setResult(Activity.RESULT_OK); // Inform calling activity of success.
                    finish(); // Close this activity.
                } else {
                    Toast.makeText(AddBudgetActivity.this, "Lỗi khi lưu ngân sách. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Handles the result returned from other activities, specifically ChooseGroupActivity.
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode The integer result code returned by the child activity.
     * @param data An Intent, which can carry the result data back to the parent activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_GROUP) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Retrieve selected group name and icon from the result Intent.
                selectedGroupName = data.getStringExtra("selected_group_name");
                selectedGroupIconResId = data.getIntExtra("selected_group_icon", R.drawable.ic_circle);

                // Get the database ID for the selected category using DatabaseHelper.
                // Assuming budget categories are always "Expense" type.
                selectedCategoryId = dbHelper.getCategoryId(selectedGroupName, "Expense");
                if (selectedCategoryId == -1) {
                    Toast.makeText(this, "Lỗi: Không tìm thấy ID danh mục cho '" + selectedGroupName + "'.", Toast.LENGTH_SHORT).show();
                    // This scenario should ideally not happen if default categories are correctly inserted.
                    // Or, if user-added categories are handled, ensure they are in DB.
                }

                // Update UI with selected group details.
                tvGroupName.setText(selectedGroupName);
                ivGroupIcon.setImageResource(selectedGroupIconResId);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the group selection, no changes are made.
                Toast.makeText(this, "Chọn nhóm đã bị hủy.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}