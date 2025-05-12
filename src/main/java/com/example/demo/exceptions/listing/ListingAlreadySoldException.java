package com.example.demo.exceptions.listing;

public class ListingAlreadySoldException extends RuntimeException {
    public ListingAlreadySoldException(String message) {
        super(message);
    }
}
