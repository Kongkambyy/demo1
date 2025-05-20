package com.example.demo.domain.entities;

public class Notification {
    private String notificationID;
    private String userID;
    private String sourceID;
    private String sourceType;
    private String type;
    private String message;
    private boolean isRead;
    private String createdDate;

    public Notification(String userID, String sourceID, String sourceType, String type,
                        String message, String createdDate) {
        this.userID = userID;
        this.sourceID = sourceID;
        this.sourceType = sourceType;
        this.type = type;
        this.message = message;
        this.isRead = false;
        this.createdDate = createdDate;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}