package com.example.noname.Budget;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.noname.R; // Used for resource IDs (drawables, colors, layout IDs).

/**
 * ChooseGroupActivity allows the user to select a predefined category (group) for a budget.
 * It displays a grid of categories with their icons and names, and returns the selected
 * category's name and icon resource ID back to the calling activity (AddBudgetActivity).
 */
public class ChooseGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group); // Set the layout for this activity.

        // Setup Toolbar for navigation.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the Up button (back arrow).
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Make the Up button visible.
        }

        // Handle the system's back button press (physical button or gesture).
        // This ensures a RESULT_CANCELED is sent if the user simply goes back.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED); // Set result to indicate cancellation.
                finish(); // Close the activity.
            }
        });

        // Handle the back button on the Toolbar (navigation icon click).
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED); // Set result to indicate cancellation.
            finish(); // Close the activity.
        });

        // --- Setup individual category items using the helper method ---
        // Nhu Cầu Thiết Yếu (Essential Needs)
        setupCategoryItem(R.id.item_an_uong, R.drawable.ic_restaurant, "Ăn uống", R.color.primary_green);
        setupCategoryItem(R.id.item_nha_cua, R.drawable.ic_home_and_utility, "Nhà cửa & Tiện ích", R.color.primary_green);
        setupCategoryItem(R.id.item_di_chuyen, R.drawable.ic_directions_car, "Di chuyển", R.color.primary_green);
        setupCategoryItem(R.id.item_sam_sua, R.drawable.ic_shopping_basket, "Sắm sửa", R.color.primary_green);
        setupCategoryItem(R.id.item_suc_khoe, R.drawable.ic_health, "Sức Khỏe", R.color.primary_green);
        setupCategoryItem(R.id.item_giao_tiep, R.drawable.ic_person, "Giao Tiếp", R.color.primary_green);

        // Giải Trí, Cá Nhân (Entertainment, Personal)
        setupCategoryItem(R.id.item_giai_tri, R.drawable.ic_entertainment, "Giải trí", R.color.primary_green);
        setupCategoryItem(R.id.item_the_thao, R.drawable.ic_sport, "Thể Thao", R.color.accent_yellow);
        setupCategoryItem(R.id.item_lam_dep, R.drawable.ic_beauty, "Làm Đẹp", R.color.primary_green);
        setupCategoryItem(R.id.item_qua_tang, R.drawable.ic_gift, "Quà Tặng", R.color.accent_yellow);
        setupCategoryItem(R.id.item_du_lich, R.drawable.ic_travel, "Du Lịch", R.color.primary_green);
        setupCategoryItem(R.id.item_ban_be, R.drawable.ic_friends, "Bạn Bè", R.color.accent_yellow);

        // Học Tập, Phát Triển (Learning, Development)
        setupCategoryItem(R.id.item_hoc_tap, R.drawable.ic_learning, "Học Tập", R.color.primary_green);
        setupCategoryItem(R.id.item_sach_vo, R.drawable.ic_book, "Sách vở", R.color.accent_yellow);
        setupCategoryItem(R.id.item_khoa_hoc, R.drawable.ic_course, "Khóa Học", R.color.primary_green);
        setupCategoryItem(R.id.item_dung_cu_hoc_tap, R.drawable.ic_study_tools, "Dụng Cụ Học Tập", R.color.accent_yellow);

        // Chi Phí Khác (Other Expenses)
        setupCategoryItem(R.id.item_dau_tu, R.drawable.ic_invest, "Đầu Tư", R.color.primary_green);
        setupCategoryItem(R.id.item_tiet_kiem, R.drawable.ic_saving, "Tiết Kiệm", R.color.accent_yellow);
        setupCategoryItem(R.id.item_quy_khan_cap, R.drawable.ic_emergency_fund, "Quỹ Khẩn Cấp", R.color.primary_green);
        setupCategoryItem(R.id.item_khac, R.drawable.ic_other, "Khác", R.color.accent_yellow);
        setupCategoryItem(R.id.item_add_more_category, R.drawable.ic_add_circle, "Thêm", R.color.accent_yellow);
    }

    /**
     * A helper method to initialize and set up click listeners for individual category items.
     * Each item consists of an ImageView (for the icon) and a TextView (for the category name).
     * @param viewId The resource ID of the parent layout (e.g., MaterialCardView) for the category item.
     * @param iconResId The drawable resource ID for the category's icon (e.g., R.drawable.ic_restaurant).
     * @param categoryName The display name of the category (e.g., "Ăn Uống").
     * @param tintColorResId The color resource ID to tint the icon (e.g., R.color.primary_green).
     */
    private void setupCategoryItem(int viewId, int iconResId, String categoryName, int tintColorResId) {
        View itemView = findViewById(viewId); // Find the overall item view by its ID.
        if (itemView != null) {
            ImageView iconView = itemView.findViewById(R.id.category_icon); // Find the icon ImageView within the item.
            TextView nameView = itemView.findViewById(R.id.category_name);   // Find the name TextView within the item.

            // Set the icon and its tint color.
            if (iconView != null) {
                iconView.setImageResource(iconResId);
                iconView.setColorFilter(ContextCompat.getColor(this, tintColorResId));
            }
            // Set the category name text.
            if (nameView != null) {
                nameView.setText(categoryName);
            }

            // Set an OnClickListener for the entire item view.
            itemView.setOnClickListener(v -> {
                // When an item is clicked, create an Intent to send back the result.
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_group_name", categoryName); // Put the selected category name.
                resultIntent.putExtra("selected_group_icon", iconResId); // Put the selected icon's resource ID.
                setResult(RESULT_OK, resultIntent); // Set the result code to OK and attach the Intent.
                finish(); // Close this ChooseGroupActivity.
            });
        }
    }
}