package com.example.noname.account;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.example.noname.R;


public class HelpCenterActivity extends BaseActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        toolbar = findViewById(R.id.toolbar_help_center);
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}