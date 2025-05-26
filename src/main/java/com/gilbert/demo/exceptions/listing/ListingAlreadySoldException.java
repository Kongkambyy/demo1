package com.gilbert.demo.exceptions.listing;

public class ListingAlreadySoldException extends RuntimeException {
    private final String listingAdId;

    public ListingAlreadySoldException(String listingAdId) {
        super("Listing with ID " + listingAdId + " has already been sold");
        this.listingAdId = listingAdId;
    }

    public String getListingId() {
        return listingAdId;
    }
}
