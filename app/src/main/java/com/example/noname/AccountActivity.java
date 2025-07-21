package com.example.noname;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Toolbar toolbar = findViewById(R.id.toolbar_account);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút back
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Gán sự kiện click cho một vài mục để làm ví dụ
        View menuMyWallets = findViewById(R.id.menu_my_wallets);
        //((ImageView) menuMyWallets.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_wallet);
        ((TextView) menuMyWallets.findViewById(R.id.tv_title)).setText("Ví của tôi");

        menuMyWallets.setOnClickListener(v -> {
            Toast.makeText(AccountActivity.this, "Mở Ví của tôi", Toast.LENGTH_SHORT).show();
        });

        // Bạn có thể làm tương tự cho các mục menu khác
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Xử lý khi nhấn nút back trên toolbar
        return true;
    }
}