package com.example.noname.models;

import java.io.Serializable;

public class Category implements Serializable {
    private long id;
    private Long userId;
    private String name;
    private String type;
    private String iconName;
    private int iconResId;
    private String colorCode;

    // Constructor đầy đủ cho các trường.
    public Category(long id, String name, String type, String iconName, int iconResId, String colorCode, Long userId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.iconName = iconName;
        this.iconResId = iconResId;
        this.colorCode = colorCode;
        this.userId = userId;
    }

    // Constructor để tạo lại đối tượng Category khi nhận kết quả từ Activity (ĐÃ CÓ)
    public Category(long id, String name, String type, int iconResId, String colorCode) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.iconResId = iconResId;
        this.colorCode = colorCode;
        this.userId = null; // Thêm giá trị mặc định để tránh lỗi
    }

    // Constructor CẦN THIẾT để khắc phục lỗi trong AddBudgetActivity
    public Category(long id, String name, int iconResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.userId = null;
        this.type = null;
        this.iconName = null;
        this.colorCode = null;
    }

    // Constructor mặc định
    public Category() {
    }

    // Getters
    public long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getIconName() { return iconName; }
    public int getIconResId() { return iconResId; }
    public String getColorCode() { return colorCode; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
}