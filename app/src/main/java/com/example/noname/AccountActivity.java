package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

// Đảm bảo các import này là đúng và các lớp này tồn tại
import com.example.noname.account.EditProfileActivity;
import com.example.noname.account.LauncherActivity; // Dùng để khởi động lại app sau đổi ngôn ngữ
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.UserDAO;
import com.example.noname.databinding.ActivityAccountBinding; // Nếu bạn dùng ViewBinding
import com.example.noname.account.BaseActivity; // Lớp BaseActivity mà AccountActivity kế thừa
import com.example.noname.account.LocaleHelper; // Lớp LocaleHelper mà bạn đã tạo/có
import com.example.noname.account.NotificationSettingsActivity;
import com.example.noname.account.SecurityActivity;
import com.example.noname.account.HelpCenterActivity;
import com.example.noname.account.LegalActivity;
import android.webkit.CookieManager;
import android.webkit.WebStorage;

public class AccountActivity extends BaseActivity { // Kế thừa từ BaseActivity

    private ActivityAccountBinding binding;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewBinding
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập Toolbar
        setSupportActionBar(binding.toolbarAccount);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Nút back
            getSupportActionBar().setTitle(R.string.title_account); // Đặt tiêu đề từ strings.xml
        }
        binding.toolbarAccount.setNavigationOnClickListener(v -> onBackPressed());

        userDAO = new UserDAO(this);

        // Tải thông tin người dùng từ DB
        loadUserProfileFromDatabase();
        // Thiết lập các sự kiện click cho các tùy chọn
        setupOptionListeners();
        // Thiết lập các sự kiện click cho các hành động (Đăng xuất, Xóa tài khoản)
        setupActionListeners();
    }

    // Phương thức này được gọi khi Activity quay trở lại foreground
    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại thông tin người dùng mỗi khi quay lại màn hình này
        // để hiển thị các thay đổi mới nhất (ví dụ: sau khi chỉnh sửa hồ sơ).
        loadUserProfileFromDatabase();
    }

    private void loadUserProfileFromDatabase() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        if (userEmail == null) {
            showToast("Không thể xác định người dùng!");
            // Có thể chuyển hướng về màn hình đăng nhập nếu không xác định được người dùng
            // Intent intent = new Intent(this, SignInActivity.class);
            // startActivity(intent);
            // finish();
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
                // TODO: Tải và hiển thị avatar nếu có (binding.ivAvatar.setImageResource(...))
            } else {
                Log.e("AccountActivity", "Không tìm thấy người dùng với email: " + userEmail);
                showToast("Lỗi: Không tìm thấy thông tin người dùng.");
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Lỗi khi tải thông tin người dùng", e);
            showToast("Đã xảy ra lỗi khi tải thông tin.");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            userDAO.close();
        }
    }

    /**
     * Thiết lập các sự kiện click cho các tùy chọn trong danh sách.
     */
    private void setupOptionListeners() {
        // Chỉnh sửa thông tin
        binding.optionEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class))
        );

        // Bảo mật
        binding.optionSecurity.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, SecurityActivity.class))
        );

        // Cài đặt thông báo
        binding.optionNotifications.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, NotificationSettingsActivity.class))
        );

        // Giao diện (Theme)
        binding.optionTheme.setOnClickListener(v -> showThemeSelectionDialog());

        // Ngôn ngữ
        binding.optionLanguage.setOnClickListener(v -> showLanguageSelectionDialog());

        // --- THÊM LISTENER CHO "CHI TIÊU ĐỊNH KỲ" ---
        binding.optionRecurringExpenses.setOnClickListener(v -> {
            showToast("Mở màn hình Chi tiêu định kỳ");
            Intent intent = new Intent(AccountActivity.this, RecurringExpensesActivity.class);
            startActivity(intent);
        });

        // Trung tâm trợ giúp
        binding.optionHelpCenter.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, HelpCenterActivity.class))
        );

        // Điều khoản & Chính sách
        binding.optionTerms.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, LegalActivity.class))
        );
    }

    private void setupActionListeners() {
        binding.btnLogout.setOnClickListener(v -> logoutUser());
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmationDialog());
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("LOGGED_IN_USER_EMAIL");
        editor.apply();

        showToast("Đăng xuất thành công!");

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(AccountActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        // Xóa dữ liệu web storage
        WebStorage webStorage = WebStorage.getInstance();
        webStorage.deleteAllData();


    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản của mình không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteCurrentUserAccount();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_warning) // Đảm bảo icon ic_warning tồn tại
                .show();
    }

    private void deleteCurrentUserAccount() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        if (userEmail == null) {
            showToast("Lỗi: Không thể xác định tài khoản.");
            return;
        }

        userDAO.open();
        try {
            int rowsDeleted = userDAO.deleteUserByEmail(userEmail);
            if (rowsDeleted > 0) {
                showToast("Tài khoản đã được xóa.");
                logoutUser(); // Đăng xuất sau khi xóa
            } else {
                showToast("Xóa tài khoản thất bại. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Lỗi khi xóa tài khoản: ", e);
            showToast("Đã xảy ra lỗi trong quá trình xóa.");
        } finally {
            userDAO.close();
        }
    }

    // Phương thức hiển thị hộp thoại chọn Theme
    private void showThemeSelectionDialog() {
        final String[] themes = {"Sáng", "Tối", "Hệ thống"}; // Thay bằng string resource nếu cần
        final int[] themeModes = {
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_theme)) // Đảm bảo select_theme có trong strings.xml
                .setItems(themes, (dialog, which) -> {
                    int selectedMode = themeModes[which];
                    AppCompatDelegate.setDefaultNightMode(selectedMode);
                    SharedPreferences prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putInt("theme_mode", selectedMode).apply();
                    // Có thể cần khởi động lại Activity để theme áp dụng ngay lập tức
                    // recreate();
                })
                .show();
    }

    // Phương thức hiển thị hộp thoại chọn Ngôn ngữ
    private void showLanguageSelectionDialog() {
        final String[] languages = {"Tiếng Việt", "English"}; // Thay bằng string resource nếu cần
        final String[] languageCodes = {"vi", "en"};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language)) // Đảm bảo select_language có trong strings.xml
                .setItems(languages, (dialog, which) -> {
                    // Sử dụng LocaleHelper của bạn
                    LocaleHelper.setLocale(this, languageCodes[which]);

                    // Khởi động lại ứng dụng để áp dụng ngôn ngữ mới
                    Intent intent = new Intent(this, LauncherActivity.class); // Thay LauncherActivity nếu activity khởi động là khác
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}