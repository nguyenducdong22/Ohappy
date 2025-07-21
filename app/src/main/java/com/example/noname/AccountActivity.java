package com.example.noname;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.databinding.ActivityAccountBinding;

public class AccountActivity extends AppCompatActivity {

    // Sử dụng ViewBinding thay cho findViewById
    private ActivityAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewBinding
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Tải thông tin người dùng
        loadUserProfile();

        // 2. Thiết lập các trình lắng nghe sự kiện cho các chức năng
        setupOptionListeners();

        // 3. Thiết lập các trình lắng nghe sự kiện cho các hành động quan trọng
        setupActionListeners();
    }

    /**
     * Tải và hiển thị thông tin người dùng.
     * TODO: Thay thế dữ liệu mẫu bằng dữ liệu thật từ API hoặc Firebase.
     */
    private void loadUserProfile() {
        // --- BẮT ĐẦU DỮ LIỆU MẪU ---
        String fullName = "Nguyễn Văn A";
        String email = "nguyenvana@email.com";
        // TODO: Load ảnh đại diện từ URL bằng thư viện như Glide hoặc Picasso
        // binding.ivAvatar. ...
        // --- KẾT THÚC DỮ LIỆU MẪU ---

        binding.tvFullName.setText(fullName);
        binding.tvEmail.setText(email);
    }

    /**
     * Thiết lập các sự kiện click cho các tùy chọn trong danh sách.
     */
    private void setupOptionListeners() {
        // Quản lý tài khoản
        binding.optionEditProfile.setOnClickListener(v -> {
            // TODO: Mở EditProfileActivity
            // Intent intent = new Intent(AccountActivity.this, EditProfileActivity.class);
            // startActivity(intent);
            showToast("Mở màn hình Chỉnh sửa thông tin");
        });

        binding.optionSecurity.setOnClickListener(v -> {
            // TODO: Mở SecurityActivity
            showToast("Mở màn hình Bảo mật");
        });

        // Cài đặt ứng dụng
        binding.optionNotifications.setOnClickListener(v -> {
            // TODO: Mở NotificationSettingsActivity
            showToast("Mở màn hình Cài đặt thông báo");
        });

        binding.optionTheme.setOnClickListener(v -> {
            // TODO: Hiển thị dialog chọn giao diện (Sáng/Tối)
            showThemeSelectionDialog();
        });

        binding.optionLanguage.setOnClickListener(v -> {
            // TODO: Mở màn hình chọn ngôn ngữ
            showToast("Mở màn hình chọn ngôn ngữ");
        });

        // Hỗ trợ & Pháp lý
        binding.optionHelpCenter.setOnClickListener(v -> {
            // TODO: Mở màn hình Trung tâm trợ giúp (có thể là WebView)
            showToast("Mở Trung tâm trợ giúp");
        });

        binding.optionTerms.setOnClickListener(v -> {
            // TODO: Mở Điều khoản & Chính sách (có thể là WebView)
            showToast("Mở Điều khoản & Chính sách");
        });
    }

    /**
     * Thiết lập sự kiện click cho các nút Đăng xuất và Xóa tài khoản.
     */
    private void setupActionListeners() {
        // Nút Đăng xuất
        binding.btnLogout.setOnClickListener(v -> {
            logoutUser();
        });

        // Nút Xóa tài khoản
        binding.btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountConfirmationDialog();
        });
    }

    /**
     * Xử lý logic đăng xuất.
     */
    private void logoutUser() {
        // TODO: Xóa thông tin phiên đăng nhập của người dùng (vd: SharedPreferences, token...)

        showToast("Đăng xuất thành công!");

        // Chuyển về màn hình đăng nhập và xóa các Activity cũ khỏi stack
        // Intent intent = new Intent(AccountActivity.this, SignInActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish();
    }

    /**
     * Hiển thị hộp thoại xác nhận trước khi xóa tài khoản.
     */
    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản của mình không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // TODO: Gọi API để xóa tài khoản người dùng khỏi hệ thống
                    // Sau khi thành công, thực hiện logic đăng xuất
                    showToast("Tài khoản đã được xóa.");
                    logoutUser();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_warning) // Thêm icon cảnh báo
                .show();
    }

    /**
     * Hiển thị dialog chọn giao diện.
     */
    private void showThemeSelectionDialog() {
        final String[] themes = {"Sáng", "Tối", "Hệ thống"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn giao diện")
                .setItems(themes, (dialog, which) -> {
                    // TODO: Lưu lựa chọn của người dùng và áp dụng theme
                    // Ví dụ: AppCompatDelegate.setDefaultNightMode(...)
                    showToast("Đã chọn: " + themes[which]);
                })
                .show();
    }

    // Phương thức trợ giúp để hiển thị Toast nhanh
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}