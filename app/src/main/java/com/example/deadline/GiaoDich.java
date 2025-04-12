package com.example.deadline;
public class GiaoDich {
    private String id;
    private String name;
    private int amount; // Số tiền (âm là chi tiêu, dương là thu nhập)
    private String date; // Định dạng dd/MM/yyyy
    private String category;
    private String title;

    // Constructor, getter và setter
    public GiaoDich() {
        // Cần constructor rỗng cho Firebase
    }

    public GiaoDich(String name,String title, int amount, String date, String category) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.title= title;
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getTitle() {
        return title;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}