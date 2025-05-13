package com.example.demo.exceptions.listing;

public class ListingNotFoundException extends RuntimeException {
    private final Long listingAdId;

    public ListingNotFoundException(Long listingAdId) {
        super("Listing with id " + listingAdId + " not found");
        this.listingAdId = listingAdId;
    }

    public ListingNotFoundException(String message) {
        super(message);
        this.listingAdId = null;
    }

    public Long getListingId() {
        return listingAdId;
    }
}
