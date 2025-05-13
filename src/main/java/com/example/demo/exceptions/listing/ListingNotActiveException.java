package com.example.demo.exceptions.listing;

public class ListingNotActiveException extends RuntimeException {
    private final String listingAdId;

    public ListingNotActiveException(String listingId) {
        super("Listing with ID " + listingId + " is not active.");
        this.listingAdId = listingId;
    }

    public String getListingId() {
        return listingAdId;
    }
}
