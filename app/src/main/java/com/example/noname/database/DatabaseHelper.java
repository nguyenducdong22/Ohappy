package com.example.noname.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expending_money.db";
    // Giữ nguyên phiên bản 1 hoặc tăng nếu bạn đã có dữ liệu với cấu trúc cũ
    // Nếu bạn tăng version, onUpgrade sẽ chạy. Đảm bảo bạn gỡ cài đặt ứng dụng
    // hoặc sử dụng logic ALTER TABLE nếu có dữ liệu cũ cần giữ.
    private static final int DATABASE_VERSION = 1;

    // Tên bảng
    public static final String TABLE_USERS = "users";

    // Cột bảng Users
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_FULL_NAME = "full_name"; // Cột mới
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_LAST_LOGIN = "last_login";

    // Câu lệnh tạo bảng Users - Đã loại bỏ username, thêm full_name
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," // EMAIL là định danh duy nhất và không được rỗng
            + COLUMN_FULL_NAME + " TEXT," // Có thể rỗng hoặc NOT NULL tùy ý bạn
            + COLUMN_PHONE_NUMBER + " TEXT," // Có thể rỗng hoặc NOT NULL tùy ý bạn
            + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " TEXT,"
            + COLUMN_LAST_LOGIN + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DatabaseHelper", "DatabaseHelper constructor called.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Creating table " + TABLE_USERS);
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade: (DEVELOPMENT MODE) Dropping table " + TABLE_USERS + " and recreating.");
        // Trong quá trình phát triển, chúng ta xóa bảng cũ và tạo lại.
        // TRONG ỨNG DỤNG THẬT, BẠN CẦN DI CHUYỂN DỮ LIỆU HOẶC SỬ DỤNG ALTER TABLE.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db); // Tạo lại bảng
    }
}