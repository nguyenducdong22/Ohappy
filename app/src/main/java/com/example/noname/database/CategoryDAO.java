package com.example.noname.database;

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

    private static final String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_USER_ID_FK,
            DatabaseHelper.COLUMN_CATEGORY_NAME,
            DatabaseHelper.COLUMN_CATEGORY_TYPE,
            DatabaseHelper.COLUMN_ICON_NAME,
            DatabaseHelper.COLUMN_COLOR_CODE
    };

    public CategoryDAO(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Category> getAllCategories(long userId) {
        List<Category> categories = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CATEGORIES,
                allColumns,
                DatabaseHelper.COLUMN_USER_ID_FK + " IS NULL OR " + DatabaseHelper.COLUMN_USER_ID_FK + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Category category = cursorToCategory(cursor);
            categories.add(category);
            cursor.moveToNext();
        }
        cursor.close();
        return categories;
    }

    public Category getCategoryById(long categoryId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CATEGORIES,
                allColumns,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            Category category = cursorToCategory(cursor);
            cursor.close();
            return category;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        // Kiểm tra xem cột user_id_fk có phải null không trước khi lấy giá trị
        int userIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK);
        if (cursor.isNull(userIdIndex)) {
            category.setUserId(null);
        } else {
            category.setUserId(cursor.getLong(userIdIndex));
        }
        category.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME)));
        category.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_TYPE)));
        category.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ICON_NAME)));
        category.setColorCode(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR_CODE)));
        return category;
    }
}