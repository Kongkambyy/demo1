package com.example.demo.exceptions.listing;

public class ListingNotActiveException extends RuntimeException {
    public ListingNotActiveException(String message) {
        super(message);
    }
}
