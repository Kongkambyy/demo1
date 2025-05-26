package com.example.demo.presentation;

import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.entities.Category;
import com.example.demo.domain.usecases.listing.CreateListingUseCase;
import com.example.demo.domain.usecases.category.GetCategoriesUseCase;
import com.example.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.example.demo.data.repository.ListingRepository;
import com.example.demo.data.util.LoggerUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class CreateListingController {

    private final CreateListingUseCase createListingUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final ListingRepository listingRepository;

    @Autowired
    public CreateListingController(CreateListingUseCase createListingUseCase,
                                   GetCategoriesUseCase getCategoriesUseCase,
                                   GetNotificationsUseCase getNotificationsUseCase,
                                   ListingRepository listingRepository) {
        this.createListingUseCase = createListingUseCase;
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.listingRepository = listingRepository;
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
                .collect(java.util.stream.Collectors.toList());
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
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
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

            Listing savedListing = listingRepository.save(listing);

            LoggerUtility.logEvent("Listing created: " + savedListing.getAdID() + " with category: " + categoryId + " by user: " + userId);

            redirectAttributes.addFlashAttribute("success", "Your listing has been created successfully and is now live!");
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
