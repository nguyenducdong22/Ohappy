package com.example.noname;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Addtransaction extends AppCompatActivity {

    private TextView tvDate, tvWalletName;
    private LinearLayout layoutChooseWallet;
    private static final int REQUEST_CHOOSE_WALLET = 1001;
    private EditText edtAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        // Ánh xạ View
        tvDate = findViewById(R.id.tv_date);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        layoutChooseWallet = findViewById(R.id.layout_choose_wallet);
        edtAmount = findViewById(R.id.edt_amount);

        // Hiển thị ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi"));
        String currentDate = sdf.format(calendar.getTime());
        currentDate = currentDate.substring(0, 1).toUpperCase() + currentDate.substring(1);
        tvDate.setText(currentDate);

        // Bắt sự kiện bấm vào phần chọn ví
        layoutChooseWallet.setOnClickListener(v -> {
            Intent intent = new Intent(Addtransaction.this, ChooseWalletActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_WALLET);
        });

        // Ẩn icon ❌ ban đầu
        edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        // Bước 2: Hiện icon ❌ khi có nội dung
        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    Drawable closeIcon = ContextCompat.getDrawable(Addtransaction.this, R.drawable.ic_close);
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, closeIcon, null);
                } else {
                    edtAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ✅ BƯỚC 1: Xử lý khi người dùng chạm vào icon ❌
        edtAmount.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (edtAmount.getCompoundDrawables()[2] != null) {
                    int drawableEndWidth = edtAmount.getCompoundDrawables()[2].getBounds().width();
                    if (event.getRawX() >= (edtAmount.getRight() - drawableEndWidth - edtAmount.getPaddingEnd())) {
                        edtAmount.setText(""); // XÓA số tiền
                        return true;
                    }
                }
            }
            return false;
        });
    }


    // Nhận kết quả từ ChooseWalletActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSE_WALLET && resultCode == RESULT_OK && data != null) {
            String selectedWallet = data.getStringExtra("selected_wallet");

            if (selectedWallet != null) {
                tvWalletName.setText(selectedWallet);
            }
        }
    }
}
