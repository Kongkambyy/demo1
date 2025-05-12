package com.example.demo.exceptions.listing;

public class InvalidListingStateException extends RuntimeException {
    public InvalidListingStateException(String message) {
        super(message);
    }
}
