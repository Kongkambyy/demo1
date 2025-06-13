package com.gilbert.demo.domain.usecases.listing;

import com.gilbert.demo.exceptions.presentation.FileSizeLimitException;
import com.gilbert.demo.exceptions.presentation.UnsupportedFileTypeException;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ImageUploadService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );

    public List<String> uploadImages(String listingId, List<MultipartFile> files) {
        List<String> uploadedFilePaths = new ArrayList<>();

        // Create listing directory
        Path listingDir = createListingDirectory(listingId);

        for (int i = 0; i < files.size() && i < 5; i++) { // Max 5 images
            MultipartFile file = files.get(i);

            if (file.isEmpty()) {
                continue;
            }

            try {
                validateFile(file);
                String fileName = generateFileName(file, i);
                Path targetPath = listingDir.resolve(fileName);

                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                String relativePath = "/uploads/" + listingId + "/" + fileName;
                uploadedFilePaths.add(relativePath);

                LoggerUtility.logEvent("Image uploaded: " + relativePath + " for listing: " + listingId);

            } catch (IOException e) {
                LoggerUtility.logError("Failed to upload image for listing " + listingId + ": " + e.getMessage());
                throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename());
            }
        }

        return uploadedFilePaths;
    }

    private Path createListingDirectory(String listingId) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Path listingPath = uploadPath.resolve(listingId);

            Files.createDirectories(listingPath);

            return listingPath;
        } catch (IOException e) {
            LoggerUtility.logError("Failed to create directory for listing " + listingId + ": " + e.getMessage());
            throw new RuntimeException("Failed to create upload directory");
        }
    }

    private void validateFile(MultipartFile file) {
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeLimitException(file.getSize(), MAX_FILE_SIZE);
        }

        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new UnsupportedFileTypeException(mimeType);
        }

        // Check file extension
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new UnsupportedFileTypeException("No filename");
        }

        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedFileTypeException(extension);
        }
    }

    private String generateFileName(MultipartFile file, int index) {
        String extension = getFileExtension(file.getOriginalFilename());
        return "image_" + index + "_" + System.currentTimeMillis() + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public void deleteImages(String listingId) {
        try {
            Path listingDir = Paths.get(uploadDir).resolve(listingId);
            if (Files.exists(listingDir)) {
                Files.walk(listingDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);

                LoggerUtility.logEvent("Images deleted for listing: " + listingId);
            }
        } catch (IOException e) {
            LoggerUtility.logError("Failed to delete images for listing " + listingId + ": " + e.getMessage());
        }
    }

    public List<String> getListingImages(String listingId) {
        List<String> imagePaths = new ArrayList<>();

        try {
            Path listingDir = Paths.get(uploadDir).resolve(listingId);
            if (Files.exists(listingDir)) {
                Files.list(listingDir)
                        .filter(path -> {
                            String fileName = path.getFileName().toString().toLowerCase();
                            return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                                    fileName.endsWith(".png") || fileName.endsWith(".webp");
                        })
                        .sorted()
                        .forEach(path -> {
                            String relativePath = "/uploads/" + listingId + "/" + path.getFileName().toString();
                            imagePaths.add(relativePath);
                        });
            }
        } catch (IOException e) {
            LoggerUtility.logError("Failed to get images for listing " + listingId + ": " + e.getMessage());
        }

        return imagePaths;
    }
}