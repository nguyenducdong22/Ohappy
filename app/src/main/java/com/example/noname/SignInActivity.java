package com.example.noname;

// Thêm 2 import này
import android.content.Context;
import android.content.SharedPreferences;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;

import com.example.noname.database.UserDAO;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.utils.PasswordHasher;

public class SignInActivity extends AppCompatActivity {

    // ... (Khai báo biến giữ nguyên)
    private TextView tvLogoTextSignIn;
    private ImageButton btnBack;
    private TextInputEditText etNameEmail;
    private TextInputEditText etPasswordSignIn;
    private TextView tvForgotPassword;
    private Button btnSignIn;
    private TextView tvSignUpLink;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // ... (Mã trong onCreate giữ nguyên)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvLogoTextSignIn = findViewById(R.id.tvLogoTextSignIn);
        btnBack = findViewById(R.id.btnBack);
        etNameEmail = findViewById(R.id.etNameEmail);
        etPasswordSignIn = findViewById(R.id.etPasswordSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);

        userDAO = new UserDAO(this);

        applyStyledLogoText(tvLogoTextSignIn);

        btnBack.setOnClickListener(v -> onBackPressed());

        // --- BẮT ĐẦU PHẦN CẬP NHẬT ---
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etNameEmail.getText().toString().trim();
                String plainPassword = etPasswordSignIn.getText().toString().trim();

                if (email.isEmpty() || plainPassword.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return; // Thêm return để dừng sớm
                }

                userDAO.open();
                Cursor cursor = null;
                try {
                    cursor = userDAO.getUserByEmail(email);

                    if (cursor != null && cursor.moveToFirst()) {
                        int passwordHashColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD_HASH);
                        String storedPasswordHash = (passwordHashColumnIndex != -1) ? cursor.getString(passwordHashColumnIndex) : null;

                        if (storedPasswordHash != null && PasswordHasher.verifyPassword(plainPassword, storedPasswordHash)) {
                            // --- PHẦN MÃ MỚI ĐƯỢC THÊM VÀO ---
                            // 1. LƯU EMAIL VÀO SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("LOGGED_IN_USER_EMAIL", email);
                            editor.apply();
                            // ------------------------------------

                            Toast.makeText(SignInActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // 2. Cập nhật thời gian đăng nhập cuối cùng
                            int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                            if (userIdColumnIndex != -1) {
                                long userId = cursor.getLong(userIdColumnIndex);
                                userDAO.updateLastLogin(userId);
                            }

                            // 3. Chuyển sang MainActivity
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("SignInActivity", "Error during sign in", e);
                    Toast.makeText(SignInActivity.this, "An error occurred during login.", Toast.LENGTH_SHORT).show();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    userDAO.close();
                }
            }
        });
        // --- KẾT THÚC PHẦN CẬP NHẬT ---

        tvForgotPassword.setOnClickListener(v -> Toast.makeText(SignInActivity.this, "Forgot Password clicked!", Toast.LENGTH_SHORT).show());
        tvSignUpLink.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
    }

    // ... (Hàm applyStyledLogoText giữ nguyên)
    private void applyStyledLogoText(TextView textView) {
        String fullText = "Expending Money";
        SpannableString spannableString = new SpannableString(fullText);

        int expendingStart = 0;
        int expendingEnd = "Expending".length();
        int moneyStart = "Expending ".length();
        int moneyEnd = fullText.length();

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.bicycle_text_color)),
                expendingStart, expendingEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.shop_text_color)),
                moneyStart, moneyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        Typeface customFont = ResourcesCompat.getFont(this, R.font.lobster_regular);
        if (customFont != null) {
            textView.setTypeface(customFont);
        }
    }
}