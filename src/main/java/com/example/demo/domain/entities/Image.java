package com.example.demo.domain.entities;

public class Image {
    private String imageID;
    private String fileType;
    private String filePath;
    private String fileName;
    private String uploadDate;
    private int fileSize;

    public Image(String fileType, String filePath, String fileName, String uploadDate, int fileSize) {
        this.fileType = fileType;
        this.filePath = filePath;
        this.fileName = fileName;
        this.uploadDate = uploadDate;
        this.fileSize = fileSize;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}