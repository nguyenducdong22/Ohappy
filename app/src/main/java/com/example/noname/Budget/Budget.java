package com.example.noname.Budget;

import java.io.Serializable; // Quan trọng: Import Serializable

/**
 * Budget is a data model class representing a single budget entry.
 * It holds all the attributes of a budget, including its ID, associated user and category,
 * amount, date range, and recurrence status.
 *
 * Implements Serializable to allow passing Budget objects between Activities via Intent.
 */
public class Budget implements Serializable { // Thêm 'implements Serializable' ở đây
    // --- Các Thuộc Tính (Fields) của Đối Tượng Budget ---
    private long id;
    private long userId;
    private long categoryId;
    private String groupName;
    private int groupIconResId;
    private double amount;
    private String startDate;
    private String endDate;
    private boolean repeat;

    /**
     * Constructor chính: Tạo Đối Tượng Budget từ dữ liệu truy xuất từ Cơ sở dữ liệu.
     * Hoặc dùng để cập nhật một đối tượng Budget hiện có trong bộ nhớ.
     * @param id ID duy nhất của bản ghi ngân sách trong database.
     * @param userId ID của người dùng sở hữu ngân sách này.
     * @param categoryId ID của danh mục mà ngân sách này áp dụng.
     * @param groupName Tên thân thiện với người dùng của danh mục (ví dụ: "Ăn uống").
     * @param groupIconResId ID tài nguyên drawable của biểu tượng đại diện cho danh mục.
     * @param amount Số tiền mà người dùng đã đặt làm ngân sách.
     * @param startDate Ngày bắt đầu của khoảng thời gian ngân sách, ở định dạng chuỗi (ví dụ: "2024-07-01").
     * @param endDate Ngày kết thúc của khoảng thời gian ngân sách, ở định dạng chuỗi (ví dụ: "2024-07-31").
     * @param repeat Giá trị boolean (`true` hoặc `false`) cho biết ngân sách có lặp lại định kỳ hay không.
     */
    public Budget(long id, long userId, long categoryId, String groupName, int groupIconResId, double amount, String startDate, String endDate, boolean repeat) {
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
     * Constructor phụ trợ: Tạo một đối tượng Budget MỚI *trước khi* nó được lưu vào cơ sở dữ liệu.
     * Thường dùng khi người dùng nhập dữ liệu mới từ UI.
     * @param groupName Tên của danh mục chi tiêu mà người dùng đã chọn.
     * @param groupIconResId ID tài nguyên drawable của biểu tượng cho danh mục đã chọn.
     * @param amount Số tiền mà người dùng đã nhập làm ngân sách.
     * @param dateRange Một chuỗi mô tả khoảng thời gian ngân sách (ví dụ: "Tháng này (01/07 - 31/07)").
     * Lưu ý: Đối với demo, chuỗi này được gán trực tiếp vào startDate và endDate.
     * Trong ứng dụng thực, cần parse thành ngày tháng chuẩn.
     * @param repeat Giá trị boolean (`true` hoặc `false`) từ công tắc lặp lại.
     */
    public Budget(String groupName, int groupIconResId, double amount, String dateRange, boolean repeat) {
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        this.startDate = dateRange; // Placeholder: Trong ứng dụng thực cần parse từ dateRange string.
        this.endDate = dateRange;   // Placeholder: Trong ứng dụng thực cần parse từ dateRange string.
        this.repeat = repeat;
        // Các ID này sẽ được gán bởi database sau khi chèn thành công.
        this.id = -1;
        this.userId = -1;
        this.categoryId = -1;
    }

    // --- Các Phương Thức Getter: Cung cấp quyền truy cập công khai vào các thuộc tính của đối tượng Budget ---
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getCategoryId() { return categoryId; }
    public String getGroupName() { return groupName; }
    public int getGroupIconResId() { return groupIconResId; }
    public double getAmount() { return amount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isRepeat() { return repeat; }

    // --- Các Phương Thức Setter: Cho phép sửa đổi các thuộc tính sau khi đối tượng được tạo ---
    // Hữu ích khi cập nhật dữ liệu của đối tượng trong bộ nhớ trước khi lưu lại vào DB.
    public void setId(long id) { this.id = id; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setGroupIconResId(int groupIconResId) { this.groupIconResId = groupIconResId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setRepeat(boolean repeat) { this.repeat = repeat; }

    /**
     * Phương thức tiện ích để kết hợp ngày bắt đầu và ngày kết thúc thành một chuỗi hiển thị duy nhất.
     * @return Một chuỗi đã định dạng cho khoảng ngày (e.g., "2024-07-01 - 2024-07-31").
     */
    public String getDateRange() {
        return startDate + " - " + endDate;
    }
}