package com.example.noname.Budget;

import android.app.Activity;    // Import lớp Activity, cần thiết để sử dụng các hằng số kết quả (như `Activity.RESULT_OK`, `Activity.RESULT_CANCELED`) khi nhận kết quả từ các Activity khác (ví dụ: `AddBudgetActivity`).
import android.content.Context; // Cung cấp thông tin môi trường ứng dụng, cần thiết cho các lớp khác hoặc hệ thống.
import android.content.Intent;  // Đối tượng dùng để thực hiện các thao tác "có ý định", chủ yếu là khởi động các Activity khác (như `AddBudgetActivity` hoặc `MainActivity`).
import android.os.Bundle;       // Lớp `Bundle` được dùng để lưu trữ và khôi phục trạng thái của Activity khi nó bị hủy và tạo lại (ví dụ: khi xoay màn hình).
import android.view.View;       // Lớp cơ sở cho mọi thành phần giao diện người dùng (UI) trong Android.
import android.widget.Button;    // Thành phần UI dạng nút bấm.
import android.widget.ImageView; // Thành phần UI để hiển thị hình ảnh, thường dùng cho các biểu tượng hoặc nút dạng hình ảnh.
import android.widget.LinearLayout; // Một loại `ViewGroup` (layout) tổ chức các View con theo một hàng (ngang hoặc dọc).
import android.widget.TextView;    // Thành phần UI để hiển thị văn bản không chỉnh sửa được.
import android.widget.Toast;     // Một đối tượng nhỏ, tạm thời, hiển thị thông báo phản hồi nhanh cho người dùng.
import android.widget.ProgressBar; // Thành phần UI hiển thị tiến độ (có thể không trực tiếp dùng ở đây nhưng được import nếu layout có).
import android.util.Log; // Thêm import cho Log để sử dụng Log.e.

import androidx.annotation.NonNull;     // Annotation chỉ ra rằng một tham số hoặc biến không được là `null`.
import androidx.annotation.Nullable;    // Annotation chỉ ra rằng một tham số hoặc biến CÓ THỂ là `null`.
import androidx.appcompat.app.AppCompatActivity; // Lớp cơ sở cho các Activity trong thư viện AndroidX, cung cấp khả năng tương thích ngược.
import androidx.recyclerview.widget.LinearLayoutManager; // Một `LayoutManager` cho `RecyclerView` để hiển thị các mục theo một danh sách tuyến tính (dọc hoặc ngang).
import androidx.recyclerview.widget.RecyclerView;         // Một View hiệu quả và linh hoạt để hiển thị các danh sách lớn hoặc lưới dữ liệu có thể cuộn được, bằng cách tái sử dụng các View item.

import com.example.noname.AccountActivity;
import com.example.noname.TransactionHistoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Thành phần thanh điều hướng dưới cùng theo Material Design, hiển thị các tùy chọn điều hướng chính của ứng dụng.
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Nút hành động nổi (FAB) theo Material Design, thường dùng để thực hiện hành động chính hoặc phổ biến nhất trên màn hình.

import com.example.noname.MainActivity;
import com.example.noname.R;
import com.example.noname.database.BudgetDAO;

import java.text.NumberFormat;
import java.text.ParseException; // Thêm import này
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * <p><b>BudgetOverviewActivity: Tổng quan và Danh sách Ngân sách</b></p>
 *
 * <p>Activity này đóng vai trò là màn hình tổng quan chính cho tính năng quản lý ngân sách
 * trong ứng dụng. Nó được thiết kế để cung cấp cho người dùng một cái nhìn toàn diện
 * về tình hình ngân sách của họ. Cụ thể, Activity này hiển thị:</p>
 * <ul>
 * <li><b>Một Phần Tổng Hợp (Overview Section):</b> Ở phía trên cùng của màn hình,
 * hiển thị các chỉ số tài chính quan trọng như số tiền còn lại có thể chi tiêu,
 * tổng số tiền đã được đặt ngân sách, tổng số tiền đã chi tiêu trong kỳ ngân sách hiện tại,
 * và số ngày còn lại cho đến khi kỳ ngân sách kết thúc.</li>
 * <li><b>Danh Sách Ngân sách Chi Tiết:</b> Sử dụng một {@link RecyclerView} để trình bày
 * một danh sách cuộn được của từng mục ngân sách riêng lẻ mà người dùng đã tạo.
 * Mỗi mục trong danh sách này cung cấp thông tin chi tiết về từng ngân sách cụ thể
 * và giờ đây sẽ bao gồm các tùy chọn để chỉnh sửa hoặc xóa ngân sách.</li>
 * <li><b>Nút Hành Động:</b> Bao gồm một nút để dễ dàng điều hướng đến màn hình
 * tạo ngân sách mới ({@link AddBudgetActivity}).</li>
 * <li><b>Thanh Điều Hướng Dưới Cùng:</b> Tích hợp {@link BottomNavigationView}
 * để người dùng có thể dễ dàng chuyển đổi giữa các phần chính của ứng dụng
 * (Tổng quan, Giao dịch, Ngân sách, Tài khoản).</li>
 * </ul>
 *
 * <p>Để tải và hiển thị dữ liệu ngân sách, và để xử lý các thao tác sửa/xóa,
 * Activity này tương tác trực tiếp với lớp {@link BudgetDAO}. Việc sử dụng DAO
 * giúp tách biệt logic truy cập dữ liệu khỏi logic giao diện người dùng,
 * làm cho mã sạch hơn, dễ bảo trì và dễ kiểm thử hơn.</p>
 */
