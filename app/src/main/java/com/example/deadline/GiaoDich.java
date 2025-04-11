package com.example.deadline;

public class GiaoDich {
    private String title;
    private String date;
    private int amount;
    private String type; // Thêm trường type

    public GiaoDich() {
        // required for Firebase
    }

    public GiaoDich(String title, String date, int amount, String type) {
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }
}
