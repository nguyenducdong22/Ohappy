package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.noname.account.BaseActivity;

public class ChooseGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);

        setupToolbar();

        // --- Thiết lập các mục nhóm chi tiêu bằng cách sử dụng tài nguyên chuỗi ---

        // Nhu Cầu Thiết Yếu
        setupCategoryItem(R.id.item_an_uong, R.drawable.ic_restaurant, getString(R.string.category_dining), R.color.primary_green);
        setupCategoryItem(R.id.item_nha_cua, R.drawable.ic_home_and_utility, getString(R.string.category_home_utilities), R.color.primary_green);
        setupCategoryItem(R.id.item_di_chuyen, R.drawable.ic_directions_car, getString(R.string.category_transportation), R.color.primary_green);
        setupCategoryItem(R.id.item_sam_sua, R.drawable.ic_shopping_basket, getString(R.string.category_shopping_items), R.color.primary_green);
        setupCategoryItem(R.id.item_suc_khoe, R.drawable.ic_health, getString(R.string.category_health), R.color.primary_green);
        setupCategoryItem(R.id.item_giao_tiep, R.drawable.ic_person, getString(R.string.category_communication), R.color.primary_green);

        // Giải Trí, Cá Nhân
        setupCategoryItem(R.id.item_giai_tri, R.drawable.ic_entertainment, getString(R.string.category_entertainment), R.color.primary_green);
        setupCategoryItem(R.id.item_the_thao, R.drawable.ic_sport, getString(R.string.category_sports), R.color.accent_yellow);
        setupCategoryItem(R.id.item_lam_dep, R.drawable.ic_beauty, getString(R.string.category_beauty), R.color.primary_green);
        setupCategoryItem(R.id.item_qua_tang, R.drawable.ic_gift, getString(R.string.category_gifts), R.color.accent_yellow);
        setupCategoryItem(R.id.item_du_lich, R.drawable.ic_travel, getString(R.string.category_travel), R.color.primary_green);
        setupCategoryItem(R.id.item_ban_be, R.drawable.ic_friends, getString(R.string.category_friends), R.color.accent_yellow);

        // Học Tập, Phát Triển
        setupCategoryItem(R.id.item_hoc_tap, R.drawable.ic_learning, getString(R.string.category_education), R.color.primary_green);
        setupCategoryItem(R.id.item_sach_vo, R.drawable.ic_book, getString(R.string.category_books), R.color.accent_yellow);
        setupCategoryItem(R.id.item_khoa_hoc, R.drawable.ic_course, getString(R.string.category_courses), R.color.primary_green);
        setupCategoryItem(R.id.item_dung_cu_hoc_tap, R.drawable.ic_study_tools, getString(R.string.category_study_tools), R.color.accent_yellow);

        // Chi Phí Khác
        setupCategoryItem(R.id.item_dau_tu, R.drawable.ic_invest, getString(R.string.category_investment), R.color.primary_green);
        setupCategoryItem(R.id.item_tiet_kiem, R.drawable.ic_saving, getString(R.string.category_savings), R.color.accent_yellow); // Thêm icon ic_savings
        setupCategoryItem(R.id.item_quy_khan_cap, R.drawable.ic_emergency_fund, getString(R.string.category_emergency_fund), R.color.primary_green);
        setupCategoryItem(R.id.item_khac, R.drawable.ic_other, getString(R.string.category_other), R.color.accent_yellow);
        setupCategoryItem(R.id.item_add_more_category, R.drawable.ic_add_circle, getString(R.string.add_more), R.color.accent_yellow);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupCategoryItem(int viewId, int iconResId, String categoryName, int tintColorResId) {
        View itemView = findViewById(viewId);
        if (itemView != null) {
            ImageView iconView = itemView.findViewById(R.id.category_icon);
            TextView nameView = itemView.findViewById(R.id.category_name);

            if (iconView != null) {
                iconView.setImageResource(iconResId);
                iconView.setColorFilter(ContextCompat.getColor(this, tintColorResId));
            }
            if (nameView != null) {
                nameView.setText(categoryName);
            }

            itemView.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_group_name", categoryName);
                resultIntent.putExtra("selected_group_icon", iconResId);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }
    }
}