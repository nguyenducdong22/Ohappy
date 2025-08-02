package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.noname.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context context;

    private static final String TAG = "CategoryDAO";

    public CategoryDAO(Context context) {
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

    // Thêm một lớp dữ liệu tĩnh để đóng gói Category và ID icon
    public static class CategoryWithIcon {
        public Category category;
        public int iconResId;

        public CategoryWithIcon(Category category, int iconResId) {
            this.category = category;
            this.iconResId = iconResId;
        }
    }

    /**
     * Lấy một danh mục bằng ID và bao gồm ID tài nguyên của icon.
     * @param categoryId ID của danh mục.
     * @return Một đối tượng CategoryWithIcon hoặc null.
     */
    public CategoryWithIcon getCategoryByIdWithIcon(long categoryId) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    new String[]{
                            DatabaseHelper.COLUMN_ID,
                            DatabaseHelper.COLUMN_USER_ID_FK,
                            DatabaseHelper.COLUMN_CATEGORY_NAME,
                            DatabaseHelper.COLUMN_CATEGORY_TYPE,
                            DatabaseHelper.COLUMN_ICON_NAME,
                            DatabaseHelper.COLUMN_COLOR_CODE
                    },
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(categoryId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                Category category = cursorToCategory(cursor);
                int iconResId = getIconResIdFromName(category.getIconName());
                return new CategoryWithIcon(category, iconResId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category with icon: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Lấy tất cả các danh mục của một người dùng, bao gồm các danh mục mặc định.
     * @param userId ID người dùng.
     * @return Danh sách các đối tượng Category.
     */
    public List<Category> getAllCategories(long userId) {
        List<Category> categories = new ArrayList<>();
        Cursor cursor = null;
        try {
            // Lấy các danh mục mặc định (userId is NULL) hoặc các danh mục của người dùng hiện tại
            String selection = DatabaseHelper.COLUMN_USER_ID_FK + " IS NULL OR " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            cursor = database.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    new String[]{
                            DatabaseHelper.COLUMN_ID,
                            DatabaseHelper.COLUMN_USER_ID_FK,
                            DatabaseHelper.COLUMN_CATEGORY_NAME,
                            DatabaseHelper.COLUMN_CATEGORY_TYPE,
                            DatabaseHelper.COLUMN_ICON_NAME,
                            DatabaseHelper.COLUMN_COLOR_CODE
                    },
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    categories.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all categories: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categories;
    }

    private Category cursorToCategory(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_TYPE));
        String iconName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME));
        String colorCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR_CODE));

        Long userId = null;
        int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID_FK);
        if (userIdColumnIndex != -1 && !cursor.isNull(userIdColumnIndex)) {
            userId = cursor.getLong(userIdColumnIndex);
        }

        // Chuyển đổi tên icon thành ID tài nguyên để sử dụng trong UI
        int iconResId = getIconResIdFromName(iconName);

        return new Category(id, name, type, iconName, iconResId, colorCode, userId);
    }

    private int getIconResIdFromName(String iconName) {
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    // TODO: Bổ sung các phương thức DAO khác như addCategory, etc.
}