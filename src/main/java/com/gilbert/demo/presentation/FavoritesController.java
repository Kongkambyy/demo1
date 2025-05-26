package com.gilbert.demo.presentation;

import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.gilbert.demo.domain.usecases.features.AddFavoriteUseCase;
import com.gilbert.demo.domain.usecases.features.GetFavoritesUseCase;
import com.gilbert.demo.domain.usecases.features.RemoveFavoriteUseCase;
import com.gilbert.demo.data.util.LoggerUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class FavoritesController {

    private final GetFavoritesUseCase getFavoritesUseCase;
    private final AddFavoriteUseCase addFavoriteUseCase;
    private final RemoveFavoriteUseCase removeFavoriteUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;

    @Autowired
    public FavoritesController(GetFavoritesUseCase getFavoritesUseCase,
                               AddFavoriteUseCase addFavoriteUseCase,
                               RemoveFavoriteUseCase removeFavoriteUseCase, GetNotificationsUseCase getNotificationsUseCase) {
        this.getFavoritesUseCase = getFavoritesUseCase;
        this.addFavoriteUseCase = addFavoriteUseCase;
        this.removeFavoriteUseCase = removeFavoriteUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
    }
    @GetMapping("/favorites")
    public String favorites(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        addNotificationCount(model, session);

        try {
            List<Listing> favoriteListings = getFavoritesUseCase.getFavoriteListings(userId);
            model.addAttribute("favoriteListings", favoriteListings);

            LoggerUtility.logEvent("User " + userId + " viewed favorites page with " + favoriteListings.size() + " items");

            return "favorites";
        } catch (Exception e) {
            LoggerUtility.logError("Error retrieving favorites for user " + userId + ": " + e.getMessage());
            model.addAttribute("error", "An error occurred while retrieving your favorites");
            return "favorites";
        }
    }

    @PostMapping("/favorites/add")
    public String addToFavorites(
            @RequestParam("listingId") String listingId,
            @RequestParam(value = "returnUrl", required = false, defaultValue = "/listing") String returnUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            boolean isAlreadyFavorite = getFavoritesUseCase.isListingFavorite(userId, listingId);

            if (!isAlreadyFavorite) {
                addFavoriteUseCase.execute(userId, listingId);
                LoggerUtility.logEvent("User " + userId + " added listing " + listingId + " to favorites");
                redirectAttributes.addFlashAttribute("success", "Item added to favorites");
            }

            if (returnUrl.startsWith("/listing")) {
                return "redirect:" + returnUrl + "/" + listingId;
            } else {
                return "redirect:" + returnUrl;
            }
        } catch (Exception e) {
            LoggerUtility.logError("Error adding listing " + listingId + " to favorites for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not add to favorites");

            if (returnUrl.startsWith("/listing")) {
                return "redirect:" + returnUrl + "/" + listingId;
            } else {
                return "redirect:" + returnUrl;
            }
        }
    }

    @PostMapping("/favorites/remove")
    public String removeFromFavorites(
            @RequestParam("listingId") String listingId,
            @RequestParam(value = "returnUrl", required = false, defaultValue = "/favorites") String returnUrl,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            removeFavoriteUseCase.execute(userId, listingId);
            LoggerUtility.logEvent("User " + userId + " removed listing " + listingId + " from favorites");

            if (returnUrl.equals("/favorites")) {
                redirectAttributes.addFlashAttribute("success", "Item removed from favorites");
            }

            return "redirect:" + returnUrl;
        } catch (Exception e) {
            LoggerUtility.logError("Error removing listing " + listingId + " from favorites for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not remove from favorites");
            return "redirect:" + returnUrl;
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