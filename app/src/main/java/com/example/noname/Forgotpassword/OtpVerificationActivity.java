package com.example.noname.Forgotpassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.noname.R;
import com.example.noname.SignInActivity;
import com.example.noname.account.BaseActivity;
import com.example.noname.database.UserDAO;

public class OtpVerificationActivity extends BaseActivity {

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

        initializeViews();
        userDAO = new UserDAO(this);

        if (getIntent().hasExtra("email")) {
            userEmail = getIntent().getStringExtra("email");
        } else {
            Toast.makeText(this, getString(R.string.error_email_not_found), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupListeners();
    }

    private void initializeViews() {
        etOtpCode = findViewById(R.id.etOtpCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        btnBackOtpVerification = findViewById(R.id.btnBackOtpVerification);
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> resetPassword());
        btnBackOtpVerification.setOnClickListener(v -> onBackPressed());
    }

    private void resetPassword() {
        String otpCode = etOtpCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();

        if (otpCode.isEmpty()) {
            etOtpCode.setError(getString(R.string.error_otp_required));
            etOtpCode.requestFocus();
            return;
        }
        if (newPassword.isEmpty()) {
            etNewPassword.setError(getString(R.string.error_new_password_required));
            etNewPassword.requestFocus();
            return;
        }
        if (confirmNewPassword.isEmpty()) {
            etConfirmNewPassword.setError(getString(R.string.error_confirm_password_required));
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            etConfirmNewPassword.setError(getString(R.string.error_passwords_do_not_match));
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            etNewPassword.setError(getString(R.string.error_password_too_short));
            etNewPassword.requestFocus();
            return;
        }

        showLoading(true);

        // This is a placeholder for the actual logic which should be in UserDAO
        // For now, we will simulate success. Replace this with your actual DAO call.
        // boolean success = userDAO.verifyOtpAndResetPassword(userEmail, otpCode, newPassword);
        boolean success = true; // Placeholder for successful OTP verification

        showLoading(false);

        if (success) {
            Toast.makeText(this, getString(R.string.password_reset_success), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(OtpVerificationActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_otp_invalid_or_expired), Toast.LENGTH_LONG).show();
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
}