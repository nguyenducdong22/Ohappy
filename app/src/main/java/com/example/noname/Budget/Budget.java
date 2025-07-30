package com.example.noname.Budget;

/**
 * <p><b>Budget: Lớp Mô Hình Dữ Liệu cho Một Mục Ngân sách</b></p>
 *
 * <p>Lớp {@code Budget} này đóng vai trò là một **lớp mô hình dữ liệu (Data Model)**,
 * được thiết kế để đại diện cho một mục ngân sách duy nhất trong ứng dụng.
 * Nó là một đối tượng thuần túy chứa dữ liệu (POJO - Plain Old Java Object)
 * với các thuộc tính (fields) và các phương thức getter/setter tương ứng.
 * Mục đích chính của lớp này là cấu trúc và tổ chức thông tin về một ngân sách
 * một cách rõ ràng và nhất quán.</p>
 *
 * <p><b>Các Thuộc Tính Chính của Một Ngân sách:</b></p>
 * <ul>
 * <li><b>{@code id}:</b> Định danh duy nhất của bản ghi ngân sách trong cơ sở dữ liệu.</li>
 * <li><b>{@code userId}:</b> ID của người dùng sở hữu ngân sách này (khóa ngoại).</li>
 * <li><b>{@code categoryId}:</b> ID của danh mục mà ngân sách này áp dụng (khóa ngoại).</li>
 * <li><b>{@code groupName}:</b> Tên thân thiện với người dùng của danh mục (ví dụ: "Ăn uống"),
 * được sử dụng để hiển thị trên giao diện người dùng.</li>
 * <li><b>{@code groupIconResId}:</b> ID tài nguyên của biểu tượng hình ảnh đại diện cho danh mục
 * (ví dụ: `R.drawable.ic_food`), cũng được sử dụng cho mục đích hiển thị UI.</li>
 * <li><b>{@code amount}:</b> Số tiền mà người dùng đã đặt làm ngân sách cho danh mục này.</li>
 * <li><b>{@code startDate}:</b> Ngày bắt đầu của khoảng thời gian mà ngân sách này có hiệu lực,
 * thường được lưu trữ dưới dạng chuỗi có định dạng cụ thể (ví dụ: "YYYY-MM-DD").</li>
 * <li><b>{@code endDate}:</b> Ngày kết thúc của khoảng thời gian mà ngân sách này có hiệu lực,
 * cũng được lưu trữ dưới dạng chuỗi.</li>
 * <li><b>{@code repeat}:</b> Một cờ boolean cho biết ngân sách này có được thiết lập để lặp lại
 * định kỳ (ví dụ: hàng tháng) hay không.</li>
 * </ul>
 *
 * <p><b>Tầm quan trọng của Lớp Mô hình Dữ liệu:</b></p>
 * <p>Việc sử dụng một lớp mô hình dữ liệu riêng biệt như {@code Budget} là một thực hành tốt
 * trong phát triển phần mềm (đặc biệt là trong kiến trúc Model-View-Controller hoặc MVVM).
 * Nó giúp:</p>
 * <ul>
 * <li><b>Tổ chức Dữ liệu:</b> Cung cấp một cấu trúc rõ ràng và dễ hiểu cho dữ liệu ngân sách.</li>
 * <li><b>Tái sử dụng Mã:</b> Các đối tượng {@code Budget} có thể dễ dàng được truyền
 * giữa các lớp khác nhau của ứng dụng (ví dụ: từ lớp DAO đến Adapter, hoặc giữa các Activity/Fragment).</li>
 * <li><b>Dễ bảo trì:</b> Thay đổi cấu trúc dữ liệu được tập trung trong một lớp duy nhất.</li>
 * <li><b>Khả năng kiểm thử:</b> Các đối tượng dữ liệu có thể được tạo và kiểm thử độc lập.</li>
 * </ul>
 */