public class BudgetOverviewActivity extends AppCompatActivity implements BudgetAdapter.OnBudgetActionListener {

    private static final int REQUEST_CODE_ADD_BUDGET = 2; // Request code for AddBudgetActivity (used for both add and edit).

    // --- UI elements for the overview section ---
    private ImageView btnBackBudgetOverview;
    private Button btnCreateBudget;
    private ImageView btnMoreOptionsAppBar; // Nút ba chấm dọc trên AppBar.

    private TextView tvRemainingSpendableAmount;
    private TextView tvTotalBudgetAmount;
    private TextView tvTotalSpentAmount;
    private TextView tvDaysToEndOfMonth;
    private TextView tvOverviewTitle; // TextView cho tiêu đề tổng quan (ví dụ: "Ngân sách Đang áp dụng" hoặc "Ngân sách cho Ăn uống").

    // --- RecyclerView và Adapter cho danh sách ngân sách ---
    private RecyclerView recyclerViewBudgets;
    private BudgetAdapter budgetAdapter;
    private List<Budget> currentBudgetsList;

    // --- Bottom Navigation Bar và Floating Action Button ---
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;

    private BudgetDAO budgetDAO; // Đối tượng BudgetDAO để tương tác với dữ liệu ngân sách trong database.

    // Biến này sẽ lưu trữ đối tượng Budget đang được hiển thị chi tiết ở phần tổng quan
    // (khu vực khoanh tròn màu đỏ trên ảnh). Nếu là null, có nghĩa là đang hiển thị tổng quan chung.
    private Budget selectedBudgetForOverview = null;
    // User ID giả định. Trong ứng dụng thực, lấy từ phiên đăng nhập.
    private long currentUserId = 1; // TODO: Lấy User ID thực tế.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_overview);

        // Khởi tạo BudgetDAO.
        budgetDAO = new BudgetDAO(this);

        // Ánh xạ các thành phần UI từ layout XML.
        btnBackBudgetOverview = findViewById(R.id.btn_back_budget_overview);
        btnCreateBudget = findViewById(R.id.btn_create_budget);
        btnMoreOptionsAppBar = findViewById(R.id.btn_more_options_appbar); // Ánh xạ nút ba chấm trên AppBar.

        tvRemainingSpendableAmount = findViewById(R.id.tv_remaining_spendable_amount);
        tvTotalBudgetAmount = findViewById(R.id.tv_total_budget_amount);
        tvTotalSpentAmount = findViewById(R.id.tv_total_spent_amount);
        tvDaysToEndOfMonth = findViewById(R.id.tv_days_to_end_of_month);
        tvOverviewTitle = findViewById(R.id.tv_screen_title); // TextView tiêu đề trong AppBar.

