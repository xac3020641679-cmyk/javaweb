package com.enterprise.catering.model;

import java.util.Date;

public class Recipe {
    private int id;
    private String name;
    private String category;
    private String image;
    private String unit;
    private double price;
    private Date createdAt;

    public Recipe() {
        this.createdAt = new Date();
    }

    public Recipe(String name, String category, String image, String unit, double price) {
        this();
        this.name = name;
        this.category = category;
        this.image = image;
        this.unit = unit;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}


