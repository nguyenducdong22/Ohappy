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

    // Khai báo các View từ layout
    private Toolbar toolbar;
    private EditText edtRecurringName;
    private EditText edtRecurringAmount;
    // Đã bỏ LinearLayout layoutSelectRecurringCategory;
    // Đã bỏ ImageView imgRecurringCategoryIcon;
    private EditText edtRecurringCategoryName; // Bây giờ là EditText để nhập tên nhóm

    private RadioGroup radioGroupFrequency;
    private MaterialRadioButton radioMonthly, radioWeekly, radioYearly;
    private LinearLayout layoutSelectNextDate;
    private TextView tvRecurringNextDate;
    private SwitchMaterial switchRecurringStatus;
    private MaterialButton btnDeleteRecurringExpense;
    private MaterialButton btnSaveRecurringExpense;

    private long currentExpenseId = -1; // -1 nếu là thêm mới, ID nếu là chỉnh sửa
    // Không cần lưu selectedCategoryIconResId và selectedCategoryTintColorResId nữa
    // private int selectedCategoryIconResId = R.drawable.ic_category;
    // private int selectedCategoryTintColorResId = R.color.primary_green_dark;

    // Request code cho việc chọn nhóm (Không cần nữa vì không có Activity chọn nhóm)
    // private static final int SELECT_RECURRING_CATEGORY_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recurring_expense);

        // Ánh xạ View
        toolbar = findViewById(R.id.toolbar_add_edit_recurring);
        edtRecurringName = findViewById(R.id.edt_recurring_name);
        edtRecurringAmount = findViewById(R.id.edt_recurring_amount);
        // Ánh xạ EditText tên nhóm
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
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        // Kiểm tra nếu là chế độ chỉnh sửa
        if (getIntent().hasExtra("RECURRING_EXPENSE_ID")) {
            currentExpenseId = getIntent().getLongExtra("RECURRING_EXPENSE_ID", -1);
            toolbar.setTitle("Sửa Chi Tiêu Định Kỳ");
            btnSaveRecurringExpense.setText("Cập nhật");
            btnDeleteRecurringExpense.setVisibility(View.VISIBLE);
            // Đổ dữ liệu mẫu vào các trường khi ở chế độ chỉnh sửa
            edtRecurringName.setText(getIntent().getStringExtra("RECURRING_EXPENSE_NAME_DEMO"));
            edtRecurringAmount.setText(String.valueOf(getIntent().getDoubleExtra("RECURRING_EXPENSE_AMOUNT_DEMO", 0.0)));
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
            setupDefaultNextDate();
        }

        // BỎ Listener cho chọn nhóm
        // layoutSelectRecurringCategory.setOnClickListener(v -> { ... });

        layoutSelectNextDate.setOnClickListener(v -> {
            showDatePicker();
        });

        btnSaveRecurringExpense.setOnClickListener(v -> {
            saveOrUpdateRecurringExpense();
        });

        btnDeleteRecurringExpense.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        radioMonthly.setChecked(true);
    }

    // --- Phương thức hỗ trợ ---
    private void setupDefaultNextDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvRecurringNextDate.setText(sdf.format(calendar.getTime()));
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Chọn Ngày Tiếp Theo");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvRecurringNextDate.setText(sdf.format(calendar.getTime()));
        });

        materialDatePicker.show(getSupportFragmentManager(), "RECURRING_DATE_PICKER");
    }

    // --- Logic Save/Update (Hiện tại chỉ là Toast/Dummy, sau này sẽ dùng DB) ---
    private void saveOrUpdateRecurringExpense() {
        String name = edtRecurringName.getText().toString().trim();
        String amountStr = edtRecurringAmount.getText().toString().trim();
        String categoryName = edtRecurringCategoryName.getText().toString().trim(); // Lấy từ EditText tên nhóm
        String nextDate = tvRecurringNextDate.getText().toString();
        String frequency = "";
        if (radioMonthly.isChecked()) frequency = "Hàng tháng";
        else if (radioWeekly.isChecked()) frequency = "Hàng tuần";
        else if (radioYearly.isChecked()) frequency = "Hàng năm";
        String status = switchRecurringStatus.isChecked() ? "active" : "paused";

        // Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(categoryName) || TextUtils.isEmpty(nextDate)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        // Gán icon mặc định R.drawable.ic_category (hoặc một icon khác phù hợp với tên nhóm nếu bạn có logic đó)
        int iconResId = R.drawable.ic_category;
        int iconTintColorResId = R.color.primary_green_dark; // Màu tint mặc định

        if (currentExpenseId == -1) {
            Toast.makeText(this, "Thêm mới: " + name + " - " + amount + " - Nhóm: " + categoryName, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Cập nhật: ID " + currentExpenseId + " - " + name + " - Nhóm: " + categoryName, Toast.LENGTH_LONG).show();
        }
        setResult(Activity.RESULT_OK); // Đặt kết quả OK để RecurringExpensesActivity làm mới
        finish();
    }

    // --- Logic Delete (Hiện tại chỉ là Toast/Dummy, sau này sẽ dùng DB) ---
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu định kỳ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Toast.makeText(AddEditRecurringExpenseActivity.this, "Đã xóa (mẫu) ID: " + currentExpenseId, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // BỎ onActivityResult cho chọn nhóm vì giờ là EditText
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if (requestCode == SELECT_RECURRING_CATEGORY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
        // ... (code cũ)
        // }
    }
    */
}