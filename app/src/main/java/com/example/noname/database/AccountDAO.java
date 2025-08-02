package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.models.Account;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccountDAO {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context context;

    private static final String TAG = "AccountDAO";

    private static final String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_USER_ID_FK,
            DatabaseHelper.COLUMN_ACCOUNT_NAME,
            DatabaseHelper.COLUMN_BALANCE,
            DatabaseHelper.COLUMN_CURRENCY,
            DatabaseHelper.COLUMN_ACCOUNT_TYPE,
            DatabaseHelper.COLUMN_IS_ACTIVE,
            DatabaseHelper.COLUMN_CREATED_AT
    };

    public AccountDAO(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
            Log.d(TAG, "Database opened for writing.");
        }
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
            Log.d(TAG, "Database closed.");
        }
        dbHelper.close();
    }

    /**
     * Tạo một tài khoản mới cho người dùng.
     * @param userId ID của người dùng sở hữu tài khoản.
     * @param accountName Tên tài khoản (ví dụ: "Tiền mặt", "Ví Momo").
     * @param initialBalance Số dư ban đầu.
     * @param accountType Loại tài khoản.
     * @return ID của tài khoản mới được tạo, hoặc -1 nếu có lỗi.
     */
    public long createAccount(long userId, String accountName, double initialBalance, String accountType) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_ACCOUNT_NAME, accountName);
        values.put(DatabaseHelper.COLUMN_BALANCE, initialBalance);
        values.put(DatabaseHelper.COLUMN_CURRENCY, "VND");
        values.put(DatabaseHelper.COLUMN_ACCOUNT_TYPE, accountType);
        values.put(DatabaseHelper.COLUMN_IS_ACTIVE, 1);
        values.put(DatabaseHelper.COLUMN_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        long accountId = -1;
        try {
            accountId = database.insert(DatabaseHelper.TABLE_ACCOUNTS, null, values);
            Log.d(TAG, "Account created with ID: " + accountId);
        } catch (Exception e) {
            Log.e(TAG, "Error creating account: " + e.getMessage(), e);
        }
        return accountId;
    }

    /**
     * Cập nhật thông tin của một tài khoản hiện có.
     * @param accountId ID của tài khoản cần cập nhật.
     * @param newName Tên mới của ví.
     * @param newBalance Số dư mới.
     * @return Số hàng bị ảnh hưởng.
     */
    public int updateAccount(long accountId, String newName, double newBalance) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ACCOUNT_NAME, newName);
        values.put(DatabaseHelper.COLUMN_BALANCE, newBalance);

        int rowsAffected = database.update(
                DatabaseHelper.TABLE_ACCOUNTS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(accountId)}
        );

        Log.d(TAG, "Updated account ID: " + accountId + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    public List<Account> getAllAccountsByUserId(long userId) {
        List<Account> accounts = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_ACCOUNTS,
                allColumns,
                DatabaseHelper.COLUMN_USER_ID_FK + " = ? AND " + DatabaseHelper.COLUMN_IS_ACTIVE + " = 1",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Account account = cursorToAccount(cursor);
            accounts.add(account);
            cursor.moveToNext();
        }
        cursor.close();
        return accounts;
    }

    public Account getAccountById(long accountId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_ACCOUNTS,
                allColumns,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(accountId)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            Account account = cursorToAccount(cursor);
            cursor.close();
            return account;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public boolean updateAccountBalance(long accountId, double amount, String transactionType) {
        ContentValues values = new ContentValues();
        Account currentAccount = getAccountById(accountId);
        if (currentAccount == null) {
            Log.e(TAG, "Tài khoản không tồn tại.");
            return false;
        }

        double newBalance = currentAccount.getBalance();
        if ("Income".equals(transactionType)) {
            newBalance += amount;
        } else if ("Expense".equals(transactionType)) {
            newBalance -= amount;
        } else {
            Log.e(TAG, "Loại giao dịch không hợp lệ: " + transactionType);
            return false;
        }

        values.put(DatabaseHelper.COLUMN_BALANCE, newBalance);
        int rowsAffected = database.update(
                DatabaseHelper.TABLE_ACCOUNTS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(accountId)}
        );

        return rowsAffected > 0;
    }

    private Account cursorToAccount(Cursor cursor) {
        Account account = new Account();
        account.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        account.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK)));
        account.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACCOUNT_NAME)));
        account.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BALANCE)));
        account.setCurrency(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CURRENCY)));
        account.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACCOUNT_TYPE)));
        account.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE)) == 1);
        account.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));
        return account;
    }
}