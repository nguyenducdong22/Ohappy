package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        try {
            database = dbHelper.getWritableDatabase(); // Mở kết nối để ghi và đọc
            Log.d("UserDAO", "Database opened for writing.");
        } catch (Exception e) {
            Log.e("UserDAO", "Error opening database for writing: " + e.getMessage());
        }
    }

    public void close() {
        dbHelper.close();
        Log.d("UserDAO", "Database closed.");
    }

    // --- Các phương thức CRUD cho Users ---

    /**
     * Thêm người dùng mới vào database.
     *
     * @param email        Địa chỉ email của người dùng (duy nhất).
     * @param fullName     Tên đầy đủ của người dùng.
     * @param phoneNumber  Số điện thoại của người dùng.
     * @param passwordHash Mật khẩu đã được băm (hashed).
     * @return ID của hàng mới được thêm vào, hoặc -1 nếu có lỗi.
     */
    public long createUser(String email, String fullName, String phoneNumber, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_FULL_NAME, fullName);
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(DatabaseHelper.COLUMN_PASSWORD_HASH, passwordHash);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        values.put(DatabaseHelper.COLUMN_CREATED_AT, currentTime);
        values.put(DatabaseHelper.COLUMN_LAST_LOGIN, currentTime);

        long userId = -1;
        try {
            userId = database.insert(DatabaseHelper.TABLE_USERS, null, values);
            Log.d("UserDAO", "User created with ID: " + userId);
        } catch (Exception e) {
            // Ghi log lỗi nếu không thể chèn dữ liệu (ví dụ: email đã tồn tại do ràng buộc UNIQUE)
            Log.e("UserDAO", "Error creating user: " + e.getMessage());
        }
        return userId;
    }

    /**
     * Lấy thông tin người dùng theo địa chỉ email. Đây là phương thức chính dùng để đăng nhập.
     *
     * @param email Địa chỉ email cần tìm.
     * @return Cursor chứa thông tin người dùng, hoặc null nếu không tìm thấy hoặc có lỗi.
     */
    public Cursor getUserByEmail(String email) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS, // Tên bảng
                    new String[]{                 // Các cột muốn lấy
                            DatabaseHelper.COLUMN_ID,
                            DatabaseHelper.COLUMN_EMAIL,
                            DatabaseHelper.COLUMN_FULL_NAME,
                            DatabaseHelper.COLUMN_PHONE_NUMBER,
                            DatabaseHelper.COLUMN_PASSWORD_HASH
                    },
                    DatabaseHelper.COLUMN_EMAIL + " = ?", // Mệnh đề WHERE
                    new String[]{email},             // Giá trị cho WHERE clause
                    null, null, null                 // groupBy, having, orderBy
            );
            Log.d("UserDAO", "Query getUserByEmail for: " + email + ", found " + (cursor != null ? cursor.getCount() : 0) + " rows.");
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting user by email: " + e.getMessage());
        }
        return cursor;
    }

    /**
     * Cập nhật thời gian đăng nhập cuối cùng của người dùng.
     *
     * @param userId ID của người dùng.
     * @return Số hàng bị ảnh hưởng.
     */
    public int updateLastLogin(long userId) {
        ContentValues values = new ContentValues();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        values.put(DatabaseHelper.COLUMN_LAST_LOGIN, currentTime);

        int rowsAffected = 0;
        try {
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            Log.d("UserDAO", "Updated last login for user ID: " + userId + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating last login: " + e.getMessage());
        }
        return rowsAffected;
    }

    /**
     * Kiểm tra xem một địa chỉ email đã tồn tại trong database chưa.
     *
     * @param email Địa chỉ email cần kiểm tra.
     * @return true nếu email tồn tại, false nếu không hoặc có lỗi.
     */
    public boolean isEmailExists(String email) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID}, // Chỉ cần lấy ID để kiểm tra sự tồn tại
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );
            boolean exists = cursor != null && cursor.getCount() > 0;
            Log.d("UserDAO", "Email " + email + " exists: " + exists);
            return exists;
        } catch (Exception e) {
            Log.e("UserDAO", "Error checking email existence: " + e.getMessage());
            return false; // Trả về false nếu có lỗi xảy ra
        } finally {
            if (cursor != null) {
                cursor.close(); // Đảm bảo đóng Cursor để tránh rò rỉ bộ nhớ
            }
        }
    }

    public int deleteUserByEmail(String email) {
        int rowsAffected = 0;
        try {
            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_USERS, // Tên bảng
                    DatabaseHelper.COLUMN_EMAIL + " = ?", // Mệnh đề WHERE
                    new String[]{email} // Giá trị cho WHERE
            );
            Log.d("UserDAO", "Deleted user with email: " + email + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e("UserDAO", "Error deleting user by email: " + e.getMessage());
        }
        return rowsAffected;
    }

    public int updatePassword(String email, String newPasswordHash) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD_HASH, newPasswordHash);
        int rowsAffected = 0;
        try {
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email}
            );
            Log.d("UserDAO", "Updated password for email: " + email);
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating password: " + e.getMessage());
        }
        return rowsAffected;
    }

    /**
     * Cập nhật địa chỉ email cho người dùng.
     * LƯU Ý: Việc này cần được xử lý cẩn thận vì email là định danh duy nhất.
     *
     * @param oldEmail Email cũ của người dùng.
     * @param newEmail Email mới của người dùng.
     * @return Số hàng bị ảnh hưởng.
     */
    public int updateUserEmail(String oldEmail, String newEmail) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, newEmail);
        int rowsAffected = 0;
        try {
            // Trước tiên, kiểm tra xem email mới đã tồn tại chưa
            if (isEmailExists(newEmail)) {
                Log.e("UserDAO", "New email " + newEmail + " already exists.");
                return -1; // Trả về -1 để báo hiệu email đã tồn tại
            }
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{oldEmail}
            );
            Log.d("UserDAO", "Updated email from " + oldEmail + " to " + newEmail);
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating email: " + e.getMessage());
        }
        return rowsAffected;
    }
    /**
     * Cập nhật thông tin hồ sơ của người dùng (tên đầy đủ, số điện thoại).
     * @param email Email của người dùng cần cập nhật (dùng để xác định).
     * @param newFullName Tên đầy đủ mới.
     * @param newPhoneNumber Số điện thoại mới.
     * @return Số hàng bị ảnh hưởng (thường là 1 nếu thành công).
     */
    public int updateUserProfile(String email, String newFullName, String newPhoneNumber) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_FULL_NAME, newFullName);
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, newPhoneNumber);

        int rowsAffected = 0;
        try {
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email}
            );
            Log.d("UserDAO", "Updated profile for email: " + email + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating user profile: " + e.getMessage());
        }
        return rowsAffected;
    }


}