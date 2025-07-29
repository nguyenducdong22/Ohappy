package com.example.noname.Budget;

public class Budget {
    private String groupName;
    private int groupIconResId;
    private double amount;
    private String dateRange; // Ví dụ: "Tháng này (01/07 - 31/07)"
    private boolean repeat;

    public Budget(String groupName, int groupIconResId, double amount, String dateRange, boolean repeat) {
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        this.dateRange = dateRange;
        this.repeat = repeat;
    }

    // Getters
    public String getGroupName() {
        return groupName;
    }

    public int getGroupIconResId() {
        return groupIconResId;
    }

    public double getAmount() {
        return amount;
    }

    public String getDateRange() {
        return dateRange;
    }

    public boolean isRepeat() {
        return repeat;
    }

    // Setters (nếu bạn cần thay đổi sau khi tạo)
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupIconResId(int groupIconResId) {
        this.groupIconResId = groupIconResId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}