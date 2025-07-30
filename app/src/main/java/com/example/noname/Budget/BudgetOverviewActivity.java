package com.example.noname.Budget;

import android.app.Activity;    // Import lớp Activity, cần thiết để sử dụng các hằng số kết quả (như `Activity.RESULT_OK`, `Activity.RESULT_CANCELED`) khi nhận kết quả từ các Activity khác (ví dụ: `AddBudgetActivity`).
import android.content.Context; // Cung cấp thông tin môi trường ứng dụng, đôi khi cần thiết cho các lớp khác hoặc hệ thống.
import android.content.Intent;  // Đối tượng dùng để thực hiện các thao tác "có ý định", chủ yếu là khởi động các Activity khác (như `AddBudgetActivity` hoặc `MainActivity`).
import android.os.Bundle;       // Lớp `Bundle` được dùng để lưu trữ và khôi phục trạng thái của Activity khi nó bị hủy và tạo lại (ví dụ: khi xoay màn hình).
import android.view.View;       // Lớp cơ sở cho mọi thành phần giao diện người dùng (UI) trong Android.
import android.widget.Button;    // Thành phần UI dạng nút bấm.
import android.widget.ImageView; // Thành phần UI để hiển thị hình ảnh, thường dùng cho các biểu tượng hoặc nút dạng hình ảnh.
import android.widget.LinearLayout; // Một loại `ViewGroup` (layout) tổ chức các View con theo một hàng (ngang hoặc dọc).
import android.widget.TextView;    // Thành phần UI để hiển thị văn bản không chỉnh sửa được.
import android.widget.Toast;     // Một đối tượng nhỏ, tạm thời, hiển thị thông báo phản hồi nhanh cho người dùng.
import android.widget.ProgressBar; // Thành phần UI hiển thị tiến độ (có thể không trực tiếp dùng ở đây nhưng được import nếu layout có).

import androidx.annotation.NonNull;     // Annotation chỉ ra rằng một tham số hoặc biến không được là `null`.
import androidx.annotation.Nullable;    // Annotation chỉ ra rằng một tham số hoặc biến CÓ THỂ là `null`.
import androidx.appcompat.app.AppCompatActivity; // Lớp cơ sở cho các Activity trong thư viện AndroidX, cung cấp khả năng tương thích ngược.
import androidx.recyclerview.widget.LinearLayoutManager; // Một `LayoutManager` cho `RecyclerView` để hiển thị các mục theo một danh sách tuyến tính (dọc hoặc ngang).
import androidx.recyclerview.widget.RecyclerView;         // Một View hiệu quả và linh hoạt để hiển thị các danh sách lớn hoặc lưới dữ liệu có thể cuộn được, bằng cách tái sử dụng các View item.

import com.google.android.material.bottomnavigation.BottomNavigationView; // Thành phần thanh điều hướng dưới cùng theo Material Design, hiển thị các tùy chọn điều hướng chính của ứng dụng.
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Nút hành động nổi (FAB) theo Material Design, thường dùng để thực hiện hành động chính hoặc phổ biến nhất trên màn hình.

import com.example.noname.MainActivity;      // Import `MainActivity`, được sử dụng để điều hướng trở lại màn hình tổng quan chính của ứng dụng.
import com.example.noname.R;                 // Lớp `R` tự động được tạo ra, chứa các ID cho tất cả các tài nguyên của ứng dụng (layout, drawable, id, string, v.v.).
import com.example.noname.database.BudgetDAO; // Mới: Import lớp `BudgetDAO` (Data Access Object), lớp này sẽ được sử dụng để truy xuất và quản lý dữ liệu ngân sách từ cơ sở dữ liệu.

