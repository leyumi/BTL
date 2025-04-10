package com.example.deadline;
public class GiaoDich {
    private String title;
    private String date;
    private int amount;

    public GiaoDich() {
        // required for Firebase
    }

    public GiaoDich(String title, String date, int amount) {
        this.title = title;
        this.date = date;
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getAmount() {
        return amount;
    }
}
