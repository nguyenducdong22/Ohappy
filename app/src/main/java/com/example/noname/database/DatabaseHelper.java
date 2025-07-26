package com.example.noname.database;

import android.content.ContentValues; // Import để thêm dữ liệu mặc định
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expending_money.db";
    // Tăng phiên bản database lên 2 để đảm bảo onUpgrade được kích hoạt
    // nếu bạn đã chạy ứng dụng với DATABASE_VERSION = 1 và muốn tạo các bảng mới.
    // Nếu bạn luôn gỡ cài đặt ứng dụng, bạn có thể giữ nó ở 1.
    // ĐỂ KÍCH HOẠT onUpgrade() CHO LẦN ĐẦU TIÊN CÓ CÁC BẢNG NÀY, HÃY ĐẶT LÀ 2.
    private static final int DATABASE_VERSION = 2; // Đã tăng phiên bản

    // --- Tên các Bảng (Tables) ---
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_RECURRING_TRANSACTIONS = "recurring_transactions";
    public static final String TABLE_SAVINGS_GOALS = "savings_goals";

    // --- Cột chung (Common Columns) ---
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at"; // Thời điểm tạo record
    public static final String COLUMN_UPDATED_AT = "updated_at"; // Thời điểm cập nhật record (tùy chọn)

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
    public static final String COLUMN_ACCOUNT_TYPE = "type"; // Cash, Bank, Credit Card, E-wallet
    public static final String COLUMN_IS_ACTIVE = "is_active"; // 0=false, 1=true

    // --- Cột bảng CATEGORIES ---
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_TYPE = "type"; // Expense, Income
    public static final String COLUMN_ICON_NAME = "icon_name";
    public static final String COLUMN_COLOR_CODE = "color_code"; // Mã màu hex cho danh mục

    // --- Cột bảng TRANSACTIONS ---
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_TYPE = "type"; // Expense, Income
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date"; // Ngày diễn ra giao dịch
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_NOTES = "notes";

    // --- Cột bảng BUDGETS ---
    public static final String COLUMN_BUDGET_AMOUNT = "amount";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_IS_RECURRING = "is_recurring"; // 0=false, 1=true

    // --- Cột bảng RECURRING_TRANSACTIONS ---
    public static final String COLUMN_FREQUENCY_TYPE = "frequency_type"; // Daily, Weekly, Monthly, Yearly
    public static final String COLUMN_FREQUENCY_VALUE = "frequency_value"; // e.g., 1 for "every day", 2 for "every 2 weeks"
    public static final String COLUMN_LAST_GENERATED_DATE = "last_generated_date";

    // --- Cột bảng SAVINGS_GOALS ---
    public static final String COLUMN_GOAL_NAME = "name";
    public static final String COLUMN_TARGET_AMOUNT = "target_amount";
    public static final String COLUMN_CURRENT_AMOUNT = "current_amount";
    public static final String COLUMN_TARGET_DATE = "target_date";
    public static final String COLUMN_IS_COMPLETED = "is_completed"; // 0=false, 1=true

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
            + COLUMN_USER_ID_FK + " INTEGER," // Có thể NULL cho danh mục mặc định toàn hệ thống
            + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
            + COLUMN_CATEGORY_TYPE + " TEXT NOT NULL," // Expense hoặc Income
            + COLUMN_ICON_NAME + " TEXT,"
            + COLUMN_COLOR_CODE + " TEXT,"
            + "UNIQUE(" + COLUMN_USER_ID_FK + ", " + COLUMN_CATEGORY_NAME + ", " + COLUMN_CATEGORY_TYPE + ") ON CONFLICT IGNORE," // Đảm bảo danh mục của một user là duy nhất
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 4. Bảng TRANSACTIONS
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_ACCOUNT_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_CATEGORY_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_AMOUNT + " REAL NOT NULL,"
            + COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL," // Expense hoặc Income
            + COLUMN_TRANSACTION_DATE + " TEXT NOT NULL," // YYYY-MM-DD
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
            + COLUMN_IS_RECURRING + " INTEGER DEFAULT 0," // 0=false, 1=true
            + COLUMN_CREATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID_FK + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 6. Bảng RECURRING_TRANSACTIONS
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
            + COLUMN_END_DATE + " TEXT," // NULLable
            + COLUMN_LAST_GENERATED_DATE + " TEXT," // Ngày cuối cùng được tạo tự động
            + COLUMN_CREATED_AT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID_FK + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID_FK + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // 7. Bảng SAVINGS_GOALS
    private static final String CREATE_TABLE_SAVINGS_GOALS = "CREATE TABLE " + TABLE_SAVINGS_GOALS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER NOT NULL,"
            + COLUMN_GOAL_NAME + " TEXT NOT NULL,"
            + COLUMN_TARGET_AMOUNT + " REAL NOT NULL,"
            + COLUMN_CURRENT_AMOUNT + " REAL DEFAULT 0.0,"
            + COLUMN_TARGET_DATE + " TEXT," // NULLable
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0," // 0=false, 1=true
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DatabaseHelper", "DatabaseHelper constructor called.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Creating all tables.");
        // Thực thi tất cả các câu lệnh tạo bảng ở đây
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_RECURRING_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_SAVINGS_GOALS);

        // OPTIONAL: Thêm các danh mục mặc định ban đầu
        addDefaultCategories(db);
    }

    // Phương thức này để thêm các danh mục mặc định khi database được tạo lần đầu
    private void addDefaultCategories(SQLiteDatabase db) {
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
        // Đây là phương pháp phổ biến trong giai đoạn phát triển để xử lý nâng cấp database.
        // Nó sẽ xóa tất cả dữ liệu cũ và tạo lại database với schema mới.
        // TRONG ỨNG DỤNG THẬT SỰ, BẠN SẼ CẦN CÁC LỆNH ALTER TABLE ĐỂ DUY TRÌ DỮ LIỆU.
        Log.d("DatabaseHelper", "onUpgrade: (DEVELOPMENT MODE) Dropping all tables and recreating.");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        onCreate(db); // Tạo lại tất cả các bảng với schema mới
    }
}