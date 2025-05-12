package com.example.demo.domain.entities;

public class Message {
    private String messageID;
    private String senderID;
    private String receiverID;
    private String message;
    private String date;

    public Message(String messageID, String senderID, String receiverID, String message, String date) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
        this.date = date;
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
}
