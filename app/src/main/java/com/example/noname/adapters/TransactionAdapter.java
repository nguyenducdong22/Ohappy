package com.example.noname.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;
import com.example.noname.models.Transaction;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private static final String TAG = "TransactionAdapter";

    private final Context context;
    private List<Transaction> transactions;
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("en")); // Changed to English locale
    private String lastHeaderDate = "";

    public TransactionAdapter(Context context) {
        this.context = context;
        this.transactions = new ArrayList<>();
    }

    public void setTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        this.lastHeaderDate = "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        try {
            Date transactionDate = fullDateFormat.parse(transaction.getTransactionDate());

            // Get day string for header comparison
            String currentHeaderDate = dayFormat.format(transactionDate);
            String monthAndYear = monthFormat.format(transactionDate);

            // Show header only if the date has changed or it's the first item
            if (!currentHeaderDate.equals(lastHeaderDate) || position == 0) {
                holder.headerLayout.setVisibility(View.VISIBLE);
                holder.tvDay.setText(currentHeaderDate);

                // Add logic to check for "Today"
                if (isToday(transactionDate)) {
                    // You'll need to update "Hôm nay" in your strings or locale
                    holder.tvMonth.setText("Today\n" + monthAndYear);
                } else {
                    holder.tvMonth.setText(monthAndYear);
                }
                lastHeaderDate = currentHeaderDate;
            } else {
                holder.headerLayout.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date string: " + transaction.getTransactionDate(), e);
            holder.headerLayout.setVisibility(View.GONE);
        }

        // Get icon name from Transaction and find resource ID
        int iconResId = context.getResources().getIdentifier(transaction.getIconName(), "drawable", context.getPackageName());
        if (iconResId != 0) {
            holder.ivCategoryIcon.setImageResource(iconResId);
        } else {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_other);
        }

        // Display category name and description
        holder.tvCategoryName.setText(transaction.getCategoryName());
        holder.tvDescription.setText(transaction.getDescription());

        // Format and display the amount
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
        String formattedAmount = currencyFormat.format(transaction.getAmount());

        // Change amount color based on transaction type
        if ("Income".equals(transaction.getType())) {
            holder.tvAmount.setText("+" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.primary_green_dark));
        } else {
            holder.tvAmount.setText("-" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_item_red));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private boolean isToday(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = sdf.format(date);
        String todayString = sdf.format(new Date());
        return dateString.equals(todayString);
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        LinearLayout headerLayout;
        TextView tvDay, tvMonth;
        ImageView ivCategoryIcon;
        TextView tvCategoryName, tvDescription;
        TextView tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            headerLayout = itemView.findViewById(R.id.header_date_layout);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvMonth = itemView.findViewById(R.id.tv_month_and_year);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvDescription = itemView.findViewById(R.id.tv_transaction_description);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
        }
    }
}