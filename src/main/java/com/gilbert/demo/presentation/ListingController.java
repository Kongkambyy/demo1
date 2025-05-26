package com.gilbert.demo.presentation;

import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.domain.entities.Category;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.gilbert.demo.domain.usecases.features.GetFavoritesUseCase;
import com.gilbert.demo.domain.usecases.listing.GetListingUseCase;
import com.gilbert.demo.data.repository.CategoryRepository;
import com.gilbert.demo.data.repository.UserRepository;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class ListingController {

    private final GetListingUseCase getListingUseCase;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final GetFavoritesUseCase getFavoritesUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;

    @Autowired
    public ListingController(GetListingUseCase getListingUseCase,
                             CategoryRepository categoryRepository,
                             UserRepository userRepository, GetFavoritesUseCase getFavoritesUseCase, GetNotificationsUseCase getNotificationsUseCase) {
        this.getListingUseCase = getListingUseCase;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.getFavoritesUseCase = getFavoritesUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
    }

    @GetMapping("/listing/{id}")
    public String viewListing(@PathVariable("id") String listingId,
                              Model model,
                              HttpSession session) {
        try {
            String currentUserId = (String) session.getAttribute("userId");

            addNotificationCount(model, session);


            Listing listing = getListingUseCase.execute(listingId, currentUserId);
            model.addAttribute("listing", listing);

            Optional<User> sellerOpt = userRepository.findById(listing.getUserID());
            if (sellerOpt.isPresent()) {
                User seller = sellerOpt.get();
                model.addAttribute("seller", seller);
            }

            if (listing.getCategoryID() != 0) {
                Optional<Category> categoryOpt = categoryRepository.findById(listing.getCategoryID());
                if (categoryOpt.isPresent()) {
                    Category category = categoryOpt.get();
                    model.addAttribute("category", category);

                    if (category.getParentID() != null) {
                        Optional<Category> parentOpt = categoryRepository.findById(category.getParentID());
                        parentOpt.ifPresent(parent -> model.addAttribute("parentCategory", parent));
                    }
                }
            }

            boolean isOwner = currentUserId != null && currentUserId.equals(listing.getUserID());
            model.addAttribute("isOwner", isOwner);

            model.addAttribute("isLoggedIn", currentUserId != null);

            boolean isFavorite = false;
            if (currentUserId != null) {
                isFavorite = getFavoritesUseCase.isListingFavorite(currentUserId, listingId);
            }
            model.addAttribute("isFavorite", isFavorite);

            LoggerUtility.logEvent("Listing viewed: " + listingId + " by user: " +
                    (currentUserId != null ? currentUserId : "anonymous"));

            return "listing-detail";

        } catch (Exception e) {
            LoggerUtility.logError("Error loading listing " + listingId + ": " + e.getMessage());
            return "redirect:/search";
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