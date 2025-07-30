package com.example.noname.Budget;

import android.content.Intent; // Lớp Intent là một đối tượng tin nhắn cho phép các thành phần ứng dụng giao tiếp với nhau. Ở đây, nó dùng để mang dữ liệu kết quả (tên nhóm và icon) trở lại Activity đã gọi nó.
import android.os.Bundle;     // Lớp Bundle được dùng để lưu trữ và khôi phục trạng thái của Activity.
import android.view.View;      // Lớp cơ sở cho mọi thành phần giao diện người dùng.
import android.widget.ImageView; // Thành phần UI để hiển thị hình ảnh (biểu tượng danh mục).
import android.widget.TextView;    // Thành phần UI để hiển thị văn bản (tên danh mục).
import androidx.activity.OnBackPressedCallback; // Một callback cho phép bạn tùy chỉnh hành vi khi người dùng nhấn nút quay lại của hệ thống (nút vật lý hoặc cử chỉ).
import androidx.appcompat.app.AppCompatActivity; // Lớp cơ sở cho các Activity trong thư viện AndroidX, cung cấp khả năng tương thích ngược trên các phiên bản Android cũ hơn.
import androidx.appcompat.widget.Toolbar;        // Một thành phần thanh công cụ linh hoạt, thường được sử dụng thay thế cho ActionBar mặc định.
import androidx.core.content.ContextCompat;     // Một lớp tiện ích từ AndroidX, cung cấp các phương thức tương thích ngược để truy cập tài nguyên (như màu sắc) một cách an toàn trên các phiên bản Android khác nhau.

