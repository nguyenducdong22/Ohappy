package com.example.noname; // Đảm bảo đúng package của bạn

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import android.database.Cursor;
import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.noname.account.BaseActivity;
import com.example.noname.database.LocaleHelper;
import com.google.android.material.textfield.TextInputEditText;

import com.example.noname.database.UserDAO;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.utils.PasswordHasher;
import com.example.noname.Forgotpassword.ForgotPasswordRequestActivity; // THÊM IMPORT NÀY


public class SignInActivity extends BaseActivity {

    private TextView tvLogoTextSignIn;
    private ImageButton btnBack;
    private TextInputEditText etNameEmail;
    private TextInputEditText etPasswordSignIn;
    private TextView tvForgotPassword;
    private Button btnSignIn;
    private TextView tvSignUpLink;
    private UserDAO userDAO;

    private VideoView videoBackground;

    private ImageButton btnChangeLanguage;



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
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage);

        userDAO = new UserDAO(this);


        applyStyledLogoText(tvLogoTextSignIn);

        // --- KHỞI TẠO VÀ CẤU HÌNH VIDEOVIEW BẮT ĐẦU ---
        videoBackground = findViewById(R.id.video_background);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.login_background; // Đảm bảo login_background.mp4 hoặc .avi ... nằm trong res/raw
        Uri videoUri = Uri.parse(videoPath);
        videoBackground.setVideoURI(videoUri);

        videoBackground.start();

        videoBackground.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f); // Tắt âm lượng video

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();

            float videoRatio = (float) videoWidth / videoHeight;

            int calculatedWidth = (int) (screenHeight * videoRatio);

            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) videoBackground.getLayoutParams();

            layoutParams.height = screenHeight;
            layoutParams.width = calculatedWidth;

            layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

            videoBackground.setLayoutParams(layoutParams);
        });

        // --- KHỞI TẠO VÀ CẤU HÌNH VIDEOVIEW KẾT THÚC ---


        btnBack.setOnClickListener(v -> onBackPressed());

        // --- BẮT ĐẦU PHẦN XỬ LÝ ĐĂNG NHẬP (GIỮ NGUYÊN) ---
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etNameEmail.getText().toString().trim();
                String plainPassword = etPasswordSignIn.getText().toString().trim();

                if (email.isEmpty() || plainPassword.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                userDAO.open();
                Cursor cursor = null;
                try {
                    cursor = userDAO.getUserByEmail(email);

                    if (cursor != null && cursor.moveToFirst()) {
                        int passwordHashColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD_HASH);
                        String storedPasswordHash = (passwordHashColumnIndex != -1) ? cursor.getString(passwordHashColumnIndex) : null;

                        if (storedPasswordHash != null && PasswordHasher.verifyPassword(plainPassword, storedPasswordHash)) {
                            SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("LOGGED_IN_USER_EMAIL", email);
                            editor.apply();

                            Toast.makeText(SignInActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                            if (userIdColumnIndex != -1) {
                                long userId = cursor.getLong(userIdColumnIndex);
                                userDAO.updateLastLogin(userId);
                            }

                            Intent intent = new Intent(SignInActivity.this, MainActivity.class); // Thay MainActivity bằng Activity chính của bạn
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
        // --- KẾT THÚC PHẦN XỬ LÝ ĐĂNG NHẬP ---

        // ĐÃ SỬA: Thay thế Toast bằng Intent để mở ForgotPasswordRequestActivity
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, com.example.noname.Forgotpassword.ForgotPasswordRequestActivity.class);
            startActivity(intent);
        });

        tvSignUpLink.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));

        btnChangeLanguage.setOnClickListener(v -> showLanguageSelectionDialog());



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

    // --- QUẢN LÝ VÒNG ĐỜI CỦA VIDEOVIEW BẮT ĐẦU ---
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
            videoBackground = null;
        }
    }
    // --- QUẢN LÝ VÒNG ĐỜI CỦA VIDEOVIEW KẾT THÚC ---


    // Hàm applyStyledLogoText giữ nguyên
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