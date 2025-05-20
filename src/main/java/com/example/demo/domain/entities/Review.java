package com.example.demo.domain.entities;

public class Review {
    private String reviewID;
    private String reviewerID;
    private String reviewedUserID;
    private int rating;
    private String comment;
    private String createdDate;

    public Review(String reviewerID, String reviewedUserID, int rating, String comment, String createdDate) {
        this.reviewerID = reviewerID;
        this.reviewedUserID = reviewedUserID;
        this.rating = rating;
        this.comment = comment;
        this.createdDate = createdDate;
    }

    public String getReviewID() {
        return reviewID;
    }

    public void setReviewID(String reviewID) {
        this.reviewID = reviewID;
    }

    public String getReviewerID() {
        return reviewerID;
    }

    public void setReviewerID(String reviewerID) {
        this.reviewerID = reviewerID;
    }

    public String getReviewedUserID() {
        return reviewedUserID;
    }

    public void setReviewedUserID(String reviewedUserID) {
        this.reviewedUserID = reviewedUserID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}