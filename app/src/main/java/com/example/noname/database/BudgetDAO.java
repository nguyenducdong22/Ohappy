package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.Budget.Budget; // Import the Budget model class
import com.example.noname.R; // Import R to access drawable resources

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BudgetDAO (Data Access Object) provides methods to interact with the 'budgets' table
 * in the SQLite database. It encapsulates the database operations related to budgets,
 * separating them from the UI and other business logic.
 */
public class BudgetDAO {
    private SQLiteDatabase database; // The database instance used for operations.
    private DatabaseHelper dbHelper; // Helper to get database instance.
    private Context context; // Context to access resources (e.g., for icon lookup).

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * Constructor for BudgetDAO.
     * @param context The application context, used to initialize DatabaseHelper.
     */
    public BudgetDAO(Context context) {
        this.context = context; // Store context for resource access.
        dbHelper = new DatabaseHelper(context); // Initialize the DatabaseHelper.
    }

    /**
     * Opens the database connection in writable mode.
     * Call this before performing any write operations (insert, update, delete).
     */
    public void open() {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d("BudgetDAO", "Database opened for writing.");
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error opening database for writing: " + e.getMessage());
            // It's good practice to handle this error, e.g., by rethrowing a custom exception
            // or notifying the user that the database is unavailable.
        }
    }

    /**
     * Closes the database connection.
     * Call this after completing database operations to release resources.
     */
    public void close() {
        dbHelper.close();
        Log.d("BudgetDAO", "Database closed.");
    }

    /**
     * Adds a new budget record to the 'budgets' table.
     *
     * @param userId The ID of the user creating the budget.
     * @param categoryId The ID of the category this budget applies to.
     * @param amount The budgeted amount.
     * @param startDate The start date of the budget period (YYYY-MM-DD format).
     * @param endDate The end date of the budget period (YYYY-MM-DD format).
     * @param isRecurring Whether the budget is recurring (true/false).
     * @return true if the budget was added successfully, false otherwise.
     */
    public boolean addBudget(long userId, long categoryId, double amount, String startDate, String endDate, boolean isRecurring) {
        ContentValues values = new ContentValues();

        // Populate ContentValues with the budget details.
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_BUDGET_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_IS_RECURRING, isRecurring ? 1 : 0); // Convert boolean to int (1 or 0).
        values.put(DatabaseHelper.COLUMN_CREATED_AT, getDateTime()); // Record the creation timestamp.

        // Perform the insert operation.
        long insertId = -1;
        try {
            insertId = database.insert(DatabaseHelper.TABLE_BUDGETS, null, values);
            if (insertId != -1) {
                Log.d("BudgetDAO", "Budget added successfully with ID: " + insertId + " for categoryId: " + categoryId);
                return true; // Indicate success.
            } else {
                Log.e("BudgetDAO", "Failed to add budget for categoryId: " + categoryId);
                return false; // Indicate failure.
            }
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error adding budget: " + e.getMessage());
            return false; // Indicate failure due to an exception.
        }
    }

    /**
     * Retrieves all budget records from the database, including related category information.
     * This method performs a JOIN operation between the 'budgets' and 'categories' tables
     * to fetch the category name and icon name.
     *
     * @return A List of Budget objects. Returns an empty list if no budgets are found.
     */
    public List<Budget> getAllBudgets() {
        List<Budget> budgetList = new ArrayList<>();
        // Define the SQL SELECT query.
        // We select columns from the BUDGETS table (B) and the category name, icon name columns from the CATEGORIES table (C).
        String selectQuery = "SELECT " +
                "B." + DatabaseHelper.COLUMN_ID + ", " +
                "B." + DatabaseHelper.COLUMN_USER_ID_FK + ", " +
                "B." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + ", " +
                "B." + DatabaseHelper.COLUMN_BUDGET_AMOUNT + ", " +
                "B." + DatabaseHelper.COLUMN_START_DATE + ", " +
                "B." + DatabaseHelper.COLUMN_END_DATE + ", " +
                "B." + DatabaseHelper.COLUMN_IS_RECURRING + ", " +
                "B." + DatabaseHelper.COLUMN_CREATED_AT + ", " +
                "C." + DatabaseHelper.COLUMN_CATEGORY_NAME + ", " + // Get category name from Categories table.
                "C." + DatabaseHelper.COLUMN_ICON_NAME + " " +      // Get icon name from Categories table.
                "FROM " + DatabaseHelper.TABLE_BUDGETS + " B " +
                "INNER JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON B." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = C." + DatabaseHelper.COLUMN_ID +
                " ORDER BY B." + DatabaseHelper.COLUMN_CREATED_AT + " DESC"; // Order by creation date, newest first.

        // Get a readable database instance.
        Cursor cursor = null;

        try {
            // Execute the rawQuery statement and receive a Cursor.
            cursor = database.rawQuery(selectQuery, null);

            // Iterate through all rows in the Cursor.
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Get the index of each column. Use getColumnIndexOrThrow to ensure the column exists.
                    int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID);
                    int userIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK);
                    int categoryIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID_FK);
                    int amountIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_AMOUNT);
                    int startIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE);
                    int endIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE);
                    int isRecurringIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_RECURRING);
                    int categoryNameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME);
                    int iconNameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME);

                    // Retrieve values from the Cursor by column index.
                    long id = cursor.getLong(idIndex);
                    long userId = cursor.getLong(userIdIndex);
                    long categoryId = cursor.getLong(categoryIdIndex);
                    double amount = cursor.getDouble(amountIndex);
                    String startDate = cursor.getString(startIndex);
                    String endDate = cursor.getString(endIndex);
                    boolean isRecurring = cursor.getInt(isRecurringIndex) == 1; // Convert int (0/1) to boolean.
                    String categoryName = cursor.getString(categoryNameIndex);
                    String iconName = cursor.getString(iconNameIndex);

                    // Convert the icon resource name string to an actual resource ID.
                    // This requires the application context.
                    int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                    if (iconResId == 0) {
                        // If the icon is not found, use a default icon.
                        iconResId = R.drawable.ic_circle; // Ensure 'ic_circle' exists in your drawables.
                        Log.w("BudgetDAO", "Icon resource not found for: " + iconName + ". Using default.");
                    }

                    // Create a new Budget object and add it to the list.
                    Budget budget = new Budget(id, userId, categoryId, categoryName, iconResId, amount, startDate, endDate, isRecurring);
                    budgetList.add(budget);

                } while (cursor.moveToNext()); // Move to the next row.
            }
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error getting all budgets: " + e.getMessage());
        } finally {
            // Ensure the Cursor is closed after use to prevent resource leaks.
            if (cursor != null) {
                cursor.close();
            }
            // Database connection is managed by open() and close() in DAO, so no db.close() here.
        }
        return budgetList;
    }

    // You can add more CRUD (Update, Delete) and query methods here as needed,
    // for example: getBudgetById, updateBudget, deleteBudget, etc.

    /**
     * Utility method to get the current timestamp in "yyyy-MM-dd HH:mm:ss" format.
     * Commonly used for 'created_at' and 'updated_at' columns.
     * @return Formatted current timestamp string.
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}