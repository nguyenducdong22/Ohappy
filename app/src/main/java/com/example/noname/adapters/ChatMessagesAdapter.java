package com.example.noname.adapters;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.R;
import com.example.noname.models.ChatMessage;

import java.util.ArrayList;
import java.util.Collections; // Cần import này để sắp xếp
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.TextPaint;
import android.content.Context;


public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessages;
    private OnSuggestedQuestionClickListener suggestedQuestionClickListener;
    private Context context;

    public interface OnSuggestedQuestionClickListener {
        void onSuggestedQuestionClick(String question);
    }

    public ChatMessagesAdapter(List<ChatMessage> chatMessages, OnSuggestedQuestionClickListener listener, Context context) {
        this.chatMessages = chatMessages;
        this.suggestedQuestionClickListener = listener;
        this.context = context;
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
            ((BotMessageViewHolder) holder).bind(message, suggestedQuestionClickListener, context);
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

        void bind(ChatMessage message, OnSuggestedQuestionClickListener listener, Context context) {
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
            messageText.setText(formatAndMakeSuggestionsClickable(message.getMessage(), listener, context));
        }
    }

    // <<< PHƯƠNG THỨC formatMarkdown ĐÃ ĐƯỢC CẬP NHẬT ĐỂ XÓA DẤU SAO SAU KHI ÁP DỤNG ĐỊNH DẠNG >>>
    private static CharSequence formatMarkdown(String text) {
        // 1. Xử lý xuống dòng trước
        text = text.replace("\\n", "\n");

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);

        // 2. Xử lý **bold**
        Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(spannableStringBuilder);

        // List để lưu trữ các vị trí của các cặp ** và nội dung in đậm
        List<int[]> boldMarkers = new ArrayList<>(); // [start_outer, end_outer, start_inner, end_inner]

        while (boldMatcher.find()) {
            int fullMatchStart = boldMatcher.start();
            int fullMatchEnd = boldMatcher.end();
            int innerTextStart = fullMatchStart + 2;
            int innerTextEnd = fullMatchEnd - 2;

            // Áp dụng StyleSpan (in đậm) cho nội dung bên trong ** **
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD),
                    innerTextStart, innerTextEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Lưu trữ vị trí của các dấu ** để xóa sau
            boldMarkers.add(new int[]{fullMatchStart, fullMatchStart + 2}); // Dấu ** mở
            boldMarkers.add(new int[]{fullMatchEnd - 2, fullMatchEnd});     // Dấu ** đóng
        }

        // 3. Xử lý list items (* ) - NÊN LÀM SAU KHI XỬ LÝ BOLD VÀ TRƯỚC KHI XÓA DẤU SAO
        // Tuy nhiên, việc thay thế ký tự đơn lẻ này không làm sai lệch chỉ mục span quá nhiều.
        // Vẫn nên áp dụng nó trên StringBuilder đã được span.
        // Regex cho list items: * theo sau là dấu cách
        Pattern listItemPattern = Pattern.compile("^\\*\\s*(.*)", Pattern.MULTILINE); // Bắt đầu dòng với * và dấu cách
        Matcher listItemMatcher = listItemPattern.matcher(spannableStringBuilder);

        while (listItemMatcher.find()) {
            int start = listItemMatcher.start(0); // Vị trí của *
            int end = listItemMatcher.start(0) + 1; // Vị trí của * (chỉ một ký tự)
            // Thay thế '*' bằng '•'
            spannableStringBuilder.replace(start, end, "•");
        }


        // 4. Xóa các ký tự ** còn lại (nếu có, do regex không bắt được hoặc cách xử lý span phức tạp)
        // Duyệt ngược lại các vị trí đã lưu để xóa an toàn
        // (Sắp xếp theo vị trí cuối giảm dần để tránh thay đổi chỉ mục)
        Collections.sort(boldMarkers, (a, b) -> Integer.compare(b[0], a[0])); // Sort by start index descending

        for (int[] markerRange : boldMarkers) {
            spannableStringBuilder.delete(markerRange[0], markerRange[0] + 2); // Xóa 2 ký tự **
        }

        return spannableStringBuilder;
    }


    private static CharSequence formatAndMakeSuggestionsClickable(String text, OnSuggestedQuestionClickListener listener, Context context) {
        // Áp dụng định dạng Markdown cho toàn bộ văn bản trước
        // Sử dụng một SpannableStringBuilder mới để tránh ảnh hưởng đến đối tượng gốc nếu formatMarkdown trả về SpannableStringBuilder
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(formatMarkdown(text));

        // Regex để tìm phần "Câu hỏi gợi ý: Câu hỏi 1, Câu hỏi 2"
        Pattern suggestionSectionPattern = Pattern.compile("Câu hỏi gợi ý: (.+)");
        Matcher sectionMatcher = suggestionSectionPattern.matcher(spannableBuilder);

        if (sectionMatcher.find()) {
            int sectionStart = sectionMatcher.start();
            // int sectionEnd = sectionMatcher.end(); // Không cần thiết trực tiếp ở đây
            String suggestionsRaw = sectionMatcher.group(1); // Lấy phần "Câu hỏi 1, Câu hỏi 2"

            // Áp dụng định dạng (ví dụ: in đậm) cho "Câu hỏi gợi ý:"
            spannableBuilder.setSpan(new StyleSpan(Typeface.BOLD),
                    sectionStart, sectionStart + "Câu hỏi gợi ý:".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Phân tách từng câu hỏi gợi ý
            String[] individualSuggestions = suggestionsRaw.split(",\\s*");

            // Tìm vị trí của mỗi câu hỏi và làm cho nó có thể nhấp
            int currentSearchIndex = sectionStart + "Câu hỏi gợi ý:".length();

            for (String suggestion : individualSuggestions) {
                String trimmedSuggestion = suggestion.trim();
                if (trimmedSuggestion.isEmpty()) continue;

                // Tìm vị trí của gợi ý này trong spannableBuilder (sau vị trí tìm kiếm hiện tại)
                int startOfSuggestion = spannableBuilder.toString().indexOf(trimmedSuggestion, currentSearchIndex);

                if (startOfSuggestion != -1) {
                    int endOfSuggestion = startOfSuggestion + trimmedSuggestion.length();

                    spannableBuilder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            if (listener != null) {
                                listener.onSuggestedQuestionClick(trimmedSuggestion);
                            }
                        }
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(ContextCompat.getColor(context, R.color.link_color_dark_blue));
                        }
                    }, startOfSuggestion, endOfSuggestion, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    spannableBuilder.setSpan(new UnderlineSpan(), startOfSuggestion, endOfSuggestion, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    currentSearchIndex = endOfSuggestion;
                }
            }
        }

        return spannableBuilder;
    }
}