public class Budget {
    // --- Các Thuộc Tính (Fields) của Đối Tượng Budget ---
    // Các biến này sẽ lưu trữ dữ liệu của một bản ghi ngân sách cụ thể.
    private long id;           // `id`: Là một số nguyên dài (long) duy nhất, đóng vai trò là khóa chính (Primary Key) của bản ghi ngân sách trong bảng 'budgets' của cơ sở dữ liệu. Nó được tự động gán bởi database khi bản ghi được thêm vào.
    private long userId;       // `userId`: Là một số nguyên dài (long) lưu trữ ID của người dùng sở hữu ngân sách này. Đây là một khóa ngoại (Foreign Key) liên kết đến bảng 'users'.
    private long categoryId;   // `categoryId`: Là một số nguyên dài (long) lưu trữ ID của danh mục mà ngân sách này áp dụng (ví dụ: ID của danh mục "Ăn uống"). Đây cũng là một khóa ngoại (Foreign Key) liên kết đến bảng 'categories'.
    private String groupName;  // `groupName`: Là một chuỗi (String) lưu trữ tên thân thiện với người dùng của danh mục (ví dụ: "Ăn uống", "Đi lại"). Thuộc tính này thường được truy xuất thông qua một phép nối (JOIN) với bảng 'categories' từ database.
    private int groupIconResId; // `groupIconResId`: Là một số nguyên (int) lưu trữ ID tài nguyên của biểu tượng hình ảnh đại diện cho danh mục (ví dụ: `R.drawable.ic_food`). ID này được sử dụng trực tiếp bởi ImageView trong UI để hiển thị biểu tượng.
    private double amount;     // `amount`: Là một số thực (double) lưu trữ số tiền mà người dùng đã đặt làm ngân sách. Kiểu `double` phù hợp cho các giá trị tiền tệ.
    private String startDate;  // `startDate`: Là một chuỗi (String) lưu trữ ngày bắt đầu của khoảng thời gian mà ngân sách này có hiệu lực (ví dụ: "2024-07-01"). Định dạng chuỗi này cần nhất quán với cách lưu trữ trong database.
    private String endDate;    // `endDate`: Là một chuỗi (String) lưu trữ ngày kết thúc của khoảng thời gian mà ngân sách này có hiệu lực (ví dụ: "2024-07-31"). Tương tự `startDate`, định dạng chuỗi cần nhất quán.
    private boolean repeat;    // `repeat`: Là một giá trị boolean, là `true` nếu ngân sách này được thiết lập để lặp lại định kỳ (ví dụ: hàng tháng), và `false` nếu nó chỉ là một ngân sách một lần.

    /**
     * <p><b>Constructor Chính: Tạo Đối Tượng Budget Từ Dữ Liệu Cơ Sở Dữ Liệu</b></p>
     *
     * <p>Constructor này là constructor chính và đầy đủ nhất cho lớp {@code Budget}.
     * Nó được sử dụng chủ yếu khi bạn **truy xuất (đọc) dữ liệu** của một bản ghi ngân sách
     * từ cơ sở dữ liệu. Tất cả các tham số của constructor này tương ứng trực tiếp
     * với các cột trong bảng 'budgets' và các thông tin liên quan được lấy từ bảng 'categories'
     * thông qua các phép nối (JOIN SQL).</p>
     *
     * <p>Bằng cách cung cấp tất cả các thuộc tính cần thiết, constructor này đảm bảo
     * rằng đối tượng {@code Budget} được tạo ra có trạng thái hoàn chỉnh và chính xác,
     * sẵn sàng để được sử dụng trong các lớp logic hoặc giao diện người dùng.</p>
     *
     * @param id               ID duy nhất của bản ghi ngân sách trong database.
     * @param userId           ID của người dùng sở hữu ngân sách này.
     * @param categoryId       ID của danh mục mà ngân sách này áp dụng.
     * @param groupName        Tên thân thiện với người dùng của danh mục (ví dụ: "Ăn uống").
     * @param groupIconResId   ID tài nguyên drawable của biểu tượng đại diện cho danh mục (ví dụ: `R.drawable.ic_food`).
     * @param amount           Số tiền mà người dùng đã đặt làm ngân sách.
     * @param startDate        Ngày bắt đầu của khoảng thời gian ngân sách, ở định dạng chuỗi (ví dụ: "2024-07-01").
     * @param endDate          Ngày kết thúc của khoảng thời gian ngân sách, ở định dạng chuỗi (ví dụ: "2024-07-31").
     * @param repeat           Giá trị boolean (`true` hoặc `false`) cho biết ngân sách có lặp lại định kỳ hay không.
     */
    public Budget(long id, long userId, long categoryId, String groupName, int groupIconResId, double amount, String startDate, String endDate, boolean repeat) {
        // Gán các giá trị từ tham số của constructor vào các thuộc tính (fields) tương ứng của đối tượng Budget.
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.repeat = repeat;
    }

