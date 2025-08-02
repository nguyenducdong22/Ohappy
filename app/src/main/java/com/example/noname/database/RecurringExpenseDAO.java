package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.models.RecurringExpense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context context;

    private static final String TAG = "RecurringExpenseDAO";

    public RecurringExpenseDAO(Context context) {
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

    public long addRecurringExpense(long userId, long accountId, long categoryId, String name, double amount, String transactionType, String frequency, int frequencyValue, String startDate, String endDate, String lastGeneratedDate, boolean isActive) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_ACCOUNT_ID_FK, accountId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, name);
        values.put(DatabaseHelper.COLUMN_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_TYPE, transactionType);
        values.put(DatabaseHelper.COLUMN_FREQUENCY_TYPE, frequency);
        values.put(DatabaseHelper.COLUMN_FREQUENCY_VALUE, frequencyValue);
        values.put(DatabaseHelper.COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_LAST_GENERATED_DATE, lastGeneratedDate);
        values.put(DatabaseHelper.COLUMN_IS_ACTIVE, isActive ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        long insertId = -1;
        try {
            insertId = database.insertOrThrow(DatabaseHelper.TABLE_RECURRING_TRANSACTIONS, null, values);
            Log.d(TAG, "Recurring expense added with ID: " + insertId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding recurring expense: " + e.getMessage(), e);
        }
        return insertId;
    }

    public int updateRecurringExpense(long expenseId, long userId, long accountId, long categoryId, String name, double amount, String transactionType, String frequency, int frequencyValue, String startDate, String endDate, String lastGeneratedDate, boolean isActive) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_ACCOUNT_ID_FK, accountId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, name);
        values.put(DatabaseHelper.COLUMN_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_TRANSACTION_TYPE, transactionType);
        values.put(DatabaseHelper.COLUMN_FREQUENCY_TYPE, frequency);
        values.put(DatabaseHelper.COLUMN_FREQUENCY_VALUE, frequencyValue);
        values.put(DatabaseHelper.COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_LAST_GENERATED_DATE, lastGeneratedDate);
        values.put(DatabaseHelper.COLUMN_IS_ACTIVE, isActive ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        int rowsAffected = 0;
        try {
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_RECURRING_TRANSACTIONS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?",
                    new String[]{String.valueOf(expenseId), String.valueOf(userId)}
            );
            Log.d(TAG, "Recurring expense updated. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating recurring expense: " + e.getMessage(), e);
        }
        return rowsAffected;
    }

    public boolean deleteRecurringExpense(long expenseId, long userId) {
        int rowsAffected = 0;
        try {
            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_RECURRING_TRANSACTIONS,
                    DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?",
                    new String[]{String.valueOf(expenseId), String.valueOf(userId)}
            );
            Log.d(TAG, "Recurring expense deleted. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting recurring expense: " + e.getMessage(), e);
        }
        return rowsAffected > 0;
    }

    public RecurringExpense getRecurringExpenseById(long expenseId, long userId) {
        Cursor cursor = null;
        RecurringExpense expense = null;
        try {
            String query = "SELECT T.*"
                    + " FROM " + DatabaseHelper.TABLE_RECURRING_TRANSACTIONS + " T"
                    + " WHERE T." + DatabaseHelper.COLUMN_ID + " = ? AND T." + DatabaseHelper.COLUMN_USER_ID_FK + " = ?";

            cursor = database.rawQuery(query, new String[]{String.valueOf(expenseId), String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                expense = cursorToRecurringExpense(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting recurring expense by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return expense;
    }

    public List<RecurringExpense> getAllRecurringExpenses(long userId) {
        List<RecurringExpense> expenses = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query = "SELECT T.*, C." + DatabaseHelper.COLUMN_CATEGORY_NAME + " AS category_name, C." + DatabaseHelper.COLUMN_ICON_NAME + " AS icon_name"
                    + " FROM " + DatabaseHelper.TABLE_RECURRING_TRANSACTIONS + " T"
                    + " JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON T." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = C." + DatabaseHelper.COLUMN_ID
                    + " WHERE T." + DatabaseHelper.COLUMN_USER_ID_FK + " = ?";

            cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    expenses.add(cursorToRecurringExpense(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all recurring expenses: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return expenses;
    }

    private RecurringExpense cursorToRecurringExpense(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK));
        long accountId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACCOUNT_ID_FK));
        long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID_FK));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
        String transactionType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_TYPE));
        String frequency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY_TYPE));
        int frequencyValue = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY_VALUE));
        String startDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE));
        String endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE));
        String lastGeneratedDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_GENERATED_DATE));
        boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE)) == 1;

        return new RecurringExpense(id, userId, accountId, categoryId, name, amount, transactionType, frequency, frequencyValue, startDate, endDate, lastGeneratedDate, isActive);
    }
}