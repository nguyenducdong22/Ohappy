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
    // TĂNG PHIÊN BẢN DATABASE LÊN 5 để kích hoạt onUpgrade() và tạo bảng mới OTP
    private static final int DATABASE_VERSION = 5;

    // --- Tên các Bảng (Tables) ---
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_RECURRING_TRANSACTIONS = "recurring_transactions";
    public static final String TABLE_SAVINGS_GOALS = "savings_goals";
    public static final String TABLE_OTP_TOKENS = "otp_tokens"; // Bảng MỚI cho OTP

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
    // ĐÃ XÓA: COLUMN_SECURITY_QUESTION và COLUMN_SECURITY_ANSWER_HASH

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

    // --- Cột bảng OTP_TOKENS (MỚI) ---
    public static final String COLUMN_OTP_CODE = "otp_code";
    public static final String COLUMN_EXPIRES_AT = "expires_at";
    public static final String COLUMN_IS_USED = "is_used";

    // --- Cột Khóa Ngoại (Foreign Keys) ---
    public static final String COLUMN_USER_ID_FK = "user_id";
    public static final String COLUMN_ACCOUNT_ID_FK = "account_id";
    public static final String COLUMN_CATEGORY_ID_FK = "category_id";


    // --- CÂU LỆNH TẠO BẢNG (CREATE TABLE STATEMENTS) ---

    // 1. Bảng USERS (ĐÃ BỎ CỘT CÂU HỎI BẢO MẬT)
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_FULL_NAME + " TEXT,"
            + COLUMN_PHONE_NUMBER + " TEXT,"
            + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_LAST_LOGIN + " TEXT"
            + ")";

    // 2. Bảng ACCOUNTS (giữ nguyên)
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

    // 3. Bảng CATEGORIES (giữ nguyên)
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

    // 4. Bảng TRANSACTIONS (giữ nguyên)
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

    // 5. Bảng BUDGETS (giữ nguyên)
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

    // 6. Bảng RECURRING_TRANSACTIONS (giữ nguyên)
    private static final String CREATE_TABLE_RECURRING_TRANSACTIONS = "CREATE TABLE " + TABLE_RECURRING_TRANSACTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_ACCOUNT_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_CATEGORY_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_AMOUNT + " REAL NOT NULL,"
            + COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_FREQUENCY_TYPE + " TEXT NOT NULL,"
            + COLUMN_FREQUENCY_VALUE + " INTEGER NOT NULL,"
            + COLUMN_START_DATE + " TEXT NOT NULL,"
            + COLUMN_END_DATE + " TEXT,"
            + COLUMN_LAST_GENERATED_DATE + " TEXT,"
            + COLUMN_CREATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID_FK + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID_FK + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 7. Bảng SAVINGS_GOALS (giữ nguyên)
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

    // 8. Bảng OTP_TOKENS (MỚI)
    private static final String CREATE_TABLE_OTP_TOKENS = "CREATE TABLE " + TABLE_OTP_TOKENS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_OTP_CODE + " TEXT NOT NULL UNIQUE,"
            + COLUMN_CREATED_AT + " TEXT NOT NULL,"
            + COLUMN_EXPIRES_AT + " TEXT NOT NULL,"
            + COLUMN_IS_USED + " INTEGER DEFAULT 0," // 0 = false (chưa dùng), 1 = true (đã dùng)
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DatabaseHelper", "DatabaseHelper constructor called.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Creating all tables.");
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_RECURRING_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_SAVINGS_GOALS);
        db.execSQL(CREATE_TABLE_OTP_TOKENS); // Thêm bảng OTP_TOKENS

        addDefaultCategories(db);
    }

    private void addDefaultCategories(SQLiteDatabase db) {
        // ... (Giữ nguyên phương thức này)
        ContentValues cv = new ContentValues();

        // Expense Categories
        cv.put(COLUMN_CATEGORY_NAME, "Ăn uống");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_food");
        cv.put(COLUMN_COLOR_CODE, "#FF5733"); // Màu đỏ cam
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Thuê nhà");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_house");
        cv.put(COLUMN_COLOR_CODE, "#337CFF"); // Màu xanh dương
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Mua sắm");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_shopping_basket");
        cv.put(COLUMN_COLOR_CODE, "#FFD700"); // Màu vàng
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Đi lại");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_directions_car"); // Cần icon này
        cv.put(COLUMN_COLOR_CODE, "#8A2BE2"); // Màu tím
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Giải trí");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_movie"); // Cần icon này
        cv.put(COLUMN_COLOR_CODE, "#FF1493"); // Màu hồng đậm
        db.insert(TABLE_CATEGORIES, null, cv);

        // Income Categories
        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Lương");
        cv.put(COLUMN_CATEGORY_TYPE, "Income");
        cv.put(COLUMN_ICON_NAME, "ic_money");
        cv.put(COLUMN_COLOR_CODE, "#4CAF50"); // Màu xanh lá cây
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Tiền thưởng");
        cv.put(COLUMN_CATEGORY_TYPE, "Income");
        cv.put(COLUMN_ICON_NAME, "ic_card_giftcard"); // Cần icon này
        cv.put(COLUMN_COLOR_CODE, "#FF69B4"); // Màu hồng nhạt
        db.insert(TABLE_CATEGORIES, null, cv);

        Log.d("DatabaseHelper", "Default categories inserted.");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);

        // Với sản phẩm demo, cách đơn giản nhất để nâng cấp là xóa tất cả và tạo lại.
        // Cần lưu ý rằng cách này sẽ XÓA HẾT DỮ LIỆU CŨ!
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTP_TOKENS); // Đảm bảo xóa cả bảng OTP_TOKENS

        onCreate(db); // Tạo lại tất cả các bảng với schema mới
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