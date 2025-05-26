package com.gilbert.demo.exceptions.presentation;

public class UnsupportedFileTypeException extends RuntimeException {
    private final String fileType;

    public UnsupportedFileTypeException(String fileType) {
        super("Unsupported file type: " + fileType);
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }
}
