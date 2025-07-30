package com.example.noname;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.account.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecurringExpensesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private RecurringExpenseAdapter adapter;
    private List<RecurringExpense> recurringExpenseList;
    private TextView tvEmptyStateMessage;

    private static final int ADD_RECURRING_EXPENSE_REQUEST = 1;
    private static final int EDIT_RECURRING_EXPENSE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expenses);

        Toolbar toolbar = findViewById(R.id.toolbar_recurring_expenses);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.recurring_expenses_title));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_recurring_expense);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(RecurringExpensesActivity.this, AddEditRecurringExpenseActivity.class);
            startActivityForResult(intent, ADD_RECURRING_EXPENSE_REQUEST);
        });

        recyclerView = findViewById(R.id.recyclerViewRecurringExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvEmptyStateMessage = findViewById(R.id.tv_empty_state_message);

        loadSampleData(); // Tải dữ liệu mẫu

        adapter = new RecurringExpenseAdapter(recurringExpenseList);
        recyclerView.setAdapter(adapter);

        updateEmptyState();
    }

    private void loadSampleData() {
        recurringExpenseList = new ArrayList<>();
        recurringExpenseList.add(new RecurringExpense(1, getString(R.string.recurring_expense_name_placeholder), 5000000, getString(R.string.frequency_monthly), "Ngày 15", R.drawable.ic_home_and_utility, R.color.primary_green_dark, "active"));
        recurringExpenseList.add(new RecurringExpense(2, "Tiền điện", 500000, getString(R.string.frequency_monthly), "Ngày cuối", R.drawable.ic_lightbulb, R.color.accent_yellow_dark, "active"));
        recurringExpenseList.add(new RecurringExpense(3, "Tiền internet", 200000, getString(R.string.frequency_monthly), "Ngày 1", R.drawable.ic_wifi, R.color.primary_green_dark, "active"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.data_refreshed_no_db), Toast.LENGTH_SHORT).show();

            if (requestCode == ADD_RECURRING_EXPENSE_REQUEST) {
                String name = (data != null) ? data.getStringExtra("RECURRING_EXPENSE_NAME_DEMO") : getString(R.string.new_item_placeholder);
                double amount = (data != null) ? data.getDoubleExtra("RECURRING_EXPENSE_AMOUNT_DEMO", 0.0) : 123456;
                String nextDate = (data != null) ? data.getStringExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO") : getString(R.string.next_date_placeholder);
                String frequency = (data != null) ? data.getStringExtra("RECURRING_EXPENSE_FREQUENCY_DEMO") : getString(R.string.frequency_monthly);

                recurringExpenseList.add(0, new RecurringExpense(System.currentTimeMillis(), name, amount, frequency, nextDate, R.drawable.ic_category, R.color.accent_yellow, "active"));
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);

            } else if (requestCode == EDIT_RECURRING_EXPENSE_REQUEST) {
                Toast.makeText(this, getString(R.string.recurring_expense_updated_deleted_toast), Toast.LENGTH_SHORT).show();
                // Trong thực tế, bạn sẽ tải lại từ database
                recurringExpenseList.clear();
                loadSampleData(); // Tải lại dữ liệu mẫu để demo
                adapter.notifyDataSetChanged();
            }
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (recurringExpenseList.isEmpty()) {
            tvEmptyStateMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyStateMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Lớp dữ liệu
    public static class RecurringExpense {
        private long id;
        private String name;
        private double amount;
        private String frequency;
        private String nextDate;
        private int iconResId;
        private int iconTintColorResId;
        private String status;

        public RecurringExpense(long id, String name, double amount, String frequency, String nextDate, int iconResId, int iconTintColorResId, String status) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.frequency = frequency;
            this.nextDate = nextDate;
            this.iconResId = iconResId;
            this.iconTintColorResId = iconTintColorResId;
            this.status = status;
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public double getAmount() { return amount; }
        public String getFrequency() { return frequency; }
        public String getNextDate() { return nextDate; }
        public int getIconResId() { return iconResId; }
        public int getIconTintColorResId() { return iconTintColorResId; }
        public String getStatus() { return status; }
    }

    // Adapter
    public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.ViewHolder> {
        private List<RecurringExpense> expenses;

        public RecurringExpenseAdapter(List<RecurringExpense> expenses) {
            this.expenses = expenses;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurring_expense, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecurringExpense expense = expenses.get(position);
            holder.bind(expense);
        }

        @Override
        public int getItemCount() {
            return expenses.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, frequencyNextDateTextView, amountTextView;
            ImageView iconImageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.tv_recurring_expense_name);
                frequencyNextDateTextView = itemView.findViewById(R.id.tv_recurring_expense_frequency_next_date);
                amountTextView = itemView.findViewById(R.id.tv_recurring_expense_amount);
                iconImageView = itemView.findViewById(R.id.img_recurring_expense_category_icon);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        RecurringExpense clickedExpense = expenses.get(position);
                        Intent intent = new Intent(v.getContext(), AddEditRecurringExpenseActivity.class);
                        intent.putExtra("RECURRING_EXPENSE_ID", clickedExpense.getId());
                        // Truyền dữ liệu mẫu khác nếu cần
                        ((AppCompatActivity) v.getContext()).startActivityForResult(intent, EDIT_RECURRING_EXPENSE_REQUEST);
                    }
                });
            }

            public void bind(RecurringExpense expense) {
                nameTextView.setText(expense.getName());
                frequencyNextDateTextView.setText(String.format(Locale.getDefault(), "%s (%s)", expense.getFrequency(), expense.getNextDate()));
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                String formattedAmount = formatter.format(expense.getAmount());
                amountTextView.setText(String.format("%s VND", formattedAmount));
                iconImageView.setImageResource(expense.getIconResId());
                iconImageView.setColorFilter(ContextCompat.getColor(itemView.getContext(), expense.getIconTintColorResId()));
            }
        }
    }
}