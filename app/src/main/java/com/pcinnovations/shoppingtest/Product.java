package com.pcinnovations.shoppingtest;

public class Product {
    private String ean;
    private String name;

    public Product() {}

    public Product(String ean, String name) {
        this.ean = ean;
        this.name = name;
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

}