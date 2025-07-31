package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.Budget.Budget; // Import lớp mô hình Budget.
import com.example.noname.R; // Import R để truy cập tài nguyên (ví dụ: ic_circle).

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BudgetDAO (Data Access Object) cung cấp các phương thức để tương tác với bảng 'budgets'
 * trong cơ sở dữ liệu SQLite. Nó đóng gói các thao tác database liên quan đến ngân sách,
 * tách biệt chúng khỏi UI và logic nghiệp vụ.
 */
public class BudgetDAO {
    private SQLiteDatabase database; // Thể hiện của database được sử dụng cho các thao tác.
    private DatabaseHelper dbHelper; // Helper để có được thể hiện database.
    private Context context; // Context để truy cập tài nguyên (ví dụ: cho việc tra cứu icon).

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT_DB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Định dạng ngày tháng chuẩn cho DB

    /**
     * Constructor cho BudgetDAO.
     * @param context Context của ứng dụng, được dùng để khởi tạo DatabaseHelper.
     */
    public BudgetDAO(Context context) {
        this.context = context; // Lưu trữ context.
        dbHelper = new DatabaseHelper(context); // Khởi tạo DatabaseHelper.
        Log.d("BudgetDAO", "BudgetDAO instance created.");
    }

    /**
     * Mở kết nối database ở chế độ ghi.
     * Phải gọi trước khi thực hiện bất kỳ thao tác ghi nào (insert, update, delete).
     */
    public void open() {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d("BudgetDAO", "Database opened for writing.");
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error opening database for writing: " + e.getMessage());
            // Có thể thêm xử lý lỗi mạnh mẽ hơn ở đây.
        }
    }

    /**
     * Đóng kết nối database.
     * Nên gọi sau khi hoàn tất các thao tác database để giải phóng tài nguyên.
     */
    public void close() {
        dbHelper.close(); // DatabaseHelper sẽ quản lý việc đóng SQLiteDatabase.
        Log.d("BudgetDAO", "Database closed.");
    }

    /**
     * Thêm một bản ghi ngân sách mới vào bảng 'budgets'.
     * @param userId ID của người dùng tạo ngân sách.
     * @param categoryId ID của danh mục ngân sách này.
     * @param amount Số tiền ngân sách.
     * @param startDate Ngày bắt đầu (YYYY-MM-DD).
     * @param endDate Ngày kết thúc (YYYY-MM-DD).
     * @param isRecurring Ngân sách có lặp lại (true/false).
     * @return true nếu thêm thành công, false nếu lỗi.
     */
    public boolean addBudget(long userId, long categoryId, double amount, String startDate, String endDate, boolean isRecurring) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, categoryId);
        values.put(DatabaseHelper.COLUMN_BUDGET_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_START_DATE, startDate);
        values.put(DatabaseHelper.COLUMN_END_DATE, endDate);
        values.put(DatabaseHelper.COLUMN_IS_RECURRING, isRecurring ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_CREATED_AT, getDateTime()); // Thời gian tạo.

        long insertId = -1;
        try {
            insertId = database.insert(DatabaseHelper.TABLE_BUDGETS, null, values);
            if (insertId != -1) {
                Log.d("BudgetDAO", "Budget added successfully with ID: " + insertId + " for user: " + userId);
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
     * Cập nhật một bản ghi ngân sách hiện có trong bảng 'budgets'.
     * @param budget Đối tượng Budget chứa thông tin đã cập nhật. ID của nó phải hợp lệ.
     * @return true nếu cập nhật thành công, false nếu lỗi.
     */
    public boolean updateBudget(Budget budget) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, budget.getUserId()); // Giữ userId nguyên khi cập nhật
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID_FK, budget.getCategoryId());
        values.put(DatabaseHelper.COLUMN_BUDGET_AMOUNT, budget.getAmount());
        values.put(DatabaseHelper.COLUMN_START_DATE, budget.getStartDate());
        values.put(DatabaseHelper.COLUMN_END_DATE, budget.getEndDate());
        values.put(DatabaseHelper.COLUMN_IS_RECURRING, budget.isRepeat() ? 1 : 0);
        // Có thể thêm COLUMN_UPDATED_AT nếu bạn theo dõi thời gian cập nhật.

        int rowsAffected = 0;
        try {
            rowsAffected = database.update(
                    DatabaseHelper.TABLE_BUDGETS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?", // THÊM ĐIỀU KIỆN USER_ID
                    new String[]{String.valueOf(budget.getId()), String.valueOf(budget.getUserId())} // THÊM ĐỐI SỐ USER_ID
            );
            if (rowsAffected > 0) {
                Log.d("BudgetDAO", "Budget updated successfully for ID: " + budget.getId() + " and user: " + budget.getUserId());
                return true;
            } else {
                Log.e("BudgetDAO", "Failed to update budget: no rows affected for ID: " + budget.getId() + " and user: " + budget.getUserId());
                return false;
            }
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error updating budget: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa một bản ghi ngân sách khỏi bảng 'budgets' dựa trên ID của nó và User ID.
     * @param budgetId ID của ngân sách cần xóa.
     * @param userId ID của người dùng sở hữu ngân sách.
     * @return true nếu xóa thành công, false nếu lỗi.
     */
    public boolean deleteBudget(long budgetId, long userId) { // Thêm userId vào đây
        int rowsAffected = 0;
        try {
            rowsAffected = database.delete(
                    DatabaseHelper.TABLE_BUDGETS,
                    DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?", // THÊM ĐIỀU KIỆN USER_ID
                    new String[]{String.valueOf(budgetId), String.valueOf(userId)} // THÊM ĐỐI SỐ USER_ID
            );
            if (rowsAffected > 0) {
                Log.d("BudgetDAO", "Budget deleted successfully for ID: " + budgetId + " and user: " + userId);
                return true;
            } else {
                Log.e("BudgetDAO", "Failed to delete budget: no rows affected for ID: " + budgetId + " and user: " + userId);
                return false;
            }
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error deleting budget: " + e.getMessage());
            return false;
        }
    }

    /**
     * Truy xuất tất cả các bản ghi ngân sách từ database cho một người dùng cụ thể,
     * bao gồm thông tin danh mục. Thực hiện INNER JOIN với bảng 'categories'
     * để lấy tên danh mục và tên icon.
     * @param userId ID của người dùng.
     * @return Một List các đối tượng Budget. Trả về danh sách rỗng nếu không tìm thấy.
     */
    public List<Budget> getAllBudgets(long userId) { // Thay đổi chữ ký phương thức để nhận userId
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
                " WHERE B." + DatabaseHelper.COLUMN_USER_ID_FK + " = ?" + // THÊM ĐIỀU KIỆN LỌC THEO USER_ID
                " ORDER BY B." + DatabaseHelper.COLUMN_CREATED_AT + " DESC";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(selectQuery, new String[]{String.valueOf(userId)}); // TRUYỀN USER_ID LÀM ĐỐI SỐ

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    long fetchedUserId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK)); // Lấy userId từ cursor
                    long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID_FK));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_AMOUNT));
                    String startDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE));
                    String endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE));
                    boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_RECURRING)) == 1;
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                    String iconName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME));

                    int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                    if (iconResId == 0) {
                        iconResId = R.drawable.ic_circle;
                        Log.w("BudgetDAO", "Icon resource not found for: " + iconName + ". Using default.");
                    }

                    // Sử dụng fetchedUserId để tạo đối tượng Budget
                    Budget budget = new Budget(id, fetchedUserId, categoryId, categoryName, iconResId, amount, startDate, endDate, isRecurring);
                    budgetList.add(budget);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error getting all budgets for user " + userId + ": " + e.getMessage());
            budgetList.clear();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return budgetList;
    }

    /**
     * Tính tổng số tiền đã chi tiêu cho một danh mục cụ thể trong một khoảng thời gian nhất định.
     * Phương thức này truy vấn bảng TRANSACTIONS.
     * @param categoryId ID của danh mục.
     * @param startDate Ngày bắt đầu của kỳ (YYYY-MM-DD).
     * @param endDate Ngày kết thúc của kỳ (YYYY-MM-DD).
     * @param userId ID của người dùng.
     * @return Tổng số tiền đã chi tiêu. Trả về 0.0 nếu không tìm thấy giao dịch hoặc có lỗi.
     */
    public double getTotalSpentForBudgetCategory(long categoryId, String startDate, String endDate, long userId) {
        double totalSpent = 0.0;
        // Kiểm tra xem database đã mở chưa. Phương thức này nên được gọi trong một khối open()/close().
        if (database == null || !database.isOpen()) {
            Log.e("BudgetDAO", "Database is not open when trying to get total spent for budget. Cannot query transactions.");
            return 0.0;
        }

        // Câu lệnh SQL để tính tổng số tiền giao dịch loại "Expense"
        // cho một người dùng và danh mục cụ thể trong khoảng thời gian.
        String selectQuery = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " " +
                "WHERE " + DatabaseHelper.COLUMN_USER_ID_FK + " = ? AND " + // THÊM ĐIỀU KIỆN USER_ID
                DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = ? AND " +
                DatabaseHelper.COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ? AND " +
                DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = 'Expense'";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(selectQuery, new String[]{
                    String.valueOf(userId), // TRUYỀN USER_ID LÀM ĐỐI SỐ
                    String.valueOf(categoryId),
                    startDate,
                    endDate
            });

            if (cursor != null && cursor.moveToFirst()) {
                totalSpent = cursor.getDouble(0); // Hàm SUM luôn trả về một hàng, cột đầu tiên.
            }
            Log.d("BudgetDAO", "Total spent for category " + categoryId + " between " + startDate + " and " + endDate + " by user " + userId + ": " + totalSpent);
        } catch (Exception e) {
            Log.e("BudgetDAO", "Error getting total spent for budget category: " + e.getMessage());
            totalSpent = 0.0; // Trả về 0 nếu có lỗi.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return totalSpent;
    }

    /**
     * Phương thức tiện ích để lấy dấu thời gian hiện tại theo định dạng "yyyy-MM-dd HH:mm:ss".
     * @return Chuỗi dấu thời gian đã định dạng.
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}