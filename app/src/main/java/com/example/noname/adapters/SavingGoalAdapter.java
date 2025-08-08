// SavingGoalAdapter.java
package com.example.noname.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;
import com.example.noname.models.SavingGoal;

import java.util.List;

public class SavingGoalAdapter extends RecyclerView.Adapter<SavingGoalAdapter.ViewHolder> {

    private final List<SavingGoal> mData;
    private final Context mContext;

    public SavingGoalAdapter(List<SavingGoal> data, Context context) {
        this.mData = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saving_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingGoal goal = mData.get(position);
        holder.tvGoalName.setText(goal.getName());
        holder.tvTargetDate.setText("Target: " + goal.getTargetDate());

        String progressText = String.format("%,d ₫ / %,d ₫", goal.getCurrentAmount(), goal.getTargetAmount());
        holder.tvProgressAmount.setText(progressText);

        // Tính toán và đặt tiến độ cho ProgressBar
        int progress = (int) ((double) goal.getCurrentAmount() / goal.getTargetAmount() * 100);
        holder.progressBar.setProgress(progress);

        holder.itemView.setOnClickListener(v -> {
            // Khi nhấn vào một mục, bạn có thể hiển thị một Toast hoặc một Dialog chi tiết
            Toast.makeText(mContext, "Bạn đã nhấn vào mục tiêu: " + goal.getName(), Toast.LENGTH_SHORT).show();
            // Nếu sau này bạn muốn chỉnh sửa, bạn sẽ mở SetSavingGoldActivity với dữ liệu của mục tiêu này.
            // Ví dụ:
            // Intent intent = new Intent(mContext, SetSavingGoldActivity.class);
            // intent.putExtra(SetSavingGoldActivity.EXTRA_SAVING_GOAL_ID, goal.getId()); // Nếu SavingGoal có ID
            // intent.putExtra(SetSavingGoldActivity.EXTRA_NEW_SAVING_GOAL, goal); // Gửi cả đối tượng Parcelable
            // mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName;
        TextView tvTargetDate;
        TextView tvProgressAmount;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tv_saving_goal_name);
            tvTargetDate = itemView.findViewById(R.id.tv_saving_goal_target_date);
            tvProgressAmount = itemView.findViewById(R.id.tv_saving_goal_progress_amount);
            progressBar = itemView.findViewById(R.id.progressBarSavingGoal);
        }
    }
}