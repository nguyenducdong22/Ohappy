package com.example.noname.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.MainActivity;
import com.example.noname.WelcomeActivity;
import com.example.noname.utils.GeminiApiManager; // Đảm bảo import này đã có

import java.util.Locale;

public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- PHẦN MÃ MỚI: ÁP DỤNG NGÔN NGỮ ---
        // Phải được gọi trước super.onCreate() để áp dụng cho màn hình đầu tiên
        applySavedLanguage();
        // ------------------------------------

        super.onCreate(savedInstanceState);

        // --- GỌI WARM-UP API TẠI ĐÂY ---
        // Gửi một prompt rất đơn giản hoặc một chuỗi rỗng
        // để "đánh thức" API và thiết lập kết nối.
        // Đã sửa lỗi GeminiApiManager.getInstance() thành GeminiApiManager.generateContent()
        GeminiApiManager.generateContent("Ping", new GeminiApiManager.GeminiApiResponseListener() {
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
        // Deprecated trong API 25+ nhưng vẫn hoạt động cho các API thấp hơn và là cách đơn giản
        // Nếu bạn muốn hỗ trợ API 24+ tốt hơn, hãy dùng context.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}