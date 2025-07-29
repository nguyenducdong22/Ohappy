package com.example.noname.Budget; // Đảm bảo package này đúng

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R; // Đảm bảo import đúng R class của bạn

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList;
    private NumberFormat currencyFormat;

    public BudgetAdapter(List<Budget> budgetList) {
        this.budgetList = budgetList;
        // Khởi tạo NumberFormat ở đây để tránh tạo lại nhiều lần
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
    }

    // Phương thức để cập nhật dữ liệu cho Adapter
    public void setBudgetList(List<Budget> newBudgetList) {
        this.budgetList = newBudgetList;
        notifyDataSetChanged(); // Thông báo cho RecyclerView rằng dữ liệu đã thay đổi
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
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

            // TODO: Logic tính toán số tiền đã chi và còn lại thực tế
            // Hiện tại dùng giá trị mẫu, bạn cần thay thế bằng dữ liệu giao dịch thực tế từ DB của bạn
            double amountSpent = 50000.0; // Đây là giá trị ví dụ, bạn cần lấy từ dữ liệu giao dịch
            double remainingAmount = budget.getAmount() - amountSpent;
            tvBudgetRemaining.setText(String.format("Còn lại %s", currencyFormat.format(remainingAmount)));

            // Cập nhật ProgressBar
            // Đảm bảo budget.getAmount() không phải là 0 để tránh chia cho 0
            int progressPercentage = 0;
            if (budget.getAmount() > 0) {
                progressPercentage = (int) ((amountSpent / budget.getAmount()) * 100);
            }
            if (progressPercentage > 100) progressPercentage = 100;
            if (progressPercentage < 0) progressPercentage = 0;
            progressBar.setProgress(progressPercentage);
            // Bạn có thể thay đổi màu của ProgressBar dựa trên tiến độ nếu muốn
        }
    }
}