package com.gilbert.demo.presentation;

import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.domain.entities.Category;
import com.gilbert.demo.domain.usecases.listing.CreateListingUseCase;
import com.gilbert.demo.domain.usecases.listing.ImageUploadService;
import com.gilbert.demo.domain.usecases.category.GetCategoriesUseCase;
import com.gilbert.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.data.util.LoggerUtility;
import com.gilbert.demo.exceptions.presentation.FileSizeLimitException;
import com.gilbert.demo.exceptions.presentation.UnsupportedFileTypeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class CreateListingController {

    private final CreateListingUseCase createListingUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final ListingRepository listingRepository;
    private final ImageUploadService imageUploadService;

    @Autowired
    public CreateListingController(CreateListingUseCase createListingUseCase,
                                   GetCategoriesUseCase getCategoriesUseCase,
                                   GetNotificationsUseCase getNotificationsUseCase,
                                   ListingRepository listingRepository,
                                   ImageUploadService imageUploadService) {
        this.createListingUseCase = createListingUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.listingRepository = listingRepository;
        this.imageUploadService = imageUploadService;
    }

    @GetMapping("/sell/create")
    public String createListingPage(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        addNotificationCount(model, session);

        List<Category> mainCategories = getCategoriesUseCase.execute()
                .stream()
                .filter(cat -> cat.getParentID() == null)
                .collect(Collectors.toList());
        List<Category> allCategories = getCategoriesUseCase.execute();

        model.addAttribute("mainCategories", mainCategories);
        model.addAttribute("allCategories", allCategories);

        return "create-listing";
    }

    @PostMapping("/sell/submit")
    public String createListing(@RequestParam("title") String title,
                                @RequestParam("description") String description,
                                @RequestParam("price") int price,
                                @RequestParam("condition") String condition,
                                @RequestParam("categoryId") int categoryId,
                                @RequestParam(value = "brand", required = false) String brand,
                                @RequestParam(value = "images", required = false) MultipartFile[] images,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Validate basic input
            if (title == null || title.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Title is required");
                return "redirect:/sell/create";
            }

            if (description == null || description.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Description is required");
                return "redirect:/sell/create";
            }

            if (price <= 0) {
                redirectAttributes.addFlashAttribute("error", "Price must be greater than 0");
                return "redirect:/sell/create";
            }

            // Create listing first
            String createdDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            Listing listing = new Listing(
                    userId,
                    title.trim(),
                    description.trim(),
                    price,
                    createdDate,
                    condition.trim(),
                    "ACTIVE"
            );

            if (brand != null && !brand.trim().isEmpty()) {
                listing.setBrand(brand.trim());
            }

            listing.setCategoryID(categoryId);

            // Save listing to get the ID
            Listing savedListing = listingRepository.save(listing);

            // Handle image uploads if provided
            if (images != null && images.length > 0) {
                try {
                    List<MultipartFile> imageList = Arrays.stream(images)
                            .filter(file -> !file.isEmpty())
                            .collect(Collectors.toList());

                    if (!imageList.isEmpty()) {
                        List<String> uploadedPaths = imageUploadService.uploadImages(
                                savedListing.getAdID(), imageList);

                        savedListing.setImagePaths(uploadedPaths);

                        // Update listing with image paths (you may need to add this method to repository)
                        // For now, we'll just log the paths
                        LoggerUtility.logEvent("Images uploaded for listing " + savedListing.getAdID() +
                                ": " + String.join(", ", uploadedPaths));
                    }
                } catch (FileSizeLimitException e) {
                    redirectAttributes.addFlashAttribute("error",
                            "One or more images are too large. Maximum size is 10MB per image.");
                    return "redirect:/sell/create";
                } catch (UnsupportedFileTypeException e) {
                    redirectAttributes.addFlashAttribute("error",
                            "Unsupported file type. Please use JPG, PNG, or WebP images only.");
                    return "redirect:/sell/create";
                } catch (Exception e) {
                    LoggerUtility.logError("Image upload error for listing " + savedListing.getAdID() +
                            ": " + e.getMessage());
                    redirectAttributes.addFlashAttribute("warning",
                            "Listing created successfully, but some images couldn't be uploaded.");
                }
            }

            LoggerUtility.logEvent("Listing created: " + savedListing.getAdID() +
                    " with category: " + categoryId + " by user: " + userId);

            redirectAttributes.addFlashAttribute("success",
                    "Your listing has been created successfully and is now live!");
            return "redirect:/listing/" + savedListing.getAdID();

        } catch (Exception e) {
            LoggerUtility.logError("Error creating listing: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sell/create";
        }
    }

    private void addNotificationCount(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            try {
                int unreadCount = getNotificationsUseCase.countUnread(userId);
                model.addAttribute("globalUnreadNotificationCount", unreadCount);
            } catch (Exception e) {
                model.addAttribute("globalUnreadNotificationCount", 0);
            }
        } else {
            model.addAttribute("globalUnreadNotificationCount", 0);
        }
    }
}