        // --- Thiết lập RecyclerView ---
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this)); // Sắp xếp item theo danh sách dọc.
        currentBudgetsList = new ArrayList<>(); // Khởi tạo danh sách dữ liệu.
        // Khởi tạo BudgetAdapter, truyền danh sách dữ liệu VÀ `this` làm listener.
        // `this` là BudgetOverviewActivity, đang triển khai `OnBudgetActionListener`.
        budgetAdapter = new BudgetAdapter(currentBudgetsList, this);
        recyclerViewBudgets.setAdapter(budgetAdapter);

        // --- Khởi tạo Bottom Navigation và FAB ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // --- Thiết lập các trình lắng nghe sự kiện click ---
        btnBackBudgetOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng Activity hiện tại.
            }
        });

        // Lắng nghe click cho nút Tạo Ngân sách.
        btnCreateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BudgetOverviewActivity.this, AddBudgetActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET);
            }
        });

        // Lắng nghe click cho nút ba chấm trên AppBar (tùy chọn tổng quan màn hình).
        btnMoreOptionsAppBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BudgetOverviewActivity.this, "Tùy chọn tổng quan màn hình (chưa triển khai)", Toast.LENGTH_SHORT).show();
                // TODO: Triển khai một PopupMenu hoặc Activity khác cho các tùy chọn tổng quan
                // màn hình (ví dụ: "Lọc ngân sách", "Cài đặt").
            }
        });


        // --- Cấu hình Bottom Navigation ---
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_budget); // Đặt mục "Ngân sách" được chọn.
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_overview) {
                    Intent overviewIntent = new Intent(BudgetOverviewActivity.this, MainActivity.class);
                    overviewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(overviewIntent);
                    return true;
                } else if (itemId == R.id.navigation_transactions) {
                    // Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình giao dịch!", Toast.LENGTH_SHORT).show();
                    // TODO: Điều hướng đến TransactionsActivity.
                    Intent overviewIntent = new Intent(BudgetOverviewActivity.this, TransactionHistoryActivity.class);
                    overviewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(overviewIntent);
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    return true; // Đã ở màn hình này.
                } else if (itemId == R.id.navigation_account) {
                    //Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình tài khoản!", Toast.LENGTH_SHORT).show();
                    // TODO: Điều hướng đến AccountActivity.
                    Intent overviewIntent = new Intent(BudgetOverviewActivity.this, AccountActivity.class);
                    overviewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(overviewIntent);
                    return true;
                }
                return false;
            });
        }

        // --- Cấu hình FAB "Thêm Giao dịch" ---
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(v -> {
                Toast.makeText(BudgetOverviewActivity.this, "Thêm giao dịch mới từ màn hình Ngân sách!", Toast.LENGTH_SHORT).show();
                // TODO: Điều hướng đến màn hình Thêm Giao dịch.
            });
        }

        // Tải các ngân sách và hiển thị chúng khi Activity được tạo.
        loadBudgetsAndDisplay();
    }

    /**
     * Xử lý kết quả trả về từ các Activity khác (ví dụ: AddBudgetActivity).
     * @param requestCode Mã yêu cầu.
     * @param resultCode Mã kết quả (Activity.RESULT_OK, Activity.RESULT_CANCELED).
     * @param data Intent chứa dữ liệu kết quả.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_BUDGET) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Ngân sách đã được lưu!", Toast.LENGTH_SHORT).show();
                // Sau khi thêm/sửa thành công, tải lại danh sách và đặt lại tổng quan chung.
                selectedBudgetForOverview = null; // Đặt lại về chế độ tổng quan chung.
                loadBudgetsAndDisplay(); // Tải lại dữ liệu và cập nhật UI.
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Thao tác đã bị hủy.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Tải tất cả các ngân sách từ database và cập nhật RecyclerView.
     * Đồng thời, cập nhật phần tổng quan (khu vực khoanh tròn đỏ)
     * để hiển thị chi tiết ngân sách đã chọn hoặc tổng quan chung.
     */
    private void loadBudgetsAndDisplay() {
        budgetDAO.open(); // Mở kết nối database.
        List<Budget> loadedBudgets = budgetDAO.getAllBudgets(); // Lấy tất cả ngân sách.
        budgetDAO.close(); // Đóng kết nối.

        currentBudgetsList.clear(); // Xóa dữ liệu cũ.
        currentBudgetsList.addAll(loadedBudgets); // Thêm dữ liệu mới.
        budgetAdapter.setBudgetList(currentBudgetsList); // Cập nhật Adapter, RecyclerView sẽ refresh.

        // Cập nhật phần tổng quan dựa trên `selectedBudgetForOverview`.
        // Nếu `selectedBudgetForOverview` vẫn giữ giá trị (sau khi sửa chẳng hạn),
        // thì hiển thị chi tiết của nó. Ngược lại, hiển thị tổng quan chung.
        displayOverviewForBudget(selectedBudgetForOverview); // FIXED: Corrected variable name.
    }

    /**
     * Cập nhật phần tổng quan (khu vực khoanh tròn màu đỏ) để hiển thị chi tiết của một ngân sách cụ thể,
     * hoặc một tổng quan chung nếu không có ngân sách nào được cung cấp (hoặc là null).
     * @param budget Đối tượng Budget để hiển thị chi tiết, hoặc null để hiển thị tổng quan chung.
     */
    private void displayOverviewForBudget(@Nullable Budget budget) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);

        // TODO: Lấy User ID thực tế từ phiên đăng nhập.
        //long userId = 1; // Sử dụng userId đã có.

        if (budget == null) {
            // --- Hiển thị Tổng quan chung (khi không có ngân sách cụ thể nào được chọn) ---
            tvOverviewTitle.setText("Ngân sách Đang áp dụng"); // Tiêu đề mặc định cho tổng quan chung.
            selectedBudgetForOverview = null; // Đảm bảo biến này là null khi ở chế độ tổng quan chung.

            double totalAllBudgetsAmount = 0.0; // Tổng số tiền từ tất cả các ngân sách.
            double totalAllSpentAmount = 0.0;   // Tổng số tiền đã chi tiêu qua tất cả các ngân sách.

            budgetDAO.open(); // Mở DB để tính toán tổng chi tiêu.
            for (Budget b : currentBudgetsList) {
                totalAllBudgetsAmount += b.getAmount(); // Tổng tất cả ngân sách.
                // Tính tổng đã chi cho MỖI ngân sách và cộng dồn.
                totalAllSpentAmount += budgetDAO.getTotalSpentForBudgetCategory(b.getCategoryId(), b.getStartDate(), b.getEndDate(), currentUserId);
            }
            budgetDAO.close(); // Đóng DB.

            int daysToEndOfCurrentMonth = getDaysToEndOfCurrentMonth();

            tvRemainingSpendableAmount.setText(currencyFormat.format(totalAllBudgetsAmount - totalAllSpentAmount));
            tvTotalBudgetAmount.setText(String.format("%,.0f M", totalAllBudgetsAmount / 1000000.0)); // Hiển thị "M" cho triệu.
            tvTotalSpentAmount.setText(String.format("%,.0f K", totalAllSpentAmount / 1000.0)); // Hiển thị "K" cho nghìn.
            tvDaysToEndOfMonth.setText(daysToEndOfCurrentMonth + " ngày");

        } else {
            // --- Hiển thị Chi tiết Ngân sách Cụ thể (khi một ngân sách được click) ---
            tvOverviewTitle.setText("Ngân sách cho " + budget.getGroupName()); // Tiêu đề động theo tên danh mục.
            selectedBudgetForOverview = budget; // Lưu lại ngân sách đang được hiển thị chi tiết.

            budgetDAO.open(); // Mở DB để tính toán chi tiêu cho ngân sách này.
            double spentAmountForThisBudget = budgetDAO.getTotalSpentForBudgetCategory(budget.getCategoryId(), budget.getStartDate(), budget.getEndDate(), currentUserId);
            budgetDAO.close(); // Đóng DB.

            double remainingAmountForThisBudget = budget.getAmount() - spentAmountForThisBudget;
            int daysRemainingForThisBudget = getDaysBetweenDates(new Date(), parseDate(budget.getEndDate()));

            tvRemainingSpendableAmount.setText(currencyFormat.format(remainingAmountForThisBudget)); // Số tiền còn lại của ngân sách này.
            tvTotalBudgetAmount.setText(currencyFormat.format(budget.getAmount())); // Tổng ngân sách này (không phải tổng các M).
            tvTotalSpentAmount.setText(currencyFormat.format(spentAmountForThisBudget)); // Số tiền đã chi của ngân sách này.
            tvDaysToEndOfMonth.setText(daysRemainingForThisBudget + " ngày");
        }
    }


    //region OnBudgetActionListener Implementations

    /**
     * Xử lý hành động "Sửa Ngân sách" từ BudgetAdapter.
     * Bắt đầu AddBudgetActivity ở chế độ chỉnh sửa, truyền dữ liệu ngân sách qua Intent.
     * @param budget Đối tượng Budget cần được sửa.
     */
    @Override
    public void onEditBudget(Budget budget) {
        Intent intent = new Intent(BudgetOverviewActivity.this, AddBudgetActivity.class);
        // Truyền đối tượng Budget cần sửa bằng cách sử dụng Serializable.
        intent.putExtra(AddBudgetActivity.EXTRA_BUDGET_TO_EDIT, budget);
        startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET);
    }

    /**
     * Xử lý hành động "Xóa Ngân sách" từ BudgetAdapter.
     * Xóa ngân sách khỏi database và làm mới danh sách.
     * @param budget Đối tượng Budget cần được xóa.
     */
    @Override
    public void onDeleteBudget(Budget budget) {
        // Thực hiện xóa ngân sách từ database.
        // Trong ứng dụng thực tế, nên thêm một hộp thoại xác nhận trước khi xóa để cải thiện UX.
        budgetDAO.open();
        boolean success = budgetDAO.deleteBudget(budget.getId());
        budgetDAO.close();

        if (success) {
            Toast.makeText(this, "Đã xóa ngân sách: " + budget.getGroupName(), Toast.LENGTH_SHORT).show();
            // Nếu ngân sách bị xóa là ngân sách đang được hiển thị chi tiết ở phần tổng quan,
            // thì chúng ta cần đặt lại `selectedBudgetForOverview` về null để hiển thị tổng quan chung.
            if (selectedBudgetForOverview != null && selectedBudgetForOverview.getId() == budget.getId()) {
                selectedBudgetForOverview = null;
            }
            loadBudgetsAndDisplay(); // Làm mới danh sách và phần tổng quan.
        } else {
            Toast.makeText(this, "Lỗi khi xóa ngân sách: " + budget.getGroupName(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xử lý hành động click vào một mục ngân sách từ BudgetAdapter.
     * Cập nhật phần tổng quan để hiển thị chi tiết của ngân sách được click.
     * @param budget Đối tượng Budget đã được click.
     */
    @Override
    public void onBudgetClick(Budget budget) {
        // Khi một mục ngân sách được click, chúng ta sẽ cập nhật phần tổng quan
        // để hiển thị chi tiết của ngân sách đó.
        displayOverviewForBudget(budget);
        Toast.makeText(this, "Hiển thị chi tiết ngân sách: " + budget.getGroupName(), Toast.LENGTH_SHORT).show();
    }
    //endregion


    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại ngân sách mỗi khi Activity trở lại foreground
        // để đảm bảo danh sách và tổng quan luôn được cập nhật.
        loadBudgetsAndDisplay();
    }

    //region Date Calculation Utility Methods (cho mục đích hiển thị)

    /**
     * Tính toán số ngày còn lại đến cuối tháng hiện tại.
     * Đây là một phương thức tiện ích đơn giản cho phần tổng quan chung.
     * @return Số ngày còn lại. Trả về 0 nếu đã qua cuối tháng.
     */
    private int getDaysToEndOfCurrentMonth() {
        Calendar today = Calendar.getInstance();
        Calendar lastDayOfMonth = (Calendar) today.clone();
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastDayOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        lastDayOfMonth.set(Calendar.MINUTE, 59);
        lastDayOfMonth.set(Calendar.SECOND, 59);
        lastDayOfMonth.set(Calendar.MILLISECOND, 999); // Đảm bảo tính đến hết giây cuối cùng của ngày.

        long diffMillis = lastDayOfMonth.getTimeInMillis() - today.getTimeInMillis();
        if (diffMillis < 0) return 0; // Nếu đã qua cuối tháng.
        return (int) TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Tính toán số ngày giữa hai ngày cụ thể.
     * @param startDate Ngày bắt đầu.
     * @param endDate Ngày kết thúc.
     * @return Số ngày. Trả về 0 nếu ngày không hợp lệ hoặc ngày bắt đầu sau ngày kết thúc.
     */
    private int getDaysBetweenDates(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        // Đảm bảo chỉ so sánh ngày, bỏ qua giờ, phút, giây để tính đúng số ngày.
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startDate);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        long diffMillis = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        if (diffMillis < 0) return 0; // Nếu ngày kết thúc trước ngày bắt đầu.
        return (int) TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Phân tích một chuỗi ngày (YYYY-MM-DD) thành một đối tượng Date.
     * @param dateString Chuỗi ngày cần phân tích.
     * @return Đối tượng Date, hoặc null nếu phân tích thất bại.
     */
    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("BudgetOverviewActivity", "Lỗi phân tích chuỗi ngày: " + dateString + " - " + e.getMessage());
            return null;
        }
    }
    //endregion
}