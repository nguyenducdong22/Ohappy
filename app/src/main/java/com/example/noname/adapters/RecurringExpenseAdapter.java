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
import com.example.noname.database.CategoryDAO;
import com.example.noname.models.Category;
import com.example.noname.models.RecurringExpense;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.RecurringExpenseViewHolder> {

    private static final String TAG = "RecurringExpenseAdapter";
    private List<RecurringExpense> expenses = new ArrayList<>();
    private OnItemClickListener listener;
    private Context context;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat uiDateFormat = new SimpleDateFormat("dd", Locale.getDefault()); // Lấy ngày

    public interface OnItemClickListener {
        void onItemClick(RecurringExpense expense);
    }

    public RecurringExpenseAdapter(OnItemClickListener listener) {
        this.listener = listener;
        this.context = (Context) listener;
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
    }

    public void setExpenses(List<RecurringExpense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecurringExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurring_expense, parent, false);
        return new RecurringExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringExpenseViewHolder holder, int position) {
        RecurringExpense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class RecurringExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExpenseName, tvFrequencyNextDate, tvAmount;
        private ImageView imgCategoryIcon;
        private LinearLayout itemContainer;

        public RecurringExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName = itemView.findViewById(R.id.tv_recurring_expense_name);
            tvFrequencyNextDate = itemView.findViewById(R.id.tv_recurring_expense_frequency_next_date);
            tvAmount = itemView.findViewById(R.id.tv_recurring_expense_amount);
            imgCategoryIcon = itemView.findViewById(R.id.img_recurring_expense_category_icon);
            itemContainer = itemView.findViewById(R.id.item_container);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(expenses.get(position));
                }
            });
        }

        public void bind(RecurringExpense expense) {
            tvExpenseName.setText(expense.getName());
            tvAmount.setText(currencyFormat.format(expense.getAmount()));

            try {
                // Sửa lỗi: Gọi phương thức getStartDate() thay vì getNextDate()
                Date nextDate = dbDateFormat.parse(expense.getStartDate());
                String dayOfMonth = uiDateFormat.format(nextDate);
                String frequencyText = String.format(Locale.getDefault(), "%s (Ngày %s)", expense.getFrequency(), dayOfMonth);
                tvFrequencyNextDate.setText(frequencyText);
            } catch (ParseException e) {
                Log.e(TAG, "Lỗi phân tích ngày: " + e.getMessage());
                tvFrequencyNextDate.setText(String.format(Locale.getDefault(), "%s (N/A)", expense.getFrequency()));
            }

            CategoryDAO categoryDAO = new CategoryDAO(context);
            categoryDAO.open();
            CategoryDAO.CategoryWithIcon categoryInfo = categoryDAO.getCategoryByIdWithIcon(expense.getCategoryId());
            if (categoryInfo != null) {
                imgCategoryIcon.setImageResource(categoryInfo.iconResId);
                imgCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_green_dark));
            } else {
                imgCategoryIcon.setImageResource(R.drawable.ic_category);
                imgCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_dark));
            }
            categoryDAO.close();

            if (!expense.isActive()) {
                itemContainer.setAlpha(0.6f);
            } else {
                itemContainer.setAlpha(1.0f);
            }
        }
    }
}