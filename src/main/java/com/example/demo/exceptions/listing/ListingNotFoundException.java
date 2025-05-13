package com.example.demo.exceptions.listing;

public class ListingNotFoundException extends RuntimeException {
    private final String listingAdId;

    public ListingNotFoundException(String listingAdId) {
        super("Listing with id '" + listingAdId + "' not found");
        this.listingAdId = listingAdId;
    }

    public String getListingId() {
        return listingAdId;
    }
}
