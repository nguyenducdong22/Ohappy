package com.example.noname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import com.example.noname.database.CategoryDAO;
import com.example.noname.database.RecurringExpenseDAO;
import com.example.noname.models.Category;
import com.example.noname.models.RecurringExpense;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddEditRecurringExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_RECURRING_EXPENSE_ID = "EXTRA_RECURRING_EXPENSE_ID";
    private static final int SELECT_RECURRING_CATEGORY_REQUEST_CODE = 1001;

    private Toolbar toolbar;
    private EditText edtRecurringName;
    private EditText edtRecurringAmount;
    private LinearLayout layoutSelectRecurringCategory;
    private ImageView imgRecurringCategoryIcon;
    private TextView tvRecurringCategoryName;

    private RadioGroup radioGroupFrequency;
    private MaterialRadioButton radioMonthly, radioWeekly, radioYearly;
    private LinearLayout layoutSelectNextDate;
    private TextView tvRecurringNextDate;
    private SwitchMaterial switchRecurringStatus;
    private MaterialButton btnDeleteRecurringExpense;
    private MaterialButton btnSaveRecurringExpense;

    private RecurringExpenseDAO recurringExpenseDAO;
    private long currentUserId;
    private RecurringExpense expenseToEdit = null;
    private Category selectedCategory = null;

    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat uiDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recurring_expense);

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recurringExpenseDAO = new RecurringExpenseDAO(this);

        toolbar = findViewById(R.id.toolbar_add_edit_recurring);
        edtRecurringName = findViewById(R.id.edt_recurring_name);
        edtRecurringAmount = findViewById(R.id.edt_recurring_amount);
        layoutSelectRecurringCategory = findViewById(R.id.layout_select_recurring_category);
        imgRecurringCategoryIcon = findViewById(R.id.img_recurring_category_icon);
        tvRecurringCategoryName = findViewById(R.id.tv_recurring_category_name);

        radioGroupFrequency = findViewById(R.id.radio_group_frequency);
        radioMonthly = findViewById(R.id.radio_monthly);
        radioWeekly = findViewById(R.id.radio_weekly);
        radioYearly = findViewById(R.id.radio_yearly);
        layoutSelectNextDate = findViewById(R.id.layout_select_next_date);
        tvRecurringNextDate = findViewById(R.id.tv_recurring_next_date);
        switchRecurringStatus = findViewById(R.id.switch_recurring_status);
        btnDeleteRecurringExpense = findViewById(R.id.btn_delete_recurring_expense);
        btnSaveRecurringExpense = findViewById(R.id.btn_save_recurring_expense);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        long expenseId = getIntent().getLongExtra(EXTRA_RECURRING_EXPENSE_ID, -1);
        if (expenseId != -1) {
            recurringExpenseDAO.open();
            expenseToEdit = recurringExpenseDAO.getRecurringExpenseById(expenseId, currentUserId);
            recurringExpenseDAO.close();

            if (expenseToEdit != null) {
                updateUiForEditMode(expenseToEdit);
            } else {
                Toast.makeText(this, "Không tìm thấy khoản chi tiêu định kỳ.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            toolbar.setTitle("Thêm Chi Tiêu Định Kỳ");
            btnDeleteRecurringExpense.setVisibility(View.GONE);
            setupDefaultNextDate();
        }

        layoutSelectRecurringCategory.setOnClickListener(v -> {
            Intent intent = new Intent(AddEditRecurringExpenseActivity.this, ChooseGroupActivity.class);
            startActivityForResult(intent, SELECT_RECURRING_CATEGORY_REQUEST_CODE);
        });

        layoutSelectNextDate.setOnClickListener(v -> showDatePicker());

        btnSaveRecurringExpense.setOnClickListener(v -> saveOrUpdateRecurringExpense());

        if (expenseToEdit != null) {
            btnDeleteRecurringExpense.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        radioMonthly.setChecked(true);
    }

    private void updateUiForEditMode(RecurringExpense expense) {
        toolbar.setTitle("Sửa Chi Tiêu Định Kỳ");
        btnSaveRecurringExpense.setText("Cập nhật");
        btnDeleteRecurringExpense.setVisibility(View.VISIBLE);

        edtRecurringName.setText(expense.getName());
        edtRecurringAmount.setText(String.valueOf(expense.getAmount()));

        CategoryDAO categoryDAO = new CategoryDAO(this);
        categoryDAO.open();
        CategoryDAO.CategoryWithIcon categoryInfo = categoryDAO.getCategoryByIdWithIcon(expense.getCategoryId());
        if (categoryInfo != null) {
            selectedCategory = categoryInfo.category;
            tvRecurringCategoryName.setText(selectedCategory.getName());
            imgRecurringCategoryIcon.setImageResource(categoryInfo.iconResId);
        }
        categoryDAO.close();

        String frequency = expense.getFrequency();
        if ("Hàng tháng".equals(frequency)) radioMonthly.setChecked(true);
        else if ("Hàng tuần".equals(frequency)) radioWeekly.setChecked(true);
        else if ("Hàng năm".equals(frequency)) radioYearly.setChecked(true);

        switchRecurringStatus.setChecked(expense.isActive());

        try {
            Date startDate = dbDateFormat.parse(expense.getStartDate());
            tvRecurringNextDate.setText(uiDateFormat.format(startDate));
        } catch (ParseException e) {
            Log.e("AddEditRecurringExpense", "Lỗi phân tích ngày: " + e.getMessage());
        }
    }

    private void setupDefaultNextDate() {
        Calendar calendar = Calendar.getInstance();
        tvRecurringNextDate.setText(uiDateFormat.format(calendar.getTime()));
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Chọn Ngày Tiếp Theo");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            tvRecurringNextDate.setText(uiDateFormat.format(calendar.getTime()));
        });

        materialDatePicker.show(getSupportFragmentManager(), "RECURRING_DATE_PICKER");
    }

    private void saveOrUpdateRecurringExpense() {
        String name = edtRecurringName.getText().toString().trim();
        String amountStr = edtRecurringAmount.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr) || selectedCategory == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String frequency = "";
        int frequencyValue = 1; // Giả định giá trị mặc định là 1
        if (radioMonthly.isChecked()) frequency = "Hàng tháng";
        else if (radioWeekly.isChecked()) frequency = "Hàng tuần";
        else if (radioYearly.isChecked()) frequency = "Hàng năm";

        boolean isActive = switchRecurringStatus.isChecked();

        long accountId = 1;
        String transactionType = "Expense";
        String endDate = null;
        String lastGeneratedDate = null;

        Date startDate = null;
        try {
            startDate = uiDateFormat.parse(tvRecurringNextDate.getText().toString());
        } catch (ParseException e) {
            Log.e("AddEditRecurringExpense", "Lỗi phân tích ngày để lưu: " + e.getMessage());
            Toast.makeText(this, "Ngày không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }
        String startDateDb = dbDateFormat.format(startDate);

        recurringExpenseDAO.open();
        long result;
        if (expenseToEdit == null) {
            result = recurringExpenseDAO.addRecurringExpense(currentUserId, accountId, selectedCategory.getId(), name, amount, transactionType, frequency, frequencyValue, startDateDb, endDate, lastGeneratedDate, isActive);
        } else {
            result = recurringExpenseDAO.updateRecurringExpense(expenseToEdit.getId(), currentUserId, accountId, selectedCategory.getId(), name, amount, transactionType, frequency, frequencyValue, startDateDb, endDate, lastGeneratedDate, isActive);
        }
        recurringExpenseDAO.close();

        if (result > 0) {
            Toast.makeText(this, (expenseToEdit == null ? "Đã thêm mới" : "Đã cập nhật") + " thành công!", Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Thao tác thất bại.", Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu định kỳ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    recurringExpenseDAO.open();
                    boolean success = recurringExpenseDAO.deleteRecurringExpense(expenseToEdit.getId(), currentUserId);
                    recurringExpenseDAO.close();
                    if (success) {
                        Toast.makeText(AddEditRecurringExpenseActivity.this, "Đã xóa thành công!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEditRecurringExpenseActivity.this, "Lỗi khi xóa.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_RECURRING_CATEGORY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            long categoryId = data.getLongExtra("selected_category_id", -1);
            String categoryName = data.getStringExtra("selected_group_name");
            int iconResId = data.getIntExtra("selected_group_icon", -1);

            if (categoryId != -1) {
                selectedCategory = new Category(categoryId, categoryName, iconResId);
                tvRecurringCategoryName.setText(selectedCategory.getName());
                imgRecurringCategoryIcon.setImageResource(selectedCategory.getIconResId());
                imgRecurringCategoryIcon.setColorFilter(ContextCompat.getColor(this, R.color.primary_green_dark));
                // checkInputs(); // Cần gọi để cập nhật trạng thái nút lưu
            }
        }
    }
}