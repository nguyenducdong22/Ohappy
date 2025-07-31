package com.example.noname; // CHÍNH XÁC: package com.example.noname;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.models.Transaction; // Import model Transaction

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

        // Tải dữ liệu từ Intent
        displayTransactionFromIntent();

        updateEmptyState();
    }

    /**
     * Lấy dữ liệu giao dịch từ Intent và hiển thị lên RecyclerView.
     */
    private void displayTransactionFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy dữ liệu từng trường từ Intent
            long userId = intent.getLongExtra("TRANSACTION_USER_ID", 0);
            double amount = intent.getDoubleExtra("TRANSACTION_AMOUNT", 0.0);
            String wallet = intent.getStringExtra("TRANSACTION_WALLET_NAME");
            String group = intent.getStringExtra("TRANSACTION_GROUP_NAME");
            int iconResId = intent.getIntExtra("TRANSACTION_ICON_RES_ID", 0);
            int colorResId = intent.getIntExtra("TRANSACTION_COLOR_RES_ID", 0);
            long date = intent.getLongExtra("TRANSACTION_DATE", 0);
            String note = intent.getStringExtra("TRANSACTION_NOTE");
            String type = intent.getStringExtra("TRANSACTION_TYPE");

            // Tạo đối tượng Transaction từ dữ liệu Intent
            // Kiểm tra null cho String để tránh crash nếu Intent không có extra
            if (wallet == null) wallet = "Không rõ ví";
            if (group == null) group = "Không rõ nhóm";
            if (note == null) note = "";
            if (type == null) type = "Expense"; // Mặc định

            Transaction receivedTransaction = new Transaction(
                    userId, amount, wallet, group, iconResId, colorResId, date, note, type
            );

            // Xóa danh sách cũ và thêm giao dịch mới nhận được
            transactionList.clear();
            transactionList.add(receivedTransaction);
            adapter.notifyDataSetChanged();
            Log.d("TransactionListActivity", "Displayed 1 transaction from Intent.");
        } else {
            Log.d("TransactionListActivity", "No Intent data received.");
            Toast.makeText(this, "Không có dữ liệu giao dịch để hiển thị.", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Trong trường hợp này, onResume không cần tải lại dữ liệu vì dữ liệu được truyền qua Intent
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // --- ADAPTER CHO RECYCLERVIEW ---
    // Lớp Adapter này có thể được tách ra thành file riêng TransactionAdapter.java
    public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

        private List<Transaction> transactions;
        private Context context;

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

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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

                itemView.setOnClickListener(v -> {
                    // Logic khi item được click
                });
            }
        }
    }
}