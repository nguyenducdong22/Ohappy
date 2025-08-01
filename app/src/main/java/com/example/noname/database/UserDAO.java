package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
    private Context context;

    private AccountDAO accountDAO;

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final String TAG = "UserDAO";

    public UserDAO(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
        accountDAO = new AccountDAO(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened for writing.");
    }

    public void close() {
        if (database != null && database.isOpen()) {
            dbHelper.close();
            Log.d(TAG, "Database closed.");
        }
    }

    /**
     * Thêm người dùng mới vào database.
     * @return ID của hàng mới được thêm vào, hoặc -1 nếu có lỗi.
     */
    public long createUser(String email, String fullName, String phoneNumber, String passwordHash) {
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
            open();
            userId = database.insert(DatabaseHelper.TABLE_USERS, null, values);
            if (userId != -1) {
                Log.d(TAG, "User created successfully with ID: " + userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage(), e);
        } finally {
            close();
        }
        return userId;
    }

    /**
     * Phương thức công khai để tạo các ví mặc định cho một người dùng.
     * @param userId ID của người dùng.
     */
    public void createDefaultWalletsForUser(long userId) {
        try {
            accountDAO.open();
            long cashWalletId = accountDAO.createAccount(userId, "Tiền mặt", 0.0, "Cash");
            if (cashWalletId != -1) {
                Log.d(TAG, "Default 'Tiền mặt' wallet created for user " + userId + " with ID: " + cashWalletId);
            } else {
                Log.e(TAG, "Failed to create 'Tiền mặt' wallet for user " + userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in createDefaultWalletsForUser: " + e.getMessage(), e);
        } finally {
            accountDAO.close();
        }
    }

    public Cursor getUserByEmail(String email) {
        Cursor cursor = null;
        try {
            open();
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
            Log.d(TAG, "Query getUserByEmail for: " + email + ", found " + (cursor != null ? cursor.getCount() : 0) + " rows.");
        }
        catch (Exception e) {
            Log.e(TAG, "Error getting user by email: " + e.getMessage());
        } finally {
            close();
        }
        return cursor;
    }

    public long getUserIdByEmail(String email) {
        Cursor cursor = null;
        long userId = -1;
        try {
            open();
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
                    Log.d(TAG, "Found user ID " + userId + " for email: " + email);
                } else {
                    Log.e(TAG, "COLUMN_ID not found in cursor for email: " + email);
                }
            } else {
                Log.d(TAG, "No user found for email: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID by email: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return userId;
    }

    public int updateLastLogin(long userId) {
        ContentValues values = new ContentValues();
        String currentTime = DATETIME_FORMAT.format(new Date());
        values.put(DatabaseHelper.COLUMN_LAST_LOGIN, currentTime);

        int rowsAffected = 0;
        try {
            open();
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            Log.d(TAG, "Updated last login for user ID: " + userId + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating last login: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected;
    }

    public boolean isEmailExists(String email) {
        Cursor cursor = null;
        try {
            open();
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );
            boolean exists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Email " + email + " exists: " + exists);
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
    }

    public int deleteUserByEmail(String email) {
        int rowsAffected = 0;
        try {
            open();
            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_USERS,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email}
            );
            Log.d(TAG, "Deleted user with email: " + email + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user by email: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected;
    }

    public int updatePassword(String email, String newPasswordHash) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD_HASH, newPasswordHash);
        int rowsAffected = 0;
        try {
            open();
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email}
            );
            Log.d(TAG, "Updated password for email: " + email);
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected;
    }

    public int updateUserEmail(String oldEmail, String newEmail) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, newEmail);
        int rowsAffected = 0;
        try {
            open();
            if (isEmailExists(newEmail)) {
                Log.e(TAG, "New email " + newEmail + " already exists.");
                return -1;
            }
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{oldEmail}
            );
            Log.d(TAG, "Updated email from " + oldEmail + " to " + newEmail);
        } catch (Exception e) {
            Log.e(TAG, "Error updating email: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected;
    }

    public int updateUserProfile(String email, String newFullName, String newPhoneNumber) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_FULL_NAME, newFullName);
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, newPhoneNumber);

        int rowsAffected = 0;
        try {
            open();
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email}
            );
            Log.d(TAG, "Updated profile for email: " + email + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating user profile: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected;
    }

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
            open();
            database.delete(DatabaseHelper.TABLE_OTP_TOKENS,
                    DatabaseHelper.COLUMN_USER_ID_FK + " = ? AND " + DatabaseHelper.COLUMN_IS_USED + " = 0",
                    new String[]{String.valueOf(userId)});

            rowId = database.insert(DatabaseHelper.TABLE_OTP_TOKENS, null, values);
            if (rowId != -1) {
                Log.d(TAG, "OTP " + otpCode + " created for user ID: " + userId + ", expires at: " + expiresAt);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating OTP: " + e.getMessage());
        } finally {
            close();
        }
        return rowId;
    }

    public boolean verifyOtpAndResetPassword(String email, String providedOtp, String newPassword) {
        Cursor userCursor = null;
        Cursor otpCursor = null;
        try {
            open();
            long userId = -1;
            userCursor = getUserByEmail(email);
            if (userCursor != null && userCursor.moveToFirst()) {
                userId = userCursor.getLong(userCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            } else {
                Log.w(TAG, "User not found for OTP verification: " + email);
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
                        Log.d(TAG, "OTP " + providedOtp + " for email " + email + " is valid and not expired.");

                        ContentValues userValues = new ContentValues();
                        userValues.put(DatabaseHelper.COLUMN_PASSWORD_HASH, PasswordHasher.hashPassword(newPassword));

                        int rowsAffected = database.update(
                                DatabaseHelper.TABLE_USERS,
                                userValues,
                                DatabaseHelper.COLUMN_ID + " = ?",
                                new String[]{String.valueOf(userId)}
                        );

                        if (rowsAffected > 0) {
                            Log.d(TAG, "Password reset successfully for " + email);
                            markOtpAsUsed(providedOtp);
                            return true;
                        } else {
                            Log.e(TAG, "Failed to update password for " + email + " after OTP verification.");
                            return false;
                        }
                    } else {
                        Log.w(TAG, "OTP " + providedOtp + " for email " + email + " has expired.");
                        return false;
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing expiration date for OTP: " + e.getMessage());
                    return false;
                }
            } else {
                Log.w(TAG, "OTP " + providedOtp + " not found, already used, or invalid for email: " + email);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying OTP and resetting password: " + e.getMessage());
            return false;
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
            if (otpCursor != null) {
                otpCursor.close();
            }
            close();
        }
    }

    public int markOtpAsUsed(String otpCode) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_USED, 1);

        int rowsAffected = 0;
        try {
            open();
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_OTP_TOKENS,
                    values,
                    DatabaseHelper.COLUMN_OTP_CODE + " = ?",
                    new String[]{otpCode}
            );
            Log.d(TAG, "OTP " + otpCode + " marked as used, rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error marking OTP as used: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected;
    }

    public int cleanUpExpiredOtps() {
        int rowsAffected = 0;
        try {
            open();
            String currentTime = DATETIME_FORMAT.format(new Date());
            String whereClause = DatabaseHelper.COLUMN_EXPIRES_AT + " < ? OR " +
                    DatabaseHelper.COLUMN_IS_USED + " = 1";

            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_OTP_TOKENS,
                    whereClause,
                    new String[]{currentTime}
            );
            Log.d(TAG, "Cleaned up " + rowsAffected + " expired/used OTPs.");
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up expired OTPs: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected;
    }
}