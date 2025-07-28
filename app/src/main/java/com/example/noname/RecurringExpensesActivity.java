package com.example.noname;

import android.content.Context; // Đảm bảo có import này
import android.content.Intent;
import android.content.SharedPreferences; // Đảm bảo có import này
import android.os.Bundle;
import android.view.LayoutInflater; // Đảm bảo có import này
import android.view.View;
import android.view.ViewGroup; // Đảm bảo có import này
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
// import android.widget.Toolbar; // XÓA DÒNG NÀY (Android widget Toolbar)

import androidx.annotation.NonNull; // Đảm bảo có import này
import androidx.annotation.Nullable; // Đảm bảo có import này
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // THAY THẾ BẰNG DÒNG NÀY (AndroidX Toolbar)
import androidx.recyclerview.widget.LinearLayoutManager; // Đảm bảo có import này
import androidx.recyclerview.widget.RecyclerView; // Đảm bảo có import này
import androidx.core.content.ContextCompat; // Đảm bảo có import này
import androidx.recyclerview.widget.ItemTouchHelper; // Đảm bảo có import này

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog; // Đảm bảo có import này

import java.text.NumberFormat; // Đảm bảo có import này
import java.util.ArrayList; // Đảm bảo có import này
import java.util.List; // Đảm bảo có import này
import java.util.Locale; // Đảm bảo có import này

public class RecurringExpensesActivity extends AppCompatActivity {

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
        setSupportActionBar(toolbar); // Hàm này giờ sẽ đúng với androidx.appcompat.widget.Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi Tiêu Định Kỳ");
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

        // Khởi tạo danh sách (sẽ được tải từ database sau)
        recurringExpenseList = new ArrayList<>();
        // Dữ liệu mẫu (chỉ để xem trước giao diện, sẽ bị xóa khi dùng database)
        // Đảm bảo các ID icon và màu sắc ở đây là đúng và tồn tại trong res/drawable và res/values/colors.xml
        recurringExpenseList.add(new RecurringExpense(1, "Tiền thuê nhà", 5000000, "Hàng tháng", "Ngày 15", R.drawable.ic_home_and_utility, R.color.primary_green_dark, "active"));
        recurringExpenseList.add(new RecurringExpense(2, "Tiền điện", 500000, "Hàng tháng", "Ngày cuối", R.drawable.ic_lightbulb, R.color.accent_yellow_dark, "active"));
        recurringExpenseList.add(new RecurringExpense(3, "Tiền internet", 200000, "Hàng tháng", "Ngày 1", R.drawable.ic_wifi, R.color.primary_green_dark, "active"));
        recurringExpenseList.add(new RecurringExpense(4, "Học phí", 15000000, "Hàng năm", "Ngày 10/9", R.drawable.ic_learning, R.color.accent_yellow_dark, "active"));
        recurringExpenseList.add(new RecurringExpense(5, "Đóng quỹ lớp", 50000, "Hàng tháng", "Ngày 5", R.drawable.ic_people, R.color.primary_green_dark, "active"));

        adapter = new RecurringExpenseAdapter(recurringExpenseList);
        recyclerView.setAdapter(adapter);

