package com.example.noname;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noname.account.BaseActivity;
import com.example.noname.adapters.ChatMessagesAdapter;
import com.example.noname.models.ChatMessage;
import com.example.noname.utils.GeminiApiManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends BaseActivity implements ChatMessagesAdapter.OnSuggestedQuestionClickListener {

    private TextInputEditText etChatInput;
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

        initializeViews();
        setupRecyclerView();
        setupListeners();

        // Gửi tin nhắn chào mừng ban đầu
        chatAdapter.addMessage(new ChatMessage(getString(R.string.chatbot_welcome_message), ChatMessage.SENDER_BOT));
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        performApiPrewarming();
    }

    private void initializeViews() {
        ImageButton btnBackChatbot = findViewById(R.id.btn_back_chatbot);
        etChatInput = findViewById(R.id.et_chat_input);
        ImageButton btnSendChat = findViewById(R.id.btn_send_chat);
        recyclerViewChat = findViewById(R.id.recycler_view_chat);
    }

    private void setupRecyclerView() {
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatMessagesAdapter(chatMessageList, this, this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.btn_back_chatbot).setOnClickListener(v -> finish());

        findViewById(R.id.btn_send_chat).setOnClickListener(v -> {
            String message = etChatInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageAndGetReply(message);
                etChatInput.setText("");
                hideKeyboard();
            } else {
                Toast.makeText(this, getString(R.string.error_please_enter_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessageAndGetReply(String message) {
        chatAdapter.addMessage(new ChatMessage(message, ChatMessage.SENDER_USER));
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        ChatMessage loadingMessage = new ChatMessage(getString(R.string.chatbot_responding), ChatMessage.SENDER_BOT);
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
                    removeLoadingMessage(loadingMessage);
                    chatAdapter.addMessage(new ChatMessage(responseText, ChatMessage.SENDER_BOT));
                    recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
                    Toast.makeText(ChatbotActivity.this, getString(R.string.chatbot_response_received), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("ChatbotActivity", "Gemini API failure: " + errorMessage);
                    removeLoadingMessage(loadingMessage);
                    String errorText = getString(R.string.chatbot_error_prefix, errorMessage);
                    chatAdapter.addMessage(new ChatMessage(errorText, ChatMessage.SENDER_BOT));
                    recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
                    String toastError = getString(R.string.chatbot_connection_error, errorMessage);
                    Toast.makeText(ChatbotActivity.this, toastError, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void removeLoadingMessage(ChatMessage loadingMessage) {
        int index = chatMessageList.indexOf(loadingMessage);
        if (index != -1) {
            chatMessageList.remove(index);
            chatAdapter.notifyItemRemoved(index);
        }
    }

    private void performApiPrewarming() {
        GeminiApiManager.generateContent("ping", new GeminiApiManager.GeminiApiResponseListener() {
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
        sendMessageAndGetReply(question);
        hideKeyboard();
    }

    private void hideKeyboard() {
        etChatInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etChatInput.getWindowToken(), 0);
        }
    }
}