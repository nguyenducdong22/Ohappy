package com.example.noname;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log; // Import Log để debug

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;

import com.example.noname.database.UserDAO; // Import UserDAO
import com.example.noname.utils.PasswordHasher; // Import lớp PasswordHasher

public class SignUpActivity extends AppCompatActivity {

    private TextView tvLogoTextSignUp;
    private ImageButton btnBackSignUp;
    // Khai báo biến UI để khớp với IDs và thứ tự mới trong XML
    private TextInputEditText etEmailSignUp; // ID: etEmailSignUp, Hint: Email
    private TextInputEditText etFullNameSignUp; // ID: etFullNameSignUp, Hint: Full Name
    private TextInputEditText etPhoneNumberSignUp; // ID: etPhoneNumberSignUp, Hint: Số điện thoại
    private TextInputEditText etPasswordSignUp; // ID: etPasswordSignUp, Hint: Password
    private TextInputEditText etConfirmPasswordSignUp; // ID: etConfirmPasswordSignUp, Hint: Confirm Password

    private CheckBox cbTermsAndConditions;
    private TextView tvTermsAndConditions;
    private Button btnSignUp;
    private TextView tvSignInLink;

    private UserDAO userDAO; // Khai báo UserDAO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI elements - ÁNH XẠ THEO CÁC ID MỚI TRONG XML VÀ THỨ TỰ LOGICAL
        tvLogoTextSignUp = findViewById(R.id.tvLogoTextSignUp);
        btnBackSignUp = findViewById(R.id.btnBackSignUp);

        etEmailSignUp = findViewById(R.id.etEmailSignUp);          // Trường Email đầu tiên
        etFullNameSignUp = findViewById(R.id.etFullNameSignUp);    // Trường Full Name
        etPhoneNumberSignUp = findViewById(R.id.etPhoneNumberSignUp); // Trường Phone Number
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);      // Trường Password
        etConfirmPasswordSignUp = findViewById(R.id.etConfirmPasswordSignUp); // Trường Confirm Password

        cbTermsAndConditions = findViewById(R.id.cbTermsAndConditions);
        tvTermsAndConditions = findViewById(R.id.tvTermsAndConditions);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignInLink = findViewById(R.id.tvSignInLink);

        userDAO = new UserDAO(this); // Khởi tạo UserDAO

        applyStyledLogoText(tvLogoTextSignUp);

        // Set up Listeners
        btnBackSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity (SignInActivity)
            }
        });

        tvTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUpActivity.this, "Terms & Conditions clicked!", Toast.LENGTH_SHORT).show();
                // TODO: Implement logic to show Terms & Conditions
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy dữ liệu từ các trường UI đã được ánh xạ đúng
                String email = etEmailSignUp.getText().toString().trim();
                String fullName = etFullNameSignUp.getText().toString().trim();
                String phoneNumber = etPhoneNumberSignUp.getText().toString().trim();
                String plainPassword = etPasswordSignUp.getText().toString().trim();
                String confirmPassword = etConfirmPasswordSignUp.getText().toString().trim();

                // Kiểm tra các trường rỗng
                if (email.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty() || plainPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra mật khẩu khớp
                if (!plainPassword.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra chấp nhận điều khoản
                if (!cbTermsAndConditions.isChecked()) {
                    Toast.makeText(SignUpActivity.this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show();
                    return;
                }

                userDAO.open(); // Mở database connection
                try {
                    // Kiểm tra email đã tồn tại (email là UNIQUE trong DB)
                    if (userDAO.isEmailExists(email)) {
                        Toast.makeText(SignUpActivity.this, "Email already exists. Please sign in or use another email.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Băm mật khẩu bằng PasswordHasher
                    String hashedPassword = PasswordHasher.hashPassword(plainPassword);

                    // Tạo người dùng mới trong database với các thông tin đã thu thập
                    // CHỮ KÝ PHƯƠNG THỨC createUser() TRONG USERDAO PHẢI LÀ:
                    // public long createUser(String email, String fullName, String phoneNumber, String passwordHash)
                    long userId = userDAO.createUser(email, fullName, phoneNumber, hashedPassword);
                    if (userId != -1) {
                        Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        Log.d("SignUpActivity", "User registered: " + email + " with ID: " + userId);

                        // Chuyển sang MainActivity sau khi đăng ký thành công
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Kết thúc SignUpActivity
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e("SignUpActivity", "Failed to create user in database.");
                    }
                } catch (RuntimeException e) {
                    // Ghi log lỗi từ PasswordHasher nếu có vấn đề trong quá trình băm/xác thực mật khẩu
                    Log.e("SignUpActivity", "Password hashing error: " + e.getMessage(), e);
                    Toast.makeText(SignUpActivity.this, "Error during password processing.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Ghi log các lỗi khác trong quá trình đăng ký
                    Log.e("SignUpActivity", "Error during sign up: " + e.getMessage(), e);
                    Toast.makeText(SignUpActivity.this, "An error occurred during registration.", Toast.LENGTH_SHORT).show();
                } finally {
                    userDAO.close(); // Đảm bảo đóng database connection
                }
            }
        });

        tvSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển về SignInActivity
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish(); // Kết thúc SignUpActivity để người dùng không quay lại được bằng nút Back
            }
        });
    }

    // Hàm áp dụng font và màu sắc cho text logo "Expending Money"
    private void applyStyledLogoText(TextView textView) {
        String fullText = "Expending Money";
        SpannableString spannableString = new SpannableString(fullText);

        int expendingStart = 0;
        int expendingEnd = "Expending".length();
        int moneyStart = "Expending ".length();
        int moneyEnd = fullText.length();

        // Tô màu cho phần "Expending"
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.bicycle_text_color)),
                expendingStart, expendingEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Tô màu cho phần "Money"
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.shop_text_color)),
                moneyStart, moneyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        // Áp dụng font chữ tùy chỉnh "lobster_regular"
        Typeface customFont = ResourcesCompat.getFont(this, R.font.lobster_regular);
        if (customFont != null) {
            textView.setTypeface(customFont);
        }
    }
}