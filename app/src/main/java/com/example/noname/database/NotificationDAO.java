// File: com.example.noname.database.NotificationDAO.java
package com.example.noname.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.noname.models.NotificationItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationDAO {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private static final String TAG = "NotificationDAO";

    public NotificationDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public long addNotification(long userId, String message, String type) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID_FK, userId);
        values.put(DatabaseHelper.COLUMN_MESSAGE, message);
        values.put(DatabaseHelper.COLUMN_NOTIFICATION_TYPE, type);
        values.put(DatabaseHelper.COLUMN_IS_READ, 0);
        values.put(DatabaseHelper.COLUMN_CREATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        long insertId = -1;
        try {
            open();
            insertId = database.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding notification: " + e.getMessage());
        } finally {
            close();
        }
        return insertId;
    }

    public List<NotificationItem> getAllNotifications(long userId) {
        List<NotificationItem> notifications = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            cursor = database.query(DatabaseHelper.TABLE_NOTIFICATIONS, null,
                    DatabaseHelper.COLUMN_USER_ID_FK + " = ?",
                    new String[]{String.valueOf(userId)}, null, null,
                    DatabaseHelper.COLUMN_CREATED_AT + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    notifications.add(cursorToNotification(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting notifications: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return notifications;
    }

    public int getUnreadNotificationsCount(long userId) {
        int count = 0;
        Cursor cursor = null;
        try {
            open();
            cursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NOTIFICATIONS
                            + " WHERE " + DatabaseHelper.COLUMN_USER_ID_FK + " = ? AND " + DatabaseHelper.COLUMN_IS_READ + " = 0",
                    new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting unread notification count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return count;
    }

    public void markAsRead(long notificationId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_READ, 1);
        try {
            open();
            database.update(DatabaseHelper.TABLE_NOTIFICATIONS, values,
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(notificationId)});
        } catch (Exception e) {
            Log.e(TAG, "Error marking notification as read: " + e.getMessage());
        } finally {
            close();
        }
    }

    private NotificationItem cursorToNotification(Cursor cursor) {
        NotificationItem item = new NotificationItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        item.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID_FK)));
        item.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MESSAGE)));
        item.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_TYPE)));
        item.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_READ)) == 1);
        item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));
        return item;
    }
}