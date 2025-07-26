package com.example.sogiaodich;

public class Transaction {
    private int id;
    private String description;
    private String date; // "yyyy-MM-dd"
    private double amount;
    private String category;

    public Transaction(int id, String description, String date, double amount, String category) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.category = category;
    }

    // Getter
    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
}
