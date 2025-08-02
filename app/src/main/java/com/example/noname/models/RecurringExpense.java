package com.example.noname.models;

import java.io.Serializable;

public class RecurringExpense implements Serializable {
    private long id;
    private long userId;
    private long accountId;
    private long categoryId;
    private String name;
    private double amount;
    private String transactionType;
    private String frequency;
    private int frequencyValue;
    private String startDate;
    private String endDate;
    private String lastGeneratedDate;
    private boolean isActive;

    public RecurringExpense(long id, long userId, long accountId, long categoryId, String name, double amount, String transactionType, String frequency, int frequencyValue, String startDate, String endDate, String lastGeneratedDate, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.name = name;
        this.amount = amount;
        this.transactionType = transactionType;
        this.frequency = frequency;
        this.frequencyValue = frequencyValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lastGeneratedDate = lastGeneratedDate;
        this.isActive = isActive;
    }

    // Getters
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getAccountId() { return accountId; }
    public long getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public double getAmount() { return amount; }
    public String getTransactionType() { return transactionType; }
    public String getFrequency() { return frequency; }
    public int getFrequencyValue() { return frequencyValue; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getLastGeneratedDate() { return lastGeneratedDate; }
    public boolean isActive() { return isActive; }
}