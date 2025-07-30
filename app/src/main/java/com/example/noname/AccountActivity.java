package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration; // Import
import android.content.res.Resources;   // Import
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.noname.account.EditProfileActivity;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.UserDAO;
import com.example.noname.databinding.ActivityAccountBinding;
import com.example.noname.account.BaseActivity;
import com.example.noname.database.LocaleHelper;
import com.example.noname.account.NotificationSettingsActivity;
import com.example.noname.account.SecurityActivity;
import com.example.noname.account.HelpCenterActivity;
import com.example.noname.account.LegalActivity;
import com.example.noname.account.LauncherActivity;

import java.util.Locale; // Import

public class AccountActivity extends BaseActivity {

    private ActivityAccountBinding binding;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- PHẦN MÃ MỚI: THIẾT LẬP TOOLBAR VÀ NÚT BACK ---
        setSupportActionBar(binding.toolbarAccount);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarAccount.setNavigationOnClickListener(v -> onBackPressed());
        // ---------------------------------------------------


        userDAO = new UserDAO(this);

        loadUserProfileFromDatabase();
        setupOptionListeners();
        setupActionListeners();

    }

    private void loadUserProfileFromDatabase() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        if (userEmail == null) {
            Toast.makeText(this, "Không thể xác định người dùng!", Toast.LENGTH_LONG).show();
            return;
        }

        userDAO.open();
        Cursor cursor = null;
        try {
            cursor = userDAO.getUserByEmail(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                int fullNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FULL_NAME);
                int emailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL);

                String fullName = (fullNameIndex != -1) ? cursor.getString(fullNameIndex) : "Không có tên";
                String email = (emailIndex != -1) ? cursor.getString(emailIndex) : "Không có email";

                binding.tvFullName.setText(fullName);
                binding.tvEmail.setText(email);
            } else {
                Log.e("AccountActivity", "Không tìm thấy người dùng với email: " + userEmail);
                Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Lỗi khi tải thông tin người dùng", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            userDAO.close();
        }
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("LOGGED_IN_USER_EMAIL");
        editor.apply();

        showToast("Đăng xuất thành công!");

        Intent intent = new Intent(AccountActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Thiết lập các sự kiện click cho các tùy chọn trong danh sách.
     * PHIÊN BẢN ĐÃ CẬP NHẬT
     */
    /**
     * Thiết lập các sự kiện click cho các tùy chọn trong danh sách.
     */
    private void setupOptionListeners() {
        // Mở màn hình Chỉnh sửa thông tin
        binding.optionEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class))
        );

        // ...
        // Cập nhật listener này
        binding.optionSecurity.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, SecurityActivity.class));
        });
        // ...

        // SỬA LẠI DÒNG NÀY: Mở màn hình Cài đặt thông báo
        binding.optionNotifications.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, NotificationSettingsActivity.class))
        );

        // Mở hộp thoại chọn Giao diện
        binding.optionTheme.setOnClickListener(v -> showThemeSelectionDialog());

        // Mở hộp thoại chọn Ngôn ngữ
        binding.optionLanguage.setOnClickListener(v -> showLanguageSelectionDialog());

        // Cập nhật listener này
        binding.optionHelpCenter.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, HelpCenterActivity.class));
        });
        // ...
        // Cập nhật listener này
        binding.optionTerms.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, LegalActivity.class));
        });
        binding.optionRecurringExpenses.setOnClickListener(v -> {

            Intent intent = new Intent(AccountActivity.this, RecurringExpensesActivity.class);
            startActivity(intent);
        });
        // ...

    }

    private void setupActionListeners() {
        binding.btnLogout.setOnClickListener(v -> logoutUser());
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmationDialog());
    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản của mình không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteCurrentUserAccount();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    private void deleteCurrentUserAccount() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        if (userEmail == null) {
            Toast.makeText(this, "Lỗi: Không thể xác định tài khoản.", Toast.LENGTH_SHORT).show();
            return;
        }

        userDAO.open();
        try {
            int rowsDeleted = userDAO.deleteUserByEmail(userEmail);
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Tài khoản đã được xóa.", Toast.LENGTH_SHORT).show();
                logoutUser();
            } else {
                Toast.makeText(this, "Xóa tài khoản thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Lỗi khi xóa tài khoản: ", e);
            Toast.makeText(this, "Đã xảy ra lỗi trong quá trình xóa.", Toast.LENGTH_SHORT).show();
        } finally {
            userDAO.close();
        }
    }

    // Trong AccountActivity.java (đã kế thừa từ BaseActivity)

    // Phương thức này giờ đã đúng và sẽ không xung đột
    private void showThemeSelectionDialog() {
        final String[] themes = {"Sáng", "Tối", "Hệ thống"};
        final int[] themeModes = {
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        };

        new AlertDialog.Builder(this) // Sửa ở đây
                .setTitle(getString(R.string.select_theme))
                .setItems(themes, (dialog, which) -> {
                    int selectedMode = themeModes[which];
                    AppCompatDelegate.setDefaultNightMode(selectedMode);

                    // Sửa ở đây
                    SharedPreferences prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putInt("theme_mode", selectedMode).apply();
                })
                .show();
    }
    // Phương thức này giờ đã đúng và sẽ không xung đột
    private void showLanguageSelectionDialog() {
        final String[] languages = {"Tiếng Việt", "English"};
        final String[] languageCodes = {"vi", "en"};

        new AlertDialog.Builder(this) // Sửa ở đây
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    // Sửa ở đây
                    LocaleHelper.setLocale(this, languageCodes[which]);

                    // Sửa ở đây
                    Intent intent = new Intent(this, LauncherActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
    /**
     * Phương thức để thay đổi ngôn ngữ của ứng dụng.
     * PHƯƠNG THỨC MỚI
     */
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    protected void onResume() {
        super.onResume();
        // Tải lại thông tin người dùng mỗi khi quay lại màn hình này
        // để hiển thị các thay đổi mới nhất.
        loadUserProfileFromDatabase();
    }

}