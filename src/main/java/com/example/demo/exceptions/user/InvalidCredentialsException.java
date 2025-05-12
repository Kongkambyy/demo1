package com.example.demo.exceptions.user;

public class InvalidCredentialsException extends RuntimeException {

    private final String username;

    public InvalidCredentialsException() {
        super("Invalid username or password");
        this.username = null;
    }

    public InvalidCredentialsException(String username) {
        super(String.format("Invalid credentials for user: %s", username));
        this.username = username;
    }

    public InvalidCredentialsException(String message, String username) {
        super(message);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}