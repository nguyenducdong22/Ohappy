package com.example.noname.models;

import androidx.annotation.NonNull; // Chỉ cần nếu bạn dùng @NonNull annotation

public class RecurringExpense {
    private long id;
    private String name; // Tên khoản chi tiêu (được lưu vào cột DESCRIPTION trong DB)
    private double amount;
    private String type; // Ví dụ: "Expense" hoặc "Income"
    private String frequency; // Ví dụ: "Hàng tháng", "Hàng năm", "Hàng tuần"
    private String nextDate; // Ngày tiếp theo dự kiến, ví dụ: "Ngày 15", "Ngày cuối"
    private int iconResId; // Resource ID của icon (ví dụ: R.drawable.ic_home_and_utility)
    private int iconTintColorResId; // Resource ID của màu tint (ví dụ: R.color.primary_green_dark)
    private String status; // Ví dụ: "active" hoặc "inactive"

    // Constructor đầy đủ (khi lấy dữ liệu từ DB, đã có ID)
    public RecurringExpense(long id, String name, double amount, String type, String frequency, String nextDate, int iconResId, int iconTintColorResId, String status) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.frequency = frequency;
        this.nextDate = nextDate;
        this.iconResId = iconResId;
        this.iconTintColorResId = iconTintColorResId;
        this.status = status;
    }

    // Constructor cho việc thêm mới (khi ID chưa được gán bởi DB)
    public RecurringExpense(String name, double amount, String type, String frequency, String nextDate, int iconResId, int iconTintColorResId, String status) {
        this.id = 0; // Đặt ID là 0 hoặc -1 để chỉ ra đây là một đối tượng mới chưa có trong DB
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.frequency = frequency;
        this.nextDate = nextDate;
        this.iconResId = iconResId;
        this.iconTintColorResId = iconTintColorResId;
        this.status = status;
    }

    // --- Getters và Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getNextDate() { return nextDate; }
    public void setNextDate(String nextDate) { this.nextDate = nextDate; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public int getIconTintColorResId() { return iconTintColorResId; }
    public void setIconTintColorResId(int iconTintColorResId) { this.iconTintColorResId = iconTintColorResId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}