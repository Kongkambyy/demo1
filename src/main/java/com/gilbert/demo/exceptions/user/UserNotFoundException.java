package com.gilbert.demo.exceptions.user;

public class UserNotFoundException extends RuntimeException {

    private final String userId;

    public UserNotFoundException(String userId) {
        super(String.format("User not found with ID: %s", userId));
        this.userId = userId;
    }

    public UserNotFoundException(String message, String userId) {
        super(message);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}