import java.text.NumberFormat; // Lớp này được dùng để định dạng các giá trị số (ví dụ: số tiền) thành chuỗi văn bản theo định dạng tiền tệ hoặc số cụ thể.
import java.util.ArrayList;    // Một lớp triển khai của giao diện `List`, cung cấp một mảng có thể thay đổi kích thước. Dùng để lưu trữ danh sách các đối tượng `Budget`.
import java.util.List;         // Giao diện `List` từ Java Collections Framework, đại diện cho một tập hợp có thứ tự của các phần tử.
import java.util.Locale;       // Đối tượng `Locale` đại diện cho một khu vực địa lý hoặc văn hóa. Dùng để định dạng tiền tệ theo quy ước địa phương (ví dụ: tiếng Việt, Việt Nam).

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
 * Mỗi mục trong danh sách này cung cấp thông tin chi tiết về từng ngân sách cụ thể.</li>
 * <li><b>Nút Hành Động:</b> Bao gồm một nút để dễ dàng điều hướng đến màn hình
 * tạo ngân sách mới ({@link AddBudgetActivity}).</li>
 * <li><b>Thanh Điều Hướng Dưới Cùng:</b> Tích hợp {@link BottomNavigationView}
 * để người dùng có thể dễ dàng chuyển đổi giữa các phần chính của ứng dụng
 * (Tổng quan, Giao dịch, Ngân sách, Tài khoản).</li>
 * </ul>
 *
 * <p>Để tải và hiển thị dữ liệu ngân sách, Activity này tương tác trực tiếp
 * với lớp {@link BudgetDAO}. Việc sử dụng DAO giúp tách biệt logic truy cập dữ liệu
 * khỏi logic giao diện người dùng, làm cho mã sạch hơn, dễ bảo trì và dễ kiểm thử hơn.</p>
 */
public class BudgetOverviewActivity extends AppCompatActivity {

    // Mã yêu cầu (request code) duy nhất được sử dụng khi khởi chạy AddBudgetActivity.
    // Mã này giúp xác định kết quả trả về trong phương thức `onActivityResult()`
    // và đảm bảo rằng chúng ta xử lý đúng phản hồi từ `AddBudgetActivity`.
    private static final int REQUEST_CODE_ADD_BUDGET = 2;

    // --- Khai báo các thành phần giao diện người dùng (UI elements) cho phần tổng quan ---
    // Các biến này sẽ được liên kết với các View tương ứng trong layout XML thông qua `findViewById()`.
    private ImageView btnBackBudgetOverview; // ImageView đóng vai trò là nút quay lại, thường hiển thị biểu tượng mũi tên hoặc "X".
    private Button btnCreateBudget;          // Nút bấm này sẽ kích hoạt việc mở `AddBudgetActivity` để tạo một ngân sách mới.

    private TextView tvRemainingSpendableAmount; // TextView hiển thị số tiền còn lại mà người dùng có thể chi tiêu trong kỳ ngân sách hiện tại.
    private TextView tvTotalBudgetAmount;        // TextView hiển thị tổng số tiền mà người dùng đã đặt làm ngân sách cho tất cả các danh mục.
    private TextView tvTotalSpentAmount;         // TextView hiển thị tổng số tiền thực tế mà người dùng đã chi tiêu trong kỳ ngân sách hiện tại.
    private TextView tvDaysToEndOfMonth;         // TextView hiển thị số ngày còn lại cho đến khi kỳ ngân sách hiện tại kết thúc (ví dụ: đến cuối tháng).

    // --- Khai báo các thành phần liên quan đến RecyclerView và Adapter cho danh sách ngân sách ---
    private RecyclerView recyclerViewBudgets;  // `RecyclerView` là một View mạnh mẽ và hiệu quả để hiển thị danh sách dữ liệu có thể cuộn được.
    private BudgetAdapter budgetAdapter;      // `BudgetAdapter` là Adapter chịu trách nhiệm "kết nối" dữ liệu (danh sách `Budget` objects) với `RecyclerView` để hiển thị.
    private List<Budget> currentBudgetsList; // Danh sách các đối tượng `Budget` hiện tại. Đây là nguồn dữ liệu mà `budgetAdapter` sẽ sử dụng để hiển thị các mục ngân sách. Nó sẽ được tải từ cơ sở dữ liệu.

