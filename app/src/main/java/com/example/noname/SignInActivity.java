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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;
// import com.google.android.material.floatingactionbutton.FloatingActionButton; // Đã bỏ social login icons, nên không cần import này nữa

public class SignInActivity extends AppCompatActivity {

    private TextView tvLogoTextSignIn;
    private ImageButton btnBack;
    private TextInputEditText etNameEmail;
    private TextInputEditText etPasswordSignIn;
    private TextView tvForgotPassword;
    private Button btnSignIn;
    // private FloatingActionButton fabGoogle; // Đã bỏ
    // private FloatingActionButton fabApple; // Đã bỏ
    // private FloatingActionButton fabFacebook; // Đã bỏ
    private TextView tvSignUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Hide the default ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI elements
        tvLogoTextSignIn = findViewById(R.id.tvLogoTextSignIn);
        btnBack = findViewById(R.id.btnBack);
        etNameEmail = findViewById(R.id.etNameEmail);
        etPasswordSignIn = findViewById(R.id.etPasswordSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        // fabGoogle = findViewById(R.id.fabGoogle); // Đã bỏ
        // fabApple = findViewById(R.id.fabApple); // Đã bỏ
        // fabFacebook = findViewById(R.id.fabFacebook); // Đã bỏ
        tvSignUpLink = findViewById(R.id.tvSignUpLink);

        // Apply custom font and coloring to the text logo
        applyStyledLogoText(tvLogoTextSignIn);

        // Set up Listeners
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity (WelcomeActivity)
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameOrEmail = etNameEmail.getText().toString().trim();
                String password = etPasswordSignIn.getText().toString().trim();

                if (nameOrEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter name/email and password", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Implement your actual sign-in logic here
                    Toast.makeText(SignInActivity.this, "Signing in with: " + nameOrEmail, Toast.LENGTH_SHORT).show();
                    // Example: if (isValidUser(nameOrEmail, password)) {
                    // Go to MainActivity after successful login
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish SignInActivity
                    // } else { showError(); }
                }
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignInActivity.this, "Forgot Password clicked!", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Forgot Password screen
            }
        });

        // fabGoogle.setOnClickListener(new View.OnClickListener() { /* ... */ }); // Đã bỏ
        // fabApple.setOnClickListener(new View.OnClickListener() { /* ... */ }); // Đã bỏ
        // fabFacebook.setOnClickListener(new View.OnClickListener() { /* ... */ }); // Đã bỏ

        tvSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Sign Up Activity
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    // This function is similar to the one in WelcomeActivity, ensure it's here
    private void applyStyledLogoText(TextView textView) {
        String fullText = "Expending Money";
        SpannableString spannableString = new SpannableString(fullText);

        int bicycleStart = 0;
        int bicycleEnd = "Expending".length();
        int shopStart = "Expending ".length();
        int shopEnd = fullText.length();

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.bicycle_text_color)),
                bicycleStart, bicycleEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.shop_text_color)),
                shopStart, shopEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        Typeface customFont = ResourcesCompat.getFont(this, R.font.lobster_regular);
        if (customFont != null) {
            textView.setTypeface(customFont);
        }
    }
}