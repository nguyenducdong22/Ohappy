package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expending_money.db";
    // TĂNG PHIÊN BẢN DATABASE LÊN 8 để kích hoạt onUpgrade()
    // Mỗi khi thay đổi schema của bất kỳ bảng nào, bạn cần tăng version để áp dụng thay đổi
    private static final int DATABASE_VERSION = 8; // Đã tăng từ 7 lên 8

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
    public static final String COLUMN_ICON_NAME = "icon_name"; // Thêm cột này cho Category
    public static final String COLUMN_COLOR_CODE = "color_code"; // Thêm cột này cho Category

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
    public static final String COLUMN_FREQUENCY_VALUE = "frequency_value"; // Tùy chọn, nếu cần giá trị số
    public static final String COLUMN_LAST_GENERATED_DATE = "last_generated_date";
    public static final String COLUMN_NEXT_DATE = "next_date"; // Mới
    public static final String COLUMN_STATUS = "status"; // Mới

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


    // --- CÂU LỆNH TẠO BẢNG (CREATE TABLE STATEMENTS) ---

    // 1. Bảng USERS
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_FULL_NAME + " TEXT,"
            + COLUMN_PHONE_NUMBER + " TEXT,"
            + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_LAST_LOGIN + " TEXT"
            + ")";

    // 2. Bảng ACCOUNTS
    private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + TABLE_ACCOUNTS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_ACCOUNT_NAME + " TEXT NOT NULL,"
            + COLUMN_BALANCE + " REAL DEFAULT 0.0,"
            + COLUMN_CURRENCY + " TEXT DEFAULT 'VND',"
            + COLUMN_ACCOUNT_TYPE + " TEXT,"
            + COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
            + COLUMN_CREATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 3. Bảng CATEGORIES
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER,"
            + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
            + COLUMN_CATEGORY_TYPE + " TEXT NOT NULL,"
            + COLUMN_ICON_NAME + " TEXT,"
            + COLUMN_COLOR_CODE + " TEXT,"
            + "UNIQUE(" + COLUMN_USER_ID_FK + ", " + COLUMN_CATEGORY_NAME + ", " + COLUMN_CATEGORY_TYPE + ") ON CONFLICT IGNORE,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 4. Bảng TRANSACTIONS
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_ACCOUNT_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_CATEGORY_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_AMOUNT + " REAL NOT NULL,"
            + COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL,"
            + COLUMN_TRANSACTION_DATE + " TEXT NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_NOTES + " TEXT,"
            + COLUMN_CREATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID_FK + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID_FK + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 5. Bảng BUDGETS
    private static final String CREATE_TABLE_BUDGETS = "CREATE TABLE " + TABLE_BUDGETS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_CATEGORY_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_BUDGET_AMOUNT + " REAL NOT NULL,"
            + COLUMN_START_DATE + " TEXT NOT NULL,"
            + COLUMN_END_DATE + " TEXT NOT NULL,"
            + COLUMN_IS_RECURRING + " INTEGER DEFAULT 0,"
            + COLUMN_CREATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID_FK + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 6. Bảng RECURRING_TRANSACTIONS
    private static final String CREATE_TABLE_RECURRING_TRANSACTIONS = "CREATE TABLE " + TABLE_RECURRING_TRANSACTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_ACCOUNT_ID_FK + " INTEGER,"
            + COLUMN_CATEGORY_ID_FK + " INTEGER,"
            + COLUMN_AMOUNT + " REAL NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL,"
            + COLUMN_FREQUENCY_TYPE + " TEXT NOT NULL,"
            + COLUMN_FREQUENCY_VALUE + " INTEGER,"
            + COLUMN_START_DATE + " TEXT NOT NULL,"
            + COLUMN_NEXT_DATE + " TEXT,"
            + COLUMN_ICON_NAME + " TEXT,"
            + COLUMN_COLOR_CODE + " TEXT,"
            + COLUMN_LAST_GENERATED_DATE + " TEXT,"
            + COLUMN_STATUS + " TEXT DEFAULT 'active',"
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_UPDATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID_FK + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ") ON DELETE SET NULL,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID_FK + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE SET NULL"
            + ")";

    // 7. Bảng SAVINGS_GOALS
    private static final String CREATE_TABLE_SAVINGS_GOALS = "CREATE TABLE " + TABLE_SAVINGS_GOALS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_GOAL_NAME + " TEXT NOT NULL,"
            + COLUMN_TARGET_AMOUNT + " REAL NOT NULL,"
            + COLUMN_CURRENT_AMOUNT + " REAL DEFAULT 0.0,"
            + COLUMN_TARGET_DATE + " TEXT,"
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 8. Bảng OTP_TOKENS
    private static final String CREATE_TABLE_OTP_TOKENS = "CREATE TABLE " + TABLE_OTP_TOKENS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_OTP_CODE + " TEXT NOT NULL UNIQUE,"
            + COLUMN_CREATED_AT + " TEXT NOT NULL,"
            + COLUMN_EXPIRES_AT + " TEXT NOT NULL,"
            + COLUMN_IS_USED + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // Context của ứng dụng (để truy cập Resources)
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; // Lưu context
        Log.d("DatabaseHelper", "DatabaseHelper constructor called. Version: " + DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Creating all tables.");
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_RECURRING_TRANSACTIONS); // Thêm bảng này
        db.execSQL(CREATE_TABLE_SAVINGS_GOALS);
        db.execSQL(CREATE_TABLE_OTP_TOKENS);

        addDefaultCategories(db);
    }

    private void addDefaultCategories(SQLiteDatabase db) {
        // Hàm này sẽ tự động thêm các danh mục mặc định khi database được tạo lần đầu
        ContentValues cv = new ContentValues();

        // Expense Categories
        cv.put(COLUMN_CATEGORY_NAME, "Ăn uống");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_food");
        cv.put(COLUMN_COLOR_CODE, "#FF5733");
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Thuê nhà");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_house");
        cv.put(COLUMN_COLOR_CODE, "#337CFF");
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Mua sắm");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_shopping_basket");
        cv.put(COLUMN_COLOR_CODE, "#FFD700");
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Đi lại");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_directions_car");
        cv.put(COLUMN_COLOR_CODE, "#8A2BE2");
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Giải trí");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_movie");
        cv.put(COLUMN_COLOR_CODE, "#FF1493");
        db.insert(TABLE_CATEGORIES, null, cv);

        // Income Categories
        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Lương");
        cv.put(COLUMN_CATEGORY_TYPE, "Income");
        cv.put(COLUMN_ICON_NAME, "ic_money");
        cv.put(COLUMN_COLOR_CODE, "#4CAF50");
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Tiền thưởng");
        cv.put(COLUMN_CATEGORY_TYPE, "Income");
        cv.put(COLUMN_ICON_NAME, "ic_card_giftcard");
        cv.put(COLUMN_COLOR_CODE, "#FF69B4");
        db.insert(TABLE_CATEGORIES, null, cv);

        Log.d("DatabaseHelper", "Default categories inserted.");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);

        // Xóa tất cả các bảng theo thứ tự từ phụ thuộc đến độc lập (để tránh lỗi khóa ngoại)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTP_TOKENS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS); // Accounts phụ thuộc vào Users
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES); // Categories phụ thuộc vào Users
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // Users là bảng độc lập

        onCreate(db); // Tạo lại tất cả các bảng với schema mới
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Luôn bật khóa ngoại khi database được mở để đảm bảo tính toàn vẹn dữ liệu
            db.execSQL("PRAGMA foreign_keys=ON;");
            Log.d("DatabaseHelper", "Foreign keys enabled.");
        }
    }

    // Helper method để lấy timestamp hiện tại dưới dạng chuỗi
    public static String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Getter cho Context (được sử dụng bởi DAO để truy cập Resources)
    public Context getContext() {
        return context;
    }
}