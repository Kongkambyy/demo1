package com.gilbert.demo.domain.entities;

public class Message {
    private String messageID;
    private String senderID;
    private String receiverID;
    private String message;
    private String date;
    private boolean isRead;

    public Message(String senderID, String receiverID, String message, String date) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
        this.date = date;
        this.isRead = false;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}