    // --- Khai báo các thành phần điều hướng và hành động nổi ---
    private BottomNavigationView bottomNavigationView; // `BottomNavigationView` là thanh điều hướng dưới cùng của ứng dụng, chứa các icon để chuyển đổi giữa các màn hình chính.
    private FloatingActionButton fabAddTransaction;  // `FloatingActionButton` (FAB) là một nút hành động nổi, thường được dùng để kích hoạt hành động chính hoặc phổ biến nhất trên màn hình (ví dụ: thêm giao dịch mới).

    private BudgetDAO budgetDAO; // Đối tượng `BudgetDAO` (Data Access Object) chuyên trách các thao tác dữ liệu liên quan đến ngân sách (thêm, đọc, cập nhật, xóa). Chúng ta sẽ dùng nó để tải dữ liệu ngân sách từ DB.

    /**
     * Phương thức callback này được gọi khi Activity lần đầu tiên được tạo.
     * Đây là nơi chính để khởi tạo giao diện người dùng (UI), thiết lập các trình lắng nghe sự kiện,
     * và thực hiện các khởi tạo cần thiết khác cho Activity.
     * @param savedInstanceState Một `Bundle` chứa trạng thái Activity được lưu lại trước đó (nếu có). Điều này hữu ích để khôi phục trạng thái UI sau khi Activity bị hủy và tạo lại (ví dụ: do xoay màn hình).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Luôn gọi phương thức `onCreate` của lớp cha để thực hiện các khởi tạo cơ bản của hệ thống Android.
        setContentView(R.layout.activity_budget_overview); // Gán layout XML `activity_budget_overview.xml` làm giao diện người dùng chính cho Activity này. Hệ thống sẽ inflate (phóng to) layout này, tạo ra các đối tượng View từ XML.

        // --- Khởi tạo đối tượng `BudgetDAO` ---
        // Chúng ta khởi tạo `BudgetDAO` ở đây để có thể sử dụng nó để tải dữ liệu ngân sách
        // từ cơ sở dữ liệu và tương tác với các bản ghi ngân sách.
        budgetDAO = new BudgetDAO(this); // Truyền Context của Activity hiện tại cho DAO.

        // --- Khởi tạo và tham chiếu các thành phần UI từ layout XML ---
        // Sử dụng `findViewById()` để lấy tham chiếu đến các View con đã được định nghĩa trong `activity_budget_overview.xml`.
        btnBackBudgetOverview = findViewById(R.id.btn_back_budget_overview);
        btnCreateBudget = findViewById(R.id.btn_create_budget);

        tvRemainingSpendableAmount = findViewById(R.id.tv_remaining_spendable_amount);
        tvTotalBudgetAmount = findViewById(R.id.tv_total_budget_amount);
        tvTotalSpentAmount = findViewById(R.id.tv_total_spent_amount);
        tvDaysToEndOfMonth = findViewById(R.id.tv_days_to_end_of_month);

        // --- Thiết lập `RecyclerView` cho việc hiển thị danh sách ngân sách ---
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);
        // Thiết lập `LayoutManager` cho `RecyclerView`. `LinearLayoutManager` sẽ sắp xếp các mục theo một danh sách tuyến tính (mặc định là dọc).
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(this));
        currentBudgetsList = new ArrayList<>(); // Khởi tạo một `ArrayList` trống. Danh sách này sẽ là nguồn dữ liệu cho Adapter.
        budgetAdapter = new BudgetAdapter(currentBudgetsList); // Khởi tạo `BudgetAdapter`, truyền danh sách dữ liệu ban đầu vào.
        recyclerViewBudgets.setAdapter(budgetAdapter); // Gắn `BudgetAdapter` vào `RecyclerView` để nó biết cách hiển thị dữ liệu.

        // --- Khởi tạo `BottomNavigationView` và `FloatingActionButton` ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fab_add_transaction);

        // --- Thiết lập các trình lắng nghe sự kiện click (Click Listeners) ---

        // Lắng nghe sự kiện click cho nút quay lại ở góc trên bên trái của màn hình.
        btnBackBudgetOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi nút này được nhấn, Activity hiện tại sẽ đóng.
                finish(); // `finish()` sẽ kết thúc Activity và đưa người dùng trở lại Activity trước đó trên ngăn xếp hoạt động của ứng dụng.
            }
        });

        // Lắng nghe sự kiện click cho nút "Tạo Ngân sách" mới.
        btnCreateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một `Intent` mới để khởi động `AddBudgetActivity`.
                Intent intent = new Intent(BudgetOverviewActivity.this, AddBudgetActivity.class);
                // Sử dụng `startActivityForResult()` thay vì `startActivity()`
                // bởi vì chúng ta muốn nhận một kết quả (ví dụ: ngân sách đã được tạo thành công)
                // từ `AddBudgetActivity` khi nó kết thúc.
                startActivityForResult(intent, REQUEST_CODE_ADD_BUDGET);
            }
        });

        // --- Cấu hình `Bottom Navigation View` (Thanh điều hướng dưới cùng) ---
        // Kiểm tra để đảm bảo `bottomNavigationView` không phải là `null` trước khi cấu hình.
        if (bottomNavigationView != null) {
            // Đặt mục "Ngân sách" là mục được chọn mặc định khi Activity này được hiển thị.
            // Điều này giúp người dùng biết họ đang ở đâu trong ứng dụng.
            bottomNavigationView.setSelectedItemId(R.id.navigation_budget);

            // Thiết lập `OnItemSelectedListener` để xử lý các sự kiện khi người dùng chọn một mục trên thanh điều hướng.
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId(); // Lấy ID của mục menu đã chọn.
                if (itemId == R.id.navigation_overview) {
                    // Nếu người dùng chọn mục "Tổng quan", điều hướng đến `MainActivity`.
                    Intent overviewIntent = new Intent(BudgetOverviewActivity.this, MainActivity.class);
                    // Các cờ `FLAG_ACTIVITY_CLEAR_TOP` và `FLAG_ACTIVITY_SINGLE_TOP` được sử dụng để:
                    // - `CLEAR_TOP`: Nếu `MainActivity` đã có trên ngăn xếp hoạt động, nó sẽ được đưa lên đầu, và tất cả các Activity nằm phía trên nó sẽ bị xóa.
                    // - `SINGLE_TOP`: Nếu `MainActivity` đã ở đầu ngăn xếp, nó sẽ không được tạo lại, mà chỉ nhận một Intent mới.
                    // Điều này giúp ngăn chặn việc tạo nhiều thể hiện của cùng một Activity và duy trì ngăn xếp hoạt động sạch sẽ.
                    startActivity(overviewIntent);
                    return true; // Trả về `true` để đánh dấu rằng sự kiện đã được xử lý.
                } else if (itemId == R.id.navigation_transactions) {
                    // Nếu người dùng chọn mục "Giao dịch".
                    Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình giao dịch!", Toast.LENGTH_SHORT).show();
                    // TODO: Ở đây, bạn cần triển khai logic để điều hướng đến `TransactionsActivity` (một Activity khác để quản lý các giao dịch).
                    return true;
                } else if (itemId == R.id.navigation_budget) {
                    // Nếu người dùng chọn mục "Ngân sách".
                    return true; // Chúng ta đã ở trên màn hình này rồi, không cần thực hiện điều hướng nào thêm.
                } else if (itemId == R.id.navigation_account) {
                    // Nếu người dùng chọn mục "Tài khoản".
                    Toast.makeText(BudgetOverviewActivity.this, "Mở màn hình tài khoản!", Toast.LENGTH_SHORT).show();
                    // TODO: Ở đây, bạn cần triển khai logic để điều hướng đến `AccountActivity` (một Activity để quản lý tài khoản).
                    return true;
                }
                return false; // Trả về `false` nếu sự kiện không được xử lý bởi listener này.
            });
        }

        // --- Cấu hình `Floating Action Button` (FAB) "Thêm Giao dịch" ---
        // Kiểm tra để đảm bảo `fabAddTransaction` không phải là `null`.
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(v -> {
                Toast.makeText(BudgetOverviewActivity.this, "Thêm giao dịch mới từ màn hình Ngân sách!", Toast.LENGTH_SHORT).show();
                // TODO: Ở đây, bạn cần triển khai logic để điều hướng đến màn hình "Thêm Giao dịch".
                // Đây có thể là cùng một Activity được gọi từ `MainActivity` hoặc một `AddTransactionActivity` riêng.
            });
        }

        // Tải các ngân sách và hiển thị chúng ngay khi Activity được tạo lần đầu.
        // Điều này đảm bảo rằng giao diện người dùng được điền dữ liệu ngay lập tức.
        loadBudgetsAndDisplay();
    }

    /**
     * <p><b>`onActivityResult`: Xử lý Kết quả Trả về từ Các Activity Khác</b></p>
     * <p>Phương thức callback này được hệ thống Android gọi tự động khi một Activity
     * đã được khởi chạy bằng `startActivityForResult()` hoàn thành công việc của nó
     * và trả về một kết quả. Đây là nơi chúng ta sẽ xử lý dữ liệu hoặc thông báo
     * được trả về từ {@link AddBudgetActivity}.</p>
     *
     * @param requestCode Mã yêu cầu số nguyên ban đầu được cung cấp cho `startActivityForResult()`.
     * Đây là mã mà bạn sử dụng để xác định Activity con nào đã trả về kết quả này
     * (ví dụ: `REQUEST_CODE_ADD_BUDGET` cho `AddBudgetActivity`).
     * @param resultCode Mã kết quả số nguyên được trả về bởi Activity con.
     * Nó thường là `Activity.RESULT_OK` (thao tác thành công)
     * hoặc `Activity.RESULT_CANCELED` (thao tác bị hủy).
     * @param data Một đối tượng `Intent`, có thể mang theo dữ liệu kết quả
     * (ví dụ: ID của ngân sách mới được tạo) trở lại Activity cha.
     * Tham số này có thể là `@Nullable`, nghĩa là nó có thể là `null` nếu không có dữ liệu.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // Luôn gọi phương thức `onActivityResult` của lớp cha để đảm bảo hành vi hệ thống được duy trì.

        // Bước 1: Kiểm tra `requestCode` để xác định rằng kết quả này đến từ `AddBudgetActivity`.
        if (requestCode == REQUEST_CODE_ADD_BUDGET) {
            // Bước 2: Kiểm tra `resultCode` để biết liệu thao tác trong `AddBudgetActivity` có thành công hay không.
            if (resultCode == Activity.RESULT_OK) {
                // Nếu `resultCode` là `RESULT_OK`, nghĩa là ngân sách mới đã được tạo thành công.
                Toast.makeText(this, "Ngân sách mới đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                // Quan trọng: Tải lại danh sách ngân sách để giao diện người dùng phản ánh ngay lập tức
                // ngân sách mới vừa được thêm vào cơ sở dữ liệu.
                loadBudgetsAndDisplay();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Nếu `resultCode` là `RESULT_CANCELED`, nghĩa là người dùng đã hủy bỏ
                // thao tác trong `AddBudgetActivity` (ví dụ: nhấn nút quay lại hoặc nút "Hủy").
                Toast.makeText(this, "Thao tác tạo ngân sách đã bị hủy bỏ.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * <p><b>`loadBudgetsAndDisplay()`: Tải Dữ liệu Ngân sách và Cập nhật UI</b></p>
     * <p>Phương thức này chịu trách nhiệm chính trong việc truy xuất dữ liệu ngân sách
     * từ cơ sở dữ liệu bằng cách sử dụng {@link BudgetDAO} và sau đó cập nhật
     * toàn bộ giao diện người dùng của `BudgetOverviewActivity` để hiển thị dữ liệu này.</p>
     *
     * <p><b>Luồng Hoạt Động:</b></p>
     * <ol>
     * <li>Mở kết nối database thông qua `BudgetDAO`.</li>
     * <li>Truy vấn và lấy danh sách các đối tượng {@link Budget}.</li>
     * <li>Đóng kết nối database.</li>
     * <li>Cập nhật `RecyclerView` với dữ liệu mới.</li>
     * <li>Cập nhật các chỉ số tổng quan ở phía trên màn hình.</li>
     * </ol>
     */
    private void loadBudgetsAndDisplay() {
        // Bước 1: Mở kết nối cơ sở dữ liệu thông qua BudgetDAO.
        // Việc mở kết nối là cần thiết trước khi thực hiện bất kỳ truy vấn đọc hoặc ghi nào.
        budgetDAO.open();
        // Bước 2: Truy xuất tất cả các bản ghi ngân sách từ cơ sở dữ liệu.
        // `getAllBudgets()` trong BudgetDAO sẽ trả về một List các đối tượng Budget.
        List<Budget> loadedBudgets = budgetDAO.getAllBudgets();
        // Bước 3: Đóng kết nối cơ sở dữ liệu ngay lập tức sau khi hoàn thành việc lấy dữ liệu.
        // Điều này rất quan trọng để giải phóng tài nguyên và tránh rò rỉ bộ nhớ hoặc khóa database.
        budgetDAO.close();

        // Bước 4: Xử lý dữ liệu đã tải và cập nhật giao diện người dùng.
        if (loadedBudgets != null && !loadedBudgets.isEmpty()) {
            // Nếu danh sách `loadedBudgets` không rỗng (có dữ liệu ngân sách):
            // Cập nhật danh sách dữ liệu của Adapter.
            currentBudgetsList.clear(); // Xóa tất cả các mục hiện có khỏi danh sách hiện tại.
            currentBudgetsList.addAll(loadedBudgets); // Thêm tất cả các ngân sách vừa tải vào danh sách.
            // Thông báo cho `BudgetAdapter` rằng tập dữ liệu của nó đã thay đổi.
            // Điều này sẽ khiến `RecyclerView` tự động làm mới giao diện người dùng để hiển thị các ngân sách mới.
            budgetAdapter.setBudgetList(currentBudgetsList); // Phương thức này gọi `notifyDataSetChanged()` bên trong Adapter.

            // Cập nhật các thống kê tổng quan ở phía trên cùng của màn hình.
            // Hiện tại, để minh họa, chúng ta đang sử dụng dữ liệu từ ngân sách đầu tiên trong danh sách.
            // Trong một ứng dụng thực tế đầy đủ, bạn sẽ cần tính toán các giá trị tổng hợp
            // (ví dụ: tổng ngân sách của tất cả các danh mục, tổng số tiền đã chi tiêu qua tất cả các ngân sách).
            Budget firstBudget = currentBudgetsList.get(0); // Lấy ngân sách đầu tiên để hiển thị trong phần tổng quan.
            // Khởi tạo đối tượng `NumberFormat` để định dạng số tiền thành chuỗi tiền tệ (ví dụ: "100.000 đ").
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            currencyFormat.setMinimumFractionDigits(0); // Đảm bảo không có chữ số thập phân cho VND.
            currencyFormat.setMaximumFractionDigits(0);

            // TODO: Triển khai logic tính toán THỰC TẾ cho các giá trị này bằng cách truy vấn bảng `transactions`.
            // Đây là một phần quan trọng cần được phát triển thêm.
            // Để có các giá trị chính xác cho `tvRemainingSpendableAmount` và `tvTotalSpentAmount`, bạn sẽ cần:
            // 1. **Tính tổng số tiền đã chi tiêu:** Truy vấn bảng `transactions` để tính tổng cột `amount`
            //    cho tất cả các giao dịch:
            //    - Thuộc loại "Expense" (chi tiêu).
            //    - Nằm trong khoảng thời gian của kỳ ngân sách hiện tại (hoặc tổng hợp cho tất cả các ngân sách đang hoạt động).
            //    - Có thể lọc theo `category_id` nếu bạn muốn tổng chi cho một danh mục cụ thể.
            // 2. **Tính số tiền còn lại có thể chi:** Tổng ngân sách - Tổng đã chi tiêu.
            // 3. **Tính số ngày còn lại:** Dựa trên ngày hiện tại và ngày kết thúc của kỳ ngân sách.
            tvRemainingSpendableAmount.setText(currencyFormat.format(firstBudget.getAmount())); // Giá trị placeholder cho "số tiền còn lại có thể chi tiêu". Hiện tại bằng tổng ngân sách.
            tvTotalBudgetAmount.setText(String.format("%,.0f M", firstBudget.getAmount() / 1000000.0)); // Giá trị placeholder cho "tổng số tiền ngân sách". Chia cho 1 triệu để hiển thị dạng "M" (triệu).
            tvTotalSpentAmount.setText(String.format("%,.0f K", 50.0)); // Giá trị placeholder cho "tổng số tiền đã chi tiêu". Giả định 50.000đ.
            tvDaysToEndOfMonth.setText("3 ngày"); // Giá trị placeholder cho "số ngày còn lại đến cuối tháng".

            // Đảm bảo nút "Tạo Ngân sách" luôn hiển thị, bất kể có ngân sách nào được tìm thấy hay không.
            btnCreateBudget.setVisibility(View.VISIBLE);

        } else {
            // Nếu không có ngân sách nào được tải từ database (danh sách `loadedBudgets` rỗng):
            // Xóa danh sách hiện tại của Adapter để đảm bảo `RecyclerView` hiển thị trống.
            currentBudgetsList.clear();
            budgetAdapter.setBudgetList(currentBudgetsList);

            // Đặt các chỉ số tổng quan về 0 hoặc các giá trị mặc định để phản ánh không có ngân sách nào.
            tvRemainingSpendableAmount.setText("0.00 đ");
            tvTotalBudgetAmount.setText("0 M");
            tvTotalSpentAmount.setText("0 K");
            tvDaysToEndOfMonth.setText("0 ngày");

            // Nút "Tạo Ngân sách" vẫn phải hiển thị để người dùng có thể bắt đầu tạo ngân sách đầu tiên của họ.
            btnCreateBudget.setVisibility(View.VISIBLE);
        }
    }

