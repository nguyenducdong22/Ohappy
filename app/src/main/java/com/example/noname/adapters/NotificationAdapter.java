package com.example.noname.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;
import com.example.noname.models.NotificationItem;
import com.example.noname.database.NotificationDAO;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private Context context;
    private NotificationDAO notificationDAO;

    public NotificationAdapter(List<NotificationItem> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
        this.notificationDAO = new NotificationDAO(context);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        holder.tvMessage.setText(item.getMessage());
        holder.tvTimestamp.setText(item.getCreatedAt());

        // Thay đổi màu sắc nền hoặc icon nếu thông báo chưa đọc
        if (!item.isRead()) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.background_light_pink));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        // Đặt lắng nghe sự kiện khi click vào một mục thông báo
        holder.itemView.setOnClickListener(v -> {
            if (!item.isRead()) {
                // Đánh dấu thông báo là đã đọc trong database
                notificationDAO.markAsRead(item.getId());
                // Cập nhật lại giao diện
                item.setRead(true);
                notifyItemChanged(position);
                // Có thể thêm hành động khác sau khi đọc, ví dụ: cập nhật lại badge trên trang chủ
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimestamp;
        // Có thể thêm ImageView cho icon nếu bạn muốn

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.notification_message);
            tvTimestamp = itemView.findViewById(R.id.notification_timestamp);
        }
    }
}