package com.example.noname.Forgotpassword; // ĐÃ SỬA: Thêm .Forgotpassword

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noname.R;
import com.example.noname.account.BaseActivity;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.UserDAO;

import java.util.Locale;
import java.util.Random;
import android.database.Cursor;
import com.example.noname.database.LocaleHelper;

public class ForgotPasswordRequestActivity extends BaseActivity {

    private EditText etEmailOrPhone;
    private Button btnSendOtpForReset;
    private ImageButton btnBackForgotPassword;
    private ProgressBar progressBar;
    private TextView tvOtpDisplayDemo;
    private TextView tvGeneratedOtpCode;
    private TextView tvOtpInstructionDemo;

    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_request);

        etEmailOrPhone = findViewById(R.id.etEmailOrPhone);
        btnSendOtpForReset = findViewById(R.id.btnSendOtpForReset);
        btnBackForgotPassword = findViewById(R.id.btnBackForgotPassword);
        progressBar = findViewById(R.id.progressBarRequest);
        tvOtpDisplayDemo = findViewById(R.id.tvOtpDisplayDemo);
        tvGeneratedOtpCode = findViewById(R.id.tvGeneratedOtpCode);
        tvOtpInstructionDemo = findViewById(R.id.tvOtpInstructionDemo);

        userDAO = new UserDAO(this);

        btnSendOtpForReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOtp();
            }
        });

        btnBackForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void requestOtp() {
        String emailOrPhone = etEmailOrPhone.getText().toString().trim();

        if (emailOrPhone.isEmpty()) {
            etEmailOrPhone.setError("Vui lòng nhập email hoặc số điện thoại.");
            etEmailOrPhone.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
            etEmailOrPhone.setError("Vui lòng nhập định dạng email hợp lệ.");
            etEmailOrPhone.requestFocus();
            return;
        }

        showLoading(true);

        userDAO.open();
        boolean emailExists = userDAO.isEmailExists(emailOrPhone);
        long userId = -1;

        if (emailExists) {
            Cursor cursor = userDAO.getUserByEmail(emailOrPhone);
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                cursor.close();
            }
        }
        userDAO.close();

        if (userId != -1) {
            Random random = new Random();
            String otpCode = String.format(Locale.getDefault(), "%06d", random.nextInt(1000000));

            userDAO.open();
            long otpRowId = userDAO.createOtpForUser(userId, otpCode, 5);
            userDAO.close();

            if (otpRowId != -1) {
                tvOtpDisplayDemo.setVisibility(View.VISIBLE);
                tvGeneratedOtpCode.setText(otpCode);
                tvGeneratedOtpCode.setVisibility(View.VISIBLE);
                tvOtpInstructionDemo.setVisibility(View.VISIBLE);

                new AlertDialog.Builder(this)
                        .setTitle("Mã OTP DEMO của bạn")
                        .setMessage("Đây là mã OTP cho mục đích demo. Trong ứng dụng thực, mã này sẽ được gửi qua Email/SMS.\n\nMã OTP: " + otpCode + "\n\n(Mã này sẽ hết hạn sau 5 phút)")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(ForgotPasswordRequestActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", emailOrPhone);
                            startActivity(intent);
                        })
                        .setCancelable(false)
                        .show();

                Toast.makeText(this, "Mã OTP đã được tạo và hiển thị (DEMO).", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Không thể tạo mã OTP. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Email hoặc số điện thoại không tồn tại trong hệ thống.", Toast.LENGTH_LONG).show();
        }

        showLoading(false);
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnSendOtpForReset.setEnabled(!isLoading);
        etEmailOrPhone.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}