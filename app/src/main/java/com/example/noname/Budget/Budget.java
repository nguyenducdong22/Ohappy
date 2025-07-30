package com.example.noname.Budget;

/**
 * Budget is a data model class representing a single budget entry.
 * It holds all the attributes of a budget, including its ID, associated user and category,
 * amount, date range, and recurrence status.
 */
public class Budget {
    private long id; // Unique ID of the budget record in the database.
    private long userId; // ID of the user who owns this budget (foreign key).
    private long categoryId; // ID of the category this budget applies to (foreign key).
    private String groupName; // User-friendly name of the category (e.g., "Ăn uống").
    private int groupIconResId; // Resource ID for the icon representing the category (e.g., R.drawable.ic_food).
    private double amount; // The budgeted amount.
    private String startDate; // The start date of the budget period (e.g., "2024-07-01").
    private String endDate; // The end date of the budget period (e.g., "2024-07-31").
    private boolean repeat; // True if the budget is recurring, false otherwise.

    /**
     * Constructor for creating a Budget object from database retrieval.
     * @param id Unique ID of the budget.
     * @param userId ID of the user.
     * @param categoryId ID of the associated category.
     * @param groupName Name of the category.
     * @param groupIconResId Resource ID of the category's icon.
     * @param amount Budgeted amount.
     * @param startDate Start date of the budget.
     * @param endDate End date of the budget.
     * @param repeat True if recurring.
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
     * Constructor for creating a new Budget object *before* it's saved to the database.
     * Note: This constructor combines start/end dates into a single dateRange string for UI display.
     * You might need a more robust date handling mechanism if `dateRange` is a complex string.
     * @param groupName Name of the category.
     * @param groupIconResId Resource ID of the category's icon.
     * @param amount Budgeted amount.
     * @param dateRange A combined string representing the date range (e.g., "Tháng này (01/07 - 31/07)").
     * @param repeat True if recurring.
     */
    public Budget(String groupName, int groupIconResId, double amount, String dateRange, boolean repeat) {
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.amount = amount;
        // For simplicity, directly assign dateRange to startDate and endDate.
        // In a real app, you'd parse this string to get actual start/end dates.
        this.startDate = dateRange;
        this.endDate = dateRange;
        this.repeat = repeat;
        // id, userId, categoryId will be set once saved to DB.
        this.id = -1;
        this.userId = -1;
        this.categoryId = -1;
    }

    // --- Getters ---
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getCategoryId() { return categoryId; }
    public String getGroupName() { return groupName; }
    public int getGroupIconResId() { return groupIconResId; }
    public double getAmount() { return amount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isRepeat() { return repeat; }

    // --- Setters (if needed for object modification after creation) ---
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
     * Combines startDate and endDate into a single display string.
     * @return A formatted string for the date range (e.g., "2024-07-01 - 2024-07-31").
     */
    public String getDateRange() {
        return startDate + " - " + endDate;
    }
}