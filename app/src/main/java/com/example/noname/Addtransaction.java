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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Tên lớp được đổi thành Addtransaction để khớp với tên file
public class Addtransaction extends AppCompatActivity {

    private TextView tvDate, tvWalletName, tvSelectedGroup;
    private LinearLayout layoutChooseWallet, layoutSelectGroup, layoutNote, layoutSelectDate;
    private ImageView imgGroupIcon;
    private EditText edtAmount, edtNote;

    private static final int REQUEST_CHOOSE_WALLET = 1001;
    private static final int CHOOSE_GROUP_REQUEST_CODE = 100;
    private static final int REQUEST_SELECT_DATE = 101;

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

        // Xử lý sự kiện chọn ngày (ví dụ mở DatePicker)
        layoutSelectDate.setOnClickListener(v -> {
            Toast.makeText(Addtransaction.this, "Mở trình chọn ngày", Toast.LENGTH_SHORT).show();
        });


        // Ẩn icon ❌ ban đầu cho edtAmount
        edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        // Bắt sự kiện hiện icon ❌ khi nhập cho edtAmount
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
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bắt sự kiện xóa nội dung khi bấm icon ❌ cho edtAmount
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

        // Xử lý icon ❌ và TextWatcher cho edtNote
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

    // Nhận kết quả từ các Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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