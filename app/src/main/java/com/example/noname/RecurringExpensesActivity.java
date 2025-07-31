package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Để lấy User ID
import android.os.Bundle;
import android.util.Log; // Thêm import Log
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// IMPORT CÁC LỚP TỪ DATABASE VÀ MODELS
import com.example.noname.database.RecurringExpensesDao;
import com.example.noname.models.RecurringExpense;


public class RecurringExpensesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecurringExpenseAdapter adapter;
    private List<RecurringExpense> recurringExpenseList;
    private TextView tvEmptyStateMessage;

    private RecurringExpensesDao recurringExpensesDao; // Khai báo DAO
    private long currentUserId; // ID người dùng hiện tại

    private static final int ADD_RECURRING_EXPENSE_REQUEST = 1;
    private static final int EDIT_RECURRING_EXPENSE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expenses);

        // Khởi tạo DAO
        recurringExpensesDao = new RecurringExpensesDao(this);

        // LẤY USER ID HIỆN TẠI TỪ SHARED PREFERENCES
        // Đảm bảo bạn đã lưu USER_ID khi người dùng đăng nhập thành công.
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE); // Thay "LoginPrefs" bằng tên SharedPreferences của bạn
        currentUserId = preferences.getLong("USER_ID", -1); // "USER_ID" là key, -1 là giá trị mặc định nếu không tìm thấy
        Log.d("RecurringExpensesActivity", "Current User ID from SharedPreferences: " + currentUserId); // Log ID người dùng

        if (currentUserId == -1) {
            // TẠM THỜI gán một USER_ID cứng để không bị crash khi phát triển
            // TRONG ỨNG DỤNG THẬT, bạn phải Đảm bảo USER_ID được thiết lập hợp lệ từ màn hình đăng nhập!
            currentUserId = 1;
            Log.w("RecurringExpensesActivity", "USER_ID not found in SharedPreferences, using temporary ID: " + currentUserId);
            Toast.makeText(this, "DEBUG: Sử dụng ID người dùng tạm thời (ID: 1).", Toast.LENGTH_LONG).show();
            // Nếu bạn muốn ép người dùng đăng nhập:
            // Toast.makeText(this, "Lỗi: Không tìm thấy ID người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // finish();
            // return;
        }


        Toolbar toolbar = findViewById(R.id.toolbar_recurring_expenses);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi Tiêu Định Kỳ");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_recurring_expense);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(RecurringExpensesActivity.this, AddEditRecurringExpenseActivity.class);
            // Có thể truyền USER_ID đến AddEditRecurringExpenseActivity nếu bạn cần nó ở đó
            intent.putExtra("CURRENT_USER_ID", currentUserId);
            startActivityForResult(intent, ADD_RECURRING_EXPENSE_REQUEST);
        });

        recyclerView = findViewById(R.id.recyclerViewRecurringExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvEmptyStateMessage = findViewById(R.id.tv_empty_state_message);

        recurringExpenseList = new ArrayList<>();
        adapter = new RecurringExpenseAdapter(recurringExpenseList);
        recyclerView.setAdapter(adapter);

        // Tải dữ liệu từ database khi Activity được tạo
        loadRecurringExpenses();

        // Cập nhật trạng thái rỗng ban đầu
        updateEmptyState();

        // Thiết lập ItemTouchHelper để xử lý vuốt xóa
        setupSwipeToDelete();
    }

    /**
     * Tải tất cả các khoản chi tiêu định kỳ cho người dùng hiện tại từ database
     * và cập nhật RecyclerView.
     */
    private void loadRecurringExpenses() {
        recurringExpenseList.clear();
        recurringExpenseList.addAll(recurringExpensesDao.getAllRecurringExpenses(currentUserId));
        adapter.notifyDataSetChanged(); // Rất quan trọng để adapter cập nhật UI
        updateEmptyState();
        Log.d("RecurringExpensesActivity", "Loaded " + recurringExpenseList.size() + " recurring expenses for user " + currentUserId);
    }

    // Phương thức này sẽ được gọi khi AddEditRecurringExpenseActivity trả về kết quả
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            // Kiểm tra nếu là hành động xóa được gửi từ AddEditRecurringExpenseActivity
            boolean isDeleteAction = data.getBooleanExtra("ACTION_DELETE", false);
            long expenseIdFromIntent = data.getLongExtra("RECURRING_EXPENSE_ID", -1);

            if (isDeleteAction && expenseIdFromIntent != -1) {
                // Đây là yêu cầu xóa từ AddEditRecurringExpenseActivity
                // Hiển thị hộp thoại xác nhận xóa (hoặc xóa ngay nếu đã xác nhận trong AddEdit)
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu định kỳ này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            int rowsAffected = recurringExpensesDao.deleteRecurringExpense(expenseIdFromIntent);
                            if (rowsAffected > 0) {
                                loadRecurringExpenses(); // Tải lại danh sách sau khi xóa
                                Toast.makeText(RecurringExpensesActivity.this, "Đã xóa khoản chi định kỳ.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RecurringExpensesActivity.this, "Không thể xóa khoản chi định kỳ.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null) // Không làm gì nếu hủy
                        .show();

            } else {
                // Đây là hành động Thêm Mới hoặc Cập Nhật từ AddEditRecurringExpenseActivity
                // Lấy dữ liệu từ Intent
                long expenseId = data.getLongExtra("RECURRING_EXPENSE_ID", -1); // ID sẽ là -1 nếu là thêm mới
                String name = data.getStringExtra("RECURRING_EXPENSE_NAME_DEMO");
                double amount = data.getDoubleExtra("RECURRING_EXPENSE_AMOUNT_DEMO", 0.0);
                String frequency = data.getStringExtra("RECURRING_EXPENSE_FREQUENCY_DEMO");
                String nextDate = data.getStringExtra("RECURRING_EXPENSE_NEXT_DATE_DEMO");
                int iconResId = data.getIntExtra("RECURRING_EXPENSE_ICON_RES_ID_DEMO", 0);
                int iconTintColorResId = data.getIntExtra("RECURRING_EXPENSE_ICON_TINT_COLOR_RES_ID_DEMO", 0);
                String status = data.getStringExtra("RECURRING_EXPENSE_STATUS_DEMO");
                String type = data.getStringExtra("RECURRING_EXPENSE_TYPE_DEMO");

                if (type == null || type.isEmpty()) {
                    type = "Expense"; // Mặc định là "Expense" nếu không được truyền
                }

                RecurringExpense receivedExpense = new RecurringExpense(
                        expenseId, name, amount, type, frequency, nextDate, iconResId, iconTintColorResId, status
                );

                if (expenseId == -1) { // Là thêm mới
                    long newId = recurringExpensesDao.addRecurringExpense(receivedExpense, currentUserId, null, null);
                    if (newId != -1) {
                        Log.d("RecurringExpensesActivity", "New expense added to DB with ID: " + newId);
                        Toast.makeText(this, "Đã thêm khoản chi định kỳ mới!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("RecurringExpensesActivity", "Failed to add new expense to DB.");
                        Toast.makeText(this, "Lỗi khi thêm khoản chi định kỳ.", Toast.LENGTH_SHORT).show();
                    }
                } else { // Là cập nhật
                    int rowsAffected = recurringExpensesDao.updateRecurringExpense(receivedExpense);
                    if (rowsAffected > 0) {
                        Log.d("RecurringExpensesActivity", "Expense updated in DB: " + expenseId);
                        Toast.makeText(this, "Đã cập nhật khoản chi định kỳ.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("RecurringExpensesActivity", "Failed to update expense in DB: " + expenseId);
                        Toast.makeText(this, "Không thể cập nhật khoản chi định kỳ.", Toast.LENGTH_SHORT).show();
                    }
                }
                // Sau khi thêm hoặc cập nhật, tải lại toàn bộ danh sách để hiển thị thay đổi
                loadRecurringExpenses();
            }
        }
        // Nếu resultCode là RESULT_CANCELED, không làm gì cả
    }

    /**
     * Cấu hình vuốt để xóa (swipe-to-delete) cho RecyclerView.
     */
    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không hỗ trợ kéo thả để sắp xếp lại
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final RecurringExpense expenseToDelete = recurringExpenseList.get(position);

                // Hiển thị hộp thoại xác nhận trước khi xóa
                new AlertDialog.Builder(RecurringExpensesActivity.this)
                        .setTitle("Xóa khoản chi định kỳ")
                        .setMessage("Bạn có chắc chắn muốn xóa '" + expenseToDelete.getName() + "' không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // Thực hiện xóa khỏi database
                            int rowsAffected = recurringExpensesDao.deleteRecurringExpense(expenseToDelete.getId());
                            if (rowsAffected > 0) {
                                loadRecurringExpenses(); // Tải lại danh sách sau khi xóa
                                Toast.makeText(RecurringExpensesActivity.this, "Đã xóa: " + expenseToDelete.getName(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RecurringExpensesActivity.this, "Không thể xóa khoản chi định kỳ.", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemChanged(position); // Đặt lại item nếu không xóa được
                            }
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            // Nếu hủy, đặt lại item về vị trí cũ
                            adapter.notifyItemChanged(position);
                            dialog.dismiss();
                        })
                        .setOnCancelListener(dialog -> {
                            // Nếu hộp thoại bị hủy (chạm ra ngoài), đặt lại item
                            adapter.notifyItemChanged(position);
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }


    /**
     * Cập nhật trạng thái TextView hiển thị thông báo rỗng (Empty State Message).
     */
    private void updateEmptyState() {
        if (recurringExpenseList.isEmpty()) {
            tvEmptyStateMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyStateMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Lớp Adapter cho RecyclerView
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

            // Đảm bảo các Resource ID là hợp lệ trước khi sử dụng
            if (expense.getIconResId() != 0) { // Kiểm tra nếu ID không phải 0 (ID hợp lệ)
                holder.iconImageView.setImageResource(expense.getIconResId());
            } else {
                holder.iconImageView.setImageResource(R.drawable.ic_category); // Icon mặc định nếu không tìm thấy
            }

            if (expense.getIconTintColorResId() != 0) { // Kiểm tra nếu ID màu không phải 0
                holder.iconImageView.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), expense.getIconTintColorResId()));
            } else {
                holder.iconImageView.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_medium)); // Màu mặc định
            }

            // Đặt màu số tiền dựa trên loại giao dịch
            if (expense.getType() != null && expense.getType().equals("Income")) {
                holder.amountTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_green_dark));
            } else { // Mặc định là Expense
                holder.amountTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.button_red));
            }
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
                        intent.putExtra("RECURRING_EXPENSE_TYPE_DEMO", clickedExpense.getType()); // Truyền thêm type

                        ((AppCompatActivity) v.getContext()).startActivityForResult(intent, EDIT_RECURRING_EXPENSE_REQUEST);
                    }
                });
            }
        }
    }
}