package com.gilbert.demo.presentation;

import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class StaticResourceController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/uploads/{listingId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String listingId,
                                              @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(listingId).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                MediaType mediaType = getMediaType(filename);

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                LoggerUtility.logWarning("File not found or not readable: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LoggerUtility.logError("Error serving file " + filename + " for listing " + listingId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private MediaType getMediaType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "webp":
                return MediaType.valueOf("image/webp");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
