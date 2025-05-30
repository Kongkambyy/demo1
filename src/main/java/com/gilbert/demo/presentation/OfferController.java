package com.gilbert.demo.presentation;

import com.gilbert.demo.domain.entities.Offer;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.domain.usecases.Transactions.MakeOfferUseCase;
import com.gilbert.demo.domain.usecases.listing.GetListingUseCase;
import com.gilbert.demo.domain.usecases.user.GetUserUseCase;
import com.gilbert.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.gilbert.demo.data.repository.OfferRepository;
import com.gilbert.demo.data.util.LoggerUtility;
import com.gilbert.demo.exceptions.listing.ListingNotFoundException;
import com.gilbert.demo.exceptions.listing.ListingNotActiveException;
import com.gilbert.demo.exceptions.user.UserNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

@Controller
public class OfferController {

    private final MakeOfferUseCase makeOfferUseCase;
    private final GetListingUseCase getListingUseCase;
    private final GetUserUseCase getUserUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final OfferRepository offerRepository;

    @Autowired
    public OfferController(MakeOfferUseCase makeOfferUseCase,
                           GetListingUseCase getListingUseCase,
                           GetUserUseCase getUserUseCase,
                           GetNotificationsUseCase getNotificationsUseCase,
                           OfferRepository offerRepository) {
        this.makeOfferUseCase = makeOfferUseCase;
        this.getListingUseCase = getListingUseCase;
        this.getUserUseCase = getUserUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.offerRepository = offerRepository;
    }

    @GetMapping("/offer/{listingId}")
    public String makeOfferPage(@PathVariable("listingId") String listingId,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to make an offer");
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get the listing
            Listing listing = getListingUseCase.execute(listingId, userId);

            // Check if listing is active
            if (!"ACTIVE".equals(listing.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "This listing is not available for offers");
                return "redirect:/listing/" + listingId;
            }

            // Check if user is trying to make offer on their own listing
            if (listing.getUserID().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You cannot make an offer on your own listing");
                return "redirect:/listing/" + listingId;
            }

            // Get seller information
            Optional<User> sellerOpt = getUserUseCase.findById(listing.getUserID());
            if (sellerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Seller information not found");
                return "redirect:/listing/" + listingId;
            }

            // Get user's existing offers for this listing
            List<Offer> existingOffers = offerRepository.findByListingId(listingId)
                    .stream()
                    .filter(offer -> offer.getBuyerID().equals(userId))
                    .collect(java.util.stream.Collectors.toList());

            // Calculate minimum offer amount (70% of original price)
            int minimumOffer = (int) (listing.getPrice() * 0.7);

            // Add data to model
            model.addAttribute("listing", listing);
            model.addAttribute("seller", sellerOpt.get());
            model.addAttribute("existingOffers", existingOffers);
            model.addAttribute("minimumOffer", minimumOffer);
            model.addAttribute("minimumPercentage", 70);

            LoggerUtility.logEvent("User " + userId + " accessed make offer page for listing " + listingId);

            return "make-offer";

        } catch (ListingNotFoundException e) {
            LoggerUtility.logError("Listing not found for offer: " + listingId);
            redirectAttributes.addFlashAttribute("error", "Listing not found");
            return "redirect:/search";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading make offer page for listing " + listingId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading the offer page");
            return "redirect:/listing/" + listingId;
        }
    }

    @PostMapping("/offer/submit")
    public String submitOffer(@RequestParam("listingId") String listingId,
                              @RequestParam("offerAmount") int offerAmount,
                              @RequestParam(value = "message", required = false) String message,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to make an offer");
            return "redirect:/login";
        }

        try {
            if (offerAmount <= 0) {
                redirectAttributes.addFlashAttribute("error", "Offer amount must be greater than 0");
                return "redirect:/offer/" + listingId;
            }

            Offer offer = makeOfferUseCase.execute(userId, listingId, offerAmount);

            LoggerUtility.logEvent("Offer created successfully: " + offer.getOfferID() + " by user: " + userId);

            redirectAttributes.addFlashAttribute("success",
                    "Your offer of " + offerAmount + " DKK has been submitted successfully! " +
                            "The seller will be notified and can accept or decline your offer.");

            return "redirect:/listing/" + listingId;

        } catch (ListingNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Listing not found");
            return "redirect:/search";
        } catch (ListingNotActiveException e) {
            redirectAttributes.addFlashAttribute("error", "This listing is no longer available for offers");
            return "redirect:/listing/" + listingId;
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User account not found");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/offer/" + listingId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while submitting your offer. Please try again.");
            return "redirect:/offer/" + listingId;
        }
    }

    @GetMapping("/offers")
    public String viewOffers(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            addNotificationCount(model, session);

            List<Offer> madeOffers = offerRepository.findByBuyerId(userId);

            List<Offer> receivedOffers = offerRepository.findBySellerId(userId);

            model.addAttribute("madeOffers", madeOffers);
            model.addAttribute("receivedOffers", receivedOffers);

            return "offers";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while loading your offers");
            return "offers";
        }
    }

    @PostMapping("/offer/respond")
    public String respondToOffer(@RequestParam("offerId") String offerId,
                                 @RequestParam("action") String action,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to respond to offers");
            return "redirect:/login";
        }

        try {
            Optional<Offer> offerOpt = offerRepository.findById(offerId);
            if (offerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Offer not found");
                return "redirect:/offers";
            }

            Offer offer = offerOpt.get();

            if (!offer.getSellerID().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to respond to this offer");
                return "redirect:/offers";
            }

            if (!"PENDING".equals(offer.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "This offer has already been responded to");
                return "redirect:/offers";
            }

            String newStatus;
            String successMessage;

            if ("accept".equals(action)) {
                newStatus = "ACCEPTED";
                successMessage = "Offer accepted successfully! You can now proceed with the transaction.";
            } else if ("reject".equals(action)) {
                newStatus = "REJECTED";
                successMessage = "Offer rejected successfully.";
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid action");
                return "redirect:/offers";
            }

            offerRepository.updateStatus(offerId, newStatus);

            LoggerUtility.logEvent("Offer " + offerId + " " + newStatus.toLowerCase() + " by user: " + userId);

            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:/offers";

        } catch (Exception e) {
            LoggerUtility.logError("Error responding to offer " + offerId + " by user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while responding to the offer");
            return "redirect:/offers";
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