package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView; // Cần import TextView
import androidx.appcompat.app.AppCompatActivity;

public class BudgetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget); // Đảm bảo layout của bạn có tên này

        // Ẩn ActionBar nếu không muốn hiển thị
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Xử lý nút "Tạo ngân sách"
        Button btnCreateBudget = findViewById(R.id.btnCreateBudget);
        if (btnCreateBudget != null) { // Đảm bảo nút tồn tại trong layout
            btnCreateBudget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BudgetActivity.this, AddBudgetActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Xử lý TextView "Tổng quan" để quay lại MainActivity
        TextView tvOverview = findViewById(R.id.tvOverview); // Lấy tham chiếu đến tvOverview (đã thêm ID trong XML)
        if (tvOverview != null) { // Đảm bảo TextView tồn tại trong layout
            tvOverview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Khi "Tổng quan" được nhấp, kết thúc BudgetActivity để quay lại Activity trước đó (MainActivity)
                    finish();
                }
            });
        }
    }

    // Phương thức này xử lý khi người dùng nhấn nút "mũi tên lên" trên ActionBar (nếu có và không ẩn ActionBar)
    // Trong trường hợp này, bạn đang ẩn ActionBar, nên việc quay lại chủ yếu sẽ thông qua tvOverview hoặc nút back của hệ thống.
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}