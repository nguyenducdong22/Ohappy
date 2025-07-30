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

import com.example.noname.R;

/**
 * <p><b>ChooseGroupActivity: Chọn Danh mục (Nhóm) cho Ngân sách</b></p>
 *
 * <p>Activity này được thiết kế để cung cấp một giao diện người dùng trực quan
 * cho phép người dùng chọn một danh mục (hay còn gọi là nhóm chi tiêu)
 * từ một danh sách các danh mục được định nghĩa trước. Danh mục được chọn
 * sau đó sẽ được liên kết với một mục ngân sách mới (hoặc một giao dịch mới).</p>
 *
 * <p><b>Chức năng Chính:</b></p>
 * <ul>
 * <li><b>Hiển thị Lưới Danh mục:</b> Trình bày một tập hợp các danh mục dưới dạng lưới,
 * mỗi danh mục được đại diện bằng một biểu tượng và tên thân thiện với người dùng.</li>
 * <li><b>Xử lý Lựa chọn Người dùng:</b> Khi người dùng chạm vào một mục danh mục,
 * Activity này sẽ ghi nhận lựa chọn đó.</li>
 * <li><b>Trả về Kết quả:</b> Điều quan trọng là Activity này sẽ trả về
 * **tên của danh mục đã chọn** và **ID tài nguyên của biểu tượng tương ứng**
 * trở lại Activity đã gọi nó (thường là {@link AddBudgetActivity}).
 * Điều này được thực hiện thông qua cơ chế `startActivityForResult()` và `onActivityResult()`.</li>
 * <li><b>Điều hướng:</b> Cung cấp các cách để người dùng quay lại Activity trước đó
 * (sử dụng nút quay lại của hệ thống hoặc nút trên Toolbar).</li>
 * </ul>
 */
public class ChooseGroupActivity extends AppCompatActivity {

    /**
     * Phương thức callback này được gọi khi Activity lần đầu tiên được tạo.
     * Đây là nơi chính để khởi tạo giao diện người dùng (UI) và thiết lập các trình lắng nghe sự kiện.
     * @param savedInstanceState Một `Bundle` chứa trạng thái Activity được lưu lại trước đó (nếu có).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);

        // --- Thiết lập Toolbar (thanh công cụ) ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- Xử lý nút quay lại của hệ thống (system back button press) ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true /* enabled */) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // --- Xử lý sự kiện click cho nút quay lại trên Toolbar (navigation icon click) ---
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // --- Thiết lập các mục danh mục riêng lẻ bằng cách sử dụng phương thức trợ giúp ---
        setupCategoryItem(R.id.item_an_uong, R.drawable.ic_restaurant, "Ăn uống", R.color.primary_green);
        setupCategoryItem(R.id.item_nha_cua, R.drawable.ic_home_and_utility, "Nhà cửa & Tiện ích", R.color.primary_green);
        setupCategoryItem(R.id.item_di_chuyen, R.drawable.ic_directions_car, "Di chuyển", R.color.primary_green);
        setupCategoryItem(R.id.item_sam_sua, R.drawable.ic_shopping_basket, "Sắm sửa", R.color.primary_green);
        setupCategoryItem(R.id.item_suc_khoe, R.drawable.ic_health, "Sức Khỏe", R.color.primary_green);
        setupCategoryItem(R.id.item_giao_tiep, R.drawable.ic_person, "Giao Tiếp", R.color.primary_green);

        setupCategoryItem(R.id.item_giai_tri, R.drawable.ic_entertainment, "Giải trí", R.color.primary_green);
        setupCategoryItem(R.id.item_the_thao, R.drawable.ic_sport, "Thể Thao", R.color.accent_yellow);
        setupCategoryItem(R.id.item_lam_dep, R.drawable.ic_beauty, "Làm Đẹp", R.color.primary_green);
        setupCategoryItem(R.id.item_qua_tang, R.drawable.ic_gift, "Quà Tặng", R.color.accent_yellow);
        setupCategoryItem(R.id.item_du_lich, R.drawable.ic_travel, "Du Lịch", R.color.primary_green);
        setupCategoryItem(R.id.item_ban_be, R.drawable.ic_friends, "Bạn Bè", R.color.accent_yellow);

        setupCategoryItem(R.id.item_hoc_tap, R.drawable.ic_learning, "Học Tập", R.color.primary_green);
        setupCategoryItem(R.id.item_sach_vo, R.drawable.ic_book, "Sách vở", R.color.accent_yellow);
        setupCategoryItem(R.id.item_khoa_hoc, R.drawable.ic_course, "Khóa Học", R.color.primary_green);
        setupCategoryItem(R.id.item_dung_cu_hoc_tap, R.drawable.ic_study_tools, "Dụng Cụ Học Tập", R.color.accent_yellow);

        setupCategoryItem(R.id.item_dau_tu, R.drawable.ic_invest, "Đầu Tư", R.color.primary_green);
        setupCategoryItem(R.id.item_tiet_kiem, R.drawable.ic_saving, "Tiết Kiệm", R.color.accent_yellow);
        setupCategoryItem(R.id.item_quy_khan_cap, R.drawable.ic_emergency_fund, "Quỹ Khẩn Cấp", R.color.primary_green);
        setupCategoryItem(R.id.item_khac, R.drawable.ic_other, "Khác", R.color.accent_yellow);
        setupCategoryItem(R.id.item_add_more_category, R.drawable.ic_add_circle, "Thêm", R.color.accent_yellow);
    }

    /**
     * Phương thức trợ giúp để khởi tạo và thiết lập các lắng nghe sự kiện click cho từng mục danh mục riêng lẻ.
     * @param viewId ID tài nguyên của layout cha cho mục danh mục.
     * @param iconResId ID tài nguyên drawable cho biểu tượng của danh mục.
     * @param categoryName Tên hiển thị của danh mục.
     * @param tintColorResId ID tài nguyên màu để tô màu biểu tượng.
     */
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