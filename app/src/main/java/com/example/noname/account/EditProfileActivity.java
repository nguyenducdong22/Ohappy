package com.example.noname.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.noname.R;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.UserDAO;
import com.example.noname.utils.PasswordHasher;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends BaseActivity {

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
            Toast.makeText(this, getString(R.string.error_loading_user_info), Toast.LENGTH_SHORT).show();
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
        String newFullName = etEditFullName.getText().toString().trim();
        String newPhoneNumber = etEditPhoneNumber.getText().toString().trim();
        String newEmail = etEditEmail.getText().toString().trim();
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();

        boolean isPasswordChangeAttempted = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmNewPassword.isEmpty();
        boolean somethingChanged = !newFullName.equals(originalFullName) || !newPhoneNumber.equals(originalPhoneNumber) || !newEmail.equals(originalEmail) || isPasswordChangeAttempted;

        if (!somethingChanged) {
            Toast.makeText(this, getString(R.string.no_changes_to_save), Toast.LENGTH_SHORT).show();
            return;
        }

        userDAO.open();
        try {
            if (isPasswordChangeAttempted) {
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                    Toast.makeText(this, getString(R.string.error_fill_all_password_fields), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPassword.equals(confirmNewPassword)) {
                    etConfirmNewPassword.setError(getString(R.string.error_new_passwords_do_not_match));
                    return;
                }

                Cursor cursor = userDAO.getUserByEmail(currentUserEmail);
                if (cursor != null && cursor.moveToFirst()) {
                    String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH));
                    if (!PasswordHasher.verifyPassword(currentPassword, storedHash)) {
                        etCurrentPassword.setError(getString(R.string.error_current_password_incorrect));
                        cursor.close();
                        return;
                    }
                    cursor.close();

                    String newHashedPassword = PasswordHasher.hashPassword(newPassword);
                    userDAO.updatePassword(currentUserEmail, newHashedPassword);
                }
            }

            userDAO.updateUserProfile(currentUserEmail, newFullName, newPhoneNumber);

            if (!newEmail.equals(originalEmail)) {
                int emailUpdateResult = userDAO.updateUserEmail(currentUserEmail, newEmail);
                if (emailUpdateResult > 0) {
                    SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("LOGGED_IN_USER_EMAIL", newEmail).apply();
                } else if (emailUpdateResult == -1) {
                    etEditEmail.setError(getString(R.string.error_email_already_used));
                    return;
                }
            }

            Toast.makeText(this, getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
            finish();

        } finally {
            userDAO.close();
        }
    }
}