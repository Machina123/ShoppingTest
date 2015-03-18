package com.pcinnovations.shoppingtest;

public class ListItem {
    private String ean;
    private String name;
    private String amount;

    public ListItem() {}

    public ListItem(String ean, String name) {
        this.ean = ean;
        this.name = name;
        this.amount = "1";
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}