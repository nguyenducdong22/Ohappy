package com.example.noname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.adapters.RecurringExpenseAdapter;
import com.example.noname.database.RecurringExpenseDAO;
import com.example.noname.models.RecurringExpense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class RecurringExpensesActivity extends AppCompatActivity implements RecurringExpenseAdapter.OnItemClickListener {

    private static final String TAG = "RecurringExpensesActivity";
    private static final int ADD_EDIT_RECURRING_EXPENSE_REQUEST_CODE = 1;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecurringExpenseAdapter adapter;
    private TextView emptyStateMessage;
    private FloatingActionButton fabAdd;

    private RecurringExpenseDAO recurringExpenseDAO;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expenses);

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recurringExpenseDAO = new RecurringExpenseDAO(this);

        toolbar = findViewById(R.id.toolbar_recurring_expenses);
        recyclerView = findViewById(R.id.recyclerViewRecurringExpenses);
        emptyStateMessage = findViewById(R.id.tv_empty_state_message);
        fabAdd = findViewById(R.id.fab_add_recurring_expense);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecurringExpenseAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRecurringExpenseActivity.class);
            startActivityForResult(intent, ADD_EDIT_RECURRING_EXPENSE_REQUEST_CODE);
        });

        loadRecurringExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecurringExpenses();
    }

    private void loadRecurringExpenses() {
        recurringExpenseDAO.open();
        List<RecurringExpense> expenses = recurringExpenseDAO.getAllRecurringExpenses(currentUserId);
        recurringExpenseDAO.close();

        if (expenses.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateMessage.setVisibility(View.GONE);
            adapter.setExpenses(expenses);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EDIT_RECURRING_EXPENSE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            loadRecurringExpenses();
        }
    }

    @Override
    public void onItemClick(RecurringExpense expense) {
        Intent intent = new Intent(this, AddEditRecurringExpenseActivity.class);
        intent.putExtra(AddEditRecurringExpenseActivity.EXTRA_RECURRING_EXPENSE_ID, expense.getId());
        startActivityForResult(intent, ADD_EDIT_RECURRING_EXPENSE_REQUEST_CODE);
    }
}