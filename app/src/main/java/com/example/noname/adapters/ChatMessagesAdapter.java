package com.example.noname.adapters;

import android.graphics.Typeface; // Import này để dùng Typeface.BOLD
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;
import com.example.noname.models.ChatMessage;

import java.util.ArrayList; // Cần thêm import này
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
        // Có thể thêm một Handler.postDelayed để cuộn mượt mà hơn
        // recyclerViewChat.scrollToPosition(chatMessages.size() - 1); // Cần tham chiếu tới RecyclerView từ Adapter
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
        // 1. Xử lý xuống dòng trước
        // Thay thế "\\n" thành "\n" (nếu API trả về escape characters)
        // và thay thế "\n\n" thành "\n" (có thể không cần thiết nếu \n đã đúng)
        text = text.replace("\\n", "\n");
        // text = text.replace("\n\n", "\n"); // Đôi khi có 2 xuống dòng liền, cân nhắc bỏ nếu không gây vấn đề

        // 2. Chuyển đổi * thành dấu chấm cho list items
        // Cần đảm bảo rằng nó chỉ thay đổi khi * đứng đầu dòng hoặc sau dấu cách
        // Để đơn giản, ta sẽ chỉ thay thế "* " thành "• "
        text = text.replace("* ", "• ");


        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);

        // 3. Xử lý **bold**
        // Mẫu regex tìm kiếm hai dấu sao, sau đó là bất kỳ ký tự nào không phải xuống dòng (lazy),
        // và kết thúc bằng hai dấu sao.
        Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(spannableStringBuilder);

        // Danh sách để lưu trữ các vị trí và nội dung cần định dạng
        List<int[]> boldRanges = new ArrayList<>(); // Lưu start, end, boldText.length()

        // Bước 1: Tìm tất cả các khớp và lưu lại thông tin
        while (boldMatcher.find()) {
            int start = boldMatcher.start();
            int end = boldMatcher.end();
            String boldText = boldMatcher.group(1); // Lấy văn bản bên trong ** **
            boldRanges.add(new int[]{start, end, boldText.length()});
        }

        // Bước 2: Duyệt ngược lại danh sách các khớp để áp dụng span và xóa ký tự
        // Duyệt ngược để việc xóa ký tự không làm sai lệch chỉ mục của các khớp chưa xử lý
        for (int i = boldRanges.size() - 1; i >= 0; i--) {
            int[] range = boldRanges.get(i);
            int start = range[0];
            int end = range[1];
            int boldTextLength = range[2];

            // Lấy lại văn bản bên trong ** ** từ StringBuilder
            // Điều này cần thiết vì chuỗi ban đầu đã bị thay đổi nếu có nhiều bold
            // Tuy nhiên, nếu dùng group(1) từ matcher ban đầu, ta đã có nội dung
            String originalBoldContent = text.substring(start + 2, end - 2); // Lấy từ chuỗi 'text' ban đầu

            // Thay thế "******" bằng nội dung in đậm
            spannableStringBuilder.replace(start, end, originalBoldContent);

            // Áp dụng kiểu in đậm cho văn bản đã thay thế
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD),
                    start, start + originalBoldContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableStringBuilder;
    }
}