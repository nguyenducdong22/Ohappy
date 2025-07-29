package com.example.noname.Budget; // Đảm bảo package này đúng

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets") // Định nghĩa tên bảng trong cơ sở dữ liệu là "budgets"
public class Budget {

    // Khóa chính tự động tăng, Room sẽ tự động gán ID duy nhất
    @PrimaryKey(autoGenerate = true)
    private int id;

    // Định nghĩa tên cột trong cơ sở dữ liệu, mặc định sẽ là tên biến nếu không có @ColumnInfo
    @ColumnInfo(name = "group_name")
    private String groupName;

    @ColumnInfo(name = "group_icon_res_id")
    private int groupIconResId;

    private double amount;

    @ColumnInfo(name = "date_range")
    private String dateRange;

    private boolean repeat;

    // Constructor được Room sử dụng khi tự động tạo ID
    public Budget(String groupName, int groupIconResId, double amount, String dateRange, boolean repeat) {
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        this.dateRange = dateRange;
        this.repeat = repeat;
    }

    // Constructor được Room sử dụng khi đọc dữ liệu từ cơ sở dữ liệu
    public Budget(int id, String groupName, int groupIconResId, double amount, String dateRange, boolean repeat) {
        this.id = id;
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        this.dateRange = dateRange;
        this.repeat = repeat;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupIconResId() {
        return groupIconResId;
    }

    public void setGroupIconResId(int groupIconResId) {
        this.groupIconResId = groupIconResId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}