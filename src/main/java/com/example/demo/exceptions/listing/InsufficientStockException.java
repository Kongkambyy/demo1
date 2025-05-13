package com.example.demo.exceptions.listing;

public class InsufficientStockException extends RuntimeException {
    private final String listingAdId;

    public InsufficientStockException(String listingAdId) {
        super("Insufficient stock for listing with ID: " + listingAdId);
        this.listingAdId = listingAdId;
    }

    public String getListingAdId() {
        return listingAdId;
    }
}
