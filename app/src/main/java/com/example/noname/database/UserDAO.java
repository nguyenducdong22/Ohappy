package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.utils.PasswordHasher;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UserDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d("UserDAO", "Database opened for writing.");
        } catch (Exception e) {
            Log.e("UserDAO", "Error opening database for writing: " + e.getMessage());
        }
    }

    public void close() {
        dbHelper.close();
        Log.d("UserDAO", "Database closed.");
    }

    /**
     * Thêm người dùng mới vào database (đã bỏ câu hỏi bảo mật).
     *
     * @param email Địa chỉ email của người dùng (duy nhất).
     * @param fullName Tên đầy đủ của người dùng.
     * @param phoneNumber Số điện thoại của người dùng.
     * @param passwordHash Mật khẩu đã được băm (hashed).
     * @return ID của hàng mới được thêm vào, hoặc -1 nếu có lỗi.
     */
    public long createUser(String email, String fullName, String phoneNumber,
                           String passwordHash) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_FULL_NAME, fullName);
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(DatabaseHelper.COLUMN_PASSWORD_HASH, passwordHash);

        String currentTime = DATETIME_FORMAT.format(new Date());
        values.put(DatabaseHelper.COLUMN_CREATED_AT, currentTime);
        values.put(DatabaseHelper.COLUMN_LAST_LOGIN, currentTime);

        long userId = -1;
        try {
            userId = database.insert(DatabaseHelper.TABLE_USERS, null, values);
            Log.d("UserDAO", "User created with ID: " + userId);
        } catch (Exception e) {
            Log.e("UserDAO", "Error creating user: " + e.getMessage());
        }
        return userId;
    }

    /**
     * Lấy thông tin người dùng theo địa chỉ email (đã bỏ cột câu hỏi bảo mật).
     *
     * @param email Địa chỉ email cần tìm.
     * @return Cursor chứa thông tin người dùng, hoặc null nếu không tìm thấy.
     */
    public Cursor getUserByEmail(String email) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{
                            DatabaseHelper.COLUMN_ID,
                            DatabaseHelper.COLUMN_EMAIL,
                            DatabaseHelper.COLUMN_FULL_NAME,
                            DatabaseHelper.COLUMN_PHONE_NUMBER,
                            DatabaseHelper.COLUMN_PASSWORD_HASH
                    },
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );
            Log.d("UserDAO", "Query getUserByEmail for: " + email + ", found " + (cursor != null ? cursor.getCount() : 0) + " rows.");
        }
        catch (Exception e) {
            Log.e("UserDAO", "Error getting user by email: " + e.getMessage());
        }
        return cursor;
    }

    /**
     * Lấy ID người dùng dựa trên địa chỉ email.
     *
     * @param email Địa chỉ email của người dùng.
     * @return ID của người dùng nếu tìm thấy, hoặc -1 nếu không tìm thấy hoặc có lỗi.
     */
    public long getUserIdByEmail(String email) {
        Cursor cursor = null;
        long userId = -1;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                if (idColumnIndex != -1) {
                    userId = cursor.getLong(idColumnIndex);
                    Log.d("UserDAO", "Found user ID " + userId + " for email: " + email);
                } else {
                    Log.e("UserDAO", "COLUMN_ID not found in cursor for email: " + email);
                }
            } else {
                Log.d("UserDAO", "No user found for email: " + email);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting user ID by email: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userId;
    }

    public int updateLastLogin(long userId) {
        ContentValues values = new ContentValues();
        String currentTime = DATETIME_FORMAT.format(new Date());
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

    public boolean isEmailExists(String email) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );
            boolean exists = cursor != null && cursor.getCount() > 0;
            Log.d("UserDAO", "Email " + email + " exists: " + exists);
            return exists;
        } catch (Exception e) {
            Log.e("UserDAO", "Error checking email existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int deleteUserByEmail(String email) {
        int rowsAffected = 0;
        try {
            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_USERS,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email}
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

    public int updateUserEmail(String oldEmail, String newEmail) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, newEmail);
        int rowsAffected = 0;
        try {
            if (isEmailExists(newEmail)) {
                Log.e("UserDAO", "New email " + newEmail + " already exists.");
                return -1;
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

    /**
     * Tạo và lưu một mã OTP mới cho người dùng.
     *
     * @param userId ID của người dùng.
     * @param otpCode Mã OTP được tạo ngẫu nhiên.
     * @param expiresInMinutes Thời gian OTP hết hạn tính bằng phút.
     * @return ID của hàng mới được thêm vào, hoặc -1 nếu có lỗi.
     */
    public long createOtpForUser(long userId, String otpCode, int expiresInMinutes) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_OTP_CODE, otpCode);

        String currentTime = DATETIME_FORMAT.format(new Date());
        values.put(DatabaseHelper.COLUMN_CREATED_AT, currentTime);

        Date expiresAtDate = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiresInMinutes));
        String expiresAt = DATETIME_FORMAT.format(expiresAtDate);
        values.put(DatabaseHelper.COLUMN_EXPIRES_AT, expiresAt);

        values.put(DatabaseHelper.COLUMN_IS_USED, 0);

        long rowId = -1;
        try {
            database.delete(DatabaseHelper.TABLE_OTP_TOKENS,
                    DatabaseHelper.COLUMN_USER_ID_FK + " = ? AND " + DatabaseHelper.COLUMN_IS_USED + " = 0",
                    new String[]{String.valueOf(userId)});

            rowId = database.insert(DatabaseHelper.TABLE_OTP_TOKENS, null, values);
            if (rowId != -1) {
                Log.d("UserDAO", "OTP " + otpCode + " created for user ID: " + userId + ", expires at: " + expiresAt);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error creating OTP: " + e.getMessage());
        }
        return rowId;
    }

    /**
     * Xác minh mã OTP và đặt lại mật khẩu của người dùng.
     * Phương thức này kiểm tra tính hợp lệ của OTP và cập nhật mật khẩu.
     *
     * @param email Email của người dùng.
     * @param providedOtp Mã OTP do người dùng nhập.
     * @param newPassword Mật khẩu mới chưa được băm.
     * @return true nếu OTP hợp lệ và mật khẩu được cập nhật thành công, false nếu ngược lại.
     */
    public boolean verifyOtpAndResetPassword(String email, String providedOtp, String newPassword) {
        Cursor userCursor = null;
        Cursor otpCursor = null;
        try {
            long userId = -1;
            userCursor = getUserByEmail(email);
            if (userCursor != null && userCursor.moveToFirst()) {
                userId = userCursor.getLong(userCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            } else {
                Log.w("UserDAO", "User not found for OTP verification: " + email);
                return false;
            }

            String selection = DatabaseHelper.COLUMN_USER_ID_FK + " = ? AND " +
                    DatabaseHelper.COLUMN_OTP_CODE + " = ? AND " +
                    DatabaseHelper.COLUMN_IS_USED + " = 0";

            String[] selectionArgs = {String.valueOf(userId), providedOtp};

            otpCursor = database.query(
                    DatabaseHelper.TABLE_OTP_TOKENS,
                    new String[]{DatabaseHelper.COLUMN_EXPIRES_AT},
                    selection,
                    selectionArgs,
                    null, null, DatabaseHelper.COLUMN_CREATED_AT + " DESC", "1"
            );

            if (otpCursor != null && otpCursor.moveToFirst()) {
                String expiresAtString = otpCursor.getString(otpCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPIRES_AT));
                try {
                    Date expiresAt = DATETIME_FORMAT.parse(expiresAtString);
                    Date now = new Date();

                    if (now.before(expiresAt)) {
                        Log.d("UserDAO", "OTP " + providedOtp + " for email " + email + " is valid and not expired.");

                        ContentValues userValues = new ContentValues();
                        userValues.put(DatabaseHelper.COLUMN_PASSWORD_HASH, PasswordHasher.hashPassword(newPassword));

                        int rowsAffected = database.update(
                                DatabaseHelper.TABLE_USERS,
                                userValues,
                                DatabaseHelper.COLUMN_ID + " = ?",
                                new String[]{String.valueOf(userId)}
                        );

                        if (rowsAffected > 0) {
                            Log.d("UserDAO", "Password reset successfully for " + email);
                            markOtpAsUsed(providedOtp);
                            return true;
                        } else {
                            Log.e("UserDAO", "Failed to update password for " + email + " after OTP verification.");
                            return false;
                        }
                    } else {
                        Log.w("UserDAO", "OTP " + providedOtp + " for email " + email + " has expired.");
                        return false;
                    }
                } catch (ParseException e) {
                    Log.e("UserDAO", "Error parsing expiration date for OTP: " + e.getMessage());
                    return false;
                }
            } else {
                Log.w("UserDAO", "OTP " + providedOtp + " not found, already used, or invalid for email: " + email);
                return false;
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error verifying OTP and resetting password: " + e.getMessage());
            return false;
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
            if (otpCursor != null) {
                otpCursor.close();
            }
        }
    }

    /**
     * Đánh dấu một OTP là đã sử dụng.
     *
     * @param otpCode Mã OTP cần đánh dấu.
     * @return Số hàng bị ảnh hưởng.
     */
    public int markOtpAsUsed(String otpCode) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_USED, 1);

        int rowsAffected = 0;
        try {
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_OTP_TOKENS,
                    values,
                    DatabaseHelper.COLUMN_OTP_CODE + " = ?",
                    new String[]{otpCode}
            );
            Log.d("UserDAO", "OTP " + otpCode + " marked as used, rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e("UserDAO", "Error marking OTP as used: " + e.getMessage());
        }
        return rowsAffected;
    }

    /**
     * Xóa các OTP đã hết hạn hoặc đã sử dụng (để dọn dẹp database).
     * Nên được gọi định kỳ (ví dụ: khi ứng dụng khởi động).
     *
     * @return Số hàng đã bị xóa.
     */
    public int cleanUpExpiredOtps() {
        int rowsAffected = 0;
        try {
            String currentTime = DATETIME_FORMAT.format(new Date());
            String whereClause = DatabaseHelper.COLUMN_EXPIRES_AT + " < ? OR " +
                    DatabaseHelper.COLUMN_IS_USED + " = 1";

            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_OTP_TOKENS,
                    whereClause,
                    new String[]{currentTime}
            );
            Log.d("UserDAO", "Cleaned up " + rowsAffected + " expired/used OTPs.");
        } catch (Exception e) {
            Log.e("UserDAO", "Error cleaning up expired OTPs: " + e.getMessage());
        }
        return rowsAffected;
    }
}