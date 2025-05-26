package com.gilbert.demo.domain.entities;

public class Favorite {
    private String favoriteID;
    private String userID;
    private String listingAdID;
    private String createdDate;

    public Favorite(String userID, String listingAdID, String createdDate) {
        this.userID = userID;
        this.listingAdID = listingAdID;
        this.createdDate = createdDate;
    }

    public String getFavoriteID() {
        return favoriteID;
    }

    public void setFavoriteID(String favoriteID) {
        this.favoriteID = favoriteID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getListingAdID() {
        return listingAdID;
    }

    public void setListingAdID(String listingAdID) {
        this.listingAdID = listingAdID;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}