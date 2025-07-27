package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration; // Import
import android.content.res.Resources;   // Import
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import java.util.Locale; // Import

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- PHẦN MÃ MỚI: ÁP DỤNG NGÔN NGỮ ---
        // Phải được gọi trước super.onCreate() để áp dụng cho màn hình đầu tiên
        applySavedLanguage();
        // ------------------------------------

        super.onCreate(savedInstanceState);

        // --- PHẦN MÃ ÁP DỤNG GIAO DIỆN GIỮ NGUYÊN ---
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        int savedMode = themePrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
        // -------------------------------------------

        // --- PHẦN MÃ KIỂM TRA ĐĂNG NHẬP GIỮ NGUYÊN ---
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = userPrefs.getString("LOGGED_IN_USER_EMAIL", null);

        Intent intent;
        if (userEmail != null) {
            intent = new Intent(LauncherActivity.this, MainActivity.class);
        } else {
            intent = new Intent(LauncherActivity.this, WelcomeActivity.class);
        }

        startActivity(intent);
        finish();
    }

    /**
     * Đọc và áp dụng ngôn ngữ đã được lưu từ SharedPreferences.
     * PHƯƠNG THỨC MỚI
     */
    private void applySavedLanguage() {
        // Đọc lựa chọn ngôn ngữ đã được lưu
        SharedPreferences settingsPrefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        // Mặc định là ngôn ngữ của hệ thống nếu chưa có lựa chọn nào
        String languageCode = settingsPrefs.getString("app_language", Locale.getDefault().getLanguage());
        setLocale(languageCode);
    }

    /**
     * Phương thức để thay đổi ngôn ngữ của ứng dụng.
     * PHƯƠNG THỨC MỚI
     */
    private void setLocale(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return;
        }
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getBaseContext().getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}