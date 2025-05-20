package com.example.demo.domain.entities;

public class Offer {
    private String offerID;
    private String listingID;
    private String buyerID;
    private String sellerID;
    private int originalPrice;
    private int offerAmount;
    private String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    private String createdDate;
    private String expiryDate;

    public Offer(String listingID, String buyerID, String sellerID, int originalPrice, int offerAmount, String status, String createdDate, String expiryDate) {
        this.listingID = listingID;
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.originalPrice = originalPrice;
        this.offerAmount = offerAmount;
        this.status = status;
        this.createdDate = createdDate;
        this.expiryDate = expiryDate;
    }

    public String getOfferID() {
        return offerID;
    }

    public void setOfferID(String offerID) {
        this.offerID = offerID;
    }

    public String getListingID() {
        return listingID;
    }

    public void setListingID(String listingID) {
        this.listingID = listingID;
    }

    public String getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(String buyerID) {
        this.buyerID = buyerID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(int offerAmount) {
        this.offerAmount = offerAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}