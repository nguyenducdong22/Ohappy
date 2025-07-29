package com.example.noname;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log; // Đảm bảo import Log

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

// Giữ nguyên tên lớp là Addtransaction
public class Addtransaction extends AppCompatActivity {

    // Khai báo các View
    private TextView tvDate, tvWalletName, tvSelectedGroup, tvAddDetail;
    private LinearLayout layoutChooseWallet, layoutSelectGroup, layoutNote, layoutSelectDate;
    private ImageView imgGroupIcon;
    private EditText edtAmount, edtNote;
    private MaterialButton btnSave;

    // Định nghĩa Request Codes
    private static final int REQUEST_CHOOSE_WALLET = 1001;
    private static final int CHOOSE_GROUP_REQUEST_CODE = 100;
    // REQUEST_SELECT_DATE không cần nếu dùng MaterialDatePicker

    @SuppressLint("MissingInflatedId") // Bỏ qua cảnh báo nếu có ID không được tìm thấy trong layout (ví dụ: khi ánh xạ tùy chọn)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        // --- 1. Ánh xạ các View ---
        Toolbar toolbar = findViewById(R.id.toolbar_add_transaction);
        tvDate = findViewById(R.id.tv_date);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        layoutChooseWallet = findViewById(R.id.layout_choose_wallet);
        edtAmount = findViewById(R.id.edt_amount);
        edtNote = findViewById(R.id.edt_note);
        layoutSelectGroup = findViewById(R.id.layout_select_group);
        tvSelectedGroup = findViewById(R.id.tv_selected_group);
        imgGroupIcon = findViewById(R.id.img_group_icon);
        layoutNote = findViewById(R.id.layout_note);
        layoutSelectDate = findViewById(R.id.layout_select_date);
        btnSave = findViewById(R.id.btn_save); // Ánh xạ nút Lưu

