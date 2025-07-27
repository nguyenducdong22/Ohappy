// app/src/main/java/com/example/noname/LauncherActivity.java

package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log; // Import Log để ghi log
import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.utils.GeminiApiManager; // Import GeminiApiManager

import java.util.Locale;

public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity"; // Thêm TAG cho Log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- PHẦN MÃ MỚI: ÁP DỤNG NGÔN NGỮ ---
        // Phải được gọi trước super.onCreate() để áp dụng cho màn hình đầu tiên
        applySavedLanguage();
        // ------------------------------------

        super.onCreate(savedInstanceState);

        // --- GỌI WARM-UP API TẠI ĐÂY ---
        // Bạn có thể gửi một prompt rất đơn giản hoặc một chuỗi rỗng
        // để "đánh thức" API và thiết lập kết nối.
        GeminiApiManager.getInstance().generateContent("Ping", new GeminiApiManager.GeminiApiResponseListener() {
            @Override
            public void onSuccess(String responseText) {
                // API warm-up thành công
                Log.d(TAG, "Gemini API warm-up successful! Response: " + responseText);
                // Bạn không cần làm gì thêm ở đây vì LauncherActivity sẽ chuyển màn hình ngay lập tức
            }

            @Override
            public void onFailure(String errorMessage) {
                // API warm-up thất bại
                Log.e(TAG, "Gemini API warm-up failed! Error: " + errorMessage);
                // Xử lý lỗi nếu cần, nhưng thường thì đối với warm-up,
                // bạn vẫn cho phép ứng dụng khởi động bình thường.
            }
        });

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