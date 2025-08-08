// OverviewSavingGoldActivity.java
package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.adapters.SavingGoalAdapter;
import com.example.noname.models.SavingGoal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.noname.R;

import java.util.ArrayList;
import java.util.List;

public class OverviewSavingGoldActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddSavingGoal;
    private TextView tvEmptyStateMessage;
    private Toolbar toolbar;

    private List<SavingGoal> savingGoalsList;
    private SavingGoalAdapter savingGoalAdapter;

    // Khởi tạo ActivityResultLauncher để nhận kết quả từ Activity khác
    private ActivityResultLauncher<Intent> addSavingGoalLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview_saving_gold);

        toolbar = findViewById(R.id.toolbar_saving_goals_overview);
        recyclerView = findViewById(R.id.recyclerViewSavingGoals);
        fabAddSavingGoal = findViewById(R.id.fab_add_saving_goal);
        tvEmptyStateMessage = findViewById(R.id.tv_empty_state_message);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_saving_goals));
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        savingGoalsList = new ArrayList<>();
        savingGoalAdapter = new SavingGoalAdapter(savingGoalsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(savingGoalAdapter);

        // Khởi tạo và định nghĩa logic xử lý kết quả
        addSavingGoalLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        SavingGoal newGoal = result.getData().getParcelableExtra(SetSavingGoldActivity.EXTRA_NEW_SAVING_GOAL);
                        if (newGoal != null) {
                            savingGoalsList.add(newGoal);
                            savingGoalAdapter.notifyItemInserted(savingGoalsList.size() - 1); // Thông báo cho adapter có mục mới
                            checkEmptyState();
                            recyclerView.smoothScrollToPosition(savingGoalsList.size() - 1); // Cuộn xuống mục mới
                        }
                    }
                }
        );

        fabAddSavingGoal.setOnClickListener(v -> {
            Intent intent = new Intent(OverviewSavingGoldActivity.this, SetSavingGoldActivity.class);
            addSavingGoalLauncher.launch(intent); // Sử dụng launcher để mở Activity và đợi kết quả
        });

        checkEmptyState();
    }

    private void checkEmptyState() {
        if (savingGoalsList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyStateMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyStateMessage.setVisibility(View.GONE);
        }
    }
}