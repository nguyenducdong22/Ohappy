package com.example.noname; // Đảm bảo đúng package

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class ChooseGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED); // Gửi kết quả hủy nếu người dùng nhấn nút back
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED); // Gửi kết quả hủy nếu người dùng nhấn nút back trên toolbar
            finish();
        });

        // Nhu Cầu Thiết Yếu
        setupCategoryItem(R.id.item_an_uong, R.drawable.ic_restaurant, "Ăn Uống", R.color.primary_green);
        setupCategoryItem(R.id.item_nha_cua, R.drawable.ic_home_and_utility, "Nhà cửa & Tiện ích", R.color.primary_green);
        setupCategoryItem(R.id.item_di_chuyen, R.drawable.ic_directions_car, "Di Chuyển", R.color.primary_green);
        setupCategoryItem(R.id.item_sam_sua, R.drawable.ic_shopping, "Sắm Sửa", R.color.primary_green);
        setupCategoryItem(R.id.item_suc_khoe, R.drawable.ic_health, "Sức Khỏe", R.color.primary_green);

        // Giải Trí, Cá Nhân
        setupCategoryItem(R.id.item_giai_tri, R.drawable.ic_entertainment, "Giải Trí", R.color.primary_green);
        setupCategoryItem(R.id.item_the_thao, R.drawable.ic_sport, "Thể Thao", R.color.accent_yellow);
        setupCategoryItem(R.id.item_lam_dep, R.drawable.ic_beauty, "Làm Đẹp", R.color.primary_green);
        setupCategoryItem(R.id.item_qua_tang, R.drawable.ic_gift, "Quà Tặng", R.color.accent_yellow);
        setupCategoryItem(R.id.item_du_lich, R.drawable.ic_travel, "Du Lịch", R.color.primary_green);
        setupCategoryItem(R.id.item_ban_be, R.drawable.ic_friends, "Bạn Bè", R.color.accent_yellow);

        // Học Tập, Phát Triển
        setupCategoryItem(R.id.item_hoc_tap, R.drawable.ic_learning, "Học Tập", R.color.primary_green);
        setupCategoryItem(R.id.item_sach_vo, R.drawable.ic_book, "Sách vở", R.color.accent_yellow);
        setupCategoryItem(R.id.item_khoa_hoc, R.drawable.ic_course, "Khóa Học", R.color.primary_green);
        setupCategoryItem(R.id.item_dung_cu_hoc_tap, R.drawable.ic_study_tools, "Dụng Cụ Học Tập", R.color.accent_yellow);

        // Chi Phí Khác
        setupCategoryItem(R.id.item_dau_tu, R.drawable.ic_invest, "Đầu Tư", R.color.primary_green);
        setupCategoryItem(R.id.item_tiet_kiem, R.drawable.ic_savings, "Tiết Kiệm", R.color.accent_yellow);
        setupCategoryItem(R.id.item_quy_khan_cap, R.drawable.ic_emergency_fund, "Quỹ Khẩn Cấp", R.color.primary_green);
        setupCategoryItem(R.id.item_khac, R.drawable.ic_other, "Khác", R.color.accent_yellow);
        setupCategoryItem(R.id.item_add_more_category, R.drawable.ic_add_circle, "Thêm", R.color.accent_yellow);
    }

    private void setupCategoryItem(int viewId, int iconResId, String categoryName, int tintColorResId) {
        View itemView = findViewById(viewId);
        ImageView iconView = itemView.findViewById(R.id.category_icon);
        TextView nameView = itemView.findViewById(R.id.category_name);

        iconView.setImageResource(iconResId);
        iconView.setColorFilter(ContextCompat.getColor(this, tintColorResId));
        nameView.setText(categoryName);

        itemView.setOnClickListener(v -> {
            // TẠO INTENT ĐỂ TRẢ VỀ DỮ LIỆU
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_group_name", categoryName); // Tên key là "selected_group_name"
            resultIntent.putExtra("selected_group_icon", iconResId); // Gửi cả ID icon về
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}