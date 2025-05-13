package com.example.demo.exceptions.listing;

public class InvalidListingStateException extends RuntimeException {
    private final String listingAdId;

    public InvalidListingStateException(String listingAdId) {
        super("Invalid state for listing with ID : " + listingAdId);
        this.listingAdId = listingAdId;
    }

    public String getListingAdId() {
        return listingAdId;
    }
}
