package com.example.deadline;

public class GiaoDich {
    private String id;
    private String date;
    private int amount;
    private String type;
    private String category;
    private String note;
    private String title;
    private String name;

    public GiaoDich() {}

    public GiaoDich(String id, String date, int amount, String type, String category, String note,String title,String name) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.title = title;
        this.name = name;
    }

    // Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }
    public String getTitle() {
        return title;
    }
    public String getName() {
        return name;
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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setNote(String note) {
        this.note = note;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
