package com.example.noname.Budget;

import java.io.Serializable;

/**
 * Lớp mô hình dữ liệu đại diện cho một ngân sách.
 * Bao gồm các thuộc tính như ID, người dùng, danh mục, số tiền, ngày tháng và trạng thái lặp lại.
 * Triển khai Serializable để có thể truyền đối tượng này qua các Intent.
 */
public class Budget implements Serializable {
    private long id;
    private long userId;
    private long categoryId;
    private String groupName;
    private int groupIconResId;
    private double amount;
    private String startDate;
    private String endDate;
    private boolean repeat;

    // THÊM MỚI: Constructor rỗng để hỗ trợ tạo đối tượng từ Cursor
    public Budget() {}

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

    public Budget(long userId, long categoryId, String groupName, int groupIconResId, double amount, String startDate, String endDate, boolean repeat) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.repeat = repeat;
    }

    // Getters và Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public int getGroupIconResId() { return groupIconResId; }
    public void setGroupIconResId(int groupIconResId) { this.groupIconResId = groupIconResId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isRepeat() { return repeat; }
    public void setRepeat(boolean repeat) { this.repeat = repeat; }

    public String getDateRange() {
        return startDate + " - " + endDate;
    }
}