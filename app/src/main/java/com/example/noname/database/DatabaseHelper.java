package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.noname.R; // Used to access drawable resources for default categories.

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DatabaseHelper manages the creation, upgrading, and overall schema of the SQLite database.
 * It defines table and column names, and handles the initial population of default data.
 * Specific data operations (CRUD) are delegated to individual DAO classes (e.g., BudgetDAO).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "expending_money.db";
    private static final int DATABASE_VERSION = 5; // Increment this for schema changes to trigger onUpgrade().

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_RECURRING_TRANSACTIONS = "recurring_transactions";
    public static final String TABLE_SAVINGS_GOALS = "savings_goals";
    public static final String TABLE_OTP_TOKENS = "otp_tokens";

    // Common Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // USERS Table Columns
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_LAST_LOGIN = "last_login";

    // ACCOUNTS Table Columns
    public static final String COLUMN_ACCOUNT_NAME = "name";
    public static final String COLUMN_BALANCE = "balance";
    public static final String COLUMN_CURRENCY = "currency";
    public static final String COLUMN_ACCOUNT_TYPE = "type";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    // CATEGORIES Table Columns
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_TYPE = "type";
    public static final String COLUMN_ICON_NAME = "icon_name";
    public static final String COLUMN_COLOR_CODE = "color_code";

    // TRANSACTIONS Table Columns
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_TYPE = "type";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_NOTES = "notes";

    // BUDGETS Table Columns
    public static final String COLUMN_BUDGET_AMOUNT = "amount";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_IS_RECURRING = "is_recurring";

    // RECURRING_TRANSACTIONS Table Columns
    public static final String COLUMN_FREQUENCY_TYPE = "frequency_type";
    public static final String COLUMN_FREQUENCY_VALUE = "frequency_value";
    public static final String COLUMN_LAST_GENERATED_DATE = "last_generated_date";

    // SAVINGS_GOALS Table Columns
    public static final String COLUMN_GOAL_NAME = "name";
    public static final String COLUMN_TARGET_AMOUNT = "target_amount";
    public static final String COLUMN_CURRENT_AMOUNT = "current_amount";
    public static final String COLUMN_TARGET_DATE = "target_date";
    public static final String COLUMN_IS_COMPLETED = "is_completed";

    // OTP_TOKENS Table Columns
    public static final String COLUMN_OTP_CODE = "otp_code";
    public static final String COLUMN_EXPIRES_AT = "expires_at";
    public static final String COLUMN_IS_USED = "is_used";

    // Foreign Key Columns
    public static final String COLUMN_USER_ID_FK = "user_id";
    public static final String COLUMN_ACCOUNT_ID_FK = "account_id";
    public static final String COLUMN_CATEGORY_ID_FK = "category_id";

    // Context reference needed for accessing resources like drawables.
    private Context context;

    /**
     * Constructor for DatabaseHelper.
     * @param context Application context, used for database operations and resource access.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; // Store context for later use.
        Log.d("DatabaseHelper", "DatabaseHelper constructor called. Version: " + DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * Executes all CREATE TABLE SQL statements.
     * @param db The SQLiteDatabase instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Creating all tables.");

        // Execute CREATE TABLE statements in dependency order.
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_RECURRING_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_SAVINGS_GOALS);
        db.execSQL(CREATE_TABLE_OTP_TOKENS);

        // Add default categories after the CATEGORIES table is created.
        addDefaultCategories(db);
        Log.d("DatabaseHelper", "onCreate: All tables created and default categories added.");
    }

    /**
     * Called when the database needs to be upgraded (DATABASE_VERSION increases).
     * This implementation drops all tables and recreates them, leading to data loss.
     * For production apps, use ALTER TABLE statements for data migration.
     * @param db The SQLiteDatabase instance.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);
        Log.w("DatabaseHelper", "WARNING: All existing data will be DELETED during this upgrade!");

        // Drop tables in reverse foreign key order to avoid integrity errors.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTP_TOKENS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Recreate all tables with the new schema.
        onCreate(db);
        Log.d("DatabaseHelper", "onUpgrade: Database tables recreated with new schema.");
    }

    /**
     * Called when the database is opened. Ensures foreign key constraints are enabled.
     * @param db The SQLiteDatabase instance.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;"); // Enable foreign key enforcement.
            Log.d("DatabaseHelper", "onOpen: Foreign keys enabled.");
        }
    }

    // --- CREATE TABLE STATEMENTS ---

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
            + COLUMN_USER_ID_FK + " INTEGER," // Can be null for default categories.
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
     * Adds predefined default categories to the CATEGORIES table.
     * These categories are available to all users by default.
     * @param db The SQLiteDatabase instance for insertion.
     */
    private void addDefaultCategories(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        Log.d("DatabaseHelper", "Adding default categories...");

        // Expense Categories
        cv.put(COLUMN_CATEGORY_NAME, "Ăn uống"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_food"); cv.put(COLUMN_COLOR_CODE, "#FF5733"); db.insert(TABLE_CATEGORIES, null, cv); cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Thuê nhà"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_house"); cv.put(COLUMN_COLOR_CODE, "#337CFF"); db.insert(TABLE_CATEGORIES, null, cv); cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Mua sắm"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_shopping_basket"); cv.put(COLUMN_COLOR_CODE, "#FFD700"); db.insert(TABLE_CATEGORIES, null, cv); cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Đi lại"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_directions_car"); cv.put(COLUMN_COLOR_CODE, "#8A2BE2"); db.insert(TABLE_CATEGORIES, null, cv); cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Giải trí"); cv.put(COLUMN_CATEGORY_TYPE, "Expense"); cv.put(COLUMN_ICON_NAME, "ic_movie"); cv.put(COLUMN_COLOR_CODE, "#FF1493"); db.insert(TABLE_CATEGORIES, null, cv); cv.clear();

        // Income Categories
        cv.put(COLUMN_CATEGORY_NAME, "Lương"); cv.put(COLUMN_CATEGORY_TYPE, "Income"); cv.put(COLUMN_ICON_NAME, "ic_money"); cv.put(COLUMN_COLOR_CODE, "#4CAF50"); db.insert(TABLE_CATEGORIES, null, cv); cv.clear();
        cv.put(COLUMN_CATEGORY_NAME, "Tiền thưởng"); cv.put(COLUMN_CATEGORY_TYPE, "Income"); cv.put(COLUMN_ICON_NAME, "ic_card_giftcard"); cv.put(COLUMN_COLOR_CODE, "#FF69B4"); db.insert(TABLE_CATEGORIES, null, cv); cv.clear();

        Log.d("DatabaseHelper", "Default categories inserted.");
    }

    /**
     * Retrieves the ID of a category based on its name and type.
     * @param categoryName The name of the category.
     * @param categoryType The type of the category (Expense/Income).
     * @return The ID of the category, or -1 if not found.
     */
    public long getCategoryId(String categoryName, String categoryType) {
        SQLiteDatabase db = this.getReadableDatabase();
        long categoryId = -1;
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_CATEGORY_NAME + " = ? AND " + COLUMN_CATEGORY_TYPE + " = ?";
        String[] selectionArgs = {categoryName, categoryType};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CATEGORIES, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                Log.d("DatabaseHelper", "Found category ID: " + categoryId + " for '" + categoryName + "'");
            } else {
                Log.w("DatabaseHelper", "Category not found: '" + categoryName + "' (" + categoryType + ")");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting category ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return categoryId;
    }

    /**
     * Provides the application context stored in the helper.
     * @return The application context.
     */
    public Context getContext() {
        return context;
    }
}