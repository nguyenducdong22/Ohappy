package com.example.noname.Forgotpassword;

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
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.UserDAO;

import java.util.Locale;
import java.util.Random;
import android.database.Cursor;

public class ForgotPasswordRequestActivity extends AppCompatActivity {

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
            etEmailOrPhone.setError("Please enter your email or phone number.");
            etEmailOrPhone.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
            etEmailOrPhone.setError("Please enter a valid email format.");
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
                        .setTitle("Your DEMO OTP Code")
                        .setMessage("This is a demo OTP code. In a real application, this code would be sent via Email/SMS.\n\nOTP Code: " + otpCode + "\n\n(This code will expire in 5 minutes)")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(ForgotPasswordRequestActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", emailOrPhone);
                            startActivity(intent);
                        })
                        .setCancelable(false)
                        .show();

                Toast.makeText(this, "OTP code has been generated and displayed (DEMO).", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to create OTP code. Please try again.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Email or phone number does not exist in the system.", Toast.LENGTH_LONG).show();
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