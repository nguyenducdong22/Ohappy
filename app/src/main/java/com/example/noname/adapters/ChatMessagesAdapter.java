package com.example.noname.adapters;

import android.text.SpannableString; // Vẫn cần import này nếu muốn chuyển đổi
import android.text.SpannableStringBuilder; // <<< THÊM IMPORT NÀY >>>
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan; // Giữ nguyên nếu cần
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;
import com.example.noname.models.ChatMessage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessages;

    public ChatMessagesAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getSender();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.SENDER_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_bot, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (holder.getItemViewType() == ChatMessage.SENDER_USER) {
            ((UserMessageViewHolder) holder).bind(message);
        } else {
            ((BotMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text_user);
        }

        void bind(ChatMessage message) {
            messageText.setText(formatMarkdown(message.getMessage()));
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text_bot);
        }

        void bind(ChatMessage message) {
            messageText.setText(formatMarkdown(message.getMessage()));
        }
    }

    // Phương thức xử lý Markdown đơn giản (cho **bold** và *list items*)
    private static CharSequence formatMarkdown(String text) {
        // Xử lý xuống dòng trước
        text = text.replace("\\n", "\n"); // API trả về \n thay vì xuống dòng thật
        text = text.replace("\n\n", "\n"); // Đôi khi có 2 xuống dòng liền
        text = text.replace("* ", "• "); // Chuyển * thành dấu chấm cho list items

        // KHAI BÁO SpannableStringBuilder THAY VÌ SpannableString
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);

        // Xử lý **bold**
        Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(spannableStringBuilder); // Matcher trên StringBuilder

        // Vòng lặp để tìm và áp dụng định dạng.
        // Cần xử lý cẩn thận vị trí khi xóa ký tự để tránh lỗi chỉ mục.
        while (boldMatcher.find()) {
            int start = boldMatcher.start();
            int end = boldMatcher.end();
            String boldText = boldMatcher.group(1); // Lấy văn bản bên trong ** **

            // Áp dụng StyleSpan cho phần văn bản gốc (có bao gồm **)
            spannableStringBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Xóa ký tự ** ở cuối (cần xóa trước vì nó ảnh hưởng đến vị trí start)
            spannableStringBuilder.delete(end - 2, end);
            // Xóa ký tự ** ở đầu
            spannableStringBuilder.delete(start, start + 2);

            // Sau khi xóa ký tự, đối tượng Matcher không còn hợp lệ.
            // Chúng ta cần tạo lại Matcher trên StringBuilder đã sửa đổi.
            // Vì chúng ta đang xử lý từ đầu, và việc xóa ký tự sẽ làm thay đổi độ dài,
            // việc tạo lại matcher trong vòng lặp có thể phức tạp nếu không có index cố định.
            // Một cách đơn giản hơn là tạo một danh sách các Spans cần áp dụng
            // và sau đó áp dụng chúng hoặc xử lý từ cuối chuỗi trở về.

            // Tuy nhiên, với cách hiện tại (xóa ngay lập tức), matcher sẽ bị lỗi.
            // Cách tốt nhất là thu thập các vị trí cần thay đổi và sau đó áp dụng.
            // Hoặc, đơn giản hơn, chỉ áp dụng span và để ** hiển thị,
            // HOẶC sử dụng Html.fromHtml nếu bạn có thể chuyển đổi sang HTML (nhưng Gemini không trả về HTML).

            // Với cách DELETE này, bạn phải TẠO LẠI MATCHER TRONG MỖI LẦN LẶP.
            // Điều này có thể dẫn đến hiệu suất không tối ưu nếu chuỗi rất dài.
            // CÁCH AN TOÀN VÀ ĐƠN GIẢN NHẤT ĐỂ SỬA LỖI NÀY LÀ:
            // 1. Áp dụng span cho vùng có cả ký tự markdown.
            // 2. Chuyển đổi sang chuỗi và lại thay thế ký tự markdown sau đó.
            // HOẶC cách dưới đây:
            // Lấy văn bản cần in đậm sau khi đã xóa các dấu **
            spannableStringBuilder.replace(start, end, boldText);
            spannableStringBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    start, start + boldText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            boldMatcher = boldPattern.matcher(spannableStringBuilder); // TẠO LẠI MATCHER
        }

        return spannableStringBuilder;
    }
}