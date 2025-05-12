package com.example.demo.exceptions.presentation;

public class FileSizeLimitException extends RuntimeException {
    public FileSizeLimitException(String message) {
        super(message);
    }
}