        // Cập nhật trạng thái rỗng ban đầu
        updateEmptyState();
    }

    // Phương thức này sẽ được gọi khi AddEditRecurringExpenseActivity trả về kết quả
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Dữ liệu được làm mới (chưa có DB)", Toast.LENGTH_SHORT).show();

            if (requestCode == ADD_RECURRING_EXPENSE_REQUEST) {
                // Thêm một item mẫu mới vào đầu danh sách (để demo)
                String name = (data != null && data.hasExtra("RECURRING_EXPENSE_NAME_DEMO")) ? data.getStringExtra("RECURRING_EXPENSE_NAME_DEMO") : "Khoản mới (mẫu)";
                double amount = (data != null && data.hasExtra("RECURRING_EXPENSE_AMOUNT_DEMO")) ? data.getDoubleExtra("RECURRING_EXPENSE_AMOUNT_DEMO", 0.0) : 123456;
                String nextDate = (data != null && data.hasExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO")) ? data.getStringExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO") : "Ngày ?";
                String frequency = (data != null && data.hasExtra("RECURRING_EXPENSE_FREQUENCY_DEMO")) ? data.getStringExtra("RECURRING_EXPENSE_FREQUENCY_DEMO") : "Hàng tháng";
                int iconResId = (data != null && data.hasExtra("RECURRING_EXPENSE_ICON_RES_ID_DEMO")) ? data.getIntExtra("RECURRING_EXPENSE_ICON_RES_ID_DEMO", R.drawable.ic_category) : R.drawable.ic_category;
                int iconTintColorResId = (data != null && data.hasExtra("RECURRING_EXPENSE_ICON_TINT_COLOR_RES_ID_DEMO")) ? data.getIntExtra("RECURRING_EXPENSE_ICON_TINT_COLOR_RES_ID_DEMO", R.color.accent_yellow) : R.color.accent_yellow;
                String status = (data != null && data.hasExtra("RECURRING_EXPENSE_STATUS_DEMO")) ? data.getStringExtra("RECURRING_EXPENSE_STATUS_DEMO") : "active";

                recurringExpenseList.add(0, new RecurringExpense(
                        System.currentTimeMillis(), // ID tạm
                        name, amount, frequency, nextDate, iconResId, iconTintColorResId, status
                ));
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
            } else if (requestCode == EDIT_RECURRING_EXPENSE_REQUEST) {
                // Để demo, bạn có thể tải lại toàn bộ danh sách (đơn giản nhất khi không có DB)
                Toast.makeText(this, "Khoản chi định kỳ đã được cập nhật/xóa (mẫu)", Toast.LENGTH_SHORT).show();
                // Để demo việc làm mới sau sửa/xóa, chúng ta sẽ xóa hết và thêm lại dữ liệu mẫu
                // Trong thực tế, bạn sẽ tải lại từ database
                recurringExpenseList.clear();
                recurringExpenseList.add(new RecurringExpense(1, "Tiền thuê nhà (mẫu cập nhật)", 5100000, "Hàng tháng", "Ngày 15", R.drawable.ic_home_and_utility, R.color.primary_green_dark, "active"));
                recurringExpenseList.add(new RecurringExpense(2, "Tiền điện (mẫu cập nhật)", 550000, "Hàng tháng", "Ngày cuối", R.drawable.ic_lightbulb, R.color.accent_yellow_dark, "active"));
                // ... thêm lại các mục khác nếu muốn
                adapter.notifyDataSetChanged();
            }
            updateEmptyState();
        }
    }


    // Phương thức để cập nhật trạng thái rỗng
    private void updateEmptyState() {
        if (recurringExpenseList.isEmpty()) {
            tvEmptyStateMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyStateMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Lớp dữ liệu cho Chi tiêu định kỳ (Đã cập nhật để có ID và Status)
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

        // --- Getters (cần thiết cho Adapter) ---
        public long getId() { return id; }
        public String getName() { return name; }
        public double getAmount() { return amount; }
        public String getFrequency() { return frequency; }
        public String getNextDate() { return nextDate; }
        public int getIconResId() { return iconResId; }
        public int getIconTintColorResId() { return iconTintColorResId; }
        public String getStatus() { return status; }
    }

    // Adapter cho RecyclerView
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
            holder.nameTextView.setText(expense.getName());
            holder.frequencyNextDateTextView.setText(String.format(Locale.getDefault(), "%s (%s)", expense.getFrequency(), expense.getNextDate()));

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedAmount = formatter.format(expense.getAmount());
            holder.amountTextView.setText(String.format("%s VND", formattedAmount));

            holder.iconImageView.setImageResource(expense.getIconResId());
            holder.iconImageView.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), expense.getIconTintColorResId()));
            holder.amountTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.button_red));
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

                // Xử lý sự kiện click cho từng item để chỉnh sửa
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        RecurringExpense clickedExpense = expenses.get(position);

                        // Mở màn hình Thêm/Sửa để chỉnh sửa
                        Intent intent = new Intent(v.getContext(), AddEditRecurringExpenseActivity.class);
                        // Truyền tất cả dữ liệu của khoản chi tiêu sang màn hình sửa
                        intent.putExtra("RECURRING_EXPENSE_ID", clickedExpense.getId());
                        intent.putExtra("RECURRING_EXPENSE_NAME_DEMO", clickedExpense.getName());
                        intent.putExtra("RECURRING_EXPENSE_AMOUNT_DEMO", clickedExpense.getAmount());
                        intent.putExtra("RECURRING_EXPENSE_FREQUENCY_DEMO", clickedExpense.getFrequency());
                        intent.putExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO", clickedExpense.getNextDate());
                        intent.putExtra("RECURRING_EXPENSE_ICON_RES_ID_DEMO", clickedExpense.getIconResId());
                        intent.putExtra("RECURRING_EXPENSE_ICON_TINT_COLOR_RES_ID_DEMO", clickedExpense.getIconTintColorResId());
                        intent.putExtra("RECURRING_EXPENSE_STATUS_DEMO", clickedExpense.getStatus());

                        // Dùng startActivityForResult từ context của Activity cha
                        ((AppCompatActivity) v.getContext()).startActivityForResult(intent, EDIT_RECURRING_EXPENSE_REQUEST);
                    }
                });
            }
        }
    }
}