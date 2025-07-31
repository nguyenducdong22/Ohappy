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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
// KHÔNG CÓ CÁC IMPORT KHÁC LIÊN QUAN ĐẾN DATABASE/USERMANAGER/EXECUTORSERVICE
// import com.example.noname.database.AddtransactionDatabase;
// import com.example.noname.database.TransactionDao;
// import com.example.noname.utils.UserManager;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

// Import lớp Transaction model của bạn
import com.example.noname.models.Transaction; // <-- CHÍNH XÁC DÒNG NÀY


public class Addtransaction extends AppCompatActivity {

    // Khai báo các View
    private TextView tvDate, tvWalletName, tvSelectedGroup, tvAddDetail; // tvAddDetail không sử dụng
    private LinearLayout layoutChooseWallet, layoutSelectGroup, layoutNote, layoutSelectDate;
    private ImageView imgGroupIcon;
    private EditText edtAmount, edtNote;
    private MaterialButton btnSave;

    // Định nghĩa Request Codes
    private static final int REQUEST_CHOOSE_WALLET = 1001;
    private static final int CHOOSE_GROUP_REQUEST_CODE = 100;

    // Các biến Room/Database/UserManager/ExecutorService KHÔNG CÒN TỒN TẠI NỮA
    // private AddtransactionDatabase db;
    // private TransactionDao transactionDao;
    // private ExecutorService executorService;
    // private UserManager userManager;
    // private long currentUserId;

    // Biến tạm để lưu trữ groupIconResId và timestamp của ngày được chọn
    private int selectedGroupIconResId = R.drawable.ic_category; // Mặc định là icon danh mục chung
    private int selectedGroupColorResId = R.color.primary_green_dark; // Mặc định màu xanh đậm
    private long selectedDateTimestamp = -1; // Để lưu timestamp của ngày được chọn

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        // --- KHÔNG KHỞI TẠO USERMANAGER VÀ DATABASE Ở ĐÂY NỮA ---
        // userManager = new UserManager(this);
        // currentUserId = userManager.getCurrentUserId();
        // if (currentUserId == -1) { ... }
        // db = AddtransactionDatabase.getDatabase(this);
        // transactionDao = db.transactionDao();
        // executorService = Executors.newSingleThreadExecutor();

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
        btnSave = findViewById(R.id.btn_save);