    /**
     * <p><b>`onResume()`: Làm mới UI khi Activity Trở lại Foreground</b></p>
     * <p>Phương thức callback vòng đời này được gọi khi Activity sẽ bắt đầu tương tác
     * với người dùng (tức là Activity quay trở lại foreground sau khi bị tạm dừng
     * hoặc được tạo mới). Đây là một vị trí lý tưởng để làm mới giao diện người dùng
     * hoặc tải lại dữ liệu có thể đã thay đổi trong khi Activity ở trạng thái tạm dừng.</p>
     *
     * <p><b>Mục đích sử dụng:</b></p>
     * <p>Bằng cách gọi `loadBudgetsAndDisplay()` trong `onResume()`, chúng ta đảm bảo
     * rằng danh sách ngân sách và các số liệu tổng quan luôn được cập nhật
     * mỗi khi người dùng quay lại màn hình này (ví dụ: sau khi tạo một ngân sách mới
     * trong `AddBudgetActivity` và quay lại `BudgetOverviewActivity`).</p>
     */
    @Override
    protected void onResume() {
        super.onResume(); // Luôn gọi phương thức `onResume` của lớp cha.
        // Tải lại dữ liệu ngân sách và cập nhật hiển thị.
        loadBudgetsAndDisplay();
    }
}