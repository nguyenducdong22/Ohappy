package com.example.noname;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.account.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AddEditRecurringExpenseActivity extends BaseActivity {

    private Toolbar toolbar;
    private EditText edtRecurringName;
    private EditText edtRecurringAmount;
    private EditText edtRecurringCategoryName;
    private RadioGroup radioGroupFrequency;
    private MaterialRadioButton radioMonthly, radioWeekly, radioYearly;
    private TextView tvRecurringNextDate;
    private SwitchMaterial switchRecurringStatus;
    private MaterialButton btnDeleteRecurringExpense;
    private MaterialButton btnSaveRecurringExpense;

    private long currentExpenseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recurring_expense);

        initializeViews();
        setupToolbar();
        handleIntent();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_add_edit_recurring);
        edtRecurringName = findViewById(R.id.edt_recurring_name);
        edtRecurringAmount = findViewById(R.id.edt_recurring_amount);
        edtRecurringCategoryName = findViewById(R.id.edt_recurring_category_name);
        radioGroupFrequency = findViewById(R.id.radio_group_frequency);
        radioMonthly = findViewById(R.id.radio_monthly);
        radioWeekly = findViewById(R.id.radio_weekly);
        radioYearly = findViewById(R.id.radio_yearly);
        tvRecurringNextDate = findViewById(R.id.tv_recurring_next_date);
        switchRecurringStatus = findViewById(R.id.switch_recurring_status);
        btnDeleteRecurringExpense = findViewById(R.id.btn_delete_recurring_expense);
        btnSaveRecurringExpense = findViewById(R.id.btn_save_recurring_expense);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    private void handleIntent() {
        if (getIntent().hasExtra("RECURRING_EXPENSE_ID")) {
            currentExpenseId = getIntent().getLongExtra("RECURRING_EXPENSE_ID", -1);
            toolbar.setTitle(getString(R.string.edit_recurring_expense_title));
            btnSaveRecurringExpense.setText(getString(R.string.update));
            btnDeleteRecurringExpense.setVisibility(View.VISIBLE);
            populateFields();
        } else {
            toolbar.setTitle(getString(R.string.add_recurring_expense_title));
            btnDeleteRecurringExpense.setVisibility(View.GONE);
            setupDefaultNextDate();
            radioMonthly.setChecked(true);
        }
    }

    private void populateFields() {
        // Đổ dữ liệu mẫu vào các trường khi ở chế độ chỉnh sửa
        edtRecurringName.setText(getIntent().getStringExtra("RECURRING_EXPENSE_NAME_DEMO"));
        edtRecurringAmount.setText(String.valueOf(getIntent().getDoubleExtra("RECURRING_EXPENSE_AMOUNT_DEMO", 0.0)));
        edtRecurringCategoryName.setText(getIntent().getStringExtra("RECURRING_EXPENSE_CATEGORY_NAME_DEMO"));
        tvRecurringNextDate.setText(getIntent().getStringExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO"));

        String frequency = getIntent().getStringExtra("RECURRING_EXPENSE_FREQUENCY_DEMO");
        if (getString(R.string.frequency_monthly).equals(frequency)) radioMonthly.setChecked(true);
        else if (getString(R.string.frequency_weekly).equals(frequency)) radioWeekly.setChecked(true);
        else if (getString(R.string.frequency_yearly).equals(frequency)) radioYearly.setChecked(true);

        boolean isActive = "active".equals(getIntent().getStringExtra("RECURRING_EXPENSE_STATUS_DEMO"));
        switchRecurringStatus.setChecked(isActive);
    }

    private void setupListeners() {
        findViewById(R.id.layout_select_next_date).setOnClickListener(v -> showDatePicker());
        btnSaveRecurringExpense.setOnClickListener(v -> saveOrUpdateRecurringExpense());
        btnDeleteRecurringExpense.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void setupDefaultNextDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvRecurringNextDate.setText(sdf.format(calendar.getTime()));
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getString(R.string.select_next_date_title));
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

    private void saveOrUpdateRecurringExpense() {
        String name = edtRecurringName.getText().toString().trim();
        String amountStr = edtRecurringAmount.getText().toString().trim();
        String categoryName = edtRecurringCategoryName.getText().toString().trim();
        String frequency = "";
        if (radioMonthly.isChecked()) frequency = getString(R.string.frequency_monthly);
        else if (radioWeekly.isChecked()) frequency = getString(R.string.frequency_weekly);
        else if (radioYearly.isChecked()) frequency = getString(R.string.frequency_yearly);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(categoryName)) {
            Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentExpenseId == -1) {
            String message = getString(R.string.toast_add_new_success, name, amountStr, categoryName);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            String message = getString(R.string.toast_update_success, (int) currentExpenseId, name, categoryName);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_confirmation_title))
                .setMessage(getString(R.string.delete_confirmation_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    String message = getString(R.string.toast_deleted_demo, (int) currentExpenseId);
                    Toast.makeText(AddEditRecurringExpenseActivity.this, message, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}