        // --- 2. Thiết lập Toolbar ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- 3. Hiển thị ngày hiện tại và lưu timestamp mặc định ---
        Calendar calendar = Calendar.getInstance();
        selectedDateTimestamp = calendar.getTimeInMillis(); // Lưu timestamp mặc định là ngày hiện tại
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi"));
        String currentDate = sdf.format(calendar.getTime());
        currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1);
        tvDate.setText(currentDate);

        // --- 4. Thiết lập Listeners cho các mục chọn ---
        layoutChooseWallet.setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseWalletActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_WALLET);
        });

        layoutSelectGroup.setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseGroupActivity.class);
            startActivityForResult(intent, CHOOSE_GROUP_REQUEST_CODE);
        });

        layoutSelectDate.setOnClickListener(v -> {
            showDatePicker();
        });

        // --- 5. Logic cho trường số tiền (edtAmount) ---
        edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    Drawable closeIcon = ContextCompat.getDrawable(Addtransaction.this, R.drawable.ic_clear_text_input);
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, closeIcon, null);
                } else {
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                updateSaveButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
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

        // --- 7. Thiết lập trạng thái ban đầu cho nút Lưu ---
        updateSaveButtonState();

        // --- 8. Thiết lập OnClickListener cho nút Lưu ---
        btnSave.setOnClickListener(v -> {
            Log.d("Addtransaction", "Save button clicked.");
            saveTransaction();
        });
    }

    // --- Hàm hiển thị DatePicker (Pop-up chọn ngày) ---
    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Chọn Ngày");
        builder.setSelection(selectedDateTimestamp != -1 ? selectedDateTimestamp : MaterialDatePicker.todayInUtcMilliseconds());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDateTimestamp = selection;
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

    // --- Hàm cập nhật trạng thái của nút Lưu ---
    private void updateSaveButtonState() {
        String amountText = edtAmount.getText().toString();
        String groupText = tvSelectedGroup.getText().toString();
        boolean isAmountEntered = !amountText.trim().isEmpty();
        boolean isGroupSelected = !groupText.trim().equals("Chọn nhóm");

        btnSave.setEnabled(isAmountEntered && isGroupSelected);

        Log.d("SaveButtonDebug", "Amount text: '" + amountText + "', Group text: '" + groupText + "', Is amount entered: " + isAmountEntered + ", Is group selected: " + isGroupSelected + ", Button enabled: " + btnSave.isEnabled());

        if (btnSave.isEnabled()) {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green));
            btnSave.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green_light));
            btnSave.setTextColor(ContextCompat.getColor(this, // Màu mặc định
                    R.color.white));
        }
    }

    // --- Hàm xử lý logic khi nút Lưu được bấm ---
    private void saveTransaction() {
        Log.d("Addtransaction", "saveTransaction() called.");
        // Lấy dữ liệu từ các trường nhập liệu
        String amountStr = edtAmount.getText().toString().trim();
        String wallet = tvWalletName.getText().toString().trim();
        String group = tvSelectedGroup.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        // KHÔNG CẦN USER ID KHI KHÔNG DÙNG DATABASE
        long userId = 0; // Gán tạm ID người dùng là 0 hoặc bất kỳ số nào

        String type = "Expense"; // Mặc định là chi tiêu

        // Validation
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Validation Failed: Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            Log.d("SAVE_DEBUG", "Validation failed: Amount is empty.");
            return;
        }
        if (group.equals("Chọn nhóm")) {
            Toast.makeText(this, "Validation Failed: Vui lòng chọn nhóm", Toast.LENGTH_SHORT).show();
            Log.d("SAVE_DEBUG", "Validation failed: Group not selected.");
            return;
        }
        Log.d("Addtransaction", "Validation passed. Attempting to parse amount.");

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Validation Failed: Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            Log.e("SAVE_DEBUG", "Error parsing amount: " + amountStr, e);
            return;
        }
        Log.d("Addtransaction", "Amount parsed: " + amount);

        // Tạo đối tượng Transaction
        Transaction newTransaction = new Transaction( // <-- Sử dụng tên lớp Transaction
                userId,                   // user_id_fk (tạm thời không có ý nghĩa khi không dùng DB)
                amount,
                wallet,
                group,
                selectedGroupIconResId,   // group_icon_res_id
                selectedGroupColorResId,  // group_color_res_id
                selectedDateTimestamp,    // transaction_date (timestamp)
                note.isEmpty() ? "" : note, // notes
                type                      // transaction_type
        );
        Log.d("Addtransaction", "Transaction object created. Attempting to send via Intent.");

        // --- TRUYỀN DỮ LIỆU QUA INTENT SANG TRANSACTIONLISTACTIVITY ---
        Intent intent = new Intent(Addtransaction.this, TransactionListActivity.class);

        // Để truyền đối tượng Transaction, nó cần phải implements Serializable hoặc Parcelable
        // Chúng ta sẽ truyền từng trường một để đơn giản.
        intent.putExtra("TRANSACTION_USER_ID", newTransaction.getUserId());
        intent.putExtra("TRANSACTION_AMOUNT", newTransaction.getAmount());
        intent.putExtra("TRANSACTION_WALLET_NAME", newTransaction.getWalletName());
        intent.putExtra("TRANSACTION_GROUP_NAME", newTransaction.getGroupName());
        intent.putExtra("TRANSACTION_ICON_RES_ID", newTransaction.getGroupIconResId());
        intent.putExtra("TRANSACTION_COLOR_RES_ID", newTransaction.getGroupColorResId());
        intent.putExtra("TRANSACTION_DATE", newTransaction.getTransactionDate());
        intent.putExtra("TRANSACTION_NOTE", newTransaction.getNote());
        intent.putExtra("TRANSACTION_TYPE", newTransaction.getType());

        // Sử dụng FLAG_ACTIVITY_CLEAR_TOP và FLAG_ACTIVITY_NEW_TASK
        // để đảm bảo TransactionListActivity là màn hình chính mới
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Đóng AddtransactionActivity
        Log.d("Addtransaction", "Data sent via Intent. Navigated to TransactionListActivity.");
    }

    // --- Phương thức nhận kết quả từ các Activity khác (chọn ví, chọn nhóm) ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
            int groupIconResId = data.getIntExtra("selected_group_icon", R.drawable.ic_category);
            int groupColorResId = data.getIntExtra("selected_group_color", R.color.primary_green_dark);

            if (groupName != null) {
                tvSelectedGroup.setText(groupName);
                selectedGroupIconResId = groupIconResId;
                selectedGroupColorResId = groupColorResId;

                imgGroupIcon.setImageResource(groupIconResId);
                imgGroupIcon.setColorFilter(ContextCompat.getColor(this, groupColorResId), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
        // Cập nhật trạng thái nút Lưu sau khi nhận kết quả
        updateSaveButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Không cần shutdown executorService nữa khi không dùng database
    }
}