    /**
     * <p><b>Constructor Phụ Trợ: Tạo Đối Tượng Budget MỚI Trước Khi Lưu Database</b></p>
     *
     * <p>Constructor này là một constructor phụ trợ, được thiết kế để sử dụng
     * khi bạn cần tạo một đối tượng {@code Budget} mới từ thông tin người dùng
     * trên giao diện (UI) **TRƯỚC KHI** đối tượng đó được lưu trữ vào cơ sở dữ liệu.
     * Trong trường hợp này, các thuộc tính như `id`, `userId`, và `categoryId`
     * không thể được biết trước, vì chúng thường được gán tự động bởi database
     * sau khi bản ghi được chèn thành công.</p>
     *
     * <p><b>Lưu ý về `dateRange`:</b></p>
     * <p>Tham số `dateRange` ở đây là một chuỗi kết hợp (ví dụ: "Tháng này (01/07 - 31/07)").
     * Để đơn giản, constructor này trực tiếp gán `dateRange` cho cả `startDate` và `endDate`.
     * Trong một ứng dụng thực tế với yêu cầu quản lý ngày tháng phức tạp hơn,
     * bạn CÓ THỂ cần triển khai một cơ chế xử lý ngày mạnh mẽ hơn
     * để phân tích chuỗi `dateRange` này thành các đối tượng `Date` hoặc chuỗi ngày
     * `startDate` và `endDate` riêng biệt và chính xác trước khi lưu vào DB.
     * Hoặc bạn có thể yêu cầu UI trực tiếp cung cấp `startDate` và `endDate` dưới dạng riêng biệt.</p>
     *
     * @param groupName        Tên của danh mục chi tiêu mà người dùng đã chọn.
     * @param groupIconResId   ID tài nguyên drawable của biểu tượng cho danh mục đã chọn.
     * @param amount           Số tiền mà người dùng đã nhập làm ngân sách.
     * @param dateRange        Một chuỗi mô tả khoảng thời gian ngân sách (ví dụ: "Tháng này").
     * @param repeat           Giá trị boolean (`true` hoặc `false`) từ công tắc lặp lại.
     */
    public Budget(String groupName, int groupIconResId, double amount, String dateRange, boolean repeat) {
        // Gán các giá trị từ tham số của constructor vào các thuộc tính (fields) tương ứng của đối tượng Budget.
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        // Gán trực tiếp chuỗi `dateRange` cho cả `startDate` và `endDate`.
        // Đây là một cách tiếp cận đơn giản cho demo.
        this.startDate = dateRange;
        this.endDate = dateRange;
        this.repeat = repeat;
        // Các thuộc tính `id`, `userId`, `categoryId` được khởi tạo với giá trị mặc định là -1.
        // Điều này là bởi vì các giá trị thực tế của chúng chỉ có thể được gán
        // sau khi bản ghi ngân sách được lưu thành công vào cơ sở dữ liệu và database trả về các ID này.
        this.id = -1;
        this.userId = -1;
        this.categoryId = -1;
    }

