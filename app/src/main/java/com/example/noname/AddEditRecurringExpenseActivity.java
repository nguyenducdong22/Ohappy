package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AddEditRecurringExpenseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText edtRecurringName;
    private EditText edtRecurringAmount;
    private EditText edtRecurringCategoryName; // EditText để nhập tên nhóm

    private RadioGroup radioGroupFrequency;
    private MaterialRadioButton radioMonthly, radioWeekly, radioYearly;
    private LinearLayout layoutSelectNextDate;
    private TextView tvRecurringNextDate;
    private SwitchMaterial switchRecurringStatus;
    private MaterialButton btnDeleteRecurringExpense;
    private MaterialButton btnSaveRecurringExpense;

    private long currentExpenseId = -1; // -1 nếu là thêm mới, ID nếu là chỉnh sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recurring_expense);

        // Ánh xạ View
        toolbar = findViewById(R.id.toolbar_add_edit_recurring);
        edtRecurringName = findViewById(R.id.edt_recurring_name);
        edtRecurringAmount = findViewById(R.id.edt_recurring_amount);
        edtRecurringCategoryName = findViewById(R.id.edt_recurring_category_name);

        radioGroupFrequency = findViewById(R.id.radio_group_frequency);
        radioMonthly = findViewById(R.id.radio_monthly);
        radioWeekly = findViewById(R.id.radio_weekly);
        radioYearly = findViewById(R.id.radio_yearly);
        layoutSelectNextDate = findViewById(R.id.layout_select_next_date);
        tvRecurringNextDate = findViewById(R.id.tv_recurring_next_date);
        switchRecurringStatus = findViewById(R.id.switch_recurring_status);
        btnDeleteRecurringExpense = findViewById(R.id.btn_delete_recurring_expense);
        btnSaveRecurringExpense = findViewById(R.id.btn_save_recurring_expense);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED); // Nếu người dùng nhấn nút quay lại, hủy bỏ
            finish();
        });

        // Kiểm tra nếu là chế độ chỉnh sửa (EDIT)
        // Lấy dữ liệu từ Intent đã truyền từ RecurringExpensesActivity
        if (getIntent().hasExtra("RECURRING_EXPENSE_ID")) {
            currentExpenseId = getIntent().getLongExtra("RECURRING_EXPENSE_ID", -1);
            toolbar.setTitle("Sửa Chi Tiêu Định Kỳ");
            btnSaveRecurringExpense.setText("Cập nhật");
            btnDeleteRecurringExpense.setVisibility(View.VISIBLE);

            // Đổ dữ liệu từ Intent vào các trường chỉnh sửa
            edtRecurringName.setText(getIntent().getStringExtra("RECURRING_EXPENSE_NAME_DEMO"));
            edtRecurringAmount.setText(String.valueOf(getIntent().getDoubleExtra("RECURRING_EXPENSE_AMOUNT_DEMO", 0.0)));
            // Lấy tên nhóm từ intent (đã được truyền từ RecurringExpensesActivity)
            edtRecurringCategoryName.setText(getIntent().getStringExtra("RECURRING_EXPENSE_CATEGORY_NAME_DEMO"));
            tvRecurringNextDate.setText(getIntent().getStringExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO"));

            String frequency = getIntent().getStringExtra("RECURRING_EXPENSE_FREQUENCY_DEMO");
            if ("Hàng tháng".equals(frequency)) radioMonthly.setChecked(true);
            else if ("Hàng tuần".equals(frequency)) radioWeekly.setChecked(true);
            else if ("Hàng năm".equals(frequency)) radioYearly.setChecked(true);

            boolean isActive = "active".equals(getIntent().getStringExtra("RECURRING_EXPENSE_STATUS_DEMO"));
            switchRecurringStatus.setChecked(isActive);

        } else {
            toolbar.setTitle("Thêm Chi Tiêu Định Kỳ");
            btnDeleteRecurringExpense.setVisibility(View.GONE);
            setupDefaultNextDate(); // Đặt ngày mặc định cho chế độ thêm mới
        }

        // Listener cho chọn ngày tiếp theo
        layoutSelectNextDate.setOnClickListener(v -> {
            showDatePicker();
        });

        // Listener cho nút Lưu/Cập nhật
        btnSaveRecurringExpense.setOnClickListener(v -> {
            saveOrUpdateRecurringExpense();
        });

        // Listener cho nút Xóa
        btnDeleteRecurringExpense.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        // Đặt mặc định chọn "Hàng tháng" khi tạo mới
        radioMonthly.setChecked(true);
    }

    // --- Phương thức hỗ trợ ---

    /**
     * Thiết lập ngày tiếp theo mặc định là ngày hiện tại.
     */
    private void setupDefaultNextDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvRecurringNextDate.setText(sdf.format(calendar.getTime()));
    }

    /**
     * Hiển thị DatePicker để chọn ngày.
     */
    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Chọn Ngày Tiếp Theo");
        // Nếu đã có ngày, đặt ngày đó làm mặc định. Nếu không, đặt ngày hiện tại.
        try {
            if (!TextUtils.isEmpty(tvRecurringNextDate.getText())) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(sdf.parse(tvRecurringNextDate.getText().toString()));
                builder.setSelection(selectedCal.getTimeInMillis());
            } else {
                builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
            }
        } catch (java.text.ParseException e) {
            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        }


        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvRecurringNextDate.setText(sdf.format(calendar.getTime()));
        });

        materialDatePicker.show(getSupportFragmentManager(), "RECURRING_DATE_PICKER");
    }

    /**
     * Thu thập dữ liệu từ UI và truyền nó về RecurringExpensesActivity.
     */
    private void saveOrUpdateRecurringExpense() {
        String name = edtRecurringName.getText().toString().trim();
        String amountStr = edtRecurringAmount.getText().toString().trim();
        String categoryName = edtRecurringCategoryName.getText().toString().trim();
        String nextDate = tvRecurringNextDate.getText().toString();
        String frequency = "";
        if (radioMonthly.isChecked()) frequency = "Hàng tháng";
        else if (radioWeekly.isChecked()) frequency = "Hàng tuần";
        else if (radioYearly.isChecked()) frequency = "Hàng năm";
        String status = switchRecurringStatus.isChecked() ? "active" : "paused";
        String type = "Expense"; // Giả định là Expense, nếu bạn có lựa chọn thu/chi, cần lấy từ UI

        // Validation cơ bản
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(categoryName) || TextUtils.isEmpty(nextDate)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy Resource ID của icon và màu sắc
        // Vì bạn không có ChooseCategoryActivity, chúng ta sẽ dùng các ID mặc định
        // hoặc các ID bạn đã định nghĩa nếu có ánh xạ tên nhóm -> icon/màu
        int iconResId = R.drawable.ic_category; // Icon mặc định
        int iconTintColorResId = R.color.primary_green_dark; // Màu mặc định

        // --- Tạo Intent để trả về dữ liệu ---
        Intent resultIntent = new Intent();
        resultIntent.putExtra("RECURRING_EXPENSE_ID", currentExpenseId); // Truyền ID nếu là sửa
        resultIntent.putExtra("RECURRING_EXPENSE_NAME_DEMO", name); // <-- Đã đổi để truyền biến 'name'
        resultIntent.putExtra("RECURRING_EXPENSE_AMOUNT_DEMO", amount); // <-- Đã đổi để truyền biến 'amount'
        resultIntent.putExtra("RECURRING_EXPENSE_CATEGORY_NAME_DEMO", categoryName); // <-- Truyền tên nhóm
        resultIntent.putExtra("RECURRING_EXPENSE_FREQUENCY_DEMO", frequency);
        resultIntent.putExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO", nextDate);
        resultIntent.putExtra("RECURRING_EXPENSE_ICON_RES_ID_DEMO", iconResId);
        resultIntent.putExtra("RECURRING_EXPENSE_ICON_TINT_COLOR_RES_ID_DEMO", iconTintColorResId);
        resultIntent.putExtra("RECURRING_EXPENSE_STATUS_DEMO", status);
        resultIntent.putExtra("RECURRING_EXPENSE_TYPE_DEMO", type);

        // Đặt kết quả và kết thúc Activity
        setResult(Activity.RESULT_OK, resultIntent); // Đặt kết quả OK và truyền Intent
        finish();
    }

    /**
     * Hiển thị hộp thoại xác nhận xóa.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu định kỳ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Để xử lý xóa, bạn sẽ cần truyền tín hiệu về RecurringExpensesActivity
                    // Ví dụ: set result là OK và thêm extra "ACTION_DELETE"
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("ACTION_DELETE", true);
                    resultIntent.putExtra("RECURRING_EXPENSE_ID", currentExpenseId);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}