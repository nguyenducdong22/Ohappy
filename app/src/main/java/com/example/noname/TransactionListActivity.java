package com.example.noname;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.noname.models.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private TextView tvEmptyStateMessage;

    // Request Codes (được dùng để phân biệt kết quả trả về từ AddtransactionActivity)
    private static final int ADD_TRANSACTION_REQUEST = 1;
    private static final int EDIT_TRANSACTION_REQUEST = 2;
    // DELETE_TRANSACTION_REQUEST không cần thiết ở đây vì AddtransactionActivity tự xử lý việc xóa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        Toolbar toolbar = findViewById(R.id.toolbar_transaction_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh Sách Giao Dịch");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvEmptyStateMessage = findViewById(R.id.tv_empty_state_message_transactions);

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(transactionList, this);
        recyclerView.setAdapter(adapter);

        // Lấy dữ liệu giao dịch từ Intent chỉ khi Activity này được tạo mới
        // Truyền getIntent() để lấy Intent đã khởi chạy Activity này
        displayTransactionFromIntent(getIntent()); // <-- SỬA Ở ĐÂY
        updateEmptyState();

        setupSwipeToDelete();
    }

    /**
     * Phương thức này sẽ nhận dữ liệu từ Intent khi AddtransactionActivity trả về kết quả.
     * Cập nhật danh sách hiển thị.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            // Kiểm tra nếu là yêu cầu xóa từ AddtransactionActivity
            if (data.getBooleanExtra("ACTION_DELETE", false)) {
                long deletedId = data.getLongExtra("TRANSACTION_ID", -1);
                if (deletedId != -1) {
                    // Xóa khỏi danh sách hiện tại
                    for (int i = 0; i < transactionList.size(); i++) {
                        if (transactionList.get(i).getId() == deletedId) {
                            transactionList.remove(i);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                    updateEmptyState();
                    Toast.makeText(this, "Đã xóa giao dịch (chỉ trên giao diện).", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Đây là dữ liệu được thêm mới hoặc cập nhật từ AddtransactionActivity
                // Cập nhật danh sách với dữ liệu mới nhận được
                displayTransactionFromIntent(data); // Đúng khi gọi từ onActivityResult
                Toast.makeText(this, "Giao dịch đã được cập nhật/thêm.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Lấy dữ liệu giao dịch từ Intent và hiển thị lên RecyclerView.
     * @param intentData Intent chứa dữ liệu giao dịch.
     */
    private void displayTransactionFromIntent(@Nullable Intent intentData) {
        if (intentData != null && intentData.hasExtra("TRANSACTION_AMOUNT")) { // Kiểm tra để đảm bảo có dữ liệu giao dịch
            // Lấy dữ liệu từng trường từ Intent
            long id = intentData.getLongExtra("TRANSACTION_ID", System.currentTimeMillis());
            long userId = intentData.getLongExtra("TRANSACTION_USER_ID", 0);
            double amount = intentData.getDoubleExtra("TRANSACTION_AMOUNT", 0.0);
            String wallet = intentData.getStringExtra("TRANSACTION_WALLET_NAME");
            String group = intentData.getStringExtra("TRANSACTION_GROUP_NAME");
            int iconResId = intentData.getIntExtra("TRANSACTION_ICON_RES_ID", 0);
            int colorResId = intentData.getIntExtra("TRANSACTION_COLOR_RES_ID", 0);
            long date = intentData.getLongExtra("TRANSACTION_DATE", 0);
            String note = intentData.getStringExtra("TRANSACTION_NOTE");
            String type = intentData.getStringExtra("TRANSACTION_TYPE");

            // Tạo đối tượng Transaction từ dữ liệu Intent
            if (wallet == null) wallet = "Không rõ ví";
            if (group == null) group = "Không rõ nhóm";
            if (note == null) note = "";
            if (type == null) type = "Expense";

            Transaction receivedTransaction = new Transaction(
                    id, userId, amount, wallet, group, iconResId, colorResId, date, note, type
            );

            // Tìm kiếm và cập nhật hoặc thêm mới vào danh sách
            boolean found = false;
            for (int i = 0; i < transactionList.size(); i++) {
                // Sử dụng getId() để so sánh, đảm bảo Transaction model có getId()
                if (transactionList.get(i).getId() == receivedTransaction.getId()) {
                    transactionList.set(i, receivedTransaction); // Cập nhật
                    adapter.notifyItemChanged(i);
                    found = true;
                    Log.d("TransactionListActivity", "Updated transaction from Intent: " + receivedTransaction.getId());
                    break;
                }
            }
            if (!found) { // Nếu không tìm thấy (là thêm mới)
                transactionList.add(0, receivedTransaction); // Thêm vào đầu danh sách
                adapter.notifyItemInserted(0);
                Log.d("TransactionListActivity", "Added new transaction from Intent. ID: " + receivedTransaction.getId());
            }
            recyclerView.scrollToPosition(0);
            updateEmptyState();
        } else {
            Log.d("TransactionListActivity", "No valid Intent data received (or no TRANSACTION_AMOUNT extra).");
            // Toast.makeText(this, "Không có dữ liệu giao dịch để hiển thị.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Cập nhật hiển thị thông báo "chưa có giao dịch nào".
     */
    private void updateEmptyState() {
        if (transactionList.isEmpty()) {
            tvEmptyStateMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyStateMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // --- Thiết lập vuốt để xóa (Swipe to Delete) ---
    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Transaction transactionToDelete = transactionList.get(position);

                new AlertDialog.Builder(TransactionListActivity.this)
                        .setTitle("Xóa Giao Dịch")
                        .setMessage("Bạn có chắc chắn muốn xóa giao dịch '" + transactionToDelete.getNote() + "' không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // Xóa khỏi danh sách hiển thị
                            transactionList.remove(position);
                            adapter.notifyItemRemoved(position);
                            updateEmptyState();
                            Toast.makeText(TransactionListActivity.this, "Đã xóa (chỉ trên giao diện): " + transactionToDelete.getNote(), Toast.LENGTH_SHORT).show();
                            // Nhắc lại: Nếu có database, sẽ gọi dao.deleteTransaction(transactionToDelete.getId());
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // Đặt lại item nếu hủy
                            dialog.dismiss();
                        })
                        .setOnCancelListener(dialog -> {
                            adapter.notifyItemChanged(position); // Đặt lại item nếu hủy bằng cách chạm ra ngoài
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Không cần tải lại dữ liệu từ Intent trong onResume, vì nó sẽ gây trùng lặp
        // Dữ liệu chỉ được truyền một lần khi Activity được khởi tạo hoặc khi onActivityResult trả về
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // --- ADAPTER CHO RECYCLERVIEW ---
    public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

        private List<Transaction> transactions;
        private Context context;
        private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public TransactionAdapter(List<Transaction> transactions, Context context) {
            this.transactions = transactions;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction transaction = transactions.get(position);

            holder.categoryNameTextView.setText(transaction.getGroupName());
            holder.descriptionTextView.setText(transaction.getNote());

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedAmount = formatter.format(transaction.getAmount());

            if (transaction.getType() != null && transaction.getType().equalsIgnoreCase("Income")) {
                holder.amountTextView.setText(String.format("+ %s VND", formattedAmount));
                holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.primary_green_dark));
            } else {
                holder.amountTextView.setText(String.format("- %s VND", formattedAmount));
                holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.button_red));
            }

            String dateString = sdf.format(new Date(transaction.getTransactionDate()));

            holder.walletDateTextView.setText(String.format("%s - %s", transaction.getWalletName(), dateString));

            if (transaction.getGroupIconResId() != 0) {
                holder.iconImageView.setImageResource(transaction.getGroupIconResId());
            } else {
                holder.iconImageView.setImageResource(R.drawable.ic_category);
            }
            if (transaction.getGroupColorResId() != 0) {
                holder.iconImageView.setColorFilter(ContextCompat.getColor(context, transaction.getGroupColorResId()), PorterDuff.Mode.SRC_IN);
            } else {
                holder.iconImageView.setColorFilter(ContextCompat.getColor(context, R.color.primary_green_dark), PorterDuff.Mode.SRC_IN);
            }

            // --- Xử lý sự kiện click để SỬA ---
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, Addtransaction.class); // Mở lại AddtransactionActivity
                // Truyền dữ liệu của giao dịch hiện tại để chỉnh sửa
                intent.putExtra("TRANSACTION_ID", transaction.getId()); // ID để biết đang sửa cái nào
                intent.putExtra("TRANSACTION_USER_ID", transaction.getUserId());
                intent.putExtra("TRANSACTION_AMOUNT", transaction.getAmount());
                intent.putExtra("TRANSACTION_WALLET_NAME", transaction.getWalletName());
                intent.putExtra("TRANSACTION_GROUP_NAME", transaction.getGroupName());
                intent.putExtra("TRANSACTION_ICON_RES_ID", transaction.getGroupIconResId());
                intent.putExtra("TRANSACTION_COLOR_RES_ID", transaction.getGroupColorResId());
                intent.putExtra("TRANSACTION_DATE", transaction.getTransactionDate());
                intent.putExtra("TRANSACTION_NOTE", transaction.getNote());
                intent.putExtra("TRANSACTION_TYPE", transaction.getType());

                ((AppCompatActivity) context).startActivityForResult(intent, EDIT_TRANSACTION_REQUEST); // Khởi chạy với request code EDIT
            });
        }


        @Override
        public int getItemCount() {
            return transactions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView categoryNameTextView, descriptionTextView, walletDateTextView, amountTextView;
            ImageView iconImageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                categoryNameTextView = itemView.findViewById(R.id.tv_transaction_category_name);
                descriptionTextView = itemView.findViewById(R.id.tv_transaction_description);
                walletDateTextView = itemView.findViewById(R.id.tv_transaction_wallet_date);
                amountTextView = itemView.findViewById(R.id.tv_transaction_amount);
                iconImageView = itemView.findViewById(R.id.img_transaction_category_icon);
            }
        }
    }
}