        // --- 2. Thiết lập Toolbar ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Ẩn tiêu đề mặc định của Toolbar
        }
        // Xử lý sự kiện click nút quay lại trên Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- 3. Hiển thị ngày hiện tại ---
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi"));
        String currentDate = sdf.format(calendar.getTime());
        currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1); // Chữ hoa ký tự đầu
        tvDate.setText(currentDate);

        // --- 4. Thiết lập Listeners cho các mục chọn ---
        // Chọn Ví
        layoutChooseWallet.setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseWalletActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_WALLET);
        });

        // Chọn Nhóm
        layoutSelectGroup.setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseGroupActivity.class);
            startActivityForResult(intent, CHOOSE_GROUP_REQUEST_CODE);
        });

        // Chọn Ngày
        layoutSelectDate.setOnClickListener(v -> {
            showDatePicker(); // Gọi hàm hiển thị DatePicker
        });

        // --- 5. Logic cho trường số tiền (edtAmount) ---
        // Ẩn icon xóa (❌) ban đầu
        edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        // Lắng nghe sự thay đổi văn bản để hiển thị/ẩn icon xóa và cập nhật trạng thái nút Lưu
        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiển thị/ẩn icon xóa
                if (s.length() > 0) {
                    Drawable closeIcon = ContextCompat.getDrawable(Addtransaction.this, R.drawable.ic_clear_text_input);
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, closeIcon, null);
                } else {
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                updateSaveButtonState(); // Cập nhật trạng thái nút Lưu
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        // Xử lý sự kiện chạm để xóa nội dung khi bấm vào icon
        edtAmount.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (edtAmount.getCompoundDrawables()[2] != null) {
                    int drawableEndWidth = edtAmount.getCompoundDrawables()[2].getBounds().width();
                    if (event.getRawX() >= (edtAmount.getRight() - drawableEndWidth - edtAmount.getPaddingEnd())) {
                        edtAmount.setText("");
                        return true;
                    }
                }
            }
            return false;
        });

        // --- 6. Logic cho trường ghi chú (edtNote) ---
        // Ẩn icon xóa (❌) ban đầu
        edtNote.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        // Lắng nghe sự thay đổi văn bản để hiển thị/ẩn icon xóa
        edtNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    Drawable closeIcon = ContextCompat.getDrawable(Addtransaction.this, R.drawable.ic_clear_text_input);
                    edtNote.setCompoundDrawablesWithIntrinsicBounds(null, null, closeIcon, null);
                } else {
                    edtNote.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        // Xử lý sự kiện chạm để xóa nội dung khi bấm vào icon
        edtNote.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (edtNote.getCompoundDrawables()[2] != null) {
                    int drawableEndWidth = edtNote.getCompoundDrawables()[2].getBounds().width();
                    if (event.getRawX() >= (edtNote.getRight() - drawableEndWidth - edtNote.getPaddingEnd())) {
                        edtNote.setText("");
                        return true;
                    }
                }
            }
            return false;
        });

        // --- 7. Thiết lập trạng thái ban đầu cho nút Lưu ---
        // Gọi hàm này một lần để thiết lập trạng thái Enabled/Disabled ban đầu
        updateSaveButtonState();

        // --- 8. Thiết lập OnClickListener cho nút Lưu ---
        btnSave.setOnClickListener(v -> {
            saveTransaction(); // Gọi hàm xử lý lưu giao dịch
        });
    }


    // --- Hàm hiển thị DatePicker (Pop-up chọn ngày) ---
    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Chọn Ngày");
        // Đặt ngày hiện tại làm ngày mặc định được chọn
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        // Lắng nghe khi người dùng chọn ngày và nhấn "OK"
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Chuyển đổi timestamp được chọn sang định dạng ngày mong muốn
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi"));
            String selectedDate = sdf.format(calendar.getTime());
            selectedDate = selectedDate.substring(0, 1).toUpperCase() + selectedDate.substring(1); // Chữ hoa ký tự đầu

            tvDate.setText(selectedDate); // Cập nhật TextView ngày
        });

        // Lắng nghe khi người dùng nhấn "Hủy"
        materialDatePicker.addOnNegativeButtonClickListener(view -> {
            Toast.makeText(Addtransaction.this, "Hủy chọn ngày", Toast.LENGTH_SHORT).show();
        });

        // Lắng nghe khi người dùng chạm ra ngoài để hủy
        materialDatePicker.addOnCancelListener(dialogInterface -> {
            Toast.makeText(Addtransaction.this, "Hủy chọn ngày", Toast.LENGTH_SHORT).show();
        });

        // Hiển thị DatePicker
        materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    // --- Hàm cập nhật trạng thái của nút Lưu ---
    private void updateSaveButtonState() {
        String amountText = edtAmount.getText().toString();
        // Nút sẽ được kích hoạt nếu trường số tiền không rỗng
        boolean isAmountEntered = !amountText.trim().isEmpty();

        btnSave.setEnabled(isAmountEntered);

        // Ghi log để debug (bạn có thể xóa dòng này khi không cần debug nữa)
        Log.d("SaveButtonDebug", "Amount text: '" + amountText + "', Is amount entered: " + isAmountEntered + ", Button enabled: " + btnSave.isEnabled());

        // Thay đổi màu sắc của nút tùy theo trạng thái enabled/disabled
        if (isAmountEntered) {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green));
            btnSave.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green_light)); // Màu nhạt khi disabled
            btnSave.setTextColor(ContextCompat.getColor(this, R.color.white)); // Chữ trắng cho cả 2 trạng thái
        }
    }

    // --- Hàm xử lý logic khi nút Lưu được bấm ---
    private void saveTransaction() {
        // Lấy dữ liệu từ các trường nhập liệu
        String amount = edtAmount.getText().toString();
        String wallet = tvWalletName.getText().toString();
        String group = tvSelectedGroup.getText().toString();
        String date = tvDate.getText().toString();
        String note = edtNote.getText().toString();

        // --- DEBUG LOGS ---
        Log.d("SAVE_DEBUG", "Saving transaction...");
        Log.d("SAVE_DEBUG", "Amount: '" + amount + "'");
        Log.d("SAVE_DEBUG", "Group: '" + group + "'");

        // Kiểm tra ràng buộc (Validation)
        if (amount.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            Log.d("SAVE_DEBUG", "Validation failed: Amount is empty.");
            return; // Hàm dừng lại ở đây nếu số tiền rỗng
        }
        // Kiểm tra xem nhóm đã được chọn chưa (không phải là "Chọn nhóm" mặc định)
        // Thêm .trim() để loại bỏ khoảng trắng thừa nếu có
        if (group.trim().equals("Chọn nhóm")) {
            Toast.makeText(this, "Vui lòng chọn nhóm", Toast.LENGTH_SHORT).show();
            Log.d("SAVE_DEBUG", "Validation failed: Group not selected.");
            return; // Hàm dừng lại ở đây nếu nhóm chưa chọn
        }

        // --- Nếu đến được đây, nghĩa là validation thành công ---
        Log.d("SAVE_DEBUG", "Validation passed. Proceeding to save.");

        // --- Logic lưu dữ liệu vào database hoặc gửi đi ---
        // (Hiện tại chỉ hiển thị Toast, bạn sẽ thay thế bằng logic lưu thực tế)
        String transactionDetails = String.format(
                "Đã lưu: %s VND\nVí: %s\nNhóm: %s\nNgày: %s\nGhi chú: %s",
                amount, wallet, group, date, note.isEmpty() ? "Không có" : note
        );
        Toast.makeText(this, transactionDetails, Toast.LENGTH_LONG).show();
        Log.d("SAVE_DEBUG", "Transaction saved (Toast shown).");


        // --- Chuyển về màn hình trang chủ (hoặc màn hình chính) ---
        Intent intent = new Intent(this, MainActivity.class); // Thay MainActivity.class bằng tên Activity trang chủ của bạn
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa các Activity trên stack và mở mới
        startActivity(intent);
        finish(); // Đóng AddtransactionActivity sau khi chuyển hướng
        Log.d("SAVE_DEBUG", "Navigated to home screen.");
    }

    // --- Phương thức nhận kết quả từ các Activity khác (chọn ví, chọn nhóm) ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Cập nhật trạng thái nút Lưu sau khi nhận kết quả (đảm bảo điều kiện đã đủ)
        updateSaveButtonState();

        // Xử lý kết quả từ ChooseWalletActivity
        if (requestCode == REQUEST_CHOOSE_WALLET && resultCode == RESULT_OK && data != null) {
            String selectedWallet = data.getStringExtra("selected_wallet");
            if (selectedWallet != null) {
                tvWalletName.setText(selectedWallet);
            }
        }

        // Xử lý kết quả từ ChooseGroupActivity
        if (requestCode == CHOOSE_GROUP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String groupName = data.getStringExtra("selected_group_name");
            int groupIconResId = data.getIntExtra("selected_group_icon", -1); // Lấy ID icon
            if (groupName != null) {
                tvSelectedGroup.setText(groupName); // Cập nhật tên nhóm
                if (groupIconResId != -1) {
                    imgGroupIcon.setImageResource(groupIconResId); // Cập nhật icon nhóm
                }
            }
        }
    }
}