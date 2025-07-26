package com.example.noname;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.UserDAO;
import com.example.noname.databinding.ActivityAccountBinding;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding binding;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDAO = new UserDAO(this);

        loadUserProfileFromDatabase();
        setupOptionListeners();
        setupActionListeners();
    }

    /**
     * Tải và hiển thị thông tin người dùng từ cơ sở dữ liệu SQLite cục bộ.
     */
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

    /**
     * Xử lý logic đăng xuất.
     */
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
     */
    private void setupOptionListeners() {
        binding.optionEditProfile.setOnClickListener(v -> showToast("Mở màn hình Chỉnh sửa thông tin"));
        binding.optionSecurity.setOnClickListener(v -> showToast("Mở màn hình Bảo mật"));
        binding.optionNotifications.setOnClickListener(v -> showToast("Mở màn hình Cài đặt thông báo"));
        binding.optionTheme.setOnClickListener(v -> showThemeSelectionDialog());
        binding.optionLanguage.setOnClickListener(v -> showToast("Mở màn hình chọn ngôn ngữ"));
        binding.optionHelpCenter.setOnClickListener(v -> showToast("Mở Trung tâm trợ giúp"));
        binding.optionTerms.setOnClickListener(v -> showToast("Mở Điều khoản & Chính sách"));
    }

    /**
     * Thiết lập sự kiện click cho các nút Đăng xuất và Xóa tài khoản.
     */
    private void setupActionListeners() {
        binding.btnLogout.setOnClickListener(v -> logoutUser());
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmationDialog());
    }

    /**
     * Hiển thị hộp thoại xác nhận trước khi xóa tài khoản.
     * PHIÊN BẢN ĐÃ CẬP NHẬT
     */
    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản của mình không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi phương thức xử lý logic xóa tài khoản
                    deleteCurrentUserAccount();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    /**
     * Phương thức mới để xử lý logic xóa tài khoản người dùng hiện tại.
     */
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
                logoutUser(); // Đăng xuất để xóa session và chuyển màn hình
            } else {
                Toast.makeText(this, "Xóa tài khoản thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Lỗi khi xóa tài khoản: ", e);
            Toast.makeText(this, "Đã xảy ra lỗi trong quá trình xóa.", Toast.LENGTH_SHORT).show();
        } finally {
            userDAO.close(); // Luôn đóng kết nối database
        }
    }

    /**
     * Hiển thị dialog chọn giao diện.
     */
    private void showThemeSelectionDialog() {
        final String[] themes = {"Sáng", "Tối", "Hệ thống"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn giao diện")
                .setItems(themes, (dialog, which) -> {
                    showToast("Đã chọn: " + themes[which]);
                })
                .show();
    }

    /**
     * Phương thức trợ giúp để hiển thị Toast nhanh.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}