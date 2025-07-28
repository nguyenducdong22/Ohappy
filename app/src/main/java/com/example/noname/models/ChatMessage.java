package com.example.noname.models;

public class ChatMessage {
    public static final int SENDER_USER = 0;
    public static final int SENDER_BOT = 1;

    private String message;
    private int sender; // 0 for user, 1 for bot

    public ChatMessage(String message, int sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public int getSender() {
        return sender;
    }
}