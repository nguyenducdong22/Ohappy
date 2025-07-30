package com.example.noname.Budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.example.noname.R;
import com.example.noname.account.BaseActivity; // QUAN TRỌNG: Kế thừa từ BaseActivity

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AddBudgetActivity extends BaseActivity { // Kế thừa từ BaseActivity

    private static final int REQUEST_CODE_SELECT_GROUP = 1;

    private TextView tvGroupName;
    private ImageView ivGroupIcon;
    private EditText etAmount;
    private TextView tvDateRange;
    private SwitchMaterial switchRepeatBudget;

    private String selectedGroupName;
    private int selectedGroupIconResId = R.drawable.ic_circle;
    private String selectedDateRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        Toolbar toolbar = findViewById(R.id.toolbar_add_budget);
        setSupportActionBar(toolbar);

        initializeViews();
        setupInitialValues();
        setupListeners();
    }

    private void initializeViews() {
        tvGroupName = findViewById(R.id.tv_group_name);
        ivGroupIcon = findViewById(R.id.iv_group_icon);
        etAmount = findViewById(R.id.et_amount);
        tvDateRange = findViewById(R.id.tv_date_range);
        switchRepeatBudget = findViewById(R.id.switch_repeat_budget);
    }

    private void setupInitialValues(){
        // Thiết lập giá trị ban đầu từ string resources
        selectedGroupName = getString(R.string.select_group);
        selectedDateRange = getString(R.string.this_month); // Ví dụ, bạn có thể thay đổi logic này

        tvGroupName.setText(selectedGroupName);
        ivGroupIcon.setImageResource(selectedGroupIconResId);
        tvDateRange.setText(selectedDateRange);
    }

    private void setupListeners() {
        TextView btnCancel = findViewById(R.id.btn_cancel_add_budget);
        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        LinearLayout layoutChooseGroup = findViewById(R.id.layout_choose_group);
        layoutChooseGroup.setOnClickListener(v -> {
            Intent intent = new Intent(AddBudgetActivity.this, ChooseGroupActivity.class);
            // ... (truyền dữ liệu nếu cần)
            startActivityForResult(intent, REQUEST_CODE_SELECT_GROUP);
        });

        Button btnSaveBudget = findViewById(R.id.btn_save_budget);
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void saveBudget() {
        String amountString = etAmount.getText().toString().trim();
        double amount;

        if (amountString.isEmpty() || amountString.equals("0")) {
            Toast.makeText(this, getString(R.string.error_please_enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGroupName.equals(getString(R.string.select_group))) {
            Toast.makeText(this, getString(R.string.error_please_select_group), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean repeatBudget = switchRepeatBudget.isChecked();
        Budget newBudget = new Budget(selectedGroupName, selectedGroupIconResId, amount, selectedDateRange, repeatBudget);
        saveBudgetToSharedPreferences(newBudget);

        Toast.makeText(this, getString(R.string.budget_saved_for, selectedGroupName), Toast.LENGTH_LONG).show();

        setResult(Activity.RESULT_OK);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_GROUP && resultCode == Activity.RESULT_OK && data != null) {
            selectedGroupName = data.getStringExtra("selected_group_name");
            selectedGroupIconResId = data.getIntExtra("selected_group_icon_res_id", R.drawable.ic_circle);
            tvGroupName.setText(selectedGroupName);
            ivGroupIcon.setImageResource(selectedGroupIconResId);
        }
    }

    private void saveBudgetToSharedPreferences(Budget newBudget) {
        SharedPreferences sharedPref = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("all_budgets", null);
        List<Budget> existingBudgets;

        if (json == null) {
            existingBudgets = new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<Budget>>() {}.getType();
            existingBudgets = gson.fromJson(json, type);
        }

        existingBudgets.add(newBudget);
        String updatedJson = gson.toJson(existingBudgets);
        sharedPref.edit().putString("all_budgets", updatedJson).apply();
    }
}