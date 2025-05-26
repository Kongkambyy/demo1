package com.gilbert.demo.domain.entities;

public class Transaction {
    private String transactionID;
    private String listingID;
    private String buyerID;
    private String sellerID;
    private int amount;
    private String status;
    private String createdDate;
    private String completedDate;

    public Transaction(String listingID, String buyerID, String sellerID, int amount, String status, String createdDate) {
        this.listingID = listingID;
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.amount = amount;
        this.status = status;
        this.createdDate = createdDate;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }
}