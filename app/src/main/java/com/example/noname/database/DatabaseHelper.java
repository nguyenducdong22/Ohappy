package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.noname.R; // Dùng để truy cập tài nguyên drawable cho các danh mục mặc định.

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DatabaseHelper quản lý việc tạo, nâng cấp và toàn bộ lược đồ của cơ sở dữ liệu SQLite.
 * Nó định nghĩa tên bảng và tên cột, đồng thời xử lý việc điền dữ liệu mặc định ban đầu.
 * Các thao tác dữ liệu cụ thể (CRUD) được ủy quyền cho các lớp DAO riêng lẻ (ví dụ: BudgetDAO).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Thông tin cơ sở dữ liệu
    private static final String DATABASE_NAME = "expending_money.db";
    // QUAN TRỌNG: Tăng giá trị này lên khi có thay đổi lược đồ để kích hoạt onUpgrade().
    // Chúng ta tăng nó lên để đảm bảo các danh mục mới được thêm vào.
    private static final int DATABASE_VERSION = 6;

    // Tên bảng
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_RECURRING_TRANSACTIONS = "recurring_transactions";
    public static final String TABLE_SAVINGS_GOALS = "savings_goals";
    public static final String TABLE_OTP_TOKENS = "otp_tokens";

    // Các cột chung
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // Cột bảng USERS
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_LAST_LOGIN = "last_login";

    // Cột bảng ACCOUNTS
    public static final String COLUMN_ACCOUNT_NAME = "name";
    public static final String COLUMN_BALANCE = "balance";
    public static final String COLUMN_CURRENCY = "currency";
    public static final String COLUMN_ACCOUNT_TYPE = "type";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    // Cột bảng CATEGORIES
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_TYPE = "type";
    public static final String COLUMN_ICON_NAME = "icon_name";
    public static final String COLUMN_COLOR_CODE = "color_code";

    // Cột bảng TRANSACTIONS
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_TYPE = "type";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_NOTES = "notes";

    // Cột bảng BUDGETS
    public static final String COLUMN_BUDGET_AMOUNT = "amount";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_IS_RECURRING = "is_recurring";

    // Cột bảng RECURRING_TRANSACTIONS
    public static final String COLUMN_FREQUENCY_TYPE = "frequency_type";
    public static final String COLUMN_FREQUENCY_VALUE = "frequency_value";
    public static final String COLUMN_LAST_GENERATED_DATE = "last_generated_date";

    // Cột bảng SAVINGS_GOALS
    public static final String COLUMN_GOAL_NAME = "name";
    public static final String COLUMN_TARGET_AMOUNT = "target_amount";
    public static final String COLUMN_CURRENT_AMOUNT = "current_amount";
    public static final String COLUMN_TARGET_DATE = "target_date";
    public static final String COLUMN_IS_COMPLETED = "is_completed";

    // Cột bảng OTP_TOKENS
    public static final String COLUMN_OTP_CODE = "otp_code";
    public static final String COLUMN_EXPIRES_AT = "expires_at";
    public static final String COLUMN_IS_USED = "is_used";

    // Cột khóa ngoại
    public static final String COLUMN_USER_ID_FK = "user_id";
    public static final String COLUMN_ACCOUNT_ID_FK = "account_id";
    public static final String COLUMN_CATEGORY_ID_FK = "category_id";

    // Tham chiếu context cần thiết để truy cập tài nguyên như drawables.
    private Context context;

    /**
     * Constructor cho DatabaseHelper.
     * @param context Context của ứng dụng, được sử dụng cho các thao tác cơ sở dữ liệu và truy cập tài nguyên.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; // Lưu trữ context để sử dụng sau này.
        Log.d("DatabaseHelper", "Constructor DatabaseHelper được gọi. Phiên bản: " + DATABASE_VERSION);
    }

    /**
     * Được gọi khi cơ sở dữ liệu được tạo lần đầu tiên.
     * Thực thi tất cả các câu lệnh SQL CREATE TABLE.
     * @param db Thể hiện của SQLiteDatabase.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Đang tạo tất cả các bảng.");

        // Thực thi các câu lệnh CREATE TABLE theo thứ tự phụ thuộc.
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_RECURRING_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_SAVINGS_GOALS);
        db.execSQL(CREATE_TABLE_OTP_TOKENS);

        // Thêm các danh mục mặc định sau khi bảng CATEGORIES được tạo.
        addDefaultCategories(db);
        Log.d("DatabaseHelper", "onCreate: Tất cả các bảng đã được tạo và các danh mục mặc định đã được thêm.");
    }

    /**
     * Được gọi khi cơ sở dữ liệu cần được nâng cấp (DATABASE_VERSION tăng lên).
     * Cách triển khai này sẽ xóa tất cả các bảng và tạo lại chúng, dẫn đến mất dữ liệu.
     * Đối với các ứng dụng thực tế, hãy sử dụng các câu lệnh ALTER TABLE để di chuyển dữ liệu.
     * @param db Thể hiện của SQLiteDatabase.
     * @param oldVersion Phiên bản cơ sở dữ liệu cũ.
     * @param newVersion Phiên bản cơ sở dữ liệu mới.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade: Đang nâng cấp cơ sở dữ liệu từ phiên bản " + oldVersion + " lên " + newVersion);
        Log.w("DatabaseHelper", "CẢNH BÁO: Tất cả dữ liệu hiện có sẽ bị XÓA trong quá trình nâng cấp này!");

        // Xóa các bảng theo thứ tự khóa ngoại ngược để tránh lỗi toàn vẹn dữ liệu.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTP_TOKENS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Tạo lại tất cả các bảng với lược đồ mới.
        onCreate(db);
        Log.d("DatabaseHelper", "onUpgrade: Các bảng cơ sở dữ liệu đã được tạo lại với lược đồ mới.");
    }

    /**
     * Được gọi khi cơ sở dữ liệu được mở. Đảm bảo các ràng buộc khóa ngoại được bật.
     * @param db Thể hiện của SQLiteDatabase.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;"); // Bật thực thi khóa ngoại.
            Log.d("DatabaseHelper", "onOpen: Khóa ngoại đã được bật.");
        }
    }

    // --- CÁC CÂU LỆNH TẠO BẢNG ---

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
            + COLUMN_USER_ID_FK + " INTEGER," // Có thể null cho các danh mục mặc định.
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

    /**
     * Thêm các danh mục mặc định được định nghĩa trước vào bảng CATEGORIES.
     * Các danh mục này có sẵn cho tất cả người dùng theo mặc định.
     * @param db Thể hiện của SQLiteDatabase để chèn dữ liệu.
     */
    private void addDefaultCategories(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        Log.d("DatabaseHelper", "Đang thêm các danh mục mặc định...");

        // --- Danh mục Chi tiêu ---
        insertCategory(db, "Ăn uống", "Expense", "ic_food", "#FF5733");
        insertCategory(db, "Nhà cửa & Tiện ích", "Expense", "ic_home_and_utility", "#337CFF");
        insertCategory(db, "Di chuyển", "Expense", "ic_directions_car", "#8A2BE2");
        insertCategory(db, "Sắm sửa", "Expense", "ic_shopping_basket", "#FFD700");
        insertCategory(db, "Sức Khỏe", "Expense", "ic_health", "#FF6347"); // Sử dụng màu mới
        insertCategory(db, "Giao Tiếp", "Expense", "ic_person", "#4682B4"); // Sử dụng màu mới

        insertCategory(db, "Giải trí", "Expense", "ic_entertainment", "#FF1493");
        insertCategory(db, "Thể Thao", "Expense", "ic_sport", "#008080"); // Sử dụng màu mới
        insertCategory(db, "Làm Đẹp", "Expense", "ic_beauty", "#FF69B4"); // Sử dụng màu mới
        insertCategory(db, "Quà Tặng", "Expense", "ic_gift", "#DAA520"); // Sử dụng màu mới
        insertCategory(db, "Du Lịch", "Expense", "ic_travel", "#1E90FF"); // Sử dụng màu mới
        insertCategory(db, "Bạn Bè", "Expense", "ic_friends", "#9370DB"); // Sử dụng màu mới

        insertCategory(db, "Học Tập", "Expense", "ic_learning", "#228B22"); // Sử dụng màu mới
        insertCategory(db, "Sách vở", "Expense", "ic_book", "#CD853F"); // Sử dụng màu mới
        insertCategory(db, "Khóa Học", "Expense", "ic_course", "#6A5ACD"); // Sử dụng màu mới
        insertCategory(db, "Dụng Cụ Học Tập", "Expense", "ic_study_tools", "#8B4513"); // Sử dụng màu mới

        insertCategory(db, "Đầu Tư", "Expense", "ic_invest", "#006400"); // Sử dụng màu mới
        insertCategory(db, "Tiết Kiệm", "Expense", "ic_saving", "#40E0D0"); // Sử dụng màu mới
        insertCategory(db, "Quỹ Khẩn Cấp", "Expense", "ic_emergency_fund", "#B22222"); // Sử dụng màu mới
        insertCategory(db, "Khác", "Expense", "ic_other", "#A9A9A9"); // Sử dụng màu mới

        // "Thêm" (Add) có thể là một nút UI chứ không phải danh mục trong DB.
        // Nếu nó là một danh mục, hãy uncomment dòng dưới đây:
        // insertCategory(db, "Thêm", "Expense", "ic_add_circle", "#FF00FF");

        // --- Danh mục Thu nhập ---
        insertCategory(db, "Lương", "Income", "ic_money", "#4CAF50");
        insertCategory(db, "Tiền thưởng", "Income", "ic_card_giftcard", "#FF69B4");
        insertCategory(db, "Thu nhập phụ", "Income", "ic_other_income", "#FFC107"); // Thêm danh mục thu nhập mới
        insertCategory(db, "Quà tặng nhận được", "Income", "ic_gift", "#9C27B0"); // Thêm danh mục thu nhập khác

        Log.d("DatabaseHelper", "Các danh mục mặc định đã được chèn.");
    }

    /**
     * Phương thức trợ giúp để chèn một danh mục vào cơ sở dữ liệu.
     * @param db Thể hiện của SQLiteDatabase.
     * @param name Tên danh mục.
     * @param type Loại danh mục (Expense hoặc Income).
     * @param iconName Tên tài nguyên drawable cho biểu tượng.
     * @param colorCode Mã màu hex cho danh mục.
     */
    private void insertCategory(SQLiteDatabase db, String name, String type, String iconName, String colorCode) {
        ContentValues cv = new ContentValues();
        // Không gán COLUMN_USER_ID_FK cho danh mục mặc định (sẽ là NULL)
        cv.put(COLUMN_CATEGORY_NAME, name);
        cv.put(COLUMN_CATEGORY_TYPE, type);
        cv.put(COLUMN_ICON_NAME, iconName);
        cv.put(COLUMN_COLOR_CODE, colorCode);
        long newRowId = db.insert(TABLE_CATEGORIES, null, cv);
        if (newRowId == -1) {
            Log.e("DatabaseHelper", "Không chèn được danh mục: " + name + " (có thể đã tồn tại do ràng buộc UNIQUE)");
        } else {
            Log.d("DatabaseHelper", "Đã chèn danh mục: " + name + " với ID: " + newRowId);
        }
    }


    /**
     * Truy xuất ID của một danh mục dựa trên tên, loại và User ID.
     * Ưu tiên tìm danh mục của người dùng cụ thể trước, sau đó tìm danh mục mặc định (userId IS NULL).
     * @param categoryName Tên của danh mục.
     * @param categoryType Loại của danh mục (Expense/Income).
     * @param userId ID của người dùng.
     * @return ID của danh mục, hoặc -1 nếu không tìm thấy.
     */
    public long getCategoryId(String categoryName, String categoryType, long userId) { // Thêm userId
        SQLiteDatabase db = this.getReadableDatabase();
        long categoryId = -1;
        String[] columns = {COLUMN_ID};
        String selection;
        String[] selectionArgs;
        Cursor cursor = null;

        try {
            // 1. Tìm danh mục của người dùng cụ thể (user_id = userId)
            selection = COLUMN_CATEGORY_NAME + " = ? AND " + COLUMN_CATEGORY_TYPE + " = ? AND " + COLUMN_USER_ID_FK + " = ?";
            selectionArgs = new String[]{categoryName, categoryType, String.valueOf(userId)};

            cursor = db.query(TABLE_CATEGORIES, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                Log.d("DatabaseHelper", "Tìm thấy ID danh mục (cho người dùng): " + categoryId + " cho '" + categoryName + "' loại '" + categoryType + "'");
            } else {
                // 2. Nếu không tìm thấy, tìm trong các danh mục mặc định (user_id IS NULL)
                if (cursor != null) {
                    cursor.close(); // Đóng cursor cũ trước khi mở cái mới
                }
                selection = COLUMN_CATEGORY_NAME + " = ? AND " + COLUMN_CATEGORY_TYPE + " = ? AND " + COLUMN_USER_ID_FK + " IS NULL";
                selectionArgs = new String[]{categoryName, categoryType};

                cursor = db.query(TABLE_CATEGORIES, columns, selection, selectionArgs, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    Log.d("DatabaseHelper", "Tìm thấy ID danh mục (mặc định): " + categoryId + " cho '" + categoryName + "' loại '" + categoryType + "'");
                } else {
                    Log.w("DatabaseHelper", "Không tìm thấy danh mục: '" + categoryName + "' (Loại: '" + categoryType + "'). Kiểm tra lại dữ liệu mặc định của cơ sở dữ liệu.");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy ID danh mục cho '" + categoryName + "': " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close(); // Đóng cơ sở dữ liệu nếu nó được mở cục bộ cho truy vấn này
            }
        }
        return categoryId;
    }


    /**
     * Cung cấp context ứng dụng được lưu trữ trong helper.
     * @return Context của ứng dụng.
     */
    public Context getContext() {
        return context;
    }
}