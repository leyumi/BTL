package com.example.deadline;

public class ThongKeModel {
    private String category;
    private int amount;

    public ThongKeModel(String category, int amount) {
        this.category = category;
        this.amount = amount;
    }

    public String getCategory() { return category; }
    public int getAmount() { return amount; }
}

