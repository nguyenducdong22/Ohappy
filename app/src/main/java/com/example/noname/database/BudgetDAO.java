package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.Budget.Budget; // Import the Budget model class.
import com.example.noname.R;             // Import R to access drawable resources for icons.

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BudgetDAO (Data Access Object) provides methods to interact with the 'budgets' table.
 * It encapsulates all CRUD operations for budget data, abstracting raw SQL from the UI.
 */
public class BudgetDAO {
    private SQLiteDatabase database; // The SQLiteDatabase instance for operations.
    private DatabaseHelper dbHelper; // Helper to get database instance and access schema constants.
    private Context context;         // Context to access resources (e.g., for icon lookup).

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * Constructor for BudgetDAO.
     * @param context The application context.
     */
    public BudgetDAO(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
        Log.d("BudgetDAO", "BudgetDAO instance created.");
    }

    /**
     * Opens the database connection in writable mode.
     * Must be called before performing write operations (insert, update, delete).
     */
    public void open() {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d("BudgetDAO", "Database opened for writing.");
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error opening database for writing: " + e.getMessage());
            // Consider more robust error handling for production apps.
        }
    }

    /**
     * Closes the database connection.
     * Should be called after completing database operations to release resources.
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
            Log.d("BudgetDAO", "Database closed.");
        }
    }

    /**
     * Adds a new budget record to the 'budgets' table.
     * @param userId The ID of the user.
     * @param categoryId The ID of the category for this budget.
     * @param amount The budgeted amount.
     * @param startDate The start date (YYYY-MM-DD).
     * @param endDate The end date (YYYY-MM-DD).
     * @param isRecurring True if the budget is recurring, false otherwise.
     * @return True if the budget was added successfully, false otherwise.
     */
    public boolean addBudget(long userId, long categoryId, double amount, String startDate, String endDate, boolean isRecurring) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_BUDGET_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_IS_RECURRING, isRecurring ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_CREATED_AT, getDateTime());

        long insertId = -1;
        try {
            insertId = database.insert(DatabaseHelper.TABLE_BUDGETS, null, values);
            if (insertId != -1) {
                Log.d("BudgetDAO", "Budget added successfully with ID: " + insertId);
                return true;
            } else {
                Log.e("BudgetDAO", "Failed to add budget: insert returned -1.");
                return false;
            }
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error adding budget: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all budget records from the database, including related category information.
     * Performs an INNER JOIN with the 'categories' table to get category names and icon names.
     * @return A List of Budget objects. Returns an empty list if no budgets are found.
     */
    public List<Budget> getAllBudgets() {
        List<Budget> budgetList = new ArrayList<>();
        String selectQuery = "SELECT " +
                "B." + DatabaseHelper.COLUMN_ID + ", " +
                "B." + DatabaseHelper.COLUMN_USER_ID_FK + ", " +
                "B." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + ", " +
                "B." + DatabaseHelper.COLUMN_BUDGET_AMOUNT + ", " +
                "B." + DatabaseHelper.COLUMN_START_DATE + ", " +
                "B." + DatabaseHelper.COLUMN_END_DATE + ", " +
                "B." + DatabaseHelper.COLUMN_IS_RECURRING + ", " +
                "B." + DatabaseHelper.COLUMN_CREATED_AT + ", " +
                "C." + DatabaseHelper.COLUMN_CATEGORY_NAME + ", " +
                "C." + DatabaseHelper.COLUMN_ICON_NAME + " " +
                "FROM " + DatabaseHelper.TABLE_BUDGETS + " B " +
                "INNER JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON B." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = C." + DatabaseHelper.COLUMN_ID +
                " ORDER BY B." + DatabaseHelper.COLUMN_CREATED_AT + " DESC";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(selectQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK));
                    long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID_FK));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_AMOUNT));
                    String startDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE));
                    String endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE));
                    boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_RECURRING)) == 1;
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                    String iconName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME));

                    // Convert icon name string to resource ID.
                    int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                    if (iconResId == 0) {
                        iconResId = R.drawable.ic_circle; // Default icon if not found.
                        Log.w("BudgetDAO", "Icon resource not found for: " + iconName + ". Using default.");
                    }

                    Budget budget = new Budget(id, userId, categoryId, categoryName, iconResId, amount, startDate, endDate, isRecurring);
                    budgetList.add(budget);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error getting all budgets: " + e.getMessage());
            budgetList.clear(); // Clear list in case of error.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return budgetList;
    }

    /**
     * Utility method to get the current timestamp in "yyyy-MM-dd HH:mm:ss" format.
     * @return Formatted current timestamp string.
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}