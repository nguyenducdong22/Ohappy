package com.example.noname.Budget;

import android.app.Activity; // Import lớp Activity, cần thiết để sử dụng các hằng số kết quả (như Activity.RESULT_OK, Activity.RESULT_CANCELED) khi Activity này kết thúc và trả về kết quả cho Activity đã gọi nó.
import android.content.Intent; // Import lớp Intent, một đối tượng dùng để thực hiện các thao tác "có ý định", ví dụ như khởi động một Activity khác (ChooseGroupActivity) hoặc mang dữ liệu giữa các Activity.
import android.os.Bundle;     // Import lớp Bundle, được sử dụng để lưu trữ và khôi phục trạng thái của Activity. Các dữ liệu cần được bảo toàn khi Activity bị hủy và tạo lại (ví dụ: do xoay màn hình) thường được lưu trong Bundle này.
import android.view.View;      // Lớp cơ sở cho mọi thành phần giao diện người dùng (UI) trong Android. Mọi thứ bạn thấy trên màn hình (nút, văn bản, hình ảnh) đều là một loại View.
import android.widget.Button;   // Một loại View tương tác, cho phép người dùng kích hoạt một hành động khi nhấn vào nó.
import android.widget.LinearLayout; // Một loại ViewGroup (layout) tổ chức các View con theo một hàng (ngang hoặc dọc). Ở đây dùng để nhóm các thành phần UI liên quan lại với nhau.
import android.widget.TextView;    // Một loại View đơn giản dùng để hiển thị văn bản không chỉnh sửa được trên màn hình.
import android.widget.ImageView;   // Một loại View dùng để hiển thị hình ảnh (ví dụ: biểu tượng danh mục).
import android.widget.EditText;    // Một loại View cho phép người dùng nhập và chỉnh sửa văn bản. Thường dùng cho các trường nhập liệu số tiền, ghi chú.
import android.widget.Toast;     // Một đối tượng nhỏ, tạm thời, hiển thị một thông báo phản hồi nhanh chóng cho người dùng, biến mất sau một khoảng thời gian ngắn.

import androidx.annotation.Nullable; // Annotation (chú thích) này của AndroidX cho biết rằng một tham số hoặc một giá trị trả về của phương thức có thể là null. Điều này giúp tăng cường kiểm tra mã và tránh các lỗi NullPointerException.
import androidx.appcompat.app.AppCompatActivity; // Lớp cơ sở cho các Activity trong thư viện AndroidX, cung cấp khả năng tương thích ngược cho các tính năng mới của Android trên các phiên bản Android cũ hơn.
import androidx.appcompat.widget.Toolbar;        // Một thành phần thanh công cụ linh hoạt, thường được sử dụng thay thế cho ActionBar mặc định, cho phép tùy chỉnh cao hơn về tiêu đề, biểu tượng và các hành động.
import com.google.android.material.switchmaterial.SwitchMaterial; // Một loại công tắc bật/tắt tuân thủ Material Design của Google, cung cấp giao diện đẹp và tương tác mượt mà.

import com.example.noname.R;                  // Lớp `R` được Android tự động tạo ra và chứa các ID tĩnh cho tất cả các tài nguyên trong dự án (ví dụ: layout file `activity_add_budget`, ID của các View như `et_amount`, ID của các biểu tượng drawable như `ic_circle`).
import com.example.noname.database.DatabaseHelper; // Import lớp `DatabaseHelper`, được sử dụng để tương tác với cơ sở dữ liệu ở cấp độ schema và để lấy ID của danh mục dựa trên tên của nó.
import com.example.noname.database.BudgetDAO;     // Import lớp `BudgetDAO` (Data Access Object), một lớp mới được tạo ra để chuyên xử lý các thao tác dữ liệu (CRUD) cho các bản ghi ngân sách trong cơ sở dữ liệu. Nó giúp tách biệt logic database khỏi UI.

