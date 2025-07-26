package com.example.noname;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log; // Import Log

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.google.android.material.textfield.TextInputEditText;
import com.example.noname.utils.GeminiApiManager;
import com.example.noname.models.ChatMessage; // Import ChatMessage model
import com.example.noname.adapters.ChatMessagesAdapter; // Import Adapter

import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List

public class ChatbotActivity extends AppCompatActivity {

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

        // Khởi tạo RecyclerView và Adapter
        recyclerViewChat = findViewById(R.id.recycler_view_chat);
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatMessagesAdapter(chatMessageList);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        btnBackChatbot.setOnClickListener(v -> finish());

        btnSendChat.setOnClickListener(v -> {
            String message = etChatInput.getText().toString().trim();
            if (!message.isEmpty()) {
                // Thêm tin nhắn người dùng vào danh sách và hiển thị
                chatAdapter.addMessage(new ChatMessage(message, ChatMessage.SENDER_USER));
                recyclerViewChat.scrollToPosition(chatMessageList.size() - 1); // Cuộn xuống cuối
                etChatInput.setText(""); // Xóa trường nhập liệu

                // Thêm tin nhắn "Đang phản hồi..." từ chatbot
                ChatMessage loadingMessage = new ChatMessage("Đang phản hồi...", ChatMessage.SENDER_BOT);
                chatAdapter.addMessage(loadingMessage);
                recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

                // Gửi yêu cầu đến API Gemini
                callGeminiApi(message, loadingMessage);

            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });

        // Thêm lời chào ban đầu từ chatbot
        chatAdapter.addMessage(new ChatMessage("Chào bạn! Tôi có thể giúp gì cho bạn?", ChatMessage.SENDER_BOT));
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
    }

    private void callGeminiApi(String prompt, ChatMessage loadingMessage) {
        Log.d("ChatbotActivity", "Calling Gemini API with prompt: " + prompt);

        GeminiApiManager.generateContent(prompt, new GeminiApiManager.GeminiApiResponseListener() {
            @Override
            public void onSuccess(String responseText) {
                runOnUiThread(() -> {
                    Log.d("ChatbotActivity", "Gemini API success: " + responseText);
                    // Xóa tin nhắn "Đang phản hồi..."
                    chatMessageList.remove(loadingMessage);
                    chatAdapter.notifyItemRemoved(chatMessageList.size()); // Thông báo xóa item cuối cùng

                    // Thêm phản hồi thực sự của chatbot
                    chatAdapter.addMessage(new ChatMessage(responseText, ChatMessage.SENDER_BOT));
                    recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
                    Toast.makeText(ChatbotActivity.this, "Đã nhận phản hồi từ AI!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("ChatbotActivity", "Gemini API failure: " + errorMessage);
                    // Xóa tin nhắn "Đang phản hồi..."
                    chatMessageList.remove(loadingMessage);
                    chatAdapter.notifyItemRemoved(chatMessageList.size()); // Thông báo xóa item cuối cùng

                    // Thêm tin nhắn lỗi
                    chatAdapter.addMessage(new ChatMessage("Lỗi: " + errorMessage, ChatMessage.SENDER_BOT));
                    recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
                    Toast.makeText(ChatbotActivity.this, "Lỗi kết nối AI: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}