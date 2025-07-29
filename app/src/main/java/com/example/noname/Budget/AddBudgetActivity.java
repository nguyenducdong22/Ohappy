package com.example.noname.Budget; // Đảm bảo package này đúng

import android.app.Activity;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

// Đảm bảo các dòng import này đã đúng đường dẫn sau khi refactor
import com.example.noname.Budget.BudgetDao;
import com.example.noname.Budget.AppDatabase;

public class AddBudgetActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_GROUP = 1;

    private TextView tvGroupName;
    private ImageView ivGroupIcon;
    private EditText etAmount;
    private TextView tvDateRange;
    private SwitchMaterial switchRepeatBudget;

    private String selectedGroupName = "Chọn nhóm";
    private int selectedGroupIconResId = R.drawable.ic_circle;
    private String selectedDateRange = "Tháng này (01/07 - 31/07)";

    private BudgetDao budgetDao; // Khai báo đối tượng DAO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        Toolbar toolbar = findViewById(R.id.toolbar_add_budget);
        setSupportActionBar(toolbar);

        // Khởi tạo BudgetDao từ AppDatabase
        budgetDao = AppDatabase.getDatabase(this).budgetDao();

        tvGroupName = findViewById(R.id.tv_group_name);
        ivGroupIcon = findViewById(R.id.iv_group_icon);
        etAmount = findViewById(R.id.et_amount);
        tvDateRange = findViewById(R.id.tv_date_range);
        switchRepeatBudget = findViewById(R.id.switch_repeat_budget);

        // Hiển thị giá trị ban đầu
        tvGroupName.setText(selectedGroupName);
        ivGroupIcon.setImageResource(selectedGroupIconResId);
        tvDateRange.setText(selectedDateRange);

        // Xử lý nút "Hủy"
        TextView btnCancel = findViewById(R.id.btn_cancel_add_budget);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        // Xử lý click vào "Chọn nhóm"
        LinearLayout layoutChooseGroup = findViewById(R.id.layout_choose_group);
        layoutChooseGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddBudgetActivity.this, ChooseGroupActivity.class);
                intent.putExtra("current_selected_group_name", selectedGroupName);
                intent.putExtra("current_selected_group_icon_res_id", selectedGroupIconResId);
                startActivityForResult(intent, REQUEST_CODE_SELECT_GROUP);
            }
        });

        // Xử lý layout_date_range
        LinearLayout layoutDateRangeClick = findViewById(R.id.layout_date_range);
        layoutDateRangeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddBudgetActivity.this, "Mở Date Picker (chưa triển khai)", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý layout_total (nếu có và cần click)
        LinearLayout layoutTotal = findViewById(R.id.layout_total);
        if (layoutTotal != null) {
            layoutTotal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(AddBudgetActivity.this, "Mở thiết lập Tổng cộng (chưa triển khai)", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Xử lý nút "Lưu"
        Button btnSaveBudget = findViewById(R.id.btn_save_budget);
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountString = etAmount.getText().toString().trim();
                double amount = 0.0;
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

                if (selectedGroupName.equals("Chọn nhóm")) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng chọn một nhóm", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean repeatBudget = switchRepeatBudget.isChecked();

                // Tạo đối tượng Budget mới
                Budget newBudget = new Budget(selectedGroupName, selectedGroupIconResId, amount, selectedDateRange, repeatBudget);

                // LƯU DANH SÁCH NGÂN SÁCH VÀO ROOM DATABASE BẰNG DAO
                budgetDao.insert(newBudget);

                Toast.makeText(AddBudgetActivity.this, "Đã lưu ngân sách cho: " + selectedGroupName, Toast.LENGTH_LONG).show();

                // Trả về RESULT_OK cho BudgetOverviewActivity
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_GROUP) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                selectedGroupName = data.getStringExtra("selected_group_name");
                selectedGroupIconResId = data.getIntExtra("selected_group_icon_res_id", R.drawable.ic_circle);

                tvGroupName.setText(selectedGroupName);
                ivGroupIcon.setImageResource(selectedGroupIconResId);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Người dùng đã hủy chọn nhóm, không thay đổi giá trị hiện tại
            }
        }
    }

    // Phần saveBudgetToSharedPreferences đã được loại bỏ như đã thống nhất
}