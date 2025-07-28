package com.example.noname;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import android.content.Context; // <<< THÊM IMPORT NÀY >>>
import android.view.inputmethod.InputMethodManager; // <<< THÊM IMPORT NÀY >>>


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.example.noname.utils.GeminiApiManager;
import com.example.noname.models.ChatMessage;
import com.example.noname.adapters.ChatMessagesAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity implements ChatMessagesAdapter.OnSuggestedQuestionClickListener {

    private ImageButton btnBackChatbot;
    private TextInputEditText etChatInput;
    private ImageButton btnSendChat;

    private RecyclerView recyclerViewChat;
    private ChatMessagesAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnBackChatbot = findViewById(R.id.btn_back_chatbot);
        etChatInput = findViewById(R.id.et_chat_input);
        btnSendChat = findViewById(R.id.btn_send_chat);

        recyclerViewChat = findViewById(R.id.recycler_view_chat);
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatMessagesAdapter(chatMessageList, this, this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        btnBackChatbot.setOnClickListener(v -> finish());

        btnSendChat.setOnClickListener(v -> {
            String message = etChatInput.getText().toString().trim();
            if (!message.isEmpty()) {
                // Gọi phương thức gửi và nhận phản hồi
                sendMessageAndGetReply(message);

                // <<< THÊM CÁC DÒNG NÀY ĐỂ XÓA VĂN BẢN VÀ ẨN BÀN PHÍM >>>
                etChatInput.setText(""); // Xóa văn bản
                etChatInput.clearFocus(); // Xóa focus khỏi trường nhập liệu

                // Ẩn bàn phím ảo
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etChatInput.getWindowToken(), 0);
                }
                // <<< KẾT THÚC THÊM >>>

            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });

        chatAdapter.addMessage(new ChatMessage("Chào bạn! Tôi có thể giúp gì cho bạn?", ChatMessage.SENDER_BOT));
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        performApiPrewarming();
    }

    private void sendMessageAndGetReply(String message) {
        chatAdapter.addMessage(new ChatMessage(message, ChatMessage.SENDER_USER));
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        ChatMessage loadingMessage = new ChatMessage("Đang phản hồi...", ChatMessage.SENDER_BOT);
        chatAdapter.addMessage(loadingMessage);
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        callGeminiApi(message, loadingMessage);
    }


    private void callGeminiApi(String prompt, ChatMessage loadingMessage) {
        Log.d("ChatbotActivity", "Calling Gemini API with prompt: " + prompt);

        GeminiApiManager.generateContent(prompt, new GeminiApiManager.GeminiApiResponseListener() {
            @Override
            public void onSuccess(String responseText) {
                runOnUiThread(() -> {
                    Log.d("ChatbotActivity", "Gemini API success: " + responseText);

                    int index = chatMessageList.indexOf(loadingMessage);
                    if (index != -1) {
                        chatMessageList.remove(index);
                        chatAdapter.notifyItemRemoved(index);
                    } else {
                        chatAdapter.notifyDataSetChanged();
                    }

                    chatAdapter.addMessage(new ChatMessage(responseText, ChatMessage.SENDER_BOT));
                    recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
                    Toast.makeText(ChatbotActivity.this, "Đã nhận phản hồi từ AI!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("ChatbotActivity", "Gemini API failure: " + errorMessage);

                    int index = chatMessageList.indexOf(loadingMessage);
                    if (index != -1) {
                        chatMessageList.remove(index);
                        chatAdapter.notifyItemRemoved(index);
                    } else {
                        chatAdapter.notifyDataSetChanged();
                    }

                    chatAdapter.addMessage(new ChatMessage("Lỗi: " + errorMessage, ChatMessage.SENDER_BOT));
                    recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
                    Toast.makeText(ChatbotActivity.this, "Lỗi kết nối AI: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void performApiPrewarming() {
        String dummyPrompt = "ping";

        GeminiApiManager.generateContent(dummyPrompt, new GeminiApiManager.GeminiApiResponseListener() {
            @Override
            public void onSuccess(String responseText) {
                Log.d("ChatbotActivity", "API pre-warming successful.");
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ChatbotActivity", "API pre-warming failed: " + errorMessage);
            }
        });
    }

    @Override
    public void onSuggestedQuestionClick(String question) {
        // Khi một câu hỏi gợi ý được nhấp, gửi nó như một tin nhắn mới
        sendMessageAndGetReply(question);

        // Optional: Xóa bàn phím và focus sau khi click gợi ý
        etChatInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etChatInput.getWindowToken(), 0);
        }
    }
}