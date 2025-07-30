package com.example.noname;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.noname.account.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Addtransaction extends BaseActivity {

    private TextView tvDate, tvWalletName, tvSelectedGroup;
    private ImageView imgGroupIcon;
    private EditText edtAmount, edtNote;
    private MaterialButton btnSave;

    private static final int REQUEST_CHOOSE_WALLET = 1001;
    private static final int CHOOSE_GROUP_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        initializeViews();
        setupToolbar();
        setupInitialDate();
        setupListeners();
        updateSaveButtonState();
    }

    private void initializeViews() {
        tvDate = findViewById(R.id.tv_date);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        edtAmount = findViewById(R.id.edt_amount);
        edtNote = findViewById(R.id.edt_note);
        tvSelectedGroup = findViewById(R.id.tv_selected_group);
        imgGroupIcon = findViewById(R.id.img_group_icon);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_add_transaction);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupInitialDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format_long), Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime());
        currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1);
        tvDate.setText(currentDate);
    }

    private void setupListeners() {
        findViewById(R.id.layout_choose_wallet).setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseWalletActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_WALLET);
        });

        findViewById(R.id.layout_select_group).setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseGroupActivity.class);
            startActivityForResult(intent, CHOOSE_GROUP_REQUEST_CODE);
        });

        findViewById(R.id.layout_select_date).setOnClickListener(v -> showDatePicker());

        setupTextWatcher(edtAmount);
        setupTextWatcher(edtNote);

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupTextWatcher(EditText editText) {
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Drawable icon = (s.length() > 0) ? ContextCompat.getDrawable(Addtransaction.this, R.drawable.ic_clear_text_input) : null;
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                if (editText.getId() == R.id.edt_amount) {
                    updateSaveButtonState();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && editText.getCompoundDrawables()[2] != null) {
                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width() - editText.getPaddingEnd())) {
                    editText.setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getString(R.string.select_date_title));
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format_long), Locale.getDefault());
            String selectedDate = sdf.format(calendar.getTime());
            selectedDate = selectedDate.substring(0, 1).toUpperCase() + selectedDate.substring(1);
            tvDate.setText(selectedDate);
        });

        materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updateSaveButtonState() {
        boolean isAmountEntered = !edtAmount.getText().toString().trim().isEmpty();
        btnSave.setEnabled(isAmountEntered);
        int colorRes = isAmountEntered ? R.color.primary_green : R.color.primary_green_light;
        btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, colorRes));
    }

    private void saveTransaction() {
        String amount = edtAmount.getText().toString();
        String group = tvSelectedGroup.getText().toString();

        if (amount.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_please_enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        if (group.trim().equals(getString(R.string.select_group))) {
            Toast.makeText(this, getString(R.string.error_group_not_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        String wallet = tvWalletName.getText().toString();
        String date = tvDate.getText().toString();
        String note = edtNote.getText().toString();

        String transactionDetails = getString(R.string.toast_transaction_saved_details,
                amount, wallet, group, date, note.isEmpty() ? getString(R.string.no_note) : note
        );
        Toast.makeText(this, transactionDetails, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CHOOSE_WALLET) {
                String selectedWallet = data.getStringExtra("selected_wallet");
                if (selectedWallet != null) {
                    tvWalletName.setText(selectedWallet);
                }
            } else if (requestCode == CHOOSE_GROUP_REQUEST_CODE) {
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
        updateSaveButtonState();
    }
}