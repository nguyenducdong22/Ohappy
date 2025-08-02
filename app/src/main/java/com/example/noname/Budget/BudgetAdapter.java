package com.example.noname.Budget;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    public static class BudgetItem {
        public Budget budget;
        public double spentAmount;

        public BudgetItem(Budget budget, double spentAmount) {
            this.budget = budget;
            this.spentAmount = spentAmount;
        }
    }

    private List<BudgetItem> budgetItems;
    private NumberFormat currencyFormat;
    private OnBudgetActionListener listener;

    public interface OnBudgetActionListener {
        void onEditBudget(Budget budget);
        void onDeleteBudget(Budget budget);
        void onBudgetClick(Budget budget);
    }

    public BudgetAdapter(List<BudgetItem> budgetItems, OnBudgetActionListener listener) {
        this.budgetItems = budgetItems;
        this.listener = listener;
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
    }

    public void setBudgetItems(List<BudgetItem> newBudgetItems) {
        this.budgetItems = newBudgetItems;
        notifyDataSetChanged();
    }

    // Phương thức MỚI: Lấy danh sách các BudgetItem hiện tại
    public List<BudgetItem> getBudgetItems() {
        return budgetItems;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetItem item = budgetItems.get(position);
        holder.bind(item.budget, item.spentAmount);
    }

    @Override
    public int getItemCount() {
        return budgetItems.size();
    }

    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBudgetIcon;
        TextView tvBudgetItemCategory;
        TextView tvSpentAmount;
        TextView tvTotalAmount;
        TextView tvProgressPercent;
        ProgressBar budgetProgressBar;
        ImageView btnMoreOptions;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBudgetIcon = itemView.findViewById(R.id.iv_group_icon);
            tvBudgetItemCategory = itemView.findViewById(R.id.tv_group_name);
            tvSpentAmount = itemView.findViewById(R.id.tv_spent_amount);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvProgressPercent = itemView.findViewById(R.id.tv_progress_percent);
            budgetProgressBar = itemView.findViewById(R.id.budget_progress_bar);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);

            itemView.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBudgetClick(budgetItems.get(adapterPosition).budget);
                }
            });

            btnMoreOptions.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    showPopupMenu(v, budgetItems.get(adapterPosition).budget);
                }
            });
        }

        public void bind(Budget budget, double spentAmount) {
            ivBudgetIcon.setImageResource(budget.getGroupIconResId());
            tvBudgetItemCategory.setText(budget.getGroupName());
            tvTotalAmount.setText(currencyFormat.format(budget.getAmount()));
            tvSpentAmount.setText(currencyFormat.format(spentAmount));

            double totalAmount = budget.getAmount();
            int progressPercentage = (int) ((spentAmount / totalAmount) * 100);
            if (progressPercentage > 100) progressPercentage = 100;
            if (progressPercentage < 0) progressPercentage = 0;

            budgetProgressBar.setProgress(progressPercentage);
            tvProgressPercent.setText(progressPercentage + "%");
        }

        private void showPopupMenu(View view, final Budget budgetToActOn) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.budget_item_options_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit_budget) {
                    if (listener != null) {
                        listener.onEditBudget(budgetToActOn);
                    }
                    return true;
                } else if (itemId == R.id.action_delete_budget) {
                    if (listener != null) {
                        listener.onDeleteBudget(budgetToActOn);
                    }
                    return true;
                }
                return false;
            });
            popupMenu.show();
        }
    }
}