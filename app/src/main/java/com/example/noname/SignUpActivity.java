package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.example.noname.database.UserDAO;
import com.example.noname.utils.PasswordHasher;
import com.example.noname.account.BaseActivity;

// QUAN TRỌNG: Kế thừa từ BaseActivity
public class SignUpActivity extends BaseActivity {

    private TextInputEditText etEmailSignUp, etFullNameSignUp, etPhoneNumberSignUp,
            etPasswordSignUp, etConfirmPasswordSignUp;
    private CheckBox cbTermsAndConditions;
    private Button btnSignUp;
    private TextView tvSignInLink;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userDAO = new UserDAO(this);
        initializeViews();
        setupListeners();

        TextView tvLogoTextSignUp = findViewById(R.id.tvLogoTextSignUp);
        applyStyledLogoText(tvLogoTextSignUp);
    }

    private void initializeViews() {
        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        etFullNameSignUp = findViewById(R.id.etFullNameSignUp);
        etPhoneNumberSignUp = findViewById(R.id.etPhoneNumberSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        etConfirmPasswordSignUp = findViewById(R.id.etConfirmPasswordSignUp);
        cbTermsAndConditions = findViewById(R.id.cbTermsAndConditions);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignInLink = findViewById(R.id.tvSignInLink);
        ImageButton btnBackSignUp = findViewById(R.id.btnBackSignUp);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> registerUser());
        tvSignInLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        });

        ImageButton btnBackSignUp = findViewById(R.id.btnBackSignUp);
        btnBackSignUp.setOnClickListener(v -> onBackPressed());
    }

    private void registerUser() {
        String email = etEmailSignUp.getText().toString().trim();
        String fullName = etFullNameSignUp.getText().toString().trim();
        String phoneNumber = etPhoneNumberSignUp.getText().toString().trim();
        String plainPassword = etPasswordSignUp.getText().toString().trim();
        String confirmPassword = etConfirmPasswordSignUp.getText().toString().trim();

        if (email.isEmpty() || fullName.isEmpty() || plainPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!plainPassword.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.error_passwords_do_not_match), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!cbTermsAndConditions.isChecked()) {
            Toast.makeText(this, getString(R.string.error_agree_to_terms), Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = PasswordHasher.hashPassword(plainPassword);

        // Kiểm tra email đã tồn tại chưa
        if (userDAO.isEmailExists(email)) {
            Toast.makeText(this, getString(R.string.error_email_exists), Toast.LENGTH_LONG).show();
            return;
        }

        // Gọi phương thức createUser, nó sẽ không tạo ví mặc định nữa
        long userId = userDAO.createUser(email, fullName, phoneNumber, hashedPassword);

        if (userId != -1) {
            Log.d("SignUpActivity", "User registered successfully with ID: " + userId);

            // TẠO VÍ MẶC ĐỊNH NGAY SAU KHI TẠO NGƯỜI DÙNG
            userDAO.createDefaultWalletsForUser(userId);

            Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show();
            Log.e("SignUpActivity", "Failed to create user in database.");
        }
    }

    private void applyStyledLogoText(TextView textView) {
        String fullText = "Expending Money";
        SpannableString spannableString = new SpannableString(fullText);
        int expendingEnd = "Expending".length();
        int moneyStart = "Expending ".length();

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.bicycle_text_color)),
                0, expendingEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.shop_text_color)),
                moneyStart, fullText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        Typeface customFont = ResourcesCompat.getFont(this, R.font.lobster_regular);
        if (customFont != null) {
            textView.setTypeface(customFont);
        }
    }
}