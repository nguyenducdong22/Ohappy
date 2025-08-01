package com.example.noname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Thêm import cho Log
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.noname.database.AccountDAO;
import com.example.noname.models.Account;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChooseWalletActivity extends AppCompatActivity {

    private AccountDAO accountDAO;
    private long currentUserId;

    private static final String TAG = "ChooseWalletActivity"; // Định nghĩa tag cho Log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar_choose_wallet);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Toolbar back button clicked. Cancelling result.");
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "System back button clicked. Cancelling result.");
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        Log.d(TAG, "Retrieved currentUserId: " + currentUserId);

        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Current User ID is -1. Finishing activity.");
            finish();
            return;
        }

        accountDAO = new AccountDAO(this);
        accountDAO.open();
        List<Account> accounts = accountDAO.getAllAccountsByUserId(currentUserId);
        accountDAO.close();
        Log.d(TAG, "Found " + accounts.size() + " accounts for user ID " + currentUserId);

        LinearLayout walletContainer = findViewById(R.id.wallet_container);
        if (walletContainer == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy container ví", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Layout container not found with ID R.id.wallet_container");
            return;
        }

        // Xóa các View cũ nếu có
        walletContainer.removeAllViews();
        Log.d(TAG, "Removed all existing views from wallet container.");

        // Tạo một Map để liên kết tên ví với tên file icon
        Map<String, String> walletIconMap = new HashMap<>();
        walletIconMap.put("Tiền mặt", "ic_money");
        walletIconMap.put("Ví Momo", "ic_wallet_momo");
        walletIconMap.put("Ngân hàng", "ic_wallet_bank");

        // Duyệt qua danh sách tài khoản từ database và gán dữ liệu
        if (accounts.isEmpty()) {
            Toast.makeText(this, "Không có ví nào được tìm thấy. Vui lòng tạo ví mặc định.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Account list is empty. No wallets to display.");
        } else {
            for (Account account : accounts) {
                String iconName = walletIconMap.get(account.getName());
                Log.d(TAG, "Processing account: " + account.getName() + ", Icon Name: " + iconName);
                setupWalletItem(walletContainer, account, iconName);
            }
        }
    }

    private void setupWalletItem(LinearLayout parentLayout, Account account, String iconName) {
        LayoutInflater inflater = getLayoutInflater();
        View itemView = inflater.inflate(R.layout.item_wallet, parentLayout, false);
        Log.d(TAG, "Inflated new view for account: " + account.getName());

        ImageView iconView = itemView.findViewById(R.id.wallet_icon);
        TextView nameView = itemView.findViewById(R.id.wallet_name);
        TextView balanceView = itemView.findViewById(R.id.wallet_balance);

        nameView.setText(account.getName());
        Log.d(TAG, "Setting wallet name: " + account.getName());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
        String formattedBalance = currencyFormat.format(account.getBalance());
        balanceView.setText(formattedBalance);
        Log.d(TAG, "Setting balance: " + formattedBalance);

        if (iconView != null && iconName != null) {
            int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            if (iconResId != 0) {
                iconView.setImageResource(iconResId);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.primary_green_dark));
                Log.d(TAG, "Set icon for " + account.getName() + " with resource ID: " + iconResId);
            } else {
                Log.w(TAG, "Icon resource not found for name: " + iconName);
                iconView.setImageResource(R.drawable.ic_wallet);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.primary_green_dark));
            }
        } else {
            Log.w(TAG, "Icon name is null for account: " + account.getName());
            iconView.setImageResource(R.drawable.ic_wallet);
            iconView.setColorFilter(ContextCompat.getColor(this, R.color.primary_green_dark));
        }

        itemView.setOnClickListener(v -> {
            Log.d(TAG, "Wallet item clicked: " + account.getName() + ", ID: " + account.getId());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_account_id", account.getId());
            resultIntent.putExtra("selected_wallet_name", account.getName());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        parentLayout.addView(itemView);
        Log.d(TAG, "Added new wallet view to container.");
    }
}