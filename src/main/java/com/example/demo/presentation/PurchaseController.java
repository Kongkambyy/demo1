package com.example.demo.presentation;

import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.entities.Transaction;
import com.example.demo.domain.entities.User;
import com.example.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.example.demo.domain.usecases.Notifications.NotificationService;
import com.example.demo.domain.usecases.Transactions.CreateTransactionUseCase;
import com.example.demo.domain.usecases.listing.GetListingUseCase;
import com.example.demo.domain.usecases.user.GetUserUseCase;
import com.example.demo.data.util.LoggerUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Controller
public class PurchaseController {

    private final GetListingUseCase getListingUseCase;
    private final GetUserUseCase getUserUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final CreateTransactionUseCase createTransactionUseCase;
    private final NotificationService notificationService;

    @Autowired
    public PurchaseController(GetListingUseCase getListingUseCase,
                              GetUserUseCase getUserUseCase,
                              GetNotificationsUseCase getNotificationsUseCase,
                              CreateTransactionUseCase createTransactionUseCase,
                              NotificationService notificationService) {
        this.getListingUseCase = getListingUseCase;
        this.getUserUseCase = getUserUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.createTransactionUseCase = createTransactionUseCase;
        this.notificationService = notificationService;
    }

    @GetMapping("/purchase/{listingId}")
    public String purchaseConfirmationPage(@PathVariable("listingId") String listingId,
                                           Model model,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to make a purchase");
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get the listing
            Listing listing = getListingUseCase.execute(listingId, userId);

            // Check if listing is active
            if (!"ACTIVE".equals(listing.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "This listing is not available for purchase");
                return "redirect:/listing/" + listingId;
            }

            // Check if user is trying to buy their own listing
            if (listing.getUserID().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You cannot buy your own listing");
                return "redirect:/listing/" + listingId;
            }

            // Get buyer information
            User buyer = getUserUseCase.findByIdOrThrow(userId);
            model.addAttribute("buyer", buyer);

            // Get seller information
            Optional<User> sellerOpt = getUserUseCase.findById(listing.getUserID());
            if (sellerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Seller information not found");
                return "redirect:/listing/" + listingId;
            }

            User seller = sellerOpt.get();
            model.addAttribute("seller", seller);

            // Add listing to model
            model.addAttribute("listing", listing);

            LoggerUtility.logEvent("User " + userId + " accessed purchase confirmation page for listing " + listingId);

            return "purchase-confirmation";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading purchase page for listing " + listingId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading the purchase page");
            return "redirect:/listing/" + listingId;
        }
    }

    @PostMapping("/purchase/confirm")
    public String confirmPurchase(String listingId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to make a purchase");
            return "redirect:/login";
        }

        try {
            // Create transaction
            Transaction transaction = createTransactionUseCase.execute(userId, listingId);

            // Get listing info for notification
            Listing listing = getListingUseCase.execute(listingId, userId);

            // Send notification to seller
            notificationService.notifyListingSold(
                    listing.getUserID(),
                    listingId,
                    listing.getTitle(),
                    listing.getPrice()
            );

            // Send notification to users who favorited this listing
            notificationService.notifyFavoritedListingSold(
                    listingId,
                    listing.getTitle()
            );

            LoggerUtility.logEvent("Purchase completed: User " + userId + " bought listing " + listingId);

            redirectAttributes.addFlashAttribute("success",
                    "Your purchase was successful! The seller has been notified and will contact you soon.");

            return "redirect:/orders";
        } catch (Exception e) {
            LoggerUtility.logError("Error processing purchase for listing " + listingId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while processing your purchase: " + e.getMessage());
            return "redirect:/listing/" + listingId;
        }
    }

    private void addNotificationCount(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            try {
                int unreadCount = getNotificationsUseCase.countUnread(userId);
                model.addAttribute("globalUnreadNotificationCount", unreadCount);
            } catch (Exception e) {
                LoggerUtility.logError("Error getting notification count for user " + userId + ": " + e.getMessage());
                model.addAttribute("globalUnreadNotificationCount", 0);
            }
        } else {
            model.addAttribute("globalUnreadNotificationCount", 0);
        }
    }
}