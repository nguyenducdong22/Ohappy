package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AddtransactionActivity extends AppCompatActivity {

    private static final int CHOOSE_GROUP_REQUEST_CODE = 1; // Một mã yêu cầu duy nhất
    private TextView tvSelectedGroup; // Biến để giữ tham chiếu đến TextView hiển thị nhóm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction); // Đảm bảo đúng layout của màn hình "Thêm Giao Dịch"

        // Ánh xạ TextView từ layout
        tvSelectedGroup = findViewById(R.id.tv_selected_group);

        // Thiết lập trình lắng nghe sự kiện click cho phần "Chọn nhóm"
        // Thay R.id.layout_select_group bằng ID của View mà bạn click vào để mở màn hình chọn nhóm
        findViewById(R.id.layout_select_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddtransactionActivity.this, ChooseGroupActivity.class);
                // Bắt đầu Activity và mong đợi kết quả trả về
                startActivityForResult(intent, CHOOSE_GROUP_REQUEST_CODE);
            }
        });

        // ... (các khởi tạo và logic khác của AddtransactionActivity)
    }

    // Phương thức này được gọi khi Activity con (ChooseGroupActivity) kết thúc và trả về kết quả
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra xem đây có phải là kết quả từ ChooseGroupActivity và kết quả là OK
        if (requestCode == CHOOSE_GROUP_REQUEST_CODE && resultCode == RESULT_OK) {
            // Kiểm tra xem Intent có dữ liệu và có chứa "selected_group_name" không
            if (data != null && data.hasExtra("selected_group_name")) {
                String selectedGroupName = data.getStringExtra("selected_group_name");
                // Cập nhật TextView với tên nhóm đã chọn
                tvSelectedGroup.setText(selectedGroupName);
                // Bạn có thể hiển thị một Toast để xác nhận
                // Toast.makeText(this, "Nhóm đã chọn: " + selectedGroupName, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
