package com.example.demo.exceptions.listing;

public class ListingNotFoundException extends RuntimeException {
    private final Long listingId;

    public ListingNotFoundException(Long listingId) {
        super("Listing with id " + listingId + " not found");
        this.listingId = listingId;
    }
    public Long getListingId() {
        return listingId;
    }
}
