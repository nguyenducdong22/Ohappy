package com.example.noname.Budget; // Đảm bảo package này đúng

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Định nghĩa cơ sở dữ liệu Room:
// entities: Các lớp Entity mà cơ sở dữ liệu này sẽ chứa (ở đây là Budget.class)
// version: Phiên bản cơ sở dữ liệu (tăng lên nếu cấu trúc thay đổi)
// exportSchema: Có xuất schema ra file để kiểm tra không (nên là false trong môi trường production)
@Database(entities = {Budget.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Phương thức trừu tượng để lấy DAO của bạn
    public abstract BudgetDao budgetDao();

    // Đối tượng INSTANCE volatile để đảm bảo khả năng hiển thị trên các luồng
    private static volatile AppDatabase INSTANCE;

    // Phương thức singleton để lấy thể hiện của cơ sở dữ liệu
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) { // Kiểm tra nếu thể hiện chưa được tạo
            synchronized (AppDatabase.class) { // Đồng bộ hóa để tránh tạo nhiều thể hiện trong môi trường đa luồng
                if (INSTANCE == null) { // Kiểm tra lại sau khi đồng bộ hóa
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "budget_database") // Xây dựng cơ sở dữ liệu với tên "budget_database"
                            // CHO MỤC ĐÍCH ĐƠN GIẢN: Cho phép truy vấn trên luồng chính.
                            // TRONG ỨNG DỤNG THỰC TẾ: KHÔNG NÊN sử dụng allowMainThreadQueries().
                            // Hãy thực hiện các thao tác DB trên luồng nền (AsyncTask, Executor, Coroutines).
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}