package com.example.demo.exceptions.user;

public class DuplicateUserException extends RuntimeException {

    private final String email;

    public DuplicateUserException(String email) {
        super(String.format("User already exists with email: %s", email));
        this.email = email;
    }

    public DuplicateUserException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}