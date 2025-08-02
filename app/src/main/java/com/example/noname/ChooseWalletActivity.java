package com.example.noname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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
    private LinearLayout walletContainer;

    private static final String TAG = "ChooseWalletActivity";

    // Khai báo ActivityResultLauncher để nhận kết quả từ màn hình thêm/sửa ví
    private final ActivityResultLauncher<Intent> addEditWalletLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "Add/Edit wallet activity returned with OK result. Reloading data.");
                    Toast.makeText(this, "Đã cập nhật danh sách ví", Toast.LENGTH_SHORT).show();
                    loadWallets(); // Tải lại danh sách ví sau khi có thay đổi
                } else {
                    Log.d(TAG, "Add/Edit wallet activity cancelled.");
                }
            });

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
        walletContainer = findViewById(R.id.wallet_container);

        // Gắn sự kiện cho nút Thêm ví mới (nếu có trong layout)
        // Dòng này giả định có một FloatingActionButton với ID 'fab_add_wallet'
        // trong layout activity_choose_wallet.xml
        // Nếu layout của bạn không có, hãy bỏ dòng này.
        // FloatingActionButton fabAddWallet = findViewById(R.id.fab_add_wallet);
        // if (fabAddWallet != null) {
        //     fabAddWallet.setOnClickListener(v -> {
        //         Log.d(TAG, "Add new wallet FAB clicked.");
        //         Intent intent = new Intent(ChooseWalletActivity.this, AddEditWalletActivity.class);
        //         intent.putExtra("user_id", currentUserId);
        //         addEditWalletLauncher.launch(intent);
        //     });
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        accountDAO.open();
        loadWallets();
    }

    @Override
    protected void onPause() {
        super.onPause();
        accountDAO.close();
    }

    private void loadWallets() {
        if (currentUserId == -1) {
            Log.e(TAG, "Current User ID is -1. Cannot load wallets.");
            return;
        }

        List<Account> accounts = accountDAO.getAllAccountsByUserId(currentUserId);
        Log.d(TAG, "Found " + accounts.size() + " accounts for user ID " + currentUserId);

        if (walletContainer == null) {
            Log.e(TAG, "Layout container not found with ID R.id.wallet_container");
            return;
        }

        walletContainer.removeAllViews();
        Log.d(TAG, "Removed all existing views from wallet container.");

        // Thêm nút "Thêm ví mới" ở đầu danh sách
        setupAddWalletButton(walletContainer);

        if (accounts.isEmpty()) {
            Log.w(TAG, "Account list is empty. No wallets to display.");
        } else {
            for (Account account : accounts) {
                String iconName = getWalletIconName(account.getName());
                setupWalletItem(walletContainer, account, iconName);
            }
        }
    }

    private String getWalletIconName(String walletName) {
        Map<String, String> walletIconMap = new HashMap<>();
        walletIconMap.put("Tiền mặt", "ic_money");
        walletIconMap.put("Ví Momo", "ic_wallet_momo");
        walletIconMap.put("Ngân hàng", "ic_wallet_bank");
        return walletIconMap.getOrDefault(walletName, "ic_wallet");
    }

    private void setupWalletItem(LinearLayout parentLayout, Account account, String iconName) {
        LayoutInflater inflater = getLayoutInflater();
        View itemView = inflater.inflate(R.layout.item_wallet, parentLayout, false); // Sửa thành item_wallet_editable
        Log.d(TAG, "Inflated new view for account: " + account.getName());

        ImageView iconView = itemView.findViewById(R.id.wallet_icon);
        TextView nameView = itemView.findViewById(R.id.wallet_name);
        TextView balanceView = itemView.findViewById(R.id.wallet_balance);
        ImageView editButton = itemView.findViewById(R.id.btn_edit_wallet);

        if (account == null) {
            Log.e(TAG, "Account object is null, skipping view setup.");
            return;
        }

        nameView.setText(account.getName());
        Log.d(TAG, "Setting wallet name: " + account.getName());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
        String formattedBalance = currencyFormat.format(account.getBalance());
        balanceView.setText(formattedBalance);
        Log.d(TAG, "Setting balance: " + formattedBalance);

        int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (iconResId != 0) {
            iconView.setImageResource(iconResId);
        } else {
            iconView.setImageResource(R.drawable.ic_wallet);
        }
        iconView.setColorFilter(ContextCompat.getColor(this, R.color.primary_green_dark));

        itemView.setOnClickListener(v -> {
            Log.d(TAG, "Wallet item clicked: " + account.getName() + ", ID: " + account.getId());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_account_id", account.getId());
            resultIntent.putExtra("selected_wallet_name", account.getName());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                Log.d(TAG, "Edit button clicked for wallet: " + account.getName() + ", ID: " + account.getId());
                Intent intent = new Intent(ChooseWalletActivity.this, AddEditWalletActivity.class);
                intent.putExtra("user_id", currentUserId);
                intent.putExtra("edit_mode", true);
                intent.putExtra("account_id_to_edit", account.getId());
                addEditWalletLauncher.launch(intent);
            });
        }


        parentLayout.addView(itemView);
        Log.d(TAG, "Added new wallet view to container.");
    }

    private void setupAddWalletButton(LinearLayout parentLayout) {
        LayoutInflater inflater = getLayoutInflater();
        View addView = inflater.inflate(R.layout.item_add_wallet, parentLayout, false);
        addView.setOnClickListener(v -> {
            Log.d(TAG, "Add new wallet button clicked.");
            Intent intent = new Intent(ChooseWalletActivity.this, AddEditWalletActivity.class);
            intent.putExtra("user_id", currentUserId);
            addEditWalletLauncher.launch(intent);
        });
        parentLayout.addView(addView, 0); // Thêm vào vị trí đầu tiên của container
    }
}