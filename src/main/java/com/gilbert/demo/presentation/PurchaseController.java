package com.gilbert.demo.presentation;

import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.domain.entities.Transaction;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.gilbert.demo.domain.usecases.Notifications.NotificationService;
import com.gilbert.demo.domain.usecases.Transactions.CreateTransactionUseCase;
import com.gilbert.demo.domain.usecases.listing.GetListingUseCase;
import com.gilbert.demo.domain.usecases.user.GetUserUseCase;
import com.gilbert.demo.data.util.LoggerUtility;

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
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to make a purchase");
            return "redirect:/login";
        }

        try {
            addNotificationCount(model, session);

            Listing listing = getListingUseCase.execute(listingId, userId);

            if (!"ACTIVE".equals(listing.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "This listing is not available for purchase");
                return "redirect:/listing/" + listingId;
            }

            if (listing.getUserID().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You cannot buy your own listing");
                return "redirect:/listing/" + listingId;
            }

            User buyer = getUserUseCase.findByIdOrThrow(userId);
            model.addAttribute("buyer", buyer);

            Optional<User> sellerOpt = getUserUseCase.findById(listing.getUserID());
            if (sellerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Seller information not found");
                return "redirect:/listing/" + listingId;
            }

            User seller = sellerOpt.get();
            model.addAttribute("seller", seller);

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
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to make a purchase");
            return "redirect:/login";
        }

        try {
            Transaction transaction = createTransactionUseCase.execute(userId, listingId);

            Listing listing = getListingUseCase.execute(listingId, userId);

            notificationService.notifyListingSold(
                    listing.getUserID(),
                    listingId,
                    listing.getTitle(),
                    listing.getPrice()
            );

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