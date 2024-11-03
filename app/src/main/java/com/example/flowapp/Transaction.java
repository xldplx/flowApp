package com.example.flowapp;

public class Transaction {
    private int transactionId;
    private double amount;
    private String date; // You can use a Date object if you prefer
    private String title; // Title of the transaction
    private int imageResourceId; // Resource ID for the transaction type image

    public Transaction(int transactionId, double amount, String date, String title, int imageResourceId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.date = date;
        this.title = title;
        this.imageResourceId = imageResourceId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}