package com.example.receiptprocessor.model;

public class ReceiptItem {
    private String shortDescription;
    private String price;

    public ReceiptItem() {}

    public ReceiptItem(String shortDescription, String price) {
        this.shortDescription = shortDescription;
        this.price = price;
    }

    public String getShortDescription() {
        return shortDescription;
    }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
}
