package com.example.noname; // THÊM DÒNG NÀY

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.noname.R;

public class ChooseGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Bật nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Xử lý sự kiện khi nhấn nút quay lại
            }
        });


        // Ví dụ cho phần "Nhu Cầu Thiết Yếu"
        setupCategoryItem(R.id.item_an_uong, R.drawable.ic_restaurant, "Ăn Uống");
        setupCategoryItem(R.id.item_nha_cua, R.drawable.ic_home_and_utility, "Nhà cửa & Tiện ích");
        setupCategoryItem(R.id.item_di_chuyen, R.drawable.ic_directions_car, "Di Chuyển");
        setupCategoryItem(R.id.item_sam_sua, R.drawable.ic_shopping, "Sắm Sửa");
        setupCategoryItem(R.id.item_suc_khoe, R.drawable.ic_health, "Sức Khỏe");

        // Tiếp tục tương tự cho các nhóm khác: Giải Trí, Học Tập, Chi Phí Khác
        setupCategoryItem(R.id.item_giai_tri, R.drawable.ic_entertainment, "Giải Trí");
        setupCategoryItem(R.id.item_the_thao, R.drawable.ic_sport, "Thể Thao");
        setupCategoryItem(R.id.item_lam_dep, R.drawable.ic_beauty, "Làm Đẹp");
        setupCategoryItem(R.id.item_qua_tang, R.drawable.ic_gift, "Quà Tặng");
        setupCategoryItem(R.id.item_du_lich, R.drawable.ic_travel, "Du Lịch");
        setupCategoryItem(R.id.item_ban_be, R.drawable.ic_friends, "Bạn Bè");

        setupCategoryItem(R.id.item_hoc_tap, R.drawable.ic_learning, "Học Tập");
        setupCategoryItem(R.id.item_sach_vo, R.drawable.ic_book, "Sách vở");
        setupCategoryItem(R.id.item_khoa_hoc, R.drawable.ic_course, "Khóa Học");
        setupCategoryItem(R.id.item_dung_cu_hoc_tap, R.drawable.ic_study_tools, "Dụng Cụ Học Tập");

        setupCategoryItem(R.id.item_dau_tu, R.drawable.ic_invest, "Đầu Tư");
        setupCategoryItem(R.id.item_tiet_kiem, R.drawable.ic_savings, "Tiết Kiệm");
        setupCategoryItem(R.id.item_quy_khan_cap, R.drawable.ic_emergency_fund, "Quỹ Khẩn Cấp");
        setupCategoryItem(R.id.item_khac, R.drawable.ic_other, "Khác");
        setupCategoryItem(R.id.item_add_more_category, R.drawable.ic_add_circle, "Thêm");

    }

    private void setupCategoryItem(int viewId, int iconResId, String categoryName) {
        View itemView = findViewById(viewId);
        ImageView iconView = itemView.findViewById(R.id.category_icon);
        TextView nameView = itemView.findViewById(R.id.category_name);

        iconView.setImageResource(iconResId);
        nameView.setText(categoryName);

        // Đặt OnClickListener cho từng item nếu bạn muốn xử lý sự kiện click
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý khi người dùng click vào danh mục
                // Ví dụ: Toast.makeText(ChooseGroupActivity.this, "Bạn đã chọn: " + categoryName, Toast.LENGTH_SHORT).show();
            }
        });
    }
}