    // --- Các Phương Thức Getter: Cung cấp quyền truy cập CÔNG KHAI vào các thuộc tính của đối tượng Budget ---
    // Các phương thức này cho phép các lớp khác đọc giá trị của các thuộc tính private của Budget.
    public long getId() { return id; } // Trả về ID duy nhất của ngân sách.
    public long getUserId() { return userId; } // Trả về ID của người dùng sở hữu ngân sách.
    public long getCategoryId() { return categoryId; } // Trả về ID của danh mục liên quan đến ngân sách.
    public String getGroupName() { return groupName; } // Trả về tên hiển thị của danh mục.
    public int getGroupIconResId() { return groupIconResId; } // Trả về ID tài nguyên của biểu tượng danh mục.
    public double getAmount() { return amount; } // Trả về số tiền đã đặt ngân sách.
    public String getStartDate() { return startDate; } // Trả về ngày bắt đầu của ngân sách dưới dạng chuỗi.
    public String getEndDate() { return endDate; } // Trả về ngày kết thúc của ngân sách dưới dạng chuỗi.
    public boolean isRepeat() { return repeat; } // Trả về `true` nếu ngân sách lặp lại, `false` nếu không.

    // --- Các Phương Thức Setter: Cho phép sửa đổi các thuộc tính sau khi đối tượng được tạo ---
    // Các phương thức này cho phép các lớp khác cập nhật giá trị của các thuộc tính private của Budget.
    // Việc sử dụng các setter cần được cân nhắc cẩn thận, thường chỉ khi bạn cần cập nhật dữ liệu
    // của đối tượng trong bộ nhớ trước khi lưu lại vào cơ sở dữ liệu, hoặc khi bạn xây dựng
    // đối tượng từng bước.
    public void setId(long id) { this.id = id; } // Đặt ID duy nhất cho ngân sách.
    public void setUserId(long userId) { this.userId = userId; } // Đặt ID người dùng cho ngân sách.
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; } // Đặt ID danh mục cho ngân sách.
    public void setGroupName(String groupName) { this.groupName = groupName; } // Đặt tên nhóm cho ngân sách.
    public void setGroupIconResId(int groupIconResId) { this.groupIconResId = groupIconResId; } // Đặt ID tài nguyên biểu tượng cho ngân sách.
    public void setAmount(double amount) { this.amount = amount; } // Đặt số tiền ngân sách.
    public void setStartDate(String startDate) { this.startDate = startDate; } // Đặt ngày bắt đầu cho ngân sách.
    public void setEndDate(String endDate) { this.endDate = endDate; } // Đặt ngày kết thúc cho ngân sách.
    public void setRepeat(boolean repeat) { this.repeat = repeat; } // Đặt trạng thái lặp lại cho ngân sách.

    /**
     * <p><b>`getDateRange()`: Phương Thức Tiện Ích để Hiển Thị Khoảng Ngày</b></p>
     * <p>Phương thức tiện ích này được cung cấp để kết hợp ngày bắt đầu (`startDate`)
     * và ngày kết thúc (`endDate`) của ngân sách thành một chuỗi văn bản duy nhất,
     * dễ đọc và phù hợp để hiển thị trực tiếp trên giao diện người dùng (UI).</p>
     *
     * <p><b>Mục đích:</b></p>
     * <p>Thay vì phải gọi riêng `getStartDate()` và `getEndDate()` rồi nối chuỗi ở UI,
     * phương thức này cung cấp một cách thuận tiện để lấy chuỗi khoảng ngày đã định dạng.
     * Đây là một ví dụ về cách một lớp mô hình có thể cung cấp các phương thức tiện ích
     * để định dạng dữ liệu cho mục đích hiển thị mà không cần phải biết quá nhiều
     * về logic hiển thị cụ thể của UI.</p>
     *
     * @return Một chuỗi đã định dạng biểu thị khoảng thời gian ngân sách
     * (ví dụ: "2024-07-01 - 2024-07-31").
     */
    public String getDateRange() {
        return startDate + " - " + endDate; // Nối chuỗi ngày bắt đầu, " - ", và ngày kết thúc.
    }
}