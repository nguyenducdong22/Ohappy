package com.example.noname.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportManager {

    /**
     * Lấy tổng thu và tổng chi cho một tháng cụ thể.
     * @param context Context của ứng dụng.
     * @param month   Tháng cần lấy dữ liệu, định dạng "YYYY-MM".
     * @return Một Map chứa "totalIncome" và "totalExpense".
     */
    public static Map<String, Double> getMonthlySummary(Context context, String month) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalIncome", 0.0);
        summary.put("totalExpense", 0.0);

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT type, SUM(amount) as total FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE strftime('%Y-%m', transaction_date) = ? " +
                " GROUP BY type";

        try (Cursor cursor = db.rawQuery(query, new String[]{month})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));

                    if ("Income".equalsIgnoreCase(type)) {
                        summary.put("totalIncome", total);
                    } else if ("Expense".equalsIgnoreCase(type)) {
                        summary.put("totalExpense", total);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ReportManager", "Error in getMonthlySummary", e);
        }

        db.close();
        return summary;
    }

    /**
     * Lấy tổng số dư từ tất cả các tài khoản.
     * @param context Context của ứng dụng.
     * @return Tổng số dư.
     */
    public static double getTotalBalance(Context context) {
        double totalBalance = 0.0;
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(balance) as total FROM " + DatabaseHelper.TABLE_ACCOUNTS;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                totalBalance = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            }
        } catch (Exception e) {
            Log.e("ReportManager", "Error in getTotalBalance", e);
        }

        db.close();
        return totalBalance;
    }

    /**
     * Tính toán số dư tại một thời điểm (đầu tháng hoặc cuối tháng).
     * @param context Context của ứng dụng.
     * @param month   Tháng tính toán, định dạng "YYYY-MM".
     * @param isEndOfMonth True để tính số dư cuối tháng, False để tính số dư đầu tháng.
     * @return Số dư tính toán được.
     */
    private static double getBalanceAtPointInTime(Context context, String month, boolean isEndOfMonth) {
        double balance = 0;
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Lấy tổng số dư hiện tại của tất cả các ví
        balance = getTotalBalance(context);

        // 2. Lấy tất cả giao dịch TỪ thời điểm đó ĐẾN NAY
        String dateOperator = isEndOfMonth ? ">" : ">=";
        String pointInTime = isEndOfMonth ? month + "-31 23:59:59" : month + "-01 00:00:00";

        String query = "SELECT type, SUM(amount) as total FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE transaction_date " + dateOperator + " ? " +
                " GROUP BY type";

        try (Cursor cursor = db.rawQuery(query, new String[]{pointInTime})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));

                    // Logic đảo ngược:
                    // - Để tìm số dư quá khứ, ta lấy số dư hiện tại
                    // - TRỪ đi các khoản THU nhập từ đó đến nay
                    // - CỘNG lại các khoản CHI tiêu từ đó đến nay
                    if ("Income".equalsIgnoreCase(type)) {
                        balance -= total;
                    } else if ("Expense".equalsIgnoreCase(type)) {
                        balance += total;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ReportManager", "Error in getBalanceAtPointInTime", e);
        }

        db.close();
        return balance;
    }

    /**
     * Lấy số dư đầu kỳ của một tháng.
     */
    public static double getOpeningBalance(Context context, String month) {
        return getBalanceAtPointInTime(context, month, false);
    }

    /**
     * Lấy số dư cuối kỳ của một tháng.
     */
    public static double getClosingBalance(Context context, String month) {
        return getBalanceAtPointInTime(context, month, true);
    }
    /**
     * Lấy danh sách các giao dịch gần đây nhất.
     * @param context Context của ứng dụng.
     * @param limit   Số lượng giao dịch tối đa cần lấy.
     * @return Một Cursor chứa dữ liệu các giao dịch gần đây.
     */
    public static Cursor getRecentTransactions(Context context, int limit) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        final String GET_TRANSACTIONS_QUERY =
                "SELECT T.id, T.amount, T.type, T.transaction_date, T.description, C.name AS category_name, C.icon_name " +
                        "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " T " +
                        "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON T.category_id = C.id " +
                        "ORDER BY T.transaction_date DESC " +
                        "LIMIT " + limit;

        try {
            return db.rawQuery(GET_TRANSACTIONS_QUERY, null);
        } catch (Exception e) {
            Log.e("ReportManager", "Error getting recent transactions", e);
            db.close();
            return null;
        }
        // Lưu ý: Database sẽ được đóng trong Activity sau khi sử dụng xong Cursor.
    }

    /**
     * Lấy dữ liệu giao dịch đã gom nhóm theo danh mục cho một tháng và một loại cụ thể (thu hoặc chi).
     * @param context         Context của ứng dụng.
     * @param month           Tháng cần báo cáo, định dạng "YYYY-MM".
     * @param transactionType Loại giao dịch ("Income" hoặc "Expense").
     * @return Một Cursor chứa dữ liệu đã gom nhóm.
     */
    public static Cursor getSummaryByCategory(Context context, String month, String transactionType) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT C." + DatabaseHelper.COLUMN_CATEGORY_NAME + " as category_name, " +
                "SUM(T." + DatabaseHelper.COLUMN_AMOUNT + ") as total_amount, " +
                "C." + DatabaseHelper.COLUMN_COLOR_CODE + " as color_code " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " T " +
                "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " C ON T." + DatabaseHelper.COLUMN_CATEGORY_ID_FK + " = C.id " +
                "WHERE strftime('%Y-%m', T." + DatabaseHelper.COLUMN_TRANSACTION_DATE + ") = ? " +
                "AND T." + DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = ? " +
                "GROUP BY C." + DatabaseHelper.COLUMN_CATEGORY_NAME + " " +
                "ORDER BY total_amount DESC";

        try {
            return db.rawQuery(query, new String[]{month, transactionType});
        } catch (Exception e) {
            Log.e("ReportManager", "Error in getSummaryByCategory for type " + transactionType, e);
            db.close();
            return null;
        }
        // Database sẽ được đóng trong Activity sau khi dùng Cursor
    }

    /**
     * Lấy top 3 danh mục chi tiêu nhiều nhất trong tháng.
     * @param context Context
     * @param month   Tháng cần truy vấn, định dạng "yyyy-MM"
     * @return Cursor chứa category_name, icon_name, và total_amount
     */
    public static Cursor getTopExpensesForMonth(Context context, String month) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Sử dụng StringBuilder để xây dựng câu lệnh SQL một cách rõ ràng
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append("C.").append(DatabaseHelper.COLUMN_CATEGORY_NAME).append(", ");
        queryBuilder.append("C.").append(DatabaseHelper.COLUMN_ICON_NAME).append(", ");
        queryBuilder.append("SUM(T.").append(DatabaseHelper.COLUMN_AMOUNT).append(") as total_amount ");
        queryBuilder.append("FROM ").append(DatabaseHelper.TABLE_TRANSACTIONS).append(" T ");
        queryBuilder.append("JOIN ").append(DatabaseHelper.TABLE_CATEGORIES).append(" C ON T.").append(DatabaseHelper.COLUMN_CATEGORY_ID).append(" = C.").append(DatabaseHelper.COLUMN_ID).append(" ");
        queryBuilder.append("WHERE T.").append(DatabaseHelper.COLUMN_TRANSACTION_TYPE).append(" = 'Expense' AND ");
        queryBuilder.append("strftime('%Y-%m', T.").append(DatabaseHelper.COLUMN_TRANSACTION_DATE).append(") = ? ");
        queryBuilder.append("GROUP BY T.").append(DatabaseHelper.COLUMN_CATEGORY_ID).append(" ");
        queryBuilder.append("ORDER BY total_amount DESC ");
        queryBuilder.append("LIMIT 3");

        String query = queryBuilder.toString();

        return db.rawQuery(query, new String[]{month});
    }

    /**
     * Lấy tổng chi tiêu của tuần hiện tại.
     * @return tổng số tiền chi tiêu.
     */
    public static double getTotalForCurrentWeek(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0;

        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " " +
                "WHERE " + DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = 'Expense' AND " +
                "strftime('%Y-%W', " + DatabaseHelper.COLUMN_TRANSACTION_DATE + ") = strftime('%Y-%W', 'now', 'localtime')";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        }
        return total;
    }

    /**
     * Lấy tổng chi tiêu của tuần trước.
     * @return tổng số tiền chi tiêu.
     */
    public static double getTotalForPreviousWeek(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0;

        // Lấy số năm và số tuần của tuần trước
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        String lastWeekYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(cal.getTime());
        String lastWeekNumber = new SimpleDateFormat("WW", Locale.getDefault()).format(cal.getTime());


        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " " +
                "WHERE " + DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = 'Expense' AND " +
                "strftime('%Y-%W', " + DatabaseHelper.COLUMN_TRANSACTION_DATE + ") = '" + lastWeekYear + "-" + lastWeekNumber + "'";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        }
        return total;
    }

    /**
     * Lấy tổng chi tiêu của tháng hiện tại.
     * @param context Context của ứng dụng
     * @return tổng số tiền chi tiêu
     */
    public static double getTotalForCurrentMonth(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0;

        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " " +
                "WHERE " + DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = 'Expense' AND " +
                "strftime('%Y-%m', " + DatabaseHelper.COLUMN_TRANSACTION_DATE + ") = strftime('%Y-%m', 'now', 'localtime')";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        }
        return total;
    }

    /**
     * Lấy tổng chi tiêu của tháng trước.
     * @param context Context của ứng dụng
     * @return tổng số tiền chi tiêu
     */
    public static double getTotalForPreviousMonth(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0;

        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " " +
                "WHERE " + DatabaseHelper.COLUMN_TRANSACTION_TYPE + " = 'Expense' AND " +
                "strftime('%Y-%m', " + DatabaseHelper.COLUMN_TRANSACTION_DATE + ") = strftime('%Y-%m', 'now', 'localtime', '-1 month')";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        }
        return total;
    }
}