// AccountActivity.java
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

import com.example.noname.account.BaseActivity;
import com.example.noname.account.EditProfileActivity;
import com.example.noname.account.HelpCenterActivity;
import com.example.noname.account.LauncherActivity;
import com.example.noname.account.LegalActivity;
import com.example.noname.account.NotificationSettingsActivity;
import com.example.noname.account.SecurityActivity;
import com.example.noname.database.DatabaseHelper;
import com.example.noname.database.LocaleHelper;
import com.example.noname.database.UserDAO;
import com.example.noname.databinding.ActivityAccountBinding;
import com.example.noname.OverviewSavingGoldActivity;


public class AccountActivity extends BaseActivity {

    private ActivityAccountBinding binding;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarAccount);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarAccount.setNavigationOnClickListener(v -> onBackPressed());

        userDAO = new UserDAO(this);
        setupOptionListeners();
        setupActionListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileFromDatabase();
    }

    private void loadUserProfileFromDatabase() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        if (userEmail == null) {
            Toast.makeText(this, getString(R.string.error_cannot_identify_user), Toast.LENGTH_LONG).show();
            return;
        }

        userDAO.open();
        Cursor cursor = null;
        try {
            cursor = userDAO.getUserByEmail(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));

                binding.tvFullName.setText(fullName);
                binding.tvEmail.setText(email);
            } else {
                Log.e("AccountActivity", "Không tìm thấy người dùng với email: " + userEmail);
                Toast.makeText(this, getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show();
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
        prefs.edit().remove("LOGGED_IN_USER_EMAIL").apply();

        showToast(getString(R.string.logout_successful));

        Intent intent = new Intent(AccountActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupOptionListeners() {
        binding.optionEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class))
        );
        binding.optionSecurity.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, SecurityActivity.class));
        });
        binding.optionNotifications.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, NotificationSettingsActivity.class))
        );

        binding.optionLanguage.setOnClickListener(v -> showLanguageSelectionDialog());
        binding.optionHelpCenter.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, HelpCenterActivity.class));
        });
        binding.optionTerms.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, LegalActivity.class));
        });
        binding.optionRecurringExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, RecurringExpensesActivity.class);
            startActivity(intent);
        });
        // Thêm listener cho "Saving Gold"
        binding.optionSetSavingGold.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, OverviewSavingGoldActivity.class);
            startActivity(intent);
        });
    }

    private void setupActionListeners() {
        binding.btnLogout.setOnClickListener(v -> logoutUser());
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmationDialog());
    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_account_confirm_title))
                .setMessage(getString(R.string.delete_account_confirm_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> deleteCurrentUserAccount())
                .setNegativeButton(getString(R.string.cancel), null)
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    private void deleteCurrentUserAccount() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("LOGGED_IN_USER_EMAIL", null);

        if (userEmail == null) {
            showToast(getString(R.string.error_cannot_identify_account));
            return;
        }

        userDAO.open();
        try {
            int rowsDeleted = userDAO.deleteUserByEmail(userEmail);
            if (rowsDeleted > 0) {
                showToast(getString(R.string.account_deleted_successfully));
                logoutUser();
            } else {
                showToast(getString(R.string.account_delete_failed));
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Lỗi khi xóa tài khoản: ", e);
            showToast(getString(R.string.error_occurred_during_deletion));
        } finally {
            userDAO.close();
        }
    }



    private void showLanguageSelectionDialog() {
        final String[] languages = {getString(R.string.language_vietnamese), getString(R.string.language_english)};
        final String[] languageCodes = {"vi", "en"};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    LocaleHelper.setLocale(this, languageCodes[which]);
                    Intent intent = new Intent(this, LauncherActivity.class);
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