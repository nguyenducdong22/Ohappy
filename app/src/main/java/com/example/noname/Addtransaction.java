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

public class Addtransaction extends AppCompatActivity {

    private TextView tvDate, tvWalletName, tvSelectedGroup, tvAddDetail; // Đảm bảo khai báo tvAddDetail
    private LinearLayout layoutChooseWallet, layoutSelectGroup, layoutNote, layoutSelectDate;
    private ImageView imgGroupIcon;
    private EditText edtAmount, edtNote;
    private MaterialButton btnSave; // Đảm bảo khai báo btnSave

    private static final int REQUEST_CHOOSE_WALLET = 1001;
    private static final int CHOOSE_GROUP_REQUEST_CODE = 100;
    // REQUEST_SELECT_DATE không cần nếu dùng MaterialDatePicker

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        // Ánh xạ Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_add_transaction);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ View khác
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

        // Hiển thị ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi"));
        String currentDate = sdf.format(calendar.getTime());
        currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1);
        tvDate.setText(currentDate);

        // Bắt sự kiện chọn ví
        layoutChooseWallet.setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseWalletActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_WALLET);
        });

        // Bắt sự kiện chọn nhóm
        layoutSelectGroup.setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseGroupActivity.class);
            startActivityForResult(intent, CHOOSE_GROUP_REQUEST_CODE);
        });

        // Xử lý sự kiện chọn ngày (mở DatePicker)
        layoutSelectDate.setOnClickListener(v -> {
            showDatePicker();
        });

        // ===============================================
        // LOGIC CHO NÚT LƯU (Đặt đúng vị trí trong onCreate)
        // ===============================================

        btnSave.setOnClickListener(v -> {
            saveTransaction(); // Gọi hàm xử lý lưu giao dịch
        });

        // Logic để kích hoạt/vô hiệu hóa nút Lưu
        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("SaveButtonDebug", "Amount text changed: " + s.toString());
                updateSaveButtonState(); // Cập nhật trạng thái nút khi text thay đổi
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        updateSaveButtonState(); // Gọi updateSaveButtonState() một lần khi khởi tạo để thiết lập trạng thái ban đầu


        // ===============================================
        // CÁC LOGIC KHÁC (giữ nguyên)
        // ===============================================

        edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
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

        edtNote.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
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
    }

    // Hàm hiển thị DatePicker
    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Chọn Ngày");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi"));
            String selectedDate = sdf.format(calendar.getTime());
            selectedDate = selectedDate.substring(0, 1).toUpperCase() + selectedDate.substring(1);

            tvDate.setText(selectedDate);
        });

        materialDatePicker.addOnNegativeButtonClickListener(view -> {
            Toast.makeText(Addtransaction.this, "Hủy chọn ngày", Toast.LENGTH_SHORT).show();
        });

        materialDatePicker.addOnCancelListener(dialogInterface -> {
            Toast.makeText(Addtransaction.this, "Hủy chọn ngày", Toast.LENGTH_SHORT).show();
        });

        materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    // Hàm cập nhật trạng thái của nút Lưu
    private void updateSaveButtonState() {
        String amountText = edtAmount.getText().toString();
        boolean isAmountEntered = !amountText.trim().isEmpty();

        btnSave.setEnabled(isAmountEntered);

        Log.d("SaveButtonDebug", "Button Enabled: " + isAmountEntered);

        if (isAmountEntered) {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green));
            btnSave.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green_light));
            btnSave.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    // Hàm xử lý lưu giao dịch
    private void saveTransaction() {
        String amount = edtAmount.getText().toString();
        String wallet = tvWalletName.getText().toString();
        String group = tvSelectedGroup.getText().toString();
        String date = tvDate.getText().toString();
        String note = edtNote.getText().toString();

        if (amount.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        if (group.equals("Chọn nhóm")) {
            Toast.makeText(this, "Vui lòng chọn nhóm", Toast.LENGTH_SHORT).show();
            return;
        }

        String transactionDetails = String.format(
                "Đã lưu: %s VND\nVí: %s\nNhóm: %s\nNgày: %s\nGhi chú: %s",
                amount, wallet, group, date, note.isEmpty() ? "Không có" : note
        );
        Toast.makeText(this, transactionDetails, Toast.LENGTH_LONG).show();

        finish();
    }

    // Nhận kết quả từ các Activity khác (chọn ví, chọn nhóm)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        updateSaveButtonState(); // Cập nhật trạng thái nút Lưu sau khi nhận kết quả

        if (requestCode == REQUEST_CHOOSE_WALLET && resultCode == RESULT_OK && data != null) {
            String selectedWallet = data.getStringExtra("selected_wallet");
            if (selectedWallet != null) {
                tvWalletName.setText(selectedWallet);
            }
        }

        if (requestCode == CHOOSE_GROUP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String groupName = data.getStringExtra("selected_group_name");
            int groupIconResId = data.getIntExtra("selected_group_icon", -1);
            if (groupName != null) {
                tvSelectedGroup.setText(groupName);
                if (groupIconResId != -1) {
                    imgGroupIcon.setImageResource(groupIconResId);
                }
            }
        }
    }
}