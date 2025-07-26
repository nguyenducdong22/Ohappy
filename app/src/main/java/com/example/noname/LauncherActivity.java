package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kiểm tra SharedPreferences để xem email của người dùng có được lưu không
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        Intent intent;
        if (userEmail != null) {
            // 2. Nếu có email, người dùng đã đăng nhập -> Chuyển thẳng vào MainActivity
            intent = new Intent(LauncherActivity.this, MainActivity.class);
        } else {
            // 3. Nếu không có email, người dùng chưa đăng nhập -> Chuyển đến màn hình chào mừng
            intent = new Intent(LauncherActivity.this, WelcomeActivity.class);
        }

        // Bắt đầu Activity tiếp theo và kết thúc LauncherActivity
        // để người dùng không thể nhấn nút "Back" quay lại màn hình này.
        startActivity(intent);
        finish();
    }
}