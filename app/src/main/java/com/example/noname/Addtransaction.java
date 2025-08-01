package com.example.noname;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Thêm import cho Log
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.noname.database.AccountDAO;
import com.example.noname.database.TransactionDAO;
import com.example.noname.models.Category;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Addtransaction extends AppCompatActivity {

    private TextView tvDate, tvWalletName, tvSelectedGroup;
    private LinearLayout layoutChooseWallet, layoutSelectGroup, layoutSelectDate;
    private ImageView imgGroupIcon;
    private EditText edtAmount, edtNote;
    private MaterialButton btnSave;

    private static final int REQUEST_CHOOSE_WALLET = 1001;
    private static final int CHOOSE_GROUP_REQUEST_CODE = 100;

    private long selectedAccountId = -1;
    private long selectedCategoryId = -1;
    private String transactionType = "Expense";

    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;
    private long currentUserId;
    private Long selectedDateTimestamp = null;

    private static final String TAG = "Addtransaction"; // Định nghĩa tag cho Log

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        Log.d(TAG, "onCreate: Addtransaction activity started.");

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Current User ID is -1. Finishing activity.");
            finish();
            return;
        }

        transactionDAO = new TransactionDAO(this);
        accountDAO = new AccountDAO(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_transaction);
        tvDate = findViewById(R.id.tv_date);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        layoutChooseWallet = findViewById(R.id.layout_choose_wallet);
        edtAmount = findViewById(R.id.edt_amount);
        edtNote = findViewById(R.id.edt_note);
        layoutSelectGroup = findViewById(R.id.layout_select_group);
        tvSelectedGroup = findViewById(R.id.tv_selected_group);
        imgGroupIcon = findViewById(R.id.img_group_icon);
        layoutSelectDate = findViewById(R.id.layout_select_date);
        btnSave = findViewById(R.id.btn_save);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Toolbar back button clicked. Finishing activity.");
            finish();
        });

        long today = MaterialDatePicker.todayInUtcMilliseconds();
        selectedDateTimestamp = today;
        updateDateTextView(today);

        layoutChooseWallet.setOnClickListener(v -> {
            Log.d(TAG, "Choose wallet clicked.");
            Intent intent = new Intent(Addtransaction.this, ChooseWalletActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_WALLET);
        });
        layoutSelectGroup.setOnClickListener(v -> {
            Log.d(TAG, "Choose group clicked.");
            Intent intent = new Intent(Addtransaction.this, ChooseGroupActivity.class);
            startActivityForResult(intent, CHOOSE_GROUP_REQUEST_CODE);
        });
        layoutSelectDate.setOnClickListener(v -> {
            Log.d(TAG, "Choose date clicked.");
            showDatePicker();
        });

        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    Drawable closeIcon = ContextCompat.getDrawable(Addtransaction.this, R.drawable.ic_clear_text_input);
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, closeIcon, null);
                } else {
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                Log.d(TAG, "Amount text changed to: " + s.toString());
                updateSaveButtonState();
            }
            @Override public void afterTextChanged(Editable s) {}
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

        edtNote.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    Drawable closeIcon = ContextCompat.getDrawable(Addtransaction.this, R.drawable.ic_clear_text_input);
                    edtNote.setCompoundDrawablesWithIntrinsicBounds(null, null, closeIcon, null);
                } else {
                    edtNote.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
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

        updateSaveButtonState();
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Chọn Ngày");
        if (selectedDateTimestamp != null) {
            builder.setSelection(selectedDateTimestamp);
        } else {
            builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        }

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDateTimestamp = selection;
            updateDateTextView(selection);
            Log.d(TAG, "Date selected: " + getSelectedDateForDatabase(selection));
        });

        materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updateDateTextView(long timestamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", new Locale("vi"));
        String selectedDate = sdf.format(calendar.getTime());
        selectedDate = selectedDate.substring(0, 1).toUpperCase() + selectedDate.substring(1);

        tvDate.setText(selectedDate);
    }

    private void updateSaveButtonState() {
        String amountText = edtAmount.getText().toString();
        boolean isAmountEntered = !amountText.trim().isEmpty();
        boolean isWalletSelected = selectedAccountId != -1;
        boolean isGroupSelected = selectedCategoryId != -1;

        boolean canSave = isAmountEntered && isWalletSelected && isGroupSelected;
        btnSave.setEnabled(canSave);
        Log.d(TAG, "Updating save button state. Amount: " + isAmountEntered + ", Wallet: " + isWalletSelected + ", Group: " + isGroupSelected + ", Can Save: " + canSave);

        if (canSave) {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green));
        } else {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_green_light));
        }
        btnSave.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void saveTransaction() {
        Log.d(TAG, "Attempting to save transaction...");

        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtAmount.getText().toString().trim().isEmpty() || selectedAccountId == -1 || selectedCategoryId == -1) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Validation failed: Missing amount, wallet or category.");
            return;
        }

        double amount = Double.parseDouble(edtAmount.getText().toString());
        String transactionDate = getSelectedDateForDatabase(selectedDateTimestamp);
        String note = edtNote.getText().toString();
        String description = tvSelectedGroup.getText().toString();

        transactionDAO.open();
        long newTransactionId = transactionDAO.addTransaction(
                currentUserId,
                selectedAccountId,
                selectedCategoryId,
                amount,
                transactionType,
                transactionDate,
                description,
                note
        );
        transactionDAO.close();

        if (newTransactionId != -1) {
            Log.d(TAG, "Transaction successfully added to database with ID: " + newTransactionId);
            accountDAO.open();
            boolean success = accountDAO.updateAccountBalance(selectedAccountId, amount, transactionType);
            accountDAO.close();

            if (success) {
                Log.d(TAG, "Account balance updated successfully for account ID: " + selectedAccountId);
                Toast.makeText(this, "Đã lưu giao dịch thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "Failed to update account balance for account ID: " + selectedAccountId);
                Toast.makeText(this, "Lỗi khi cập nhật số dư tài khoản.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Failed to add transaction to database.");
            Toast.makeText(this, "Lỗi khi lưu giao dịch.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedDateForDatabase(Long timestamp) {
        if (timestamp == null) {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Received result from another activity. RequestCode: " + requestCode + ", ResultCode: " + resultCode);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CHOOSE_WALLET) {
                selectedAccountId = data.getLongExtra("selected_account_id", -1);
                String walletName = data.getStringExtra("selected_wallet_name");
                Log.d(TAG, "Received wallet selection. ID: " + selectedAccountId + ", Name: " + walletName);
                if (walletName != null) {
                    tvWalletName.setText(walletName);
                }
            } else if (requestCode == CHOOSE_GROUP_REQUEST_CODE) {
                selectedCategoryId = data.getLongExtra("selected_category_id", -1);
                String groupName = data.getStringExtra("selected_group_name");
                int groupIconResId = data.getIntExtra("selected_group_icon", -1);
                Log.d(TAG, "Received group selection. ID: " + selectedCategoryId + ", Name: " + groupName);
                if (groupName != null) {
                    tvSelectedGroup.setText(groupName);
                    if (groupIconResId != -1) {
                        imgGroupIcon.setImageResource(groupIconResId);
                    }
                }
            }
        } else {
            Log.d(TAG, "Result was not OK or data is null.");
        }
        updateSaveButtonState();
    }
}