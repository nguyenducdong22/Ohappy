package com.example.noname;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView; // Import ImageView

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class WelcomeActivity extends AppCompatActivity {

    private ImageView backgroundImageWelcome; // Declare ImageView
    private View overlayWelcome; // Declare overlay View

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Hide the default ActionBar for a full-screen look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize background elements (optional, if you want to manipulate them in code)
        backgroundImageWelcome = findViewById(R.id.background_image_welcome);
        overlayWelcome = findViewById(R.id.overlay_welcome);


        TextView tvLogoText = findViewById(R.id.tvLogoText);
        Button btnGetStarted = findViewById(R.id.btnGetStarted);

        // Apply custom font and coloring to the text logo
        applyStyledLogoText(tvLogoText);

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Sign In Activity
                Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                startActivity(intent);
                // Optional: finish() WelcomeActivity if you don't want to come back to it
                // finish();
            }
        });
    }

    private void applyStyledLogoText(TextView textView) {
        String fullText = "Expending  Money";
        SpannableString spannableString = new SpannableString(fullText);

        int bicycleStart = 0;
        int bicycleEnd = "Expending ".length();
        int shopStart = "Expending  ".length();
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