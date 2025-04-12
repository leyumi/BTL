package com.example.deadline;

public class TransactionType {
    private String name;
    private String ic_name;
    private String type;

    public TransactionType() {
    }

    public TransactionType(String name, String ic_name, String type) {
        this.name = name;
        this.ic_name = ic_name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIc_name() {
        return ic_name;
    }

    public void setIc_name(String ic_name) {
        this.ic_name = ic_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
