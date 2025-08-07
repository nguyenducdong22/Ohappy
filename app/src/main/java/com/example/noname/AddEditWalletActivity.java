package com.example.noname;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.database.AccountDAO;
import com.example.noname.models.Account;

public class AddEditWalletActivity extends AppCompatActivity {

    private EditText etWalletName, etInitialBalance;
    private Button btnSave;
    private AccountDAO accountDAO;
    private long currentUserId;
    private boolean isEditMode = false;
    private long accountIdToEdit;

    private static final String TAG = "AddEditWalletActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar_add_edit_wallet);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Toolbar back button clicked. Finishing activity.");
            finish();
        });

        etWalletName = findViewById(R.id.et_wallet_name);
        etInitialBalance = findViewById(R.id.et_initial_balance);
        btnSave = findViewById(R.id.btn_save_wallet);

        accountDAO = new AccountDAO(this);

        Intent intent = getIntent();
        currentUserId = intent.getLongExtra("user_id", -1);
        isEditMode = intent.getBooleanExtra("edit_mode", false);
        accountIdToEdit = intent.getLongExtra("account_id_to_edit", -1);

        if (isEditMode && accountIdToEdit != -1) {
            getSupportActionBar().setTitle("Edit Wallet");
            loadWalletData(accountIdToEdit);
        } else {
            getSupportActionBar().setTitle("Add New Wallet");
        }

        btnSave.setOnClickListener(v -> saveWallet());
    }

    private void loadWalletData(long accountId) {
        accountDAO.open();
        Account account = accountDAO.getAccountById(accountId);
        accountDAO.close();
        if (account != null) {
            etWalletName.setText(account.getName());
            etInitialBalance.setText(String.valueOf(account.getBalance()));
        } else {
            Toast.makeText(this, "Wallet to edit not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveWallet() {
        String name = etWalletName.getText().toString().trim();
        String balanceStr = etInitialBalance.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(balanceStr)) {
            Toast.makeText(this, "Please enter all the information", Toast.LENGTH_SHORT).show();
            return;
        }

        double balance = Double.parseDouble(balanceStr);

        accountDAO.open();
        long result = -1;
        if (isEditMode) {
            int rowsAffected = accountDAO.updateAccount(accountIdToEdit, name, balance);
            if (rowsAffected > 0) {
                result = 1;
            }
        } else {
            // Assuming wallet type is 'Cash' for simplicity
            result = accountDAO.createAccount(currentUserId, name, balance, "Cash");
        }
        accountDAO.close();

        if (result != -1) {
            Toast.makeText(this, "Wallet saved successfully!", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error saving wallet, please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}