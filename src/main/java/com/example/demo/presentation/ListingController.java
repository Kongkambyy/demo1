package com.example.demo.presentation;

import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.entities.Category;
import com.example.demo.domain.entities.User;
import com.example.demo.domain.usecases.listing.GetListingUseCase;
import com.example.demo.data.repository.CategoryRepository;
import com.example.demo.data.repository.UserRepository;
import com.example.demo.data.util.LoggerUtility;
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

    @Autowired
    public ListingController(GetListingUseCase getListingUseCase,
                             CategoryRepository categoryRepository,
                             UserRepository userRepository) {
        this.getListingUseCase = getListingUseCase;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/listing/{id}")
    public String viewListing(@PathVariable("id") String listingId,
                              Model model,
                              HttpSession session) {
        try {
            // Get current user ID from session
            String currentUserId = (String) session.getAttribute("userId");

            // Get the listing
            Listing listing = getListingUseCase.execute(listingId, currentUserId);
            model.addAttribute("listing", listing);

            // Get seller information
            Optional<User> sellerOpt = userRepository.findById(listing.getUserID());
            if (sellerOpt.isPresent()) {
                User seller = sellerOpt.get();
                model.addAttribute("seller", seller);
            }

            // Get category information
            if (listing.getCategoryID() != 0) {
                Optional<Category> categoryOpt = categoryRepository.findById(listing.getCategoryID());
                if (categoryOpt.isPresent()) {
                    Category category = categoryOpt.get();
                    model.addAttribute("category", category);

                    // Get parent category if this is a subcategory
                    if (category.getParentID() != null) {
                        Optional<Category> parentOpt = categoryRepository.findById(category.getParentID());
                        parentOpt.ifPresent(parent -> model.addAttribute("parentCategory", parent));
                    }
                }
            }

            // Check if current user is the owner
            boolean isOwner = currentUserId != null && currentUserId.equals(listing.getUserID());
            model.addAttribute("isOwner", isOwner);

            // Check if user is logged in
            model.addAttribute("isLoggedIn", currentUserId != null);

            LoggerUtility.logEvent("Listing viewed: " + listingId + " by user: " +
                    (currentUserId != null ? currentUserId : "anonymous"));

            return "listing-detail";

        } catch (Exception e) {
            LoggerUtility.logError("Error loading listing " + listingId + ": " + e.getMessage());
            return "redirect:/search";
        }
    }
}