import com.example.noname.R; // Lớp `R` tự động được tạo ra, chứa các ID cho tất cả các tài nguyên trong dự án (layout, drawable, color, id, v.v.).

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
        super.onCreate(savedInstanceState); // Luôn gọi phương thức `onCreate` của lớp cha để thực hiện các khởi tạo cơ bản của hệ thống Android.
        setContentView(R.layout.activity_choose_group); // Gắn layout XML `activity_choose_group.xml` làm giao diện người dùng cho Activity này. Hệ thống sẽ inflate layout này, tạo ra các đối tượng View từ XML.

        // --- Thiết lập Toolbar (thanh công cụ) ---
        Toolbar toolbar = findViewById(R.id.toolbar); // Tìm Toolbar trong layout bằng ID của nó.
        setSupportActionBar(toolbar); // Đặt Toolbar này làm Action Bar của Activity.
        if (getSupportActionBar() != null) { // Kiểm tra xem Action Bar có tồn tại không.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Bật nút "Up" (thường là biểu tượng mũi tên quay lại) ở góc trên bên trái của Toolbar.
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Đảm bảo rằng nút "Up" này được hiển thị.
        }

        // --- Xử lý nút quay lại của hệ thống (system back button press) ---
        // Android cung cấp `OnBackPressedCallback` để tùy chỉnh hành vi của nút quay lại.
        // Chúng ta thêm một callback vào `OnBackPressedDispatcher` để:
        // 1. Tùy chỉnh hành vi khi người dùng nhấn nút quay lại (vật lý hoặc cử chỉ).
        // 2. Đảm bảo rằng một `RESULT_CANCELED` được gửi về cho Activity đã gọi.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true /* enabled */) {
            @Override
            public void handleOnBackPressed() {
                // Khi nút quay lại được nhấn, chúng ta đặt kết quả của Activity là HỦY BỎ.
                // Điều này báo hiệu cho Activity cha rằng người dùng đã không chọn một nhóm nào.
                setResult(RESULT_CANCELED);
                finish(); // Gọi `finish()` để đóng Activity hiện tại và quay lại Activity trước đó trên ngăn xếp hoạt động.
            }
        });

        // --- Xử lý sự kiện click cho nút quay lại trên Toolbar (navigation icon click) ---
        // Nút này thường là biểu tượng mũi tên ở phía bên trái của Toolbar, được bật bởi `setDisplayHomeAsUpEnabled(true)`.
        toolbar.setNavigationOnClickListener(v -> {
            // Tương tự như nút quay lại của hệ thống, đặt kết quả là HỦY BỎ.
            setResult(RESULT_CANCELED);
            finish(); // Đóng Activity.
        });

        // --- Thiết lập các mục danh mục riêng lẻ bằng cách sử dụng phương thức trợ giúp ---
        // Mỗi dòng gọi phương thức `setupCategoryItem` để cấu hình một mục danh mục cụ thể
        // trong lưới hiển thị. Các tham số bao gồm:
        // - ID của layout cha cho mục đó (ví dụ: `R.id.item_an_uong`).
        // - ID tài nguyên của biểu tượng drawable cho danh mục (ví dụ: `R.drawable.ic_restaurant`).
        // - Tên hiển thị của danh mục (ví dụ: "Ăn uống").
        // - ID tài nguyên màu để tô màu biểu tượng (ví dụ: `R.color.primary_green`).

        // Nhóm "Nhu Cầu Thiết Yếu" (Essential Needs Categories)
        setupCategoryItem(R.id.item_an_uong, R.drawable.ic_restaurant, "Ăn uống", R.color.primary_green);
        setupCategoryItem(R.id.item_nha_cua, R.drawable.ic_home_and_utility, "Nhà cửa & Tiện ích", R.color.primary_green);
        setupCategoryItem(R.id.item_di_chuyen, R.drawable.ic_directions_car, "Di chuyển", R.color.primary_green);
        setupCategoryItem(R.id.item_sam_sua, R.drawable.ic_shopping_basket, "Sắm sửa", R.color.primary_green);
        setupCategoryItem(R.id.item_suc_khoe, R.drawable.ic_health, "Sức Khỏe", R.color.primary_green);
        setupCategoryItem(R.id.item_giao_tiep, R.drawable.ic_person, "Giao Tiếp", R.color.primary_green);

        // Nhóm "Giải Trí, Cá Nhân" (Entertainment, Personal Categories)
        setupCategoryItem(R.id.item_giai_tri, R.drawable.ic_entertainment, "Giải trí", R.color.primary_green);
        setupCategoryItem(R.id.item_the_thao, R.drawable.ic_sport, "Thể Thao", R.color.accent_yellow);
        setupCategoryItem(R.id.item_lam_dep, R.drawable.ic_beauty, "Làm Đẹp", R.color.primary_green);
        setupCategoryItem(R.id.item_qua_tang, R.drawable.ic_gift, "Quà Tặng", R.color.accent_yellow);
        setupCategoryItem(R.id.item_du_lich, R.drawable.ic_travel, "Du Lịch", R.color.primary_green);
        setupCategoryItem(R.id.item_ban_be, R.drawable.ic_friends, "Bạn Bè", R.color.accent_yellow);

        // Nhóm "Học Tập, Phát Triển" (Learning, Development Categories)
        setupCategoryItem(R.id.item_hoc_tap, R.drawable.ic_learning, "Học Tập", R.color.primary_green);
        setupCategoryItem(R.id.item_sach_vo, R.drawable.ic_book, "Sách vở", R.color.accent_yellow);
        setupCategoryItem(R.id.item_khoa_hoc, R.drawable.ic_course, "Khóa Học", R.color.primary_green);
        setupCategoryItem(R.id.item_dung_cu_hoc_tap, R.drawable.ic_study_tools, "Dụng Cụ Học Tập", R.color.accent_yellow);

        // Nhóm "Chi Phí Khác" (Other Expenses Categories)
        setupCategoryItem(R.id.item_dau_tu, R.drawable.ic_invest, "Đầu Tư", R.color.primary_green);
        setupCategoryItem(R.id.item_tiet_kiem, R.drawable.ic_saving, "Tiết Kiệm", R.color.accent_yellow);
        setupCategoryItem(R.id.item_quy_khan_cap, R.drawable.ic_emergency_fund, "Quỹ Khẩn Cấp", R.color.primary_green);
        setupCategoryItem(R.id.item_khac, R.drawable.ic_other, "Khác", R.color.accent_yellow);
        setupCategoryItem(R.id.item_add_more_category, R.drawable.ic_add_circle, "Thêm", R.color.accent_yellow);
        // TODO: Mục "Thêm" này có thể dẫn đến một màn hình khác để người dùng tạo danh mục tùy chỉnh mới
        // và sau đó danh mục mới này có thể được lưu vào cơ sở dữ liệu để sử dụng trong tương lai.
    }

    /**
     * <p><b>`setupCategoryItem`: Phương Thức Trợ Giúp để Thiết Lập Các Mục Danh mục</b></p>
     *
     * <p>Phương thức tiện ích này được thiết kế để đơn giản hóa quá trình khởi tạo
     * và thiết lập các trình lắng nghe sự kiện click cho từng mục danh mục riêng lẻ
     * trong lưới hiển thị. Mỗi mục danh mục trong layout (`item_category_grid.xml` hoặc tương tự)
     * thường bao gồm một {@link ImageView} (cho biểu tượng) và một {@link TextView} (cho tên danh mục).</p>
     *
     * <p><b>Chức năng:</b></p>
     * <ol>
     * <li>Tìm kiếm các View thành phần (ImageView, TextView) bên trong View cha của mục.</li>
     * <li>Đặt biểu tượng và tên cho danh mục.</li>
     * <li>Áp dụng màu sắc (tint) cho biểu tượng để phù hợp với chủ đề UI.</li>
     * <li>Thiết lập một `OnClickListener` cho toàn bộ View của mục. Khi mục này được nhấn,
     * nó sẽ tạo một `Intent` chứa thông tin về danh mục đã chọn và gửi Intent đó
     * trở lại Activity đã gọi (`AddBudgetActivity`).</li>
     * </ol>
     *
     * @param viewId        ID tài nguyên của layout cha (ví dụ: `MaterialCardView`) cho mục danh mục
     * (ví dụ: `R.id.item_an_uong`).
     * @param iconResId     ID tài nguyên drawable cho biểu tượng của danh mục (ví dụ: `R.drawable.ic_restaurant`).
     * @param categoryName  Tên hiển thị của danh mục (ví dụ: "Ăn Uống"). Đây là giá trị chuỗi sẽ được trả về.
     * @param tintColorResId ID tài nguyên màu để tô màu biểu tượng (ví dụ: `R.color.primary_green`).
     */
    private void setupCategoryItem(int viewId, int iconResId, String categoryName, int tintColorResId) {
        // 1. Tìm View gốc của mục bằng ID của nó.
        View itemView = findViewById(viewId);
        // Kiểm tra null để đảm bảo View được tìm thấy trước khi chúng ta cố gắng tương tác với nó.
        // Điều này giúp tránh NullPointerException nếu ID không tồn tại hoặc có lỗi trong layout.
        if (itemView != null) {
            // 2. Tìm các View con (ImageView và TextView) bên trong View gốc của mục.
            ImageView iconView = itemView.findViewById(R.id.category_icon); // Tìm ImageView cho biểu tượng.
            TextView nameView = itemView.findViewById(R.id.category_name);   // Tìm TextView cho tên danh mục.

            // 3. Đặt biểu tượng và màu sắc cho nó.
            if (iconView != null) { // Kiểm tra null cho ImageView.
                iconView.setImageResource(iconResId); // Đặt ID tài nguyên drawable cho ImageView.
                // Sử dụng `ContextCompat.getColor()` để lấy màu từ tài nguyên màu một cách an toàn,
                // đảm bảo tương thích trên các phiên bản Android khác nhau.
                iconView.setColorFilter(ContextCompat.getColor(this, tintColorResId)); // Đặt màu tô cho biểu tượng.
            }
            // 4. Đặt văn bản cho tên danh mục.
            if (nameView != null) { // Kiểm tra null cho TextView.
                nameView.setText(categoryName); // Đặt tên danh mục vào TextView.
            }

            // 5. Thiết lập một `OnClickListener` cho toàn bộ View của mục.
            // Khi người dùng chạm vào một mục danh mục, chúng ta sẽ gửi kết quả trở lại
            // Activity đã gọi (thường là `AddBudgetActivity`).
            itemView.setOnClickListener(v -> {
                // Tạo một đối tượng `Intent` mới. `Intent` này sẽ mang dữ liệu kết quả
                // trở lại Activity đã gọi.
                Intent resultIntent = new Intent();
                // Đặt tên danh mục đã chọn vào Intent. Key "selected_group_name" sẽ được Activity gọi sử dụng để truy xuất.
                resultIntent.putExtra("selected_group_name", categoryName);
                // Đặt ID tài nguyên của biểu tượng đã chọn vào Intent.
                resultIntent.putExtra("selected_group_icon", iconResId);
                // Đặt mã kết quả là `RESULT_OK` (nghĩa là thao tác thành công)
                // và gắn `resultIntent` chứa dữ liệu vào kết quả.
                setResult(RESULT_OK, resultIntent);
                // Gọi `finish()` để đóng `ChooseGroupActivity` hiện tại.
                // Điều này sẽ đưa người dùng trở lại Activity đã gọi
                // và kích hoạt phương thức `onActivityResult()` của Activity đó để xử lý kết quả.
                finish();
            });
        }
    }
}