import java.text.SimpleDateFormat; // Lớp này là một phần của Java's standard library, được dùng để định dạng các đối tượng `Date` thành chuỗi `String` và ngược lại. Điều này cần thiết khi lưu trữ hoặc hiển thị ngày tháng trong cơ sở dữ liệu và giao diện người dùng.
import java.util.Date;             // Đại diện cho một thời điểm cụ thể trong thời gian, với độ chính xác đến mili giây. Ở đây, được dùng để lấy ngày hiện tại làm placeholder cho ngày bắt đầu/kết thúc ngân sách.
import java.util.Locale;           // Đối tượng `Locale` đại diện cho một khu vực địa lý hoặc văn hóa cụ thể. Khi định dạng ngày tháng, việc chỉ định Locale (ví dụ: `Locale.getDefault()`) đảm bảo rằng định dạng tuân thủ các quy ước ngôn ngữ/khu vực của thiết bị.

/**
 * <p><b>AddBudgetActivity: Giao diện và Logic để Tạo Ngân sách Mới</b></p>
 *
 * <p>Activity này đóng vai trò là giao diện người dùng chính (UI) và chứa logic xử lý
 * cho việc tạo một bản ghi ngân sách mới trong ứng dụng quản lý chi tiêu.
 * Mục tiêu của Activity này là thu thập tất cả thông tin cần thiết từ người dùng
 * để định nghĩa một ngân sách cụ thể, bao gồm:</p>
 * <ul>
 * <li><b>Số tiền Ngân sách:</b> Số tiền tối đa mà người dùng muốn chi tiêu.</li>
 * <li><b>Chọn Danh mục (Nhóm Chi tiêu):</b> Liên kết ngân sách với một loại chi tiêu cụ thể
 * (ví dụ: "Ăn uống", "Đi lại").</li>
 * <li><b>Khoảng Thời Gian:</b> Xác định ngày bắt đầu và ngày kết thúc mà ngân sách này có hiệu lực.</li>
 * <li><b>Tính năng Lặp lại:</b> Cho phép người dùng thiết lập ngân sách này có tự động
 * lặp lại định kỳ hay không (ví dụ: mỗi tháng).</li>
 * </ul>
 *
 * <p>Sau khi người dùng đã nhập và xác nhận tất cả thông tin, Activity này sẽ tương tác
 * với lớp {@link BudgetDAO} để lưu dữ liệu ngân sách đã được định nghĩa vào cơ sở dữ liệu
 * SQLite của ứng dụng. Việc sử dụng DAO giúp tách biệt logic giao diện người dùng
 * khỏi logic thao tác dữ liệu, làm cho mã sạch hơn và dễ bảo trì hơn.</p>
 */
public class AddBudgetActivity extends AppCompatActivity {

    // Mã yêu cầu (request code) duy nhất được sử dụng khi khởi động ChooseGroupActivity.
    // Khi ChooseGroupActivity hoàn thành và trả về kết quả, request code này sẽ giúp
    // phương thức `onActivityResult` của Activity này xác định được kết quả đó đến từ đâu.
    private static final int REQUEST_CODE_SELECT_GROUP = 1;

    // --- Khai báo các thành phần giao diện người dùng (UI elements) của Activity ---
    // Các biến này sẽ được liên kết với các View tương ứng trong layout XML thông qua findViewById().
    private TextView tvGroupName;        // TextView dùng để hiển thị tên của nhóm/danh mục chi tiêu mà người dùng đã chọn (ví dụ: "Ăn uống").
    private ImageView ivGroupIcon;       // ImageView dùng để hiển thị biểu tượng hình ảnh đại diện cho nhóm/danh mục đã chọn.
    private EditText etAmount;           // EditText cho phép người dùng nhập vào số tiền mà họ muốn đặt làm ngân sách.
    private TextView tvDateRange;        // TextView dùng để hiển thị khoảng thời gian mà ngân sách sẽ áp dụng (ví dụ: "Tháng này (01/07 - 31/07)").
    private SwitchMaterial switchRepeatBudget; // Một công tắc bật/tắt (toggle switch) cho phép người dùng quyết định xem ngân sách này có nên được lặp lại định kỳ hay không.

    // --- Khai báo các biến để lưu trữ dữ liệu tạm thời cho ngân sách mới ---
    // Các biến này giữ trạng thái của dữ liệu mà người dùng nhập hoặc chọn trước khi lưu vào database.
    private String selectedGroupName = "Chọn nhóm"; // Lưu trữ tên của nhóm/danh mục mà người dùng đã chọn. Đây là giá trị mặc định hiển thị ban đầu.
    private int selectedGroupIconResId = R.drawable.ic_circle; // Lưu trữ ID tài nguyên drawable của biểu tượng cho nhóm/danh mục đã chọn. `R.drawable.ic_circle` là một biểu tượng mặc định nếu chưa có lựa chọn nào.
    private String selectedDateRange = "Tháng này (01/07 - 31/07)"; // Lưu trữ chuỗi hiển thị của khoảng ngày mà ngân sách áp dụng. Đây là một placeholder và cần được cập nhật khi tính năng chọn ngày được triển khai đầy đủ.
    private long selectedCategoryId = -1; // Lưu trữ ID duy nhất của danh mục đã chọn từ cơ sở dữ liệu. Giá trị -1 biểu thị rằng chưa có danh mục nào hợp lệ được chọn hoặc tìm thấy.

    // --- Khai báo các đối tượng Data Access Object (DAO) ---
    // Các đối tượng này chịu trách nhiệm tương tác với cơ sở dữ liệu.
    private DatabaseHelper dbHelper; // Thể hiện của DatabaseHelper. Được sử dụng ở đây để tra cứu ID của một Category (danh mục) dựa trên tên của nó. DatabaseHelper quản lý việc tạo và nâng cấp schema DB.
    private BudgetDAO budgetDAO;     // Thể hiện của BudgetDAO. Đây là lớp chuyên biệt chịu trách nhiệm lưu trữ (INSERT) bản ghi ngân sách mới vào database và các thao tác CRUD khác liên quan đến Budget.

    /**
     * Phương thức callback này được gọi khi Activity lần đầu tiên được tạo.
     * Đây là nơi chính để khởi tạo giao diện người dùng và thiết lập các trình lắng nghe sự kiện.
     * @param savedInstanceState Một Bundle chứa trạng thái Activity được lưu lại trước đó (nếu có).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Luôn gọi phương thức onCreate của lớp cha để thực hiện các khởi tạo cơ bản của hệ thống.
        setContentView(R.layout.activity_add_budget); // Gắn layout XML `activity_add_budget.xml` làm giao diện người dùng cho Activity này. Hệ thống sẽ inflate layout này, tạo ra các đối tượng View từ XML.

        // --- Khởi tạo các đối tượng DAO (Data Access Object) ---
        // Các DAO này sẽ được sử dụng để tương tác với cơ sở dữ liệu SQLite của ứng dụng.
        dbHelper = new DatabaseHelper(this); // Khởi tạo DatabaseHelper, truyền Context của Activity hiện tại. dbHelper sẽ giúp lấy ID danh mục.
        budgetDAO = new BudgetDAO(this);     // Khởi tạo BudgetDAO, truyền Context. budgetDAO sẽ xử lý việc thêm dữ liệu ngân sách vào DB.

        // --- Thiết lập Toolbar (thanh công cụ) ---
        Toolbar toolbar = findViewById(R.id.toolbar_add_budget); // Tìm Toolbar trong layout bằng ID của nó.
        setSupportActionBar(toolbar); // Đặt Toolbar này làm Action Bar của Activity. Điều này cho phép Toolbar hoạt động như một thanh tiêu đề và có thể hiển thị các menu hoặc nút điều hướng.
        // Mặc định, AddBudgetActivity này không có nút quay lại trên Toolbar vì nó được mở từ BudgetOverviewActivity
        // và nút "Hủy" hoặc nút back của hệ thống sẽ được sử dụng để quay lại.

        // --- Khởi tạo và tham chiếu các thành phần UI từ layout XML ---
        // Sử dụng findViewById() để lấy tham chiếu đến các View con đã được định nghĩa trong `activity_add_budget.xml`.
        tvGroupName = findViewById(R.id.tv_group_name);
        ivGroupIcon = findViewById(R.id.iv_group_icon);
        etAmount = findViewById(R.id.et_amount);
        tvDateRange = findViewById(R.id.tv_date_range);
        switchRepeatBudget = findViewById(R.id.switch_repeat_budget);

        // --- Đặt trạng thái hiển thị ban đầu cho các thành phần UI ---
        // Gán các giá trị mặc định hoặc placeholder ban đầu cho các View để người dùng thấy.
        tvGroupName.setText(selectedGroupName); // Hiển thị "Chọn nhóm" ban đầu.
        ivGroupIcon.setImageResource(selectedGroupIconResId); // Hiển thị biểu tượng mặc định.
        tvDateRange.setText(selectedDateRange); // Hiển thị chuỗi ngày tháng mặc định.

        // --- Thiết lập lắng nghe sự kiện click cho nút "Hủy" ---
        // Nút này cho phép người dùng hủy bỏ quá trình tạo ngân sách mà không lưu bất kỳ thay đổi nào.
        TextView btnCancel = findViewById(R.id.btn_cancel_add_budget);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi nút "Hủy" được nhấn, chúng ta đặt kết quả của Activity là HỦY BỎ.
                // Điều này thông báo cho Activity gọi (BudgetOverviewActivity) rằng
                // thao tác tạo ngân sách đã không được hoàn thành hoặc bị người dùng từ chối.
                setResult(Activity.RESULT_CANCELED);
                finish(); // Gọi `finish()` để đóng Activity hiện tại và quay lại Activity trước đó trên ngăn xếp hoạt động.
            }
        });

        // --- Thiết lập lắng nghe sự kiện click cho layout "Chọn nhóm" (chọn danh mục chi tiêu) ---
        // Khi người dùng nhấn vào khu vực này, họ sẽ được đưa đến một màn hình khác để chọn danh mục.
        LinearLayout layoutChooseGroup = findViewById(R.id.layout_choose_group);
        layoutChooseGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một Intent mới để khởi động `ChooseGroupActivity`.
                // `AddBudgetActivity.this` là Context của Activity hiện tại.
                Intent intent = new Intent(AddBudgetActivity.this, ChooseGroupActivity.class);
                // Truyền dữ liệu hiện tại (nếu có) vào Intent. Điều này có thể hữu ích
                // để `ChooseGroupActivity` biết được lựa chọn hiện tại của người dùng
                // hoặc để khôi phục trạng thái.
                intent.putExtra("current_selected_group_name", selectedGroupName);
                intent.putExtra("current_selected_group_icon_res_id", selectedGroupIconResId);
                // Bắt đầu `ChooseGroupActivity` và chờ đợi kết quả trả về.
                // `REQUEST_CODE_SELECT_GROUP` là một mã duy nhất sẽ được sử dụng trong `onActivityResult()`
                // để xác định rằng kết quả trả về này đến từ `ChooseGroupActivity`.
                startActivityForResult(intent, REQUEST_CODE_SELECT_GROUP);
            }
        });

        // --- Thiết lập lắng nghe sự kiện click cho layout "Khoảng thời gian" (Hiện đang là Placeholder cho DatePicker) ---
        // Khu vực này sẽ cho phép người dùng chọn ngày bắt đầu và kết thúc cho ngân sách.
        LinearLayout layoutDateRangeClick = findViewById(R.id.layout_date_range);
        layoutDateRangeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiện tại, chỉ hiển thị một Toast thông báo rằng tính năng này chưa được triển khai.
                Toast.makeText(AddBudgetActivity.this, "Mở Date Picker (chưa triển khai)", Toast.LENGTH_SHORT).show();
                // TODO: Trong một bản phát triển đầy đủ, bạn cần triển khai logic tại đây.
                // Điều này có thể bao gồm:
                // a) Hiển thị một `DatePickerDialog` hoặc một dải chọn ngày tùy chỉnh.
                // b) Cho phép người dùng chọn một hoặc hai ngày để xác định khoảng thời gian của ngân sách.
                // c) Sau khi chọn xong, cập nhật các biến `startDate` và `endDate` của Activity
                //    cũng như cập nhật chuỗi hiển thị trên `tvDateRange`.
            }
        });

        // --- Thiết lập lắng nghe sự kiện click cho layout "Tổng cộng" (Nếu có logic đặc biệt liên quan đến tổng ngân sách) ---
        // Layout này có thể được dùng cho các cài đặt nâng cao hơn về tổng số tiền.
        LinearLayout layoutTotal = findViewById(R.id.layout_total);
        if (layoutTotal != null) { // Luôn kiểm tra null nếu layout có thể không luôn tồn tại trong mọi cấu hình layout.
            layoutTotal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Hiện tại, chỉ hiển thị một Toast thông báo rằng tính năng này chưa được triển khai.
                    Toast.makeText(AddBudgetActivity.this, "Mở thiết lập Tổng cộng (chưa triển khai)", Toast.LENGTH_SHORT).show();
                    // TODO: Bạn có thể sử dụng khu vực này để:
                    // a) Mở một hộp thoại hoặc Activity mới để người dùng thiết lập các mục tiêu tổng thể
                    //    hoặc các cài đặt nâng cao cho ngân sách.
                    // b) Hiển thị một phân tích chi tiết về tổng số tiền.
                }
            });
        }

        // --- Thiết lập lắng nghe sự kiện click cho nút "Lưu" ngân sách ---
        // Nút này là điểm kích hoạt cuối cùng để xác nhận và lưu ngân sách vào database.
        Button btnSaveBudget = findViewById(R.id.btn_save_budget);
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy chuỗi số tiền từ EditText và loại bỏ khoảng trắng thừa.
                String amountString = etAmount.getText().toString().trim();
                double amount = 0.0; // Khởi tạo số tiền là 0.0.

                // --- Bước 1: Kiểm tra hợp lệ dữ liệu đầu vào (Input Validation) ---
                // Đây là một bước quan trọng để đảm bảo dữ liệu hợp lệ trước khi lưu vào database.

                // 1.1. Kiểm tra trường số tiền: Đảm bảo không trống và không phải là "0".
                if (amountString.isEmpty() || amountString.equals("0")) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                    return; // Dừng thực thi phương thức nếu dữ liệu không hợp lệ.
                }
                try {
                    // 1.2. Cố gắng chuyển đổi chuỗi số tiền thành kiểu `double`.
                    amount = Double.parseDouble(amountString);
                } catch (NumberFormatException e) {
                    // 1.3. Bắt lỗi nếu chuỗi không thể chuyển đổi thành số hợp lệ.
                    Toast.makeText(AddBudgetActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return; // Dừng thực thi.
                }

                // 2. Kiểm tra danh mục đã được chọn: Đảm bảo người dùng đã chọn một nhóm hợp lệ.
                // selectedGroupName vẫn là "Chọn nhóm" hoặc selectedCategoryId vẫn là -1
                // nếu người dùng chưa chọn hoặc có lỗi khi lấy ID.
                if (selectedGroupName.equals("Chọn nhóm") || selectedCategoryId == -1) {
                    Toast.makeText(AddBudgetActivity.this, "Vui lòng chọn một nhóm hợp lệ", Toast.LENGTH_SHORT).show();
                    return; // Dừng thực thi.
                }

                // --- Bước 2: Chuẩn bị dữ liệu để lưu vào Database ---

                // Giả định một `userId` cố định cho mục đích demo (ví dụ: `userId = 1`).
                // Trong một ứng dụng thực tế (có chức năng đăng nhập/đăng ký),
                // `userId` này sẽ được lấy từ phiên đăng nhập của người dùng hiện tại.
                // Nó thường được lưu trữ trong `SharedPreferences` hoặc một lớp `SessionManager` sau khi người dùng đăng nhập.
                long userId = 1;
                boolean repeatBudget = switchRepeatBudget.isChecked(); // Lấy trạng thái của công tắc "Lặp lại ngân sách". `true` nếu bật, `false` nếu tắt.

                // Các biến `startDate` và `endDate` sẽ lưu trữ ngày bắt đầu và kết thúc của ngân sách
                // ở định dạng chuỗi "yyyy-MM-dd" để tương thích với cơ sở dữ liệu SQLite.
                // Hiện tại, chúng được đặt là ngày hiện tại làm placeholder.
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String startDate = dateFormat.format(new Date()); // Lấy ngày hiện tại và định dạng nó.
                String endDate = dateFormat.format(new Date());   // Để đơn giản, nếu không có khoảng ngày được chọn, coi như ngân sách cho một ngày duy nhất.
                // TODO: Khi tính năng DatePicker được triển khai, logic ở đây sẽ được thay thế
                // để lấy `startDate` và `endDate` thực tế từ lựa chọn của người dùng.

                // --- Bước 3: Lưu ngân sách vào cơ sở dữ liệu bằng BudgetDAO ---
                // Đây là phần quan trọng nhất, nơi dữ liệu được chuyển từ UI sang lớp DAO để thao tác với DB.
                // Luôn luôn mở kết nối database trước khi thực hiện bất kỳ thao tác đọc/ghi nào
                // và đóng nó ngay sau khi hoàn tất để giải phóng tài nguyên.
                budgetDAO.open(); // Mở kết nối cơ sở dữ liệu ở chế độ ghi.
                // Gọi phương thức `addBudget()` của `BudgetDAO` để chèn bản ghi ngân sách mới.
                // Phương thức này trả về `true` nếu thành công, `false` nếu có lỗi.
                boolean success = budgetDAO.addBudget(userId, selectedCategoryId, amount, startDate, endDate, repeatBudget);
                budgetDAO.close(); // Đóng kết nối cơ sở dữ liệu sau khi thao tác hoàn tất.

                // --- Bước 4: Xử lý kết quả lưu trữ và phản hồi cho người dùng ---
                if (success) {
                    // Nếu `addBudget()` trả về `true`, nghĩa là ngân sách đã được lưu thành công.
                    Toast.makeText(AddBudgetActivity.this, "Đã lưu ngân sách cho: " + selectedGroupName, Toast.LENGTH_LONG).show();
                    // Đặt kết quả của Activity hiện tại là `RESULT_OK`.
                    // Điều này thông báo cho Activity đã gọi (`BudgetOverviewActivity`) rằng
                    // một ngân sách mới đã được tạo thành công và nó có thể cần làm mới giao diện người dùng.
                    setResult(Activity.RESULT_OK);
                    finish(); // Gọi `finish()` để đóng Activity `AddBudgetActivity` và quay lại `BudgetOverviewActivity`.
                } else {
                    // Nếu `addBudget()` trả về `false`, nghĩa là có lỗi xảy ra trong quá trình lưu.
                    Toast.makeText(AddBudgetActivity.this, "Lỗi khi lưu ngân sách. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    // Không gọi `setResult(RESULT_OK)` ở đây, để Activity gọi biết rằng thao tác không thành công.
                }
            }
        });
    }

    /**
     * <p><b>{@code onActivityResult}: Xử lý Kết quả từ Các Activity Khác</b></p>
     * <p>Phương thức callback này được hệ thống Android gọi tự động khi một Activity
     * đã được bắt đầu bằng `startActivityForResult()` hoàn thành và trả về một kết quả.
     * Đây là nơi chúng ta sẽ xử lý dữ liệu được trả về từ {@link ChooseGroupActivity}.</p>
     *
     * @param requestCode Mã yêu cầu số nguyên ban đầu được cung cấp cho `startActivityForResult()`.
     * Mã này giúp bạn xác định Activity con nào đã trả về kết quả này
     * (ví dụ: `REQUEST_CODE_SELECT_GROUP` để biết kết quả đến từ `ChooseGroupActivity`).
     * @param resultCode Mã kết quả số nguyên được trả về bởi Activity con.
     * Thường là `Activity.RESULT_OK` (thao tác thành công)
     * hoặc `Activity.RESULT_CANCELED` (thao tác bị hủy).
     * @param data Một đối tượng `Intent`, có thể mang theo dữ liệu kết quả
     * (ví dụ: tên nhóm đã chọn, ID biểu tượng) trở lại Activity cha.
     * Tham số này có thể là `@Nullable`, nghĩa là nó có thể là `null`.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // Luôn gọi phương thức `onActivityResult` của lớp cha để đảm bảo hành vi hệ thống được duy trì.

        // Bước 1: Kiểm tra `requestCode` để xác định Activity con nào đã trả về kết quả.
        if (requestCode == REQUEST_CODE_SELECT_GROUP) {
            // Bước 2: Kiểm tra `resultCode` để biết kết quả từ Activity con có thành công hay không.
            // Đồng thời, đảm bảo `data` Intent không phải là `null` (chứa dữ liệu).
            if (resultCode == Activity.RESULT_OK && data != null) {
                // 2.1. Lấy dữ liệu được trả về từ `ChooseGroupActivity`.
                // `selected_group_name` là key dùng để lưu tên nhóm trong Intent.
                selectedGroupName = data.getStringExtra("selected_group_name");
                // `selected_group_icon` là key dùng để lưu ID tài nguyên của biểu tượng.
                // Tham số thứ hai là giá trị mặc định nếu key không tồn tại.
                selectedGroupIconResId = data.getIntExtra("selected_group_icon", R.drawable.ic_circle);

                // 2.2. Quan trọng: Lấy ID của danh mục từ cơ sở dữ liệu.
                // Chúng ta cần ID này để lưu ngân sách vào bảng `budgets` vì `category_id` là một khóa ngoại.
                // Giả định rằng tất cả các danh mục ngân sách là loại "Expense" (chi tiêu).
                // Nếu ứng dụng có thể có ngân sách cho cả "Income" (thu nhập),
                // bạn cần thêm logic để xác định loại danh mục ở đây.
                selectedCategoryId = dbHelper.getCategoryId(selectedGroupName, "Expense");
                if (selectedCategoryId == -1) {
                    // Xử lý trường hợp không tìm thấy ID danh mục.
                    // Điều này có thể xảy ra nếu danh mục được chọn từ `ChooseGroupActivity`
                    // không tồn tại trong bảng `categories` của cơ sở dữ liệu (ví dụ: lỗi dữ liệu).
                    Toast.makeText(this, "Lỗi: Không tìm thấy ID danh mục cho '" + selectedGroupName + "'. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    // Trong trường hợp này, bạn có thể cân nhắc vô hiệu hóa nút lưu
                    // hoặc đưa người dùng trở lại màn hình chọn nhóm.
                }

                // 2.3. Cập nhật giao diện người dùng của `AddBudgetActivity`
                // với thông tin nhóm/danh mục đã được chọn.
                tvGroupName.setText(selectedGroupName); // Đặt tên nhóm vào TextView.
                ivGroupIcon.setImageResource(selectedGroupIconResId); // Đặt biểu tượng vào ImageView.

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Nếu `resultCode` là `RESULT_CANCELED`, nghĩa là người dùng đã hủy bỏ
                // thao tác trong `ChooseGroupActivity` (ví dụ: nhấn nút quay lại mà không chọn gì).
                Toast.makeText(this, "Chọn nhóm đã bị hủy.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}