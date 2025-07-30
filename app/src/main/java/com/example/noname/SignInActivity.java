package com.example.noname; // Đảm bảo đúng package của bạn

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;

import com.example.noname.Forgotpassword.ForgotPasswordRequestActivity;
import com.example.noname.account.BaseActivity;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.LocaleHelper;
import com.example.noname.database.UserDAO;
import com.example.noname.utils.PasswordHasher;
import com.google.android.material.textfield.TextInputEditText;

public class SignInActivity extends BaseActivity {

    private TextInputEditText etNameEmail;
    private TextInputEditText etPasswordSignIn;
    private UserDAO userDAO;
    private VideoView videoBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // **QUAN TRỌNG: THÊM DÒNG NÀY ĐỂ CHO PHÉP GIAO DIỆN TRÀN VIỀN**
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false); // TH&Ecirc;M D&Ograve;NG N&Agrave;Y
        setContentView(R.layout.activity_sign_in);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Khởi tạo các thành phần
        userDAO = new UserDAO(this);
        initializeViewsAndListeners();
        setupVideoBackground();

        TextView tvLogoTextSignIn = findViewById(R.id.tvLogoTextSignIn);
        applyStyledLogoText(tvLogoTextSignIn);
    }

    private void initializeViewsAndListeners() {
        videoBackground = findViewById(R.id.video_background);
        etNameEmail = findViewById(R.id.etNameEmail);
        etPasswordSignIn = findViewById(R.id.etPasswordSignIn);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvSignUpLink = findViewById(R.id.tvSignUpLink);
        ImageButton btnChangeLanguage = findViewById(R.id.btnChangeLanguage);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnSignIn.setOnClickListener(v -> signInUser());
        tvSignUpLink.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, ForgotPasswordRequestActivity.class)));
        btnChangeLanguage.setOnClickListener(v -> showLanguageSelectionDialog());
    }

    private void signInUser() {
        String email = etNameEmail.getText().toString().trim();
        String plainPassword = etPasswordSignIn.getText().toString().trim();

        if (email.isEmpty() || plainPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        userDAO.open();
        Cursor cursor = null;
        try {
            cursor = userDAO.getUserByEmail(email);
            if (cursor != null && cursor.moveToFirst()) {
                String storedPasswordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH));
                if (PasswordHasher.verifyPassword(plainPassword, storedPasswordHash)) {
                    // Đăng nhập thành công
                    SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("LOGGED_IN_USER_EMAIL", email).apply();

                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    userDAO.updateLastLogin(userId);

                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) cursor.close();
            userDAO.close();
        }
    }

    private void showLanguageSelectionDialog() {
        final String[] languages = {"Tiếng Việt", "English"};
        final String[] languageCodes = {"vi", "en"};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    LocaleHelper.setLocale(this, languageCodes[which]);
                    recreate();
                })
                .show();
    }

    // --- CÁC HÀM QUẢN LÝ VIDEO VÀ STYLE (GIỮ NGUYÊN) ---
    private void setupVideoBackground() {
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.login_background;
        Uri videoUri = Uri.parse(videoPath);
        videoBackground.setVideoURI(videoUri);
        videoBackground.start();
        videoBackground.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoBackground != null && !videoBackground.isPlaying()) {
            videoBackground.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoBackground != null && videoBackground.isPlaying()) {
            videoBackground.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoBackground != null) {
            videoBackground.stopPlayback();
        }
    }

    private void applyStyledLogoText(TextView textView) {
        String fullText = getString(R.string.expending_money_logo);
        // ... (phần code style này có thể giữ nguyên)
        textView.setText(fullText);
        Typeface customFont = ResourcesCompat.getFont(this, R.font.lobster_regular);
        if (customFont != null) {
            textView.setTypeface(customFont);
        }
    }
}