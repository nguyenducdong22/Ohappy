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
import android.content.Context; // Thêm import này
import android.content.SharedPreferences; // Thêm import này

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.example.noname.R;
import com.example.noname.database.DatabaseHelper; // Needed for getCategoryId.
import com.example.noname.database.BudgetDAO;     // For budget data operations.
import com.example.noname.SignInActivity; // Thêm import cho SignInActivity

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AddBudgetActivity allows users to create a new budget entry or edit an existing one.
 * It handles input for budget amount, category selection, date range, and recurrence.
 * It uses BudgetDAO to save or update the budget in the database.
 */
public class AddBudgetActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_GROUP = 1; // Request code for selecting a group/category.
    public static final String EXTRA_BUDGET_TO_EDIT = "extra_budget_to_edit"; // Key for passing Budget object to edit.

    // UI elements
    private TextView tvGroupName;
    private ImageView ivGroupIcon;
    private EditText etAmount;
    private TextView tvDateRange;
    private SwitchMaterial switchRepeatBudget;
    private TextView toolbarTitle; // TextView trong Toolbar để hiển thị tiêu đề động (Thêm/Sửa).

    // Data for the budget (new or existing)
    private String selectedGroupName = "Chọn nhóm";
    private int selectedGroupIconResId = R.drawable.ic_circle;
    // Lưu ý: selectedDateRange hiện chỉ là chuỗi hiển thị.
    // Trong ứng dụng thực, bạn cần các biến riêng cho startDate và endDate dạng Date/String.
    private String selectedDateRange = "Tháng này (01/07 - 31/07)";
    private long selectedCategoryId = -1;

    private Budget existingBudget = null; // Biến này sẽ giữ đối tượng Budget nếu chúng ta đang ở chế độ "sửa".

    private DatabaseHelper dbHelper; // Để lấy Category ID từ tên (vì DAO chỉ quản lý Budgets, không phải Categories).
    private BudgetDAO budgetDAO;     // Để lưu hoặc cập nhật dữ liệu ngân sách vào database.

    private long currentUserId; // Biến để lưu userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        // Lấy User ID từ SharedPreferences NGAY KHI Activity ĐƯỢC TẠO
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // Chuyển hướng về màn hình đăng nhập
            Intent loginIntent = new Intent(this, SignInActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish(); // Đóng activity nếu không có userId hợp lệ
            return;
        }

        dbHelper = new DatabaseHelper(this);
        budgetDAO = new BudgetDAO(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_budget);
        setSupportActionBar(toolbar);
        toolbarTitle = toolbar.findViewById(R.id.tv_screen_title); // Lấy TextView tiêu đề từ Toolbar.

        // Initialize UI components
        tvGroupName = findViewById(R.id.tv_group_name);
        ivGroupIcon = findViewById(R.id.iv_group_icon);
        etAmount = findViewById(R.id.et_amount);
        tvDateRange = findViewById(R.id.tv_date_range);
        switchRepeatBudget = findViewById(R.id.switch_repeat_budget);

        // --- Kiểm tra nếu có Budget object được truyền vào (chế độ SỬA) ---
        // `getIntent().hasExtra(EXTRA_BUDGET_TO_EDIT)` kiểm tra xem Intent có chứa dữ liệu với key này không.
        if (getIntent().hasExtra(EXTRA_BUDGET_TO_EDIT)) {
            // Lấy đối tượng Budget từ Intent. Ép kiểu về `Budget`.
            existingBudget = (Budget) getIntent().getSerializableExtra(EXTRA_BUDGET_TO_EDIT);
            if (existingBudget != null) {
                // --- Nếu đang ở chế độ SỬA, điền dữ liệu từ `existingBudget` vào UI ---
                toolbarTitle.setText("Sửa Ngân sách"); // Thay đổi tiêu đề Toolbar thành "Sửa Ngân sách".
                selectedGroupName = existingBudget.getGroupName();
                selectedGroupIconResId = existingBudget.getGroupIconResId();
                // Hiện tại, `getDateRange()` trả về chuỗi kết hợp. Nếu bạn có ngày tháng riêng biệt, hãy dùng chúng.
                selectedDateRange = existingBudget.getDateRange();
                selectedCategoryId = existingBudget.getCategoryId(); // Lấy Category ID đã có.
                etAmount.setText(String.valueOf(existingBudget.getAmount())); // Hiển thị số tiền.
                switchRepeatBudget.setChecked(existingBudget.isRepeat()); // Đặt trạng thái công tắc lặp lại.
            }
        } else {
            // --- Nếu không có Budget nào được truyền vào, đây là chế độ TẠO MỚI ---
            toolbarTitle.setText("Tạo Ngân sách"); // Tiêu đề mặc định cho ngân sách mới.
        }

        // --- Đặt trạng thái hiển thị ban đầu cho các thành phần UI ---
        // Các giá trị này có thể là mặc định (nếu tạo mới) hoặc từ `existingBudget` (nếu sửa).
        tvGroupName.setText(selectedGroupName);
        ivGroupIcon.setImageResource(selectedGroupIconResId);
        tvDateRange.setText(selectedDateRange);

        // --- Thiết lập lắng nghe sự kiện click cho nút "Hủy" ---
        TextView btnCancel = findViewById(R.id.btn_cancel_add_budget);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED); // Đặt kết quả là hủy.
                finish(); // Đóng activity.
            }
        });

        // --- Thiết lập lắng nghe sự kiện click cho layout "Chọn nhóm" (chọn danh mục) ---
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

        // --- Thiết lập lắng nghe sự kiện click cho layout "Khoảng thời gian" (Placeholder cho DatePicker) ---
        LinearLayout layoutDateRangeClick = findViewById(R.id.layout_date_range);
        layoutDateRangeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddBudgetActivity.this, "Mở Date Picker (chưa triển khai)", Toast.LENGTH_SHORT).show();
                // TODO: Triển khai DatePickerDialog hoặc UI chọn khoảng ngày ở đây.
                // Khi người dùng chọn, cập nhật các biến `startDate`, `endDate` và `tvDateRange`.
            }
        });

        // --- Thiết lập lắng nghe sự kiện click cho layout "Tổng cộng" ---
        LinearLayout layoutTotal = findViewById(R.id.layout_total);
        if (layoutTotal != null) {
            layoutTotal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(AddBudgetActivity.this, "Mở thiết lập Tổng cộng (chưa triển khai)", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- Thiết lập lắng nghe sự kiện click cho nút "Lưu" (hoặc "Cập nhật") ngân sách ---
        Button btnSaveBudget = findViewById(R.id.btn_save_budget);
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountString = etAmount.getText().toString().trim();
                double amount = 0.0;

                // --- Kiểm tra hợp lệ dữ liệu đầu vào ---
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

                if (selectedGroupName.equals("Chọn nhóm") || selectedCategoryId == -1) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng chọn một nhóm hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sử dụng `currentUserId` đã lấy từ SharedPreferences
                boolean repeatBudget = switchRepeatBudget.isChecked();

                // Placeholder cho startDate/endDate. Cần thay thế bằng lựa chọn thực tế từ DatePicker.
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String startDate = dateFormat.format(new Date());
                String endDate = dateFormat.format(new Date());

                boolean success = false;
                budgetDAO.open(); // Mở kết nối database trước khi thao tác.

                if (existingBudget == null) {
                    // --- Chế độ TẠO MỚI ngân sách ---
                    success = budgetDAO.addBudget(currentUserId, selectedCategoryId, amount, startDate, endDate, repeatBudget);
                } else {
                    // --- Chế độ CẬP NHẬT ngân sách hiện có ---
                    // Cập nhật đối tượng `existingBudget` với các giá trị mới từ UI.
                    existingBudget.setAmount(amount);
                    existingBudget.setCategoryId(selectedCategoryId);
                    existingBudget.setGroupName(selectedGroupName); // Cập nhật tên nhóm nếu thay đổi.
                    existingBudget.setGroupIconResId(selectedGroupIconResId); // Cập nhật icon nếu thay đổi.
                    existingBudget.setStartDate(startDate); // Cập nhật ngày bắt đầu.
                    existingBudget.setEndDate(endDate);     // Cập nhật ngày kết thúc.
                    existingBudget.setRepeat(repeatBudget); // Cập nhật trạng thái lặp lại.
                    // Đảm bảo userId không bị thay đổi khi cập nhật (nếu userId của existingBudget đã đúng)
                    // Hoặc bạn có thể thêm: existingBudget.setUserId(currentUserId); nếu userId có thể bị mất.

                    success = budgetDAO.updateBudget(existingBudget); // Gọi phương thức update.
                }
                budgetDAO.close(); // Đóng kết nối database sau khi hoàn tất.

                if (success) {
                    Toast.makeText(AddBudgetActivity.this, "Đã lưu ngân sách cho: " + selectedGroupName, Toast.LENGTH_LONG).show();
                    setResult(Activity.RESULT_OK); // Đặt kết quả OK cho Activity gọi.
                    finish(); // Đóng Activity hiện tại.
                } else {
                    Toast.makeText(AddBudgetActivity.this, "Lỗi khi lưu ngân sách. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Handles the result returned from other activities, specifically ChooseGroupActivity.
     * @param requestCode The integer request code.
     * @param resultCode The integer result code returned by the child activity.
     * @param data An Intent, which can carry the result data back to the parent activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_GROUP) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                selectedGroupName = data.getStringExtra("selected_group_name");
                selectedGroupIconResId = data.getIntExtra("selected_group_icon", R.drawable.ic_circle);

                // Truyền currentUserId vào đây
                selectedCategoryId = dbHelper.getCategoryId(selectedGroupName, "Expense", currentUserId); // THÊM currentUserId
                if (selectedCategoryId == -1) {
                    Toast.makeText(this, "Lỗi: Không tìm thấy ID danh mục cho '" + selectedGroupName + "'.", Toast.LENGTH_SHORT).show();
                }

                tvGroupName.setText(selectedGroupName);
                ivGroupIcon.setImageResource(selectedGroupIconResId);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Chọn nhóm đã bị hủy.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}