package com.gilbert.demo.exceptions.presentation;

public class FileSizeLimitException extends RuntimeException {
    private final long fileSize;
    private final long maxAllowedSize;

    public FileSizeLimitException(long fileSize, long maxAllowedSize) {
        super("File size " + fileSize + " bytes exceeds the allowed limit of " + maxAllowedSize + " bytes");
        this.fileSize = fileSize;
        this.maxAllowedSize = maxAllowedSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getMaxAllowedSize() {
        return maxAllowedSize;
    }
}
