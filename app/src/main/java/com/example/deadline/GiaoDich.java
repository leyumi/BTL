package com.example.deadline;

public class GiaoDich {
    private String id;
    private String date;
    private double amount;
    private String type;
    private String note;
    private String title;
    private String name;
    private String icon;

    public GiaoDich() {
    }

    public GiaoDich(String date, double amount, String type, String note, String title, String name, String icon) {
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.title = title;
        this.name = name;
        this.icon = icon;
    }

    public GiaoDich(String id, String date, double amount, String type, String note, String title, String name, String icon) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.title = title;
        this.name = name;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
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
