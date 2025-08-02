package com.example.noname.Forgotpassword; // ĐÃ SỬA: Thêm .Forgotpassword

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.noname.R;
import com.example.noname.SignInActivity;
import com.example.noname.database.UserDAO;
import com.example.noname.utils.PasswordHasher;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtpCode;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private ImageButton btnBackOtpVerification;

    private UserDAO userDAO;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        etOtpCode = findViewById(R.id.etOtpCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        btnBackOtpVerification = findViewById(R.id.btnBackOtpVerification);

        userDAO = new UserDAO(this);

        if (getIntent().hasExtra("email")) {
            userEmail = getIntent().getStringExtra("email");
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy email. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        btnBackOtpVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void resetPassword() {
        String otpCode = etOtpCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();

        if (otpCode.isEmpty()) {
            etOtpCode.setError("Vui lòng nhập mã OTP.");
            etOtpCode.requestFocus();
            return;
        }
        if (newPassword.isEmpty()) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới.");
            etNewPassword.requestFocus();
            return;
        }
        if (confirmNewPassword.isEmpty()) {
            etConfirmNewPassword.setError("Vui lòng xác nhận mật khẩu mới.");
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            etConfirmNewPassword.setError("Mật khẩu xác nhận không khớp.");
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            etNewPassword.requestFocus();
            return;
        }

        showLoading(true);

        userDAO.open();
        boolean success = userDAO.verifyOtpAndResetPassword(userEmail, otpCode, newPassword);
        userDAO.close();

        showLoading(false);

        if (success) {
            Toast.makeText(this, "Mật khẩu đã được đặt lại thành công!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(OtpVerificationActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Mã OTP không hợp lệ, đã hết hạn hoặc có lỗi.", Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnResetPassword.setEnabled(!isLoading);
        etOtpCode.setEnabled(!isLoading);
        etNewPassword.setEnabled(!isLoading);
        etConfirmNewPassword.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}