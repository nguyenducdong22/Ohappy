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
    // Tăng phiên bản database để kích hoạt onUpgrade()
    private static final int DATABASE_VERSION = 7;

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
    public static final String COLUMN_NEXT_DATE = "next_date";
    // Thêm các cột thiếu để khớp với DAO
    public static final String COLUMN_IS_ACTIVE_RECURRING = "is_active_recurring";

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

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_FULL_NAME + " TEXT,"
            + COLUMN_PHONE_NUMBER + " TEXT,"
            + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_LAST_LOGIN + " TEXT"
            + ")";

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

    // Đã sửa lại để khớp với code Java của bạn
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
            + COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
            + COLUMN_UPDATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID_FK + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID_FK + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

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

    private static final String CREATE_TABLE_OTP_TOKENS = "CREATE TABLE " + TABLE_OTP_TOKENS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_OTP_CODE + " TEXT NOT NULL UNIQUE,"
            + COLUMN_CREATED_AT + " TEXT NOT NULL,"
            + COLUMN_EXPIRES_AT + " TEXT NOT NULL,"
            + COLUMN_IS_USED + " INTEGER DEFAULT 0,"
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
        db.execSQL(CREATE_TABLE_OTP_TOKENS);

        addDefaultCategories(db);
    }

    private void addDefaultCategories(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CATEGORY_NAME, "Ăn uống");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_restaurant");
        cv.put(COLUMN_COLOR_CODE, "#FF5733");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Nhà cửa & Tiện ích");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_home_and_utility");
        cv.put(COLUMN_COLOR_CODE, "#337CFF");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Di Chuyển");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_directions_car");
        cv.put(COLUMN_COLOR_CODE, "#8A2BE2");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Sắm Sửa");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_shopping_basket");
        cv.put(COLUMN_COLOR_CODE, "#FFD700");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Sức Khỏe");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_health");
        cv.put(COLUMN_COLOR_CODE, "#008000");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Giao Tiếp");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_person");
        cv.put(COLUMN_COLOR_CODE, "#FFA500");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Giải Trí");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_entertainment");
        cv.put(COLUMN_COLOR_CODE, "#8B008B");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Thể Thao");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_sport");
        cv.put(COLUMN_COLOR_CODE, "#00CED1");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Làm Đẹp");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_beauty");
        cv.put(COLUMN_COLOR_CODE, "#FF69B4");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Quà Tặng");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_gift");
        cv.put(COLUMN_COLOR_CODE, "#FF8C00");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Du Lịch");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_travel");
        cv.put(COLUMN_COLOR_CODE, "#20B2AA");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Bạn Bè");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_friends");
        cv.put(COLUMN_COLOR_CODE, "#9370DB");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Học Tập");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_learning");
        cv.put(COLUMN_COLOR_CODE, "#FFD700");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Sách vở");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_book");
        cv.put(COLUMN_COLOR_CODE, "#A0522D");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Khóa Học");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_course");
        cv.put(COLUMN_COLOR_CODE, "#00BFFF");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Dụng Cụ Học Tập");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_study_tools");
        cv.put(COLUMN_COLOR_CODE, "#F08080");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Đầu Tư");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_invest");
        cv.put(COLUMN_COLOR_CODE, "#9ACD32");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Quỹ Khẩn Cấp");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_emergency_fund");
        cv.put(COLUMN_COLOR_CODE, "#B22222");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Khác");
        cv.put(COLUMN_CATEGORY_TYPE, "Expense");
        cv.put(COLUMN_ICON_NAME, "ic_other");
        cv.put(COLUMN_COLOR_CODE, "#A9A9A9");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);


        // Danh mục thu nhập mặc định
        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Lương");
        cv.put(COLUMN_CATEGORY_TYPE, "Income");
        cv.put(COLUMN_ICON_NAME, "ic_salary");
        cv.put(COLUMN_COLOR_CODE, "#4CAF50");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Tiền thưởng");
        cv.put(COLUMN_CATEGORY_TYPE, "Income");
        cv.put(COLUMN_ICON_NAME, "ic_card_giftcard");
        cv.put(COLUMN_COLOR_CODE, "#FF69B4");
        cv.putNull(COLUMN_USER_ID_FK);
        db.insert(TABLE_CATEGORIES, null, cv);

        Log.d("DatabaseHelper", "Default categories inserted.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTP_TOKENS);

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