package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionDAO {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context context;

    private static final String TAG = "TransactionDAO";
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public TransactionDAO(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened for writing.");
    }

    public void close() {
        dbHelper.close();
        Log.d(TAG, "Database closed.");
    }

    public long addTransaction(long userId, long accountId, long categoryId, double amount, String type, String date, String description, String note) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_ACCOUNT_ID_FK, accountId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_TYPE, type);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_DATE, date);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_NOTES, note);
        values.put(DatabaseHelper.COLUMN_CREATED_AT, DATETIME_FORMAT.format(new Date()));

        long insertId = -1;
        try {
            insertId = database.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
            Log.d(TAG, "Transaction added with ID: " + insertId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding transaction: " + e.getMessage(), e);
        }
        return insertId;
    }

    /**
     * Lấy danh sách giao dịch của một người dùng trong một khoảng thời gian cụ thể.
     * Phương thức này sử dụng JOIN để lấy tên danh mục và tên icon.
     * @param userId ID của người dùng.
     * @param accountId ID của tài khoản.
     * @param startDate Ngày bắt đầu (định dạng YYYY-MM-DD).
     * @param endDate Ngày kết thúc (định dạng YYYY-MM-DD).
     * @return Danh sách các đối tượng Transaction.
     */
    public List<Transaction> getTransactionsByDateRange(long userId, long accountId, String startDate, String endDate) {
        List<Transaction> transactions = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query = "SELECT T.*, C." + DatabaseHelper.COLUMN_CATEGORY_NAME + ", C." + DatabaseHelper.COLUMN_ICON_NAME
                    + " FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " T"
                    + " INNER JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON T." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = C." + DatabaseHelper.COLUMN_ID
                    + " WHERE T." + DatabaseHelper.COLUMN_USER_ID_FK + " = ?"
                    + " AND T." + DatabaseHelper.COLUMN_ACCOUNT_ID_FK + " = ?"
                    + " AND T." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?"
                    + " ORDER BY T." + DatabaseHelper.COLUMN_TRANSACTION_DATE + " DESC, T." + DatabaseHelper.COLUMN_CREATED_AT + " DESC";

            String[] selectionArgs = {String.valueOf(userId), String.valueOf(accountId), startDate, endDate};

            cursor = database.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Transaction transaction = cursorToTransaction(cursor);
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting transactions by date range: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return transactions;
    }

    /**
     * Lấy số dư của một tài khoản trước một ngày cụ thể.
     * @param accountId ID của tài khoản.
     * @param date Ngày (định dạng YYYY-MM-DD).
     * @return Số dư trước ngày đó.
     */
    public double getAccountBalanceBeforeDate(long accountId, String date) {
        double balance = 0.0;
        Cursor cursor = null;
        try {
            String query = "SELECT SUM(CASE WHEN " + DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = 'Income' THEN " + DatabaseHelper.COLUMN_AMOUNT + " ELSE -" + DatabaseHelper.COLUMN_AMOUNT + " END)"
                    + " FROM " + DatabaseHelper.TABLE_TRANSACTIONS
                    + " WHERE " + DatabaseHelper.COLUMN_ACCOUNT_ID_FK + " = ?"
                    + " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " < ?";

            String[] selectionArgs = {String.valueOf(accountId), date};

            cursor = database.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                balance = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating balance before date: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return balance;
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction transaction = new Transaction();
        transaction.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        transaction.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK)));
        transaction.setAccountId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACCOUNT_ID_FK)));
        transaction.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID_FK)));
        transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)));
        transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_TYPE)));
        transaction.setTransactionDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DATE)));
        transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
        transaction.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES)));
        transaction.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));

        // Lấy dữ liệu từ bảng categories đã được JOIN
        int categoryNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_NAME);
        if (categoryNameIndex != -1) {
            transaction.setCategoryName(cursor.getString(categoryNameIndex));
        }

        int iconNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ICON_NAME);
        if (iconNameIndex != -1) {
            transaction.setIconName(cursor.getString(iconNameIndex));
        }

        return transaction;
    }
}