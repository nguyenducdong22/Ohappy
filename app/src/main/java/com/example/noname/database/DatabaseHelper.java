package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expending_money.db";
    // Giữ nguyên phiên bản, chỉ cần gỡ cài đặt app để onCreate chạy lại
    private static final int DATABASE_VERSION = 5;

    // --- Tên các Bảng (Tables) ---
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_RECURRING_TRANSACTIONS = "recurring_transactions";
    public static final String TABLE_SAVINGS_GOALS = "savings_goals";
    public static final String TABLE_OTP_TOKENS = "otp_tokens";

    // --- Cột chung (Common Columns) ---
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // --- Cột bảng USERS ---
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_LAST_LOGIN = "last_login";

    // --- Cột bảng ACCOUNTS ---
    public static final String COLUMN_ACCOUNT_NAME = "name";
    public static final String COLUMN_BALANCE = "balance";
    public static final String COLUMN_CURRENCY = "currency";
    public static final String COLUMN_ACCOUNT_TYPE = "type";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    // --- Cột bảng CATEGORIES ---
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_TYPE = "type";
    public static final String COLUMN_ICON_NAME = "icon_name";
    public static final String COLUMN_COLOR_CODE = "color_code";

    // --- Cột bảng TRANSACTIONS ---
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_TYPE = "type";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_NOTES = "notes";

    // --- Cột bảng BUDGETS ---
    public static final String COLUMN_BUDGET_AMOUNT = "amount";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_IS_RECURRING = "is_recurring";

    // --- Cột bảng RECURRING_TRANSACTIONS ---
    public static final String COLUMN_FREQUENCY_TYPE = "frequency_type";
    public static final String COLUMN_FREQUENCY_VALUE = "frequency_value";
    public static final String COLUMN_LAST_GENERATED_DATE = "last_generated_date";

    // --- Cột bảng SAVINGS_GOALS ---
    public static final String COLUMN_GOAL_NAME = "name";
    public static final String COLUMN_TARGET_AMOUNT = "target_amount";
    public static final String COLUMN_CURRENT_AMOUNT = "current_amount";
    public static final String COLUMN_TARGET_DATE = "target_date";
    public static final String COLUMN_IS_COMPLETED = "is_completed";

    // --- Cột bảng OTP_TOKENS ---
    public static final String COLUMN_OTP_CODE = "otp_code";
    public static final String COLUMN_EXPIRES_AT = "expires_at";
    public static final String COLUMN_IS_USED = "is_used";

    // --- Cột Khóa Ngoại (Foreign Keys) ---
    public static final String COLUMN_USER_ID_FK = "user_id";
    public static final String COLUMN_ACCOUNT_ID_FK = "account_id";
    public static final String COLUMN_CATEGORY_ID_FK = "category_id";

    //
    public static final String COLUMN_CATEGORY_ID = "category_id";

    // --- CÂU LỆNH TẠO BẢNG ---
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE NOT NULL, full_name TEXT, phone_number TEXT, password_hash TEXT NOT NULL, created_at TEXT, last_login TEXT)";
    private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + TABLE_ACCOUNTS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, name TEXT NOT NULL, balance REAL DEFAULT 0.0, currency TEXT DEFAULT 'VND', type TEXT, is_active INTEGER DEFAULT 1, created_at TEXT, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, name TEXT NOT NULL, type TEXT NOT NULL, icon_name TEXT, color_code TEXT, UNIQUE(user_id, name, type) ON CONFLICT IGNORE, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, account_id INTEGER NOT NULL, category_id INTEGER NOT NULL, amount REAL NOT NULL, type TEXT NOT NULL, transaction_date TEXT NOT NULL, description TEXT, notes TEXT, created_at TEXT, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE, FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE)";
    private static final String CREATE_TABLE_BUDGETS = "CREATE TABLE " + TABLE_BUDGETS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, category_id INTEGER NOT NULL, amount REAL NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL, is_recurring INTEGER DEFAULT 0, created_at TEXT, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE)";
    private static final String CREATE_TABLE_RECURRING_TRANSACTIONS = "CREATE TABLE " + TABLE_RECURRING_TRANSACTIONS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, account_id INTEGER NOT NULL, category_id INTEGER NOT NULL, amount REAL NOT NULL, type TEXT NOT NULL, description TEXT, frequency_type TEXT NOT NULL, frequency_value INTEGER NOT NULL, start_date TEXT NOT NULL, end_date TEXT, last_generated_date TEXT, created_at TEXT, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE, FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE)";
    private static final String CREATE_TABLE_SAVINGS_GOALS = "CREATE TABLE " + TABLE_SAVINGS_GOALS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, name TEXT NOT NULL, target_amount REAL NOT NULL, current_amount REAL DEFAULT 0.0, target_date TEXT, created_at TEXT, is_completed INTEGER DEFAULT 0, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";
    private static final String CREATE_TABLE_OTP_TOKENS = "CREATE TABLE " + TABLE_OTP_TOKENS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, otp_code TEXT NOT NULL UNIQUE, created_at TEXT NOT NULL, expires_at TEXT NOT NULL, is_used INTEGER DEFAULT 0, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Creating all tables.");
        // Tạo cấu trúc bảng
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_RECURRING_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_SAVINGS_GOALS);
        db.execSQL(CREATE_TABLE_OTP_TOKENS);

        // Thêm các danh mục mặc định
        addDefaultCategories(db);

        // Thêm dữ liệu mẫu để demo
        addSampleDataForDemo(db);
    }

    private void addDefaultCategories(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Inserting default categories...");
        ContentValues cv = new ContentValues();
        // Chi tiêu
        cv.put(COLUMN_CATEGORY_NAME, "Ăn uống"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_food"); cv.put(COLUMN_COLOR_CODE, "#FF5733"); db.insert(TABLE_CATEGORIES, null, cv);
        cv.put(COLUMN_CATEGORY_NAME, "Thuê nhà"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_house"); cv.put(COLUMN_COLOR_CODE, "#337CFF"); db.insert(TABLE_CATEGORIES, null, cv);
        cv.put(COLUMN_CATEGORY_NAME, "Mua sắm"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_shopping_basket"); cv.put(COLUMN_COLOR_CODE, "#FFD700"); db.insert(TABLE_CATEGORIES, null, cv);
        cv.put(COLUMN_CATEGORY_NAME, "Đi lại"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_directions_car"); cv.put(COLUMN_COLOR_CODE, "#8A2BE2"); db.insert(TABLE_CATEGORIES, null, cv);
        cv.put(COLUMN_CATEGORY_NAME, "Giải trí"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_movie"); cv.put(COLUMN_COLOR_CODE, "#FF1493"); db.insert(TABLE_CATEGORIES, null, cv);
        // Thu nhập
        cv.put(COLUMN_CATEGORY_NAME, "Lương"); cv.put(COLUMN_CATEGORY_TYPE, "Income"); cv.put(COLUMN_ICON_NAME, "ic_money"); cv.put(COLUMN_COLOR_CODE, "#4CAF50"); db.insert(TABLE_CATEGORIES, null, cv);
        cv.put(COLUMN_CATEGORY_NAME, "Tiền thưởng"); cv.put(COLUMN_CATEGORY_TYPE, "Income"); cv.put(COLUMN_ICON_NAME, "ic_card_giftcard"); cv.put(COLUMN_COLOR_CODE, "#FF69B4"); db.insert(TABLE_CATEGORIES, null, cv);
        Log.d("DatabaseHelper", "Default categories inserted.");
    }

    /**
     * Chèn dữ liệu mẫu cho người dùng, tài khoản và giao dịch để demo.
     * @param db Đối tượng SQLiteDatabase.
     */
    private void addSampleDataForDemo(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Seeding sample data for demo...");

        // BƯỚC 1: TẠO NGƯỜI DÙNG MẪU
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_EMAIL, "demo@user.com");
        userValues.put(COLUMN_FULL_NAME, "Người dùng Demo");
        userValues.put(COLUMN_PASSWORD_HASH, "demo_password");
        userValues.put(COLUMN_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        long userId = db.insert(TABLE_USERS, null, userValues);
        if (userId == -1) {
            Log.e("DatabaseHelper", "Failed to insert sample user.");
            return;
        }

        // BƯỚC 2: TẠO TÀI KHOẢN (VÍ) MẪU
        ContentValues cashAccount = new ContentValues();
        cashAccount.put(COLUMN_USER_ID_FK, userId);
        cashAccount.put(COLUMN_ACCOUNT_NAME, "Tiền mặt");
        cashAccount.put(COLUMN_BALANCE, 1500000);
        cashAccount.put(COLUMN_ACCOUNT_TYPE, "cash");
        long cashAccountId = db.insert(TABLE_ACCOUNTS, null, cashAccount);

        ContentValues bankAccount = new ContentValues();
        bankAccount.put(COLUMN_USER_ID_FK, userId);
        bankAccount.put(COLUMN_ACCOUNT_NAME, "Ngân hàng");
        bankAccount.put(COLUMN_BALANCE, 10000000);
        bankAccount.put(COLUMN_ACCOUNT_TYPE, "bank");
        long bankAccountId = db.insert(TABLE_ACCOUNTS, null, bankAccount);

        if (cashAccountId == -1 || bankAccountId == -1) {
            Log.e("DatabaseHelper", "Failed to insert sample accounts.");
            return;
        }

        // BƯỚC 3: LẤY ID CỦA CÁC DANH MỤC ĐÃ TẠO
        long foodCategoryId = getCategoryId(db, "Ăn uống", "Expense");
        long transportCategoryId = getCategoryId(db, "Đi lại", "Expense");
        long salaryCategoryId = getCategoryId(db, "Lương", "Income");
        long shoppingCategoryId = getCategoryId(db, "Mua sắm", "Expense");
        long entertainmentCategoryId = getCategoryId(db, "Giải trí", "Expense");

        // BƯỚC 4: TẠO CÁC GIAO DỊCH MẪU
        addTransaction(db, userId, bankAccountId, salaryCategoryId, 12000000, "Income", "2025-07-25 09:00:00", "Lương tháng 7");
        addTransaction(db, userId, bankAccountId, shoppingCategoryId, 750000, "Expense", "2025-07-26 19:15:00", "Mua áo sơ mi mới");
        addTransaction(db, userId, cashAccountId, foodCategoryId, 55000, "Expense", "2025-07-28 12:30:00", "Bữa trưa cơm sườn");
        addTransaction(db, userId, cashAccountId, transportCategoryId, 30000, "Expense", "2025-07-29 08:00:00", "Grab đi làm");
        addTransaction(db, userId, bankAccountId, entertainmentCategoryId, 220000, "Expense", "2025-07-29 20:30:00", "Xem phim cuối tuần");
        addTransaction(db, userId, cashAccountId, foodCategoryId, 250000, "Expense", "2025-07-30 20:00:00", "Ăn tối cùng bạn bè");
        addTransaction(db, userId, cashAccountId, foodCategoryId, 45000, "Expense", "2025-08-01 07:45:00", "Cà phê sáng");

        Log.d("DatabaseHelper", "Sample data seeded successfully.");
    }

    private long getCategoryId(SQLiteDatabase db, String name, String type) {
        try (Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_ID},
                COLUMN_CATEGORY_NAME + " = ? AND " + COLUMN_CATEGORY_TYPE + " = ?",
                new String[]{name, type}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        }
        return -1; // Trả về -1 nếu không tìm thấy
    }

    private void addTransaction(SQLiteDatabase db, long userId, long accountId, long categoryId,
                                double amount, String type, String date, String description) {
        if (categoryId == -1) {
            Log.e("DatabaseHelper", "Cannot add transaction due to invalid category ID for: " + description);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_FK, userId);
        values.put(COLUMN_ACCOUNT_ID_FK, accountId);
        values.put(COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TRANSACTION_TYPE, type);
        values.put(COLUMN_TRANSACTION_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_TRANSACTIONS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);
        // Xóa tất cả các bảng cũ
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTP_TOKENS);
        // Tạo lại toàn bộ cơ sở dữ liệu với schema mới và dữ liệu mẫu
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            Log.d("DatabaseHelper", "Foreign keys enabled.");
        }
    }
}