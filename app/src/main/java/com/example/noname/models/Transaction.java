package com.example.noname.models; // CHÍNH XÁC: package com.example.noname.models;

// Các import cho Room KHÔNG CẦN NỮA KHI KHÔNG DÙNG DATABASE
// import androidx.room.ColumnInfo;
// import androidx.room.Entity;
// import androidx.room.PrimaryKey;

// KHÔNG CÓ ANNOTATION @Entity NỮA KHI KHÔNG DÙNG ROOM DATABASE
// @Entity(tableName = "transactions")
public class Transaction { // <-- Đã đổi tên lớp thành Transaction

    // Các annotation @PrimaryKey, @ColumnInfo KHÔNG CẦN NỮA
    // @PrimaryKey(autoGenerate = true)
    private int id;

    // @ColumnInfo(name = "user_id_fk")
    private long userId;

    // @ColumnInfo(name = "amount")
    private double amount;

    // @ColumnInfo(name = "wallet_name")
    private String walletName;

    // @ColumnInfo(name = "group_name")
    private String groupName;

    // @ColumnInfo(name = "group_icon_res_id")
    private int groupIconResId;

    // @ColumnInfo(name = "group_color_res_id")
    private int groupColorResId;

    // @ColumnInfo(name = "transaction_date")
    private long transactionDate;

    // @ColumnInfo(name = "note")
    private String note;

    // @ColumnInfo(name = "transaction_type")
    private String type;

    // Constructor đầy đủ (khi lấy từ DB hoặc tạo từ Intent)
    public Transaction(long id, long userId, double amount, String walletName, String groupName, int groupIconResId, int groupColorResId, long transactionDate, String note, String type) {
        this.id = (int) id; // Cần ép kiểu nếu id là int nhưng nhận long từ DB/Intent
        this.userId = userId;
        this.amount = amount;
        this.walletName = walletName;
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.groupColorResId = groupColorResId;
        this.transactionDate = transactionDate;
        this.note = note;
        this.type = type;
    }

    // Constructor cho việc thêm mới (ID chưa có)
    public Transaction(long userId, double amount, String walletName, String groupName, int groupIconResId, int groupColorResId, long transactionDate, String note, String type) {
        this.userId = userId;
        this.amount = amount;
        this.walletName = walletName;
        this.groupName = groupName;
        this.groupIconResId = groupIconResId;
        this.groupColorResId = groupColorResId;
        this.transactionDate = transactionDate;
        this.note = note;
        this.type = type;
    }

    // --- Getters và Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public int getGroupIconResId() { return groupIconResId; }
    public void setGroupIconResId(int groupIconResId) { this.groupIconResId = groupIconResId; }
    public int getGroupColorResId() { return groupColorResId; }
    public void setGroupColorResId(int groupColorResId) { this.groupColorResId = groupColorResId; }
    public long getTransactionDate() { return transactionDate; }
    public void setTransactionDate(long transactionDate) { this.transactionDate = transactionDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}