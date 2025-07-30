package com.example.noname.Budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R; // Ensure your R class is correctly imported.

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * BudgetAdapter is a RecyclerView.Adapter responsible for displaying a list of Budget objects.
 * It binds Budget data to the individual item views in the RecyclerView,
 * showing category name, icon, total budget, and remaining amount with a progress bar.
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList; // The list of Budget objects to display.
    private NumberFormat currencyFormat; // Formatter for displaying currency amounts.

    /**
     * Constructor for BudgetAdapter.
     * @param budgetList The initial list of Budget objects.
     */
    public BudgetAdapter(List<Budget> budgetList) {
        this.budgetList = budgetList;
        // Initialize currency formatter for Vietnamese Dong (VND).
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0); // No decimal places for VND.
        currencyFormat.setMaximumFractionDigits(0);
    }

    /**
     * Updates the list of budgets and notifies the RecyclerView to refresh its views.
     * @param newBudgetList The new list of Budget objects.
     */
    public void setBudgetList(List<Budget> newBudgetList) {
        this.budgetList = newBudgetList;
        notifyDataSetChanged(); // Essential to refresh the RecyclerView UI.
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new BudgetViewHolder that holds a View for the item.
     */
    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_budget layout to create a new view for each budget item.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the ViewHolder to reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position); // Get the Budget object for the current position.
        holder.bind(budget); // Bind the data to the ViewHolder's views.
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of budgets.
     */
    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    /**
     * BudgetViewHolder describes an item view and metadata about its place within the RecyclerView.
     * It holds references to the views within each item layout.
     */
    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBudgetIcon;
        TextView tvBudgetCategory;
        TextView tvBudgetRemaining;
        TextView tvBudgetTotal;
        ProgressBar progressBar;

        /**
         * Constructor for BudgetViewHolder.
         * @param itemView The root view of the item layout (item_budget.xml).
         */
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize all UI elements by finding them by their IDs in the item layout.
            ivBudgetIcon = itemView.findViewById(R.id.iv_budget_item_icon);
            tvBudgetCategory = itemView.findViewById(R.id.tv_budget_item_category);
            tvBudgetRemaining = itemView.findViewById(R.id.tv_budget_item_remaining);
            tvBudgetTotal = itemView.findViewById(R.id.tv_budget_item_total);
            progressBar = itemView.findViewById(R.id.progress_budget_item);
        }

        /**
         * Binds the data from a Budget object to the views in the ViewHolder.
         * @param budget The Budget object containing the data to display.
         */
        public void bind(Budget budget) {
            ivBudgetIcon.setImageResource(budget.getGroupIconResId()); // Set the category icon.
            tvBudgetCategory.setText(budget.getGroupName()); // Set the category name.
            tvBudgetTotal.setText(currencyFormat.format(budget.getAmount())); // Display total budgeted amount.

            // TODO: Implement actual logic to calculate `amountSpent`.
            // This would involve querying the `transactions` table using `budget.getCategoryId()`
            // and `budget.getStartDate()`/`budget.getEndDate()`.
            double amountSpent = 50000.0; // Placeholder: Replace with actual calculated spent amount.
            double remainingAmount = budget.getAmount() - amountSpent;
            tvBudgetRemaining.setText(String.format("Còn lại %s", currencyFormat.format(remainingAmount)));

            // Update ProgressBar based on spending progress.
            int progressPercentage = (int) ((amountSpent / budget.getAmount()) * 100);
            if (progressPercentage > 100) progressPercentage = 100; // Cap at 100% if overspent.
            if (progressPercentage < 0) progressPercentage = 0;     // Ensure non-negative progress.
            progressBar.setProgress(progressPercentage);
            // You can also change ProgressBar color based on `progressPercentage` if desired.
        }
    }
}