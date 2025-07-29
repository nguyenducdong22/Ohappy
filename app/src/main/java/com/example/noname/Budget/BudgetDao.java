package com.example.noname.Budget; // Đảm bảo package này là đúng sau khi refactor

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao // Chú thích này đánh dấu đây là một Data Access Object
public interface BudgetDao {

    @Insert // Chèn một đối tượng Budget mới vào cơ sở dữ liệu
    void insert(Budget budget);

    @Update // Cập nhật một đối tượng Budget hiện có
    void update(Budget budget);

    @Delete // Xóa một đối tượng Budget
    void delete(Budget budget);

    @Query("SELECT * FROM budgets ORDER BY id DESC") // Truy vấn tất cả ngân sách, sắp xếp theo ID giảm dần
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE id = :id") // Truy vấn ngân sách theo ID
    Budget getBudgetById(int id);

    // Bạn có thể thêm các truy vấn tùy chỉnh khác ở đây tùy theo nhu cầu
    // Ví dụ: Lấy ngân sách theo tên nhóm
    // @Query("SELECT * FROM budgets WHERE group_name = :groupName")
    // List<Budget> getBudgetsByGroupName(String groupName);
}