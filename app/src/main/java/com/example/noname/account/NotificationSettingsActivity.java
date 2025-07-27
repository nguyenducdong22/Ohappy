package com.example.noname.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationSettingsActivity extends BaseActivity {

    private MaterialSwitch switchAll, switchTransactions, switchBudget, switchDeals;
    private TextView tvOpenSystemSettings;
    private Toolbar toolbar;
    private SharedPreferences prefs;

    public static final String PREFS_NAME = "notification_prefs";
    public static final String KEY_ALL_NOTIFICATIONS = "all_notifications";
    public static final String KEY_TRANSACTION_NOTIFICATIONS = "transaction_notifications";
    public static final String KEY_BUDGET_NOTIFICATIONS = "budget_notifications";
    public static final String KEY_DEAL_NOTIFICATIONS = "deal_notifications";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initializeViews();
        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_notification_settings);
        switchAll = findViewById(R.id.switch_all_notifications);
        switchTransactions = findViewById(R.id.switch_transaction_notifications);
        switchBudget = findViewById(R.id.switch_budget_notifications);
        switchDeals = findViewById(R.id.switch_deal_notifications);
        tvOpenSystemSettings = findViewById(R.id.tv_open_system_settings);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadSettings() {
        // Tải trạng thái đã lưu, mặc định là true (bật)
        switchAll.setChecked(prefs.getBoolean(KEY_ALL_NOTIFICATIONS, true));
        switchTransactions.setChecked(prefs.getBoolean(KEY_TRANSACTION_NOTIFICATIONS, true));
        switchBudget.setChecked(prefs.getBoolean(KEY_BUDGET_NOTIFICATIONS, true));
        switchDeals.setChecked(prefs.getBoolean(KEY_DEAL_NOTIFICATIONS, true));

        // Cập nhật trạng thái của các switch con dựa trên switch tổng
        updateChildSwitchesState(switchAll.isChecked());
    }

    private void setupListeners() {
        // Khi switch tổng thay đổi
        switchAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lưu trạng thái
            prefs.edit().putBoolean(KEY_ALL_NOTIFICATIONS, isChecked).apply();
            // Bật/tắt các switch con
            updateChildSwitchesState(isChecked);
        });

        // Lưu trạng thái cho từng switch con
        saveSwitchState(switchTransactions, KEY_TRANSACTION_NOTIFICATIONS);
        saveSwitchState(switchBudget, KEY_BUDGET_NOTIFICATIONS);
        saveSwitchState(switchDeals, KEY_DEAL_NOTIFICATIONS);

        // Mở cài đặt hệ thống
        tvOpenSystemSettings.setOnClickListener(v -> openSystemNotificationSettings());
    }

    private void updateChildSwitchesState(boolean isEnabled) {
        switchTransactions.setEnabled(isEnabled);
        switchBudget.setEnabled(isEnabled);
        switchDeals.setEnabled(isEnabled);
    }

    private void saveSwitchState(MaterialSwitch switchView, String key) {
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(key, isChecked).apply();
        });
    }

    private void openSystemNotificationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }
}