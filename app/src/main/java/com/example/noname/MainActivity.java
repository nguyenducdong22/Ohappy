package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the default ActionBar (if you want a full-screen look for main content)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        btnLogout = findViewById(R.id.btnLogout);

        // Optional: Get user's name/email from Intent if passed from login
        // Intent intent = getIntent();
        // if (intent != null && intent.hasExtra("username")) {
        //     String username = intent.getStringExtra("username");
        //     tvWelcomeMessage.setText("Welcome, " + username + "!");
        // }


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement your logout logic here (e.g., clear session, remove tokens)
                Toast.makeText(MainActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

                // After logout, navigate back to the WelcomeActivity or SignInActivity
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                // Clear all previous activities from the stack to prevent going back after logout
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Finish MainActivity
            }
        });
    }
}