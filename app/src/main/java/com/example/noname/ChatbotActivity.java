package com.example.noname;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import android.content.Context; // <<< ADDED THIS IMPORT >>>
import android.view.inputmethod.InputMethodManager; // <<< ADDED THIS IMPORT >>>


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
                // Call the method to send the message and get a reply
                sendMessageAndGetReply(message);

                // <<< ADD THESE LINES TO CLEAR TEXT AND HIDE THE KEYBOARD >>>
                etChatInput.setText(""); // Clear the text
                etChatInput.clearFocus(); // Clear focus from the input field

                // Hide the soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etChatInput.getWindowToken(), 0);
                }
                // <<< END OF ADDITION >>>

            } else {
                Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show();
            }
        });

        chatAdapter.addMessage(new ChatMessage("Hello! How can I help you?", ChatMessage.SENDER_BOT));
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        performApiPrewarming();
    }

    private void sendMessageAndGetReply(String message) {
        chatAdapter.addMessage(new ChatMessage(message, ChatMessage.SENDER_USER));
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        ChatMessage loadingMessage = new ChatMessage("Responding...", ChatMessage.SENDER_BOT);
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
                    Toast.makeText(ChatbotActivity.this, "Received response from AI!", Toast.LENGTH_SHORT).show();
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

                    chatAdapter.addMessage(new ChatMessage("Error: " + errorMessage, ChatMessage.SENDER_BOT));
                    recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
                    Toast.makeText(ChatbotActivity.this, "AI connection error: " + errorMessage, Toast.LENGTH_LONG).show();
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
        // When a suggested question is clicked, send it as a new message
        sendMessageAndGetReply(question);

        // Optional: Clear keyboard and focus after clicking a suggestion
        etChatInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etChatInput.getWindowToken(), 0);
        }
    }
}