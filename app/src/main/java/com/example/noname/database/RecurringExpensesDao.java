package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.models.RecurringExpense; // Import lớp RecurringExpense từ package models

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpensesDao {

    private DatabaseHelper dbHelper;
    private Context context; // Thêm Context để truy cập Resources
    private static final String TAG = "RecurringExpensesDao";

    public RecurringExpensesDao(Context context) {
        this.context = context; // Lưu context
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Thêm một khoản chi tiêu định kỳ mới vào cơ sở dữ liệu.
     *
     * @param expense Đối tượng RecurringExpense cần thêm.
     * @param userId ID của người dùng sở hữu khoản chi tiêu này.
     * @param accountId ID của tài khoản liên quan (có thể là null nếu không bắt buộc).
     * @param categoryId ID của danh mục liên quan (có thể là null nếu không bắt buộc).
     * @return ID của hàng được chèn, hoặc -1 nếu có lỗi.
     */
    public long addRecurringExpense(RecurringExpense expense, long userId, Long accountId, Long categoryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        if (accountId != null) {
            values.put(DatabaseHelper.COLUMN_ACCOUNT_ID_FK, accountId);
        } else {
            values.putNull(DatabaseHelper.COLUMN_ACCOUNT_ID_FK);
        }
        if (categoryId != null) {
            values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        } else {
            values.putNull(DatabaseHelper.COLUMN_CATEGORY_ID_FK);
        }

        values.put(DatabaseHelper.COLUMN_AMOUNT, expense.getAmount());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, expense.getName());
        values.put(DatabaseHelper.COLUMN_TRANSACTION_TYPE, expense.getType());
        values.put(DatabaseHelper.COLUMN_FREQUENCY_TYPE, expense.getFrequency());
        // COLUMN_FREQUENCY_VALUE không có trong RecurringExpense model hiện tại của bạn.
        // Bạn có thể thêm vào model nếu muốn lưu giá trị số cho tần suất (ví dụ: "3" cho mỗi 3 tháng)
        // values.put(DatabaseHelper.COLUMN_FREQUENCY_VALUE, expense.getFrequencyValue());

        values.put(DatabaseHelper.COLUMN_START_DATE, DatabaseHelper.getCurrentTimestamp()); // Ngày hiện tại khi tạo
        values.put(DatabaseHelper.COLUMN_NEXT_DATE, expense.getNextDate());
        values.put(DatabaseHelper.COLUMN_ICON_NAME, getIconNameFromResId(expense.getIconResId()));
        values.put(DatabaseHelper.COLUMN_COLOR_CODE, getColorCodeFromResId(expense.getIconTintColorResId()));
        values.put(DatabaseHelper.COLUMN_LAST_GENERATED_DATE, DatabaseHelper.getCurrentTimestamp()); // Giả sử ngày cuối cùng tạo là ngày hiện tại
        values.put(DatabaseHelper.COLUMN_STATUS, expense.getStatus());
        values.put(DatabaseHelper.COLUMN_CREATED_AT, DatabaseHelper.getCurrentTimestamp());

        long newRowId = db.insert(DatabaseHelper.TABLE_RECURRING_TRANSACTIONS, null, values);
        db.close();
        Log.d(TAG, "Added recurring expense: " + expense.getName() + " with ID: " + newRowId);
        return newRowId;
    }

    /**
     * Lấy tất cả các khoản chi tiêu định kỳ từ cơ sở dữ liệu cho một người dùng cụ thể.
     *
     * @param userId ID của người dùng cần lấy các khoản chi tiêu.
     * @return Danh sách các đối tượng RecurringExpense.
     */
    public List<RecurringExpense> getAllRecurringExpenses(long userId) {
        List<RecurringExpense> recurringExpenseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_RECURRING_TRANSACTIONS
                + " WHERE " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_TYPE));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY_TYPE));
                String nextDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NEXT_DATE));
                String iconName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME));
                String colorCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR_CODE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS));

                // Chuyển đổi tên icon/mã màu từ String sang Resource ID
                int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                int iconTintColorResId = context.getResources().getIdentifier(colorCode.replace("#", "color_"), "color", context.getPackageName());

                recurringExpenseList.add(new RecurringExpense(id, name, amount, type, frequency, nextDate, iconResId, iconTintColorResId, status));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recurringExpenseList;
    }

    /**
     * Cập nhật một khoản chi tiêu định kỳ hiện có trong cơ sở dữ liệu.
     *
     * @param expense Đối tượng RecurringExpense với ID và dữ liệu đã cập nhật.
     * @return Số lượng hàng được cập nhật (1 nếu thành công, 0 nếu không tìm thấy).
     */
    public int updateRecurringExpense(RecurringExpense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_AMOUNT, expense.getAmount());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, expense.getName());
        values.put(DatabaseHelper.COLUMN_TRANSACTION_TYPE, expense.getType());
        values.put(DatabaseHelper.COLUMN_FREQUENCY_TYPE, expense.getFrequency());
        values.put(DatabaseHelper.COLUMN_NEXT_DATE, expense.getNextDate());
        values.put(DatabaseHelper.COLUMN_ICON_NAME, getIconNameFromResId(expense.getIconResId()));
        values.put(DatabaseHelper.COLUMN_COLOR_CODE, getColorCodeFromResId(expense.getIconTintColorResId()));
        values.put(DatabaseHelper.COLUMN_STATUS, expense.getStatus());
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, DatabaseHelper.getCurrentTimestamp());

        // Cập nhật dựa trên ID
        int rowsAffected = db.update(
                DatabaseHelper.TABLE_RECURRING_TRANSACTIONS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(expense.getId())}
        );
        db.close();
        Log.d(TAG, "Updated recurring expense ID: " + expense.getId() + ", Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Xóa một khoản chi tiêu định kỳ khỏi cơ sở dữ liệu.
     *
     * @param expenseId ID của khoản chi tiêu cần xóa.
     * @return Số lượng hàng bị xóa (1 nếu thành công, 0 nếu không tìm thấy).
     */
    public int deleteRecurringExpense(long expenseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(
                DatabaseHelper.TABLE_RECURRING_TRANSACTIONS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(expenseId)}
        );
        db.close();
        Log.d(TAG, "Deleted recurring expense ID: " + expenseId + ", Rows affected: " + rowsAffected);
        return rowsAffected;
    }

    // --- Helper methods để chuyển đổi Resource ID icon/màu sắc sang tên string/mã hex và ngược lại ---

    private String getIconNameFromResId(int resId) {
        if (resId == 0 || resId == -1) return null;
        try {
            return context.getResources().getResourceEntryName(resId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting resource name for iconId: " + resId, e);
            return null;
        }
    }

    private String getColorCodeFromResId(int resId) {
        if (resId == 0 || resId == -1) return null;
        try {
            int color = context.getResources().getColor(resId, null);
            return String.format("#%08X", color);
        } catch (Exception e) {
            Log.e(TAG, "Error getting color code for colorId: " + resId, e);
            return null;
        }
    }
}