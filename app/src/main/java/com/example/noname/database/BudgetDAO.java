package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.Budget.Budget;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetDAO {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context context;

    private static final String TAG = "BudgetDAO";

    private static final String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_USER_ID_FK,
            DatabaseHelper.COLUMN_CATEGORY_ID_FK,
            DatabaseHelper.COLUMN_BUDGET_AMOUNT,
            DatabaseHelper.COLUMN_START_DATE,
            DatabaseHelper.COLUMN_END_DATE,
            DatabaseHelper.COLUMN_IS_RECURRING,
            DatabaseHelper.COLUMN_CREATED_AT
    };

    public BudgetDAO(Context context) {
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
    }

    /**
     * Thêm một ngân sách mới vào database.
     * @param userId ID người dùng.
     * @param categoryId ID danh mục.
     * @param amount Số tiền ngân sách.
     * @param startDate Ngày bắt đầu.
     * @param endDate Ngày kết thúc.
     * @param isRecurring Ngân sách có lặp lại không.
     * @return ID của ngân sách mới, hoặc -1 nếu có lỗi.
     */
    public long addBudget(long userId, long categoryId, double amount, String startDate, String endDate, boolean isRecurring) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_BUDGET_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_IS_RECURRING, isRecurring ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        long insertId = -1;
        try {
            open();
            insertId = database.insert(DatabaseHelper.TABLE_BUDGETS, null, values);
            Log.d(TAG, "Budget added with ID: " + insertId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding budget: " + e.getMessage(), e);
        } finally {
            close();
        }
        return insertId;
    }

    /**
     * Cập nhật một ngân sách hiện có.
     * @param budgetId ID ngân sách cần cập nhật.
     * @param userId ID người dùng.
     * @param categoryId ID danh mục.
     * @param amount Số tiền ngân sách.
     * @param startDate Ngày bắt đầu.
     * @param endDate Ngày kết thúc.
     * @param isRecurring Ngân sách có lặp lại không.
     * @return Số hàng bị ảnh hưởng, hoặc 0 nếu không có gì được cập nhật.
     */
    public int updateBudget(long budgetId, long userId, long categoryId, double amount, String startDate, String endDate, boolean isRecurring) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_BUDGET_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_IS_RECURRING, isRecurring ? 1 : 0);

        int rowsAffected = 0;
        try {
            open();
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_BUDGETS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?",
                    new String[]{String.valueOf(budgetId), String.valueOf(userId)}
            );
            Log.d(TAG, "Budget updated. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating budget: " + e.getMessage(), e);
        } finally {
            close();
        }
        return rowsAffected;
    }

    /**
     * Xóa một ngân sách.
     * @param budgetId ID ngân sách cần xóa.
     * @param userId ID người dùng.
     * @return true nếu xóa thành công, false nếu ngược lại.
     */
    public boolean deleteBudget(long budgetId, long userId) {
        int rowsAffected = 0;
        try {
            open();
            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_BUDGETS,
                    DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?",
                    new String[]{String.valueOf(budgetId), String.valueOf(userId)}
            );
            Log.d(TAG, "Budget deleted. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting budget: " + e.getMessage(), e);
        } finally {
            close();
        }
        return rowsAffected > 0;
    }

    /**
     * Lấy tất cả các ngân sách của một người dùng.
     * Phương thức này sử dụng JOIN để lấy thông tin danh mục.
     * @param userId ID người dùng.
     * @return Danh sách các đối tượng Budget.
     */
    public List<Budget> getAllBudgets(long userId) {
        List<Budget> budgets = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            String query = "SELECT B.*, C." + DatabaseHelper.COLUMN_CATEGORY_NAME + ", C." + DatabaseHelper.COLUMN_ICON_NAME
                    + " FROM " + DatabaseHelper.TABLE_BUDGETS + " B"
                    + " JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON B." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = C." + DatabaseHelper.COLUMN_ID
                    + " WHERE B." + DatabaseHelper.COLUMN_USER_ID_FK + " = ?";

            cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    budgets.add(cursorToBudget(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all budgets: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return budgets;
    }

    /**
     * Tính tổng số tiền đã chi cho một danh mục trong một khoảng thời gian.
     * @param categoryId ID danh mục.
     * @param startDate Ngày bắt đầu (định dạng YYYY-MM-DD).
     * @param endDate Ngày kết thúc (định dạng YYYY-MM-DD).
     * @param userId ID người dùng.
     * @return Tổng số tiền đã chi, hoặc 0.0 nếu có lỗi.
     */
    public double getTotalSpentForBudgetCategory(long categoryId, String startDate, String endDate, long userId) {
        double totalSpent = 0.0;
        Cursor cursor = null;
        try {
            open();
            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ")"
                    + " FROM " + DatabaseHelper.TABLE_TRANSACTIONS
                    + " WHERE " + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = ?"
                    + " AND " + DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = 'Expense'"
                    + " AND " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?"
                    + " AND " + DatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?";

            String[] selectionArgs = {String.valueOf(categoryId), String.valueOf(userId), startDate, endDate};

            cursor = database.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                totalSpent = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating total spent for budget category: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return totalSpent;
    }

    // THÊM MỚI: Phương thức để lấy một ngân sách cụ thể cho việc kiểm tra
    public Budget getActiveBudgetForCategoryAndDate(long userId, long categoryId, String date) {
        Budget budget = null;
        Cursor cursor = null;
        try {
            open();
            String query = "SELECT B.*, C." + DatabaseHelper.COLUMN_CATEGORY_NAME + ", C." + DatabaseHelper.COLUMN_ICON_NAME
                    + " FROM " + DatabaseHelper.TABLE_BUDGETS + " B"
                    + " JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON B." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = C." + DatabaseHelper.COLUMN_ID
                    + " WHERE B." + DatabaseHelper.COLUMN_USER_ID_FK + " = ?"
                    + " AND B." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = ?"
                    + " AND ? BETWEEN B." + DatabaseHelper.COLUMN_START_DATE + " AND B." + DatabaseHelper.COLUMN_END_DATE;

            String[] selectionArgs = {String.valueOf(userId), String.valueOf(categoryId), date};
            cursor = database.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                budget = cursorToBudget(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting active budget for category and date: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return budget;
    }

    // THÊM MỚI: Phương thức cursorToBudget đã được điều chỉnh để khớp với lớp Budget mới
    private Budget cursorToBudget(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK));
        long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID_FK));
        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_AMOUNT));
        String startDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE));
        String endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE));
        boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_RECURRING)) == 1;

        int groupNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_NAME);
        String groupName = (groupNameIndex != -1) ? cursor.getString(groupNameIndex) : null;
        int iconNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ICON_NAME);
        String iconName = (iconNameIndex != -1) ? cursor.getString(iconNameIndex) : null;

        int groupIconResId = -1;
        if (iconName != null) {
            groupIconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        }
        // Giữ nguyên tên biến cũ của bạn để không thay đổi code
        return new Budget(id, userId, categoryId, groupName, groupIconResId, amount, startDate, endDate, isRecurring);
    }
}