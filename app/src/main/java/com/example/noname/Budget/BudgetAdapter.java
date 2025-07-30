package com.example.noname.Budget;

import android.content.Context; // THÊM IMPORT NÀY
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList;
    private NumberFormat currencyFormat;
    private Context context; // Thêm biến Context

    public BudgetAdapter(List<Budget> budgetList) {
        this.budgetList = budgetList;
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
    }

    public void setBudgetList(List<Budget> newBudgetList) {
        this.budgetList = newBudgetList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu lại context
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBudgetIcon;
        TextView tvBudgetCategory;
        TextView tvBudgetRemaining;
        TextView tvBudgetTotal;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBudgetIcon = itemView.findViewById(R.id.iv_budget_item_icon);
            tvBudgetCategory = itemView.findViewById(R.id.tv_budget_item_category);
            tvBudgetRemaining = itemView.findViewById(R.id.tv_budget_item_remaining);
            tvBudgetTotal = itemView.findViewById(R.id.tv_budget_item_total);
            progressBar = itemView.findViewById(R.id.progress_budget_item);
        }

        public void bind(Budget budget) {
            ivBudgetIcon.setImageResource(budget.getGroupIconResId());
            tvBudgetCategory.setText(budget.getGroupName());
            tvBudgetTotal.setText(currencyFormat.format(budget.getAmount()));

            double amountSpent = 50000.0;
            double remainingAmount = budget.getAmount() - amountSpent;

            // SỬ DỤNG TÀI NGUYÊN CHUỖI
            tvBudgetRemaining.setText(context.getString(R.string.budget_remaining_format, currencyFormat.format(remainingAmount)));

            int progressPercentage = (int) ((amountSpent / budget.getAmount()) * 100);
            if (progressPercentage > 100) progressPercentage = 100;
            if (progressPercentage < 0) progressPercentage = 0;
            progressBar.setProgress(progressPercentage);
        }
    }
}