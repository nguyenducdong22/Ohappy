package com.example.noname.account;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.database.LocaleHelper;

// Lớp này sẽ tự động áp dụng ngôn ngữ cho mọi Activity kế thừa nó
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Luôn gọi LocaleHelper để áp dụng ngôn ngữ đã lưu
        // trước khi Activity được tạo
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}