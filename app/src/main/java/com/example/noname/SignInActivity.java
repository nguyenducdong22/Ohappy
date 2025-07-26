package com.example.noname;

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
import android.database.Cursor; // Import Cursor
import android.util.Log; // Import Log để debug

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;

import com.example.noname.database.UserDAO; // Import UserDAO
import com.example.noname.database.DatabaseHelper; // Import DatabaseHelper để truy cập tên cột
import com.example.noname.utils.PasswordHasher; // Import lớp PasswordHasher

public class SignInActivity extends AppCompatActivity {

    private TextView tvLogoTextSignIn;
    private ImageButton btnBack;
    private TextInputEditText etNameEmail; // Coi đây là trường nhập email
    private TextInputEditText etPasswordSignIn;
    private TextView tvForgotPassword;
    private Button btnSignIn;
    private TextView tvSignUpLink;

    private UserDAO userDAO; // Khai báo UserDAO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

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

        userDAO = new UserDAO(this); // Khởi tạo UserDAO

        applyStyledLogoText(tvLogoTextSignIn);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity (WelcomeActivity)
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etNameEmail.getText().toString().trim(); // Coi trường này là email để đăng nhập
                String plainPassword = etPasswordSignIn.getText().toString().trim(); // Mật khẩu chưa băm

                if (email.isEmpty() || plainPassword.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    userDAO.open(); // Mở database connection
                    Cursor cursor = null;
                    try {
                        // Lấy người dùng theo email
                        cursor = userDAO.getUserByEmail(email);

                        if (cursor != null && cursor.moveToFirst()) {
                            // Lấy password hash đã lưu từ database
                            int passwordHashColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD_HASH);
                            String storedPasswordHash = null;
                            if (passwordHashColumnIndex != -1) {
                                storedPasswordHash = cursor.getString(passwordHashColumnIndex);
                            }

                            // So sánh mật khẩu bằng PasswordHasher
                            if (storedPasswordHash != null && PasswordHasher.verifyPassword(plainPassword, storedPasswordHash)) {
                                Toast.makeText(SignInActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                // Cập nhật thời gian đăng nhập cuối cùng
                                int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                                long userId = -1;
                                if (userIdColumnIndex != -1) {
                                    userId = cursor.getLong(userIdColumnIndex);
                                }
                                if (userId != -1) {
                                    userDAO.updateLastLogin(userId);
                                }

                                // Chuyển sang MainActivity và xóa stack các Activity trước đó
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish(); // Kết thúc SignInActivity
                            } else {
                                // Mật khẩu không khớp hoặc storedPasswordHash null
                                Toast.makeText(SignInActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Không tìm thấy email trong database
                            Toast.makeText(SignInActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (RuntimeException e) {
                        // Ghi log lỗi từ PasswordHasher để debug
                        Log.e("SignInActivity", "Password hashing/verification error: " + e.getMessage(), e);
                        Toast.makeText(SignInActivity.this, "An error occurred during password processing.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // Ghi log các lỗi khác trong quá trình đăng nhập
                        Log.e("SignInActivity", "Error during sign in: " + e.getMessage(), e);
                        Toast.makeText(SignInActivity.this, "An error occurred during login.", Toast.LENGTH_SHORT).show();
                    } finally {
                        if (cursor != null) {
                            cursor.close(); // Đảm bảo đóng Cursor
                        }
                        userDAO.close(); // Đảm bảo đóng database connection
                    }
                }
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignInActivity.this, "Forgot Password clicked!", Toast.LENGTH_SHORT).show();
                // TODO: Implement logic for Forgot Password screen (e.g., start a new Activity)
            }
        });

        tvSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang SignUpActivity
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    // Hàm áp dụng font và màu sắc cho text logo
    private void applyStyledLogoText(TextView textView) {
        String fullText = "Expending Money"; // Văn bản logo
        SpannableString spannableString = new SpannableString(fullText);

        // Định nghĩa các phần của văn bản để tô màu khác nhau
        int expendingStart = 0;
        int expendingEnd = "Expending".length();
        int moneyStart = "Expending ".length(); // Có khoảng trắng giữa
        int moneyEnd = fullText.length();

        // Tô màu cho phần "Expending" (sử dụng màu từ color.xml)
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.bicycle_text_color)),
                expendingStart, expendingEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Tô màu cho phần "Money" (sử dụng màu từ color.xml)
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.shop_text_color)),
                moneyStart, moneyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        // Áp dụng font chữ tùy chỉnh
        Typeface customFont = ResourcesCompat.getFont(this, R.font.lobster_regular);
        if (customFont != null) {
            textView.setTypeface(customFont);
        }
    }
}