package com.gilbert.demo.domain.entities;


import java.util.List;
import java.util.ArrayList;

public class Listing {

    private String adID;
    private String userID;
    private String title;
    private String description;
    private int price;
    private String createdDate;
    private String itemCondition;
    private String status;
    private String brand;
    private int categoryID;
    private List<String> imagePaths; // New field for image paths

    public Listing(String userID, String title, String description, int price, String createdDate, String itemCondition, String status) {
        this.userID = userID;
        this.title = title;
        this.description = description;
        this.price = price;
        this.createdDate = createdDate;
        this.itemCondition = itemCondition;
        this.status = status;
        this.imagePaths = new ArrayList<>(); // Initialize empty list
    }

    // Existing getters and setters
    public String getAdID() {
        return adID;
    }

    public void setAdID(String adID) {
        this.adID = adID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String condition) {
        this.itemCondition = condition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    // New methods for image handling
    public List<String> getImagePaths() {
        return imagePaths != null ? imagePaths : new ArrayList<>();
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? imagePaths : new ArrayList<>();
    }

    public void addImagePath(String imagePath) {
        if (this.imagePaths == null) {
            this.imagePaths = new ArrayList<>();
        }
        this.imagePaths.add(imagePath);
    }

    public String getMainImage() {
        return (imagePaths != null && !imagePaths.isEmpty()) ? imagePaths.get(0) : null;
    }

    public boolean hasImages() {
        return imagePaths != null && !imagePaths.isEmpty();
    }
}