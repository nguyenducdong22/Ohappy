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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextView tvLogoTextSignUp;
    private ImageButton btnBackSignUp;
    private TextInputEditText etNameOrEmailSignUp;
    private TextInputEditText etEmailSignUp;
    private TextInputEditText etNombreSignUp; // For "Nombre" field
    private TextInputEditText etPasswordSignUp;
    private TextInputEditText etConfirmPasswordSignUp;
    private CheckBox cbTermsAndConditions;
    private TextView tvTermsAndConditions; // The text part of terms and conditions
    private Button btnSignUp;
    private TextView tvSignInLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Hide the default ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI elements
        tvLogoTextSignUp = findViewById(R.id.tvLogoTextSignUp);
        btnBackSignUp = findViewById(R.id.btnBackSignUp);
        etNameOrEmailSignUp = findViewById(R.id.etNameOrEmailSignUp);
        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        etNombreSignUp = findViewById(R.id.etNombreSignUp); // Initialize "Nombre"
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        etConfirmPasswordSignUp = findViewById(R.id.etConfirmPasswordSignUp);
        cbTermsAndConditions = findViewById(R.id.cbTermsAndConditions);
        tvTermsAndConditions = findViewById(R.id.tvTermsAndConditions);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignInLink = findViewById(R.id.tvSignInLink);

        // Apply custom font and coloring to the text logo
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
                // TODO: Navigate to Terms & Conditions screen or show dialog
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameOrEmail = etNameOrEmailSignUp.getText().toString().trim();
                String email = etEmailSignUp.getText().toString().trim();
                String nombre = etNombreSignUp.getText().toString().trim(); // Get Nombre
                String password = etPasswordSignUp.getText().toString().trim();
                String confirmPassword = etConfirmPasswordSignUp.getText().toString().trim();

                if (nameOrEmail.isEmpty() || email.isEmpty() || nombre.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else if (!cbTermsAndConditions.isChecked()) {
                    Toast.makeText(SignUpActivity.this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Implement your actual sign-up logic here
                    Toast.makeText(SignUpActivity.this, "Signing Up with: " + email, Toast.LENGTH_SHORT).show();
                    // Example: if (registerUser(nameOrEmail, email, nombre, password)) {
                    // Go to MainActivity after successful registration
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish SignUpActivity
                    // } else { showError(); }
                }
            }
        });

        tvSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the Sign In Activity
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish(); // Finish SignUpActivity so user doesn't come back here with back button
            }
        });
    }

    // This function is similar to the one in WelcomeActivity and SignInActivity
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