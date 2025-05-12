package com.example.demo.domain.entities;

public class Category {

    private int categoryId;
    private String description;
    private String name;

    public Category(int categoryId, String description, String name) {
        this.categoryId = categoryId;
        this.description = description;
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


