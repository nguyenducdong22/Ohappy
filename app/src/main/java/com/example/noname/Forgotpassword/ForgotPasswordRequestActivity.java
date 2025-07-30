package com.example.noname.Forgotpassword;

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
import com.example.noname.database.UserDAO;

import java.util.Locale;
import java.util.Random;

public class ForgotPasswordRequestActivity extends BaseActivity {

    private EditText etEmailOrPhone;
    private Button btnSendOtpForReset;
    private ProgressBar progressBar;
    private TextView tvGeneratedOtpCode;

    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_request);

        initializeViews();
        setupListeners();

        userDAO = new UserDAO(this);
    }

    private void initializeViews() {
        etEmailOrPhone = findViewById(R.id.etEmailOrPhone);
        btnSendOtpForReset = findViewById(R.id.btnSendOtpForReset);
        ImageButton btnBackForgotPassword = findViewById(R.id.btnBackForgotPassword);
        progressBar = findViewById(R.id.progressBarRequest);
        tvGeneratedOtpCode = findViewById(R.id.tvGeneratedOtpCode);
    }

    private void setupListeners() {
        btnSendOtpForReset.setOnClickListener(v -> requestOtp());
        findViewById(R.id.btnBackForgotPassword).setOnClickListener(v -> onBackPressed());
    }

    private void requestOtp() {
        String emailOrPhone = etEmailOrPhone.getText().toString().trim();

        if (emailOrPhone.isEmpty()) {
            etEmailOrPhone.setError(getString(R.string.error_enter_email_or_phone));
            etEmailOrPhone.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
            etEmailOrPhone.setError(getString(R.string.error_invalid_email_format));
            etEmailOrPhone.requestFocus();
            return;
        }

        showLoading(true);
        userDAO.open();

        boolean emailExists = userDAO.isEmailExists(emailOrPhone);

        if (emailExists) {
            processOtpGeneration(emailOrPhone);
        } else {
            Toast.makeText(this, getString(R.string.error_email_phone_not_exist), Toast.LENGTH_LONG).show();
            showLoading(false);
        }

        userDAO.close();
    }

    private void processOtpGeneration(String email) {
        Random random = new Random();
        String otpCode = String.format(Locale.getDefault(), "%06d", random.nextInt(1000000));

        // In a real app, you would send this OTP via email/SMS.
        // For this demo, we show it in a dialog.

        tvGeneratedOtpCode.setText(otpCode);
        // Make the demo text visible if you want
        findViewById(R.id.tvOtpDisplayDemo).setVisibility(View.VISIBLE);
        tvGeneratedOtpCode.setVisibility(View.VISIBLE);
        findViewById(R.id.tvOtpInstructionDemo).setVisibility(View.VISIBLE);

        showOtpDialog(email, otpCode);
        Toast.makeText(this, getString(R.string.otp_created_demo), Toast.LENGTH_LONG).show();
        showLoading(false);
    }

    private void showOtpDialog(String email, String otpCode) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.your_demo_otp_code_title))
                .setMessage(getString(R.string.your_demo_otp_code_message, otpCode))
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    Intent intent = new Intent(ForgotPasswordRequestActivity.this, OtpVerificationActivity.class);
                    intent.putExtra("email", email);
                    // In a real app, you would not pass the OTP code like this for security reasons.
                    // This is for demo purposes only.
                    intent.putExtra("otp", otpCode);
                    startActivity(intent);
                })
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnSendOtpForReset.setEnabled(!isLoading);
        etEmailOrPhone.setEnabled(!isLoading);
    }
}