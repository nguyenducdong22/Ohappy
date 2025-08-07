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
     * Creates a new account for a user.
     * @param userId The ID of the user who owns the account.
     * @param accountName The name of the account (e.g., "Cash", "Momo Wallet").
     * @param initialBalance The initial balance.
     * @param accountType The type of account.
     * @return The ID of the newly created account, or -1 if an error occurred.
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
     * Updates the information of an existing account.
     * @param accountId The ID of the account to update.
     * @param newName The new name for the account.
     * @param newBalance The new balance.
     * @return The number of affected rows.
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
            Log.e(TAG, "Account does not exist.");
            return false;
        }

        double newBalance = currentAccount.getBalance();
        if ("Income".equals(transactionType)) {
            newBalance += amount;
        } else if ("Expense".equals(transactionType)) {
            newBalance -= amount;
        } else {
            Log.e(TAG, "Invalid transaction type: " + transactionType);
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