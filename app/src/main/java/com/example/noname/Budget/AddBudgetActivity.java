package com.example.noname.Budget;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.R;

public class AddBudgetActivity extends AppCompatActivity {

    private EditText etGroup, etAmount;
    private RadioGroup rgPeriod;
    private CheckBox cbRecurring;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget); // Đảm bảo tên layout của bạn là activity_add_budget

        // Ẩn ActionBar nếu không muốn hiển thị
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ánh xạ các View từ layout
        etGroup = findViewById(R.id.etGroup);
        etAmount = findViewById(R.id.etAmount);
        rgPeriod = findViewById(R.id.rgPeriod);
        cbRecurring = findViewById(R.id.cbRecurring);
        btnSave = findViewById(R.id.btnSave);

        // Xử lý sự kiện click cho nút "Lưu và Xem chi tiết"
        if (btnSave != null) { // Đảm bảo nút tồn tại trong layout
            btnSave.setOnClickListener(v -> {
                // Lấy dữ liệu từ các trường nhập liệu
                String groupName = etGroup.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                boolean isRecurring = cbRecurring.isChecked();
                int selectedPeriodId = rgPeriod.getCheckedRadioButtonId();

                // Kiểm tra dữ liệu đầu vào (ví dụ đơn giản)
                if (groupName.isEmpty()) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng nhập tên nhóm ngân sách", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (amountStr.isEmpty()) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chuyển đổi số tiền sang kiểu số
                double amount = 0;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddBudgetActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Tại đây, bạn sẽ thực hiện logic để lưu dữ liệu ngân sách vào cơ sở dữ liệu hoặc Shared Preferences
                // Ví dụ: DatabaseManager.saveBudget(groupName, amount, selectedPeriodId, isRecurring);

                Toast.makeText(AddBudgetActivity.this, "Đã lưu ngân sách: " + groupName + " - " + amount + " đ", Toast.LENGTH_LONG).show();

                // Sau khi lưu thành công, kết thúc AddBudgetActivity để quay lại BudgetActivity
                finish();
            });
        }
    }
}