package com.example.noname.Budget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log; // Added import for Log

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.noname.ChooseGroupActivity;
import com.example.noname.R;
import com.example.noname.database.BudgetDAO;
import com.example.noname.database.CategoryDAO;
import com.example.noname.models.Category;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddBudgetActivity extends AppCompatActivity {

    public static final String EXTRA_BUDGET_TO_EDIT = "EXTRA_BUDGET_TO_EDIT";
    private static final int REQUEST_CODE_CHOOSE_CATEGORY = 101;

    // UI elements
    private Toolbar toolbar;
    private TextView btnCancel;
    private LinearLayout layoutChooseCategory; // Renamed from layoutChooseGroup
    private ImageView ivCategoryIcon;           // Renamed from ivGroupIcon
    private TextView tvCategoryName;            // Renamed from tvGroupName
    private EditText etAmount;
    private LinearLayout layoutDateRange;
    private TextView tvDateRange;
    private SwitchMaterial switchRepeatBudget;
    private Button btnSaveBudget;

    // Data
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    private long currentUserId;
    private Budget budgetToEdit = null;
    private Category selectedCategory = null;

    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM", Locale.US); // Changed locale for display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        Log.d("AddBudgetActivity", "onCreate started.");

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        if (currentUserId == -1) {
            // Error: User not found.
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        budgetDAO = new BudgetDAO(this);
        categoryDAO = new CategoryDAO(this);

        toolbar = findViewById(R.id.toolbar_add_budget);
        btnCancel = findViewById(R.id.btn_cancel_add_budget);
        layoutChooseCategory = findViewById(R.id.layout_choose_group); // Keep ID, but rename variable
        ivCategoryIcon = findViewById(R.id.iv_group_icon);             // Keep ID, but rename variable
        tvCategoryName = findViewById(R.id.tv_group_name);             // Keep ID, but rename variable
        etAmount = findViewById(R.id.et_amount);
        layoutDateRange = findViewById(R.id.layout_date_range);
        tvDateRange = findViewById(R.id.tv_date_range);
        switchRepeatBudget = findViewById(R.id.switch_repeat_budget);
        btnSaveBudget = findViewById(R.id.btn_save_budget);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        btnCancel.setOnClickListener(v -> finish());
        btnSaveBudget.setOnClickListener(v -> saveBudget());
        layoutChooseCategory.setOnClickListener(v -> chooseCategory());
        layoutDateRange.setOnClickListener(v -> showDateRangePicker());

        etAmount.addTextChangedListener(textWatcher);
        switchRepeatBudget.setOnCheckedChangeListener((buttonView, isChecked) -> checkInputs());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_BUDGET_TO_EDIT)) {
            budgetToEdit = (Budget) intent.getSerializableExtra(EXTRA_BUDGET_TO_EDIT);
            updateUiForEditMode();
        } else {
            // Set default date range to the current month
            startDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
            endDateCalendar.set(Calendar.DAY_OF_MONTH, endDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            updateDateRangeDisplay();
        }

        checkInputs();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            checkInputs();
        }
    };

    private void checkInputs() {
        boolean isAmountValid = !etAmount.getText().toString().isEmpty() && Double.parseDouble(etAmount.getText().toString()) > 0;
        boolean isCategorySelected = selectedCategory != null;
        btnSaveBudget.setEnabled(isAmountValid && isCategorySelected);
        btnSaveBudget.setBackgroundResource(isAmountValid && isCategorySelected ? R.color.primary_green : R.color.primary_green_light);
        Log.d("AddBudgetActivity", "checkInputs: isAmountValid = " + isAmountValid + ", isCategorySelected = " + isCategorySelected);
    }

    private void showDateRangePicker() {
        DatePickerDialog startDatePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    startDateCalendar.set(year, month, dayOfMonth);
                    DatePickerDialog endDatePicker = new DatePickerDialog(this,
                            (view2, year2, month2, dayOfMonth2) -> {
                                endDateCalendar.set(year2, month2, dayOfMonth2);
                                if (endDateCalendar.before(startDateCalendar)) {
                                    // End date must be after start date.
                                    Toast.makeText(this, "End date must be after start date.", Toast.LENGTH_SHORT).show();
                                    endDateCalendar.setTime(startDateCalendar.getTime());
                                }
                                updateDateRangeDisplay();
                            },
                            endDateCalendar.get(Calendar.YEAR),
                            endDateCalendar.get(Calendar.MONTH),
                            endDateCalendar.get(Calendar.DAY_OF_MONTH));
                    endDatePicker.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis());
                    endDatePicker.show();
                },
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH));
        startDatePicker.show();
    }

    private void updateDateRangeDisplay() {
        String start = displayDateFormat.format(startDateCalendar.getTime());
        String end = displayDateFormat.format(endDateCalendar.getTime());
        tvDateRange.setText(String.format("%s - %s", start, end));
    }

    private void chooseCategory() {
        Intent intent = new Intent(this, ChooseGroupActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_CATEGORY);
    }

    private void updateUiForEditMode() {
        if (budgetToEdit != null) {
            // "Edit Budget"
            toolbar.setTitle("Edit Budget");
            categoryDAO.open();
            CategoryDAO.CategoryWithIcon categoryWithIcon = categoryDAO.getCategoryByIdWithIcon(budgetToEdit.getCategoryId());
            if (categoryWithIcon != null) {
                selectedCategory = categoryWithIcon.category;
                ivCategoryIcon.setImageResource(categoryWithIcon.iconResId);
                tvCategoryName.setText(categoryWithIcon.category.getName());
                tvCategoryName.setTextColor(getResources().getColor(R.color.text_dark));
            }
            categoryDAO.close();

            etAmount.setText(String.valueOf(budgetToEdit.getAmount()));
            switchRepeatBudget.setChecked(budgetToEdit.isRepeat());

            try {
                startDateCalendar.setTime(dateFormat.parse(budgetToEdit.getStartDate()));
                endDateCalendar.setTime(dateFormat.parse(budgetToEdit.getEndDate()));
                updateDateRangeDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveBudget() {
        if (selectedCategory == null) {
            // "Please choose a category."
            Toast.makeText(this, "Please choose a category.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(etAmount.getText().toString());
        } catch (NumberFormatException e) {
            // "Invalid amount."
            Toast.makeText(this, "Invalid amount.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount <= 0) {
            // "Amount must be greater than 0."
            Toast.makeText(this, "Amount must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        String startDate = dateFormat.format(startDateCalendar.getTime());
        String endDate = dateFormat.format(endDateCalendar.getTime());
        boolean isRecurring = switchRepeatBudget.isChecked();

        budgetDAO.open();
        long result;
        if (budgetToEdit == null) {
            // Adding a new budget
            result = budgetDAO.addBudget(
                    currentUserId,
                    selectedCategory.getId(),
                    amount,
                    startDate,
                    endDate,
                    isRecurring
            );
        } else {
            // Updating an existing budget
            result = budgetDAO.updateBudget(
                    budgetToEdit.getId(),
                    currentUserId,
                    selectedCategory.getId(),
                    amount,
                    startDate,
                    endDate,
                    isRecurring
            );
        }
        budgetDAO.close();

        if (result > 0) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            // "Failed to save budget."
            Toast.makeText(this, "Failed to save budget.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_CATEGORY && resultCode == Activity.RESULT_OK && data != null) {
            long categoryId = data.getLongExtra("selected_category_id", -1);
            String categoryName = data.getStringExtra("selected_group_name");
            int categoryIcon = data.getIntExtra("selected_group_icon", -1);

            Log.d("AddBudgetActivity", "onActivityResult: categoryId = " + categoryId + ", categoryName = " + categoryName + ", categoryIcon = " + categoryIcon);

            if (categoryId != -1) {
                // Create a Category object from the returned data and assign it to selectedCategory
                selectedCategory = new Category(categoryId, categoryName, categoryIcon);
                ivCategoryIcon.setImageResource(selectedCategory.getIconResId());
                tvCategoryName.setText(selectedCategory.getName());
                tvCategoryName.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
                checkInputs();
            }
        }
    }
}