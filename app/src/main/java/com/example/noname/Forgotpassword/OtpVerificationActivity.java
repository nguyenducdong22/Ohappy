package com.example.noname.Forgotpassword;

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
            // Error: Email not found. Please try again.
            Toast.makeText(this, "Error: Email not found. Please try again.", Toast.LENGTH_LONG).show();
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
            // Please enter the OTP code.
            etOtpCode.setError("Please enter the OTP code.");
            etOtpCode.requestFocus();
            return;
        }
        if (newPassword.isEmpty()) {
            // Please enter a new password.
            etNewPassword.setError("Please enter a new password.");
            etNewPassword.requestFocus();
            return;
        }
        if (confirmNewPassword.isEmpty()) {
            // Please confirm the new password.
            etConfirmNewPassword.setError("Please confirm the new password.");
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            // The confirmation password does not match.
            etConfirmNewPassword.setError("The confirmation password does not match.");
            etConfirmNewPassword.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            // Password must be at least 6 characters long.
            etNewPassword.setError("Password must be at least 6 characters long.");
            etNewPassword.requestFocus();
            return;
        }

        showLoading(true);

        userDAO.open();
        boolean success = userDAO.verifyOtpAndResetPassword(userEmail, otpCode, newPassword);
        userDAO.close();

        showLoading(false);

        if (success) {
            // Password has been reset successfully!
            Toast.makeText(this, "Password has been reset successfully!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(OtpVerificationActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // Invalid OTP, expired, or an error occurred.
            Toast.makeText(this, "Invalid OTP, expired, or an error occurred.", Toast.LENGTH_LONG).show();
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