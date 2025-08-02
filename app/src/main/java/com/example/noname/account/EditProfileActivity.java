package com.example.noname.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.R;
import com.google.android.material.textfield.TextInputEditText;

import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.UserDAO;
import com.example.noname.utils.PasswordHasher;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etEditEmail, etEditFullName, etEditPhoneNumber,
            etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnSaveChanges;
    private Toolbar toolbar;

    private UserDAO userDAO;
    private String currentUserEmail;
    private String originalFullName, originalPhoneNumber, originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userDAO = new UserDAO(this);
        initializeViews();
        setupToolbar();

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        if (currentUserEmail != null) {
            loadCurrentUserData();
        } else {
            Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_edit_profile);
        etEditEmail = findViewById(R.id.et_edit_email);
        etEditFullName = findViewById(R.id.et_edit_full_name);
        etEditPhoneNumber = findViewById(R.id.et_edit_phone_number);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCurrentUserData() {
        userDAO.open();
        Cursor cursor = null;
        try {
            cursor = userDAO.getUserByEmail(currentUserEmail);
            if (cursor != null && cursor.moveToFirst()) {
                originalEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                originalFullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME));
                originalPhoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE_NUMBER));

                etEditEmail.setText(originalEmail);
                etEditFullName.setText(originalFullName);
                etEditPhoneNumber.setText(originalPhoneNumber);
            }
        } finally {
            if (cursor != null) cursor.close();
            userDAO.close();
        }
    }

    private void saveProfileChanges() {
        // --- Lấy dữ liệu mới ---
        String newFullName = etEditFullName.getText().toString().trim();
        String newPhoneNumber = etEditPhoneNumber.getText().toString().trim();
        String newEmail = etEditEmail.getText().toString().trim();

        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();

        boolean isPasswordChangeAttempted = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmNewPassword.isEmpty();
        boolean somethingChanged = !newFullName.equals(originalFullName) || !newPhoneNumber.equals(originalPhoneNumber) || !newEmail.equals(originalEmail) || isPasswordChangeAttempted;

        if (!somethingChanged) {
            Toast.makeText(this, "Không có thay đổi nào để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        userDAO.open();
        try {
            // --- Xử lý đổi mật khẩu ---
            if (isPasswordChangeAttempted) {
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                    Toast.makeText(this, "Vui lòng điền đủ các trường mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPassword.equals(confirmNewPassword)) {
                    etConfirmNewPassword.setError("Mật khẩu mới không khớp");
                    return;
                }

                Cursor cursor = userDAO.getUserByEmail(currentUserEmail);
                if (cursor != null && cursor.moveToFirst()) {
                    String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH));
                    if (!PasswordHasher.verifyPassword(currentPassword, storedHash)) {
                        etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                        cursor.close();
                        return;
                    }
                    cursor.close();

                    // Băm và cập nhật mật khẩu mới
                    String newHashedPassword = PasswordHasher.hashPassword(newPassword);
                    userDAO.updatePassword(currentUserEmail, newHashedPassword);
                }
            }

            // --- Xử lý đổi thông tin cá nhân ---
            userDAO.updateUserProfile(currentUserEmail, newFullName, newPhoneNumber);

            // --- Xử lý đổi Email ---
            if (!newEmail.equals(originalEmail)) {
                int emailUpdateResult = userDAO.updateUserEmail(currentUserEmail, newEmail);
                if (emailUpdateResult > 0) {
                    // Cập nhật SharedPreferences nếu email thay đổi thành công
                    SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("LOGGED_IN_USER_EMAIL", newEmail).apply();
                } else if (emailUpdateResult == -1) {
                    etEditEmail.setError("Email này đã được sử dụng");
                    return; // Dừng lại nếu email đã tồn tại
                }
            }

            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish();

        } finally {
            userDAO.close();
        }
    }
}