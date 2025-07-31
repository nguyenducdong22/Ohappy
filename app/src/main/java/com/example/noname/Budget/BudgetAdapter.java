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

/**
 * BudgetAdapter is a RecyclerView.Adapter responsible for displaying a list of Budget objects.
 * It binds Budget data to the individual item views in the RecyclerView,
 * showing category name, icon, total budget, and remaining amount with a progress bar.
 * It also handles click events for item selection and more options (edit/delete).
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList;
    private NumberFormat currencyFormat;
    private OnBudgetActionListener listener; // Listener cho các hành động trên ngân sách.

    /**
     * Interface for communicating budget actions (edit, delete, click) back to the Activity/Fragment.
     */
    public interface OnBudgetActionListener {
        /**
         * Called when the user requests to edit a specific budget.
         * @param budget Đối tượng Budget cần được sửa.
         */
        void onEditBudget(Budget budget);

        /**
         * Called when the user requests to delete a specific budget.
         * @param budget Đối tượng Budget cần được xóa.
         */
        void onDeleteBudget(Budget budget);

        /**
         * Called when the user clicks on a budget item in the list.
         * Mục đích là để hiển thị chi tiết ngân sách này ở phần tổng quan.
         * @param budget Đối tượng Budget được nhấp vào.
         */
        void onBudgetClick(Budget budget);
    }

    /**
     * Constructor for BudgetAdapter.
     * @param budgetList The initial list of Budget objects.
     * @param listener   An instance of OnBudgetActionListener to handle budget-related actions.
     */
    public BudgetAdapter(List<Budget> budgetList, OnBudgetActionListener listener) {
        this.budgetList = budgetList;
        this.listener = listener;

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
    }

    /**
     * Updates the list of budgets and notifies the RecyclerView to refresh its views.
     * @param newBudgetList The new list of Budget objects.
     */
    public void setBudgetList(List<Budget> newBudgetList) {
        this.budgetList = newBudgetList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetAdapter.BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    /**
     * BudgetViewHolder describes an item view and metadata about its place within the RecyclerView.
     * It holds references to the views within each item layout and sets up click listeners.
     */
    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBudgetIcon;
        TextView tvBudgetCategory;
        TextView tvBudgetRemaining;
        TextView tvBudgetTotal;
        ProgressBar progressBar;
        ImageView ivMoreOptions; // Nút ba chấm cho menu tùy chọn.

        /**
         * Constructor for BudgetViewHolder.
         * @param itemView The root view of the item layout (item_budget.xml).
         */
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các UI components
            ivBudgetIcon = itemView.findViewById(R.id.iv_budget_item_icon);
            tvBudgetCategory = itemView.findViewById(R.id.tv_budget_item_category);
            tvBudgetRemaining = itemView.findViewById(R.id.tv_budget_item_remaining);
            tvBudgetTotal = itemView.findViewById(R.id.tv_budget_item_total);
            progressBar = itemView.findViewById(R.id.progress_budget_item);
            ivMoreOptions = itemView.findViewById(R.id.iv_budget_item_more); // SỬA LỖI: ID này bây giờ đã có trong item_budget.xml

            // --- Thiết lập OnClickListener cho TOÀN BỘ item View ---
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                        Budget clickedBudget = budgetList.get(adapterPosition);
                        listener.onBudgetClick(clickedBudget);
                    }
                }
            });

            // --- Thiết lập OnClickListener cho biểu tượng ba chấm (More Options) ---
            ivMoreOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        Budget clickedBudget = budgetList.get(adapterPosition);
                        showPopupMenu(v, clickedBudget);
                    }
                }
            });
        }

        /**
         * Gắn dữ liệu từ một đối tượng Budget vào các views trong ViewHolder.
         * @param budget Đối tượng Budget chứa dữ liệu để hiển thị.
         */
        public void bind(Budget budget) {
            ivBudgetIcon.setImageResource(budget.getGroupIconResId());
            tvBudgetCategory.setText(budget.getGroupName());
            tvBudgetTotal.setText(currencyFormat.format(budget.getAmount()));

            // TODO: Triển khai logic THỰC TẾ để tính toán `amountSpent`.
            // Phần này sẽ được tính toán trong BudgetOverviewActivity và truyền vào thông qua một hàm setter nếu cần,
            // hoặc tính toán lại tại đây nếu bạn có thể truy cập BudgetDAO.
            // Để đơn giản hóa, hiện tại tôi sẽ để placeholder.
            double amountSpent = 50000.0; // Placeholder
            double remainingAmount = budget.getAmount() - amountSpent;
            tvBudgetRemaining.setText(String.format("Còn lại %s", currencyFormat.format(remainingAmount)));

            int progressPercentage = (int) ((amountSpent / budget.getAmount()) * 100);
            if (progressPercentage > 100) progressPercentage = 100;
            if (progressPercentage < 0) progressPercentage = 0;
            progressBar.setProgress(progressPercentage);
        }

        /**
         * Hiển thị một PopupMenu với "Edit Budget" và "Delete Budget" options.
         * @param view The anchor view for the popup menu (usually the three dots icon).
         * @param budgetToActOn The Budget object associated with the clicked menu.
         */
        private void showPopupMenu(View view, final Budget budgetToActOn) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.budget_item_options_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit_budget) {
                        if (listener != null) {
                            listener.onEditBudget(budgetToActOn);
                        }
                        Toast.makeText(view.getContext(), "Sửa: " + budgetToActOn.getGroupName(), Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.action_delete_budget) {
                        if (listener != null) {
                            listener.onDeleteBudget(budgetToActOn);
                        }
                        Toast.makeText(view.getContext(), "Xóa: " + budgetToActOn.getGroupName(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }
}