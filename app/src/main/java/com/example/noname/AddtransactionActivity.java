package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.noname.account.BaseActivity;

public class AddtransactionActivity extends BaseActivity {

    private static final int CHOOSE_GROUP_REQUEST_CODE = 1;
    private TextView tvSelectedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        tvSelectedGroup = findViewById(R.id.tv_selected_group);

        findViewById(R.id.layout_select_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddtransactionActivity.this, ChooseGroupActivity.class);
                startActivityForResult(intent, CHOOSE_GROUP_REQUEST_CODE);
            }
        });

        // ... các logic khác của bạn
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_GROUP_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selected_group_name")) {
                String selectedGroupName = data.getStringExtra("selected_group_name");
                tvSelectedGroup.setText(selectedGroupName);

                // Bạn yêu cầu không xóa phần comment này
                // Toast.makeText(this, "Nhóm đã chọn: " + selectedGroupName, Toast.LENGTH_SHORT).show();
            }
        }
    }
}