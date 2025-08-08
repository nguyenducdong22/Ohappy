// SetSavingGoldActivity.java
package com.example.noname;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.models.SavingGoal;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.example.noname.R; // Đảm bảo import đúng R class của bạn

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SetSavingGoldActivity extends AppCompatActivity {

    public static final String EXTRA_NEW_SAVING_GOAL = "extra_new_saving_goal"; // Key để gửi dữ liệu

    private Toolbar toolbar;
    private EditText edtGoalName;
    private EditText edtTargetAmount;
    private LinearLayout layoutSelectTargetDate;
    private TextView tvTargetDate;
    private EditText edtAmountSaved;
    private MaterialButton btnSaveSavingGoal;
    private MaterialButton btnDeleteSavingGoal; // Giữ lại để hiển thị nếu cần, nhưng không có chức năng xóa thực tế

    private Calendar calendar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_savinggold);

        // Ánh xạ View (sử dụng findViewById thay vì View Binding nếu bạn chưa cấu hình)
        toolbar = findViewById(R.id.toolbar_set_saving_gold);
        edtGoalName = findViewById(R.id.edt_goal_name);
        edtTargetAmount = findViewById(R.id.edt_target_amount);
        layoutSelectTargetDate = findViewById(R.id.layout_select_target_date);
        tvTargetDate = findViewById(R.id.tv_target_date);
        edtAmountSaved = findViewById(R.id.edt_amount_saved);
        btnSaveSavingGoal = findViewById(R.id.btn_save_saving_gold_goal);
        btnDeleteSavingGoal = findViewById(R.id.btn_delete_saving_gold_goal);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_set_saving_gold_goal)); // Luôn là "Set Saving Goal" cho Add
        }
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED); // Trả về hủy nếu người dùng nhấn nút Back
            finish();
        });

        // Với yêu cầu chỉ hiển thị và không có database, chúng ta luôn ở chế độ "Thêm mới"
        // và không cần đọc EXTRA_SAVING_GOAL_ID hay hiển thị nút xóa.
        btnDeleteSavingGoal.setVisibility(View.GONE); // Luôn ẩn nút xóa
        setupDefaultNextDate(); // Thiết lập ngày mục tiêu mặc định là hôm nay

        // Thiết lập các Listener
        layoutSelectTargetDate.setOnClickListener(v -> showDatePicker());

        btnSaveSavingGoal.setOnClickListener(v -> saveSavingGoal());

        // Nếu bạn muốn nút Delete có hành vi nào đó (ví dụ, Toast), hãy uncomment nó:
        // btnDeleteSavingGoal.setOnClickListener(v -> Toast.makeText(this, "Delete not implemented in display-only mode.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays a MaterialDatePicker to select the target date.
     */
    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getString(R.string.select_date_title));
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat uiDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvTargetDate.setText(uiDateFormat.format(calendar.getTime()));
        });

        materialDatePicker.show(getSupportFragmentManager(), "SAVING_GOAL_DATE_PICKER");
    }

    /**
     * Sets the default target date to today and displays it in the TextView.
     */
    private void setupDefaultNextDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat uiDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvTargetDate.setText(uiDateFormat.format(calendar.getTime()));
    }

    /**
     * Collects user input, validates it, creates a SavingGoal object,
     * and sends it back to the calling Activity (OverviewSavingGoldActivity).
     */
    // Sửa đổi trong SetSavingGoldActivity.java

    private void saveSavingGoal() {
        String goalName = edtGoalName.getText().toString().trim();
        String targetAmountStr = edtTargetAmount.getText().toString().trim();
        String amountSavedStr = edtAmountSaved.getText().toString().trim();
        String targetDate = tvTargetDate.getText().toString();

        // Kiểm tra dữ liệu nhập liệu
        if (TextUtils.isEmpty(goalName) || TextUtils.isEmpty(targetAmountStr) || TextUtils.isEmpty(amountSavedStr) || targetDate.equals(getString(R.string.text_select_target_date))) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long targetAmount = Long.parseLong(targetAmountStr);
            long amountSaved = Long.parseLong(amountSavedStr);

            // Tạo đối tượng SavingGoal từ dữ liệu nhập vào
            SavingGoal newGoal = new SavingGoal(goalName, targetAmount, amountSaved, targetDate);

            // Tạo Intent để gửi dữ liệu trở lại
            Intent resultIntent = new Intent();
            // Sửa dòng này: Truyền đối tượng SavingGoal trực tiếp vào Intent
            resultIntent.putExtra(EXTRA_NEW_SAVING_GOAL, newGoal);
            setResult(RESULT_OK, resultIntent); // Đặt kết quả thành OK và đính kèm Intent

            Toast.makeText(this, "Saving goal added!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity hiện tại và quay lại Activity gọi nó
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount entered!", Toast.LENGTH_SHORT).show();
        }
    }
}