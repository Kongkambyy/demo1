package com.example.demo.domain.usecases.Transactions;

import com.example.demo.data.repository.OfferRepository;
import com.example.demo.data.repository.ListingRepository;
import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.Offer;
import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.usecases.Notifications.NotificationService;
import com.example.demo.exceptions.listing.ListingNotFoundException;
import com.example.demo.exceptions.listing.ListingNotActiveException;
import com.example.demo.exceptions.user.UserNotFoundException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class MakeOfferUseCase {

    private final OfferRepository offerRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Minimum offer percentage (70% of original price)
    private static final double MINIMUM_OFFER_PERCENTAGE = 0.7;

    public MakeOfferUseCase(OfferRepository offerRepository,
                            ListingRepository listingRepository,
                            UserRepository userRepository,
                            NotificationService notificationService) {
        this.offerRepository = offerRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Offer execute(String buyerId, String listingId, int offerAmount) {
        // Validate buyer exists
        if (!userRepository.findById(buyerId).isPresent()) {
            LoggerUtility.logError("Offer creation attempt by non-existent user: " + buyerId);
            throw new UserNotFoundException(buyerId);
        }

        // Validate and get listing
        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Offer creation attempt for non-existent listing: " + listingId);
            throw new ListingNotFoundException(listingId);
        }

        Listing listing = listingOpt.get();

        // Check if listing is active
        if (!"ACTIVE".equals(listing.getStatus())) {
            LoggerUtility.logError("Offer creation attempt for non-active listing: " + listingId);
            throw new ListingNotActiveException(listingId);
        }

        // Prevent buyer from making offer on their own listing
        if (listing.getUserID().equals(buyerId)) {
            LoggerUtility.logError("User attempted to make offer on their own listing: " + buyerId);
            throw new IllegalArgumentException("Cannot make offer on your own listing");
        }

        // Validate offer amount meets minimum requirement (70% of original price)
        int minimumAmount = (int) (listing.getPrice() * MINIMUM_OFFER_PERCENTAGE);
        if (offerAmount < minimumAmount) {
            LoggerUtility.logError("Offer amount too low: " + offerAmount + " (minimum: " + minimumAmount + ")");
            throw new IllegalArgumentException("Offer amount must be at least " +
                    (MINIMUM_OFFER_PERCENTAGE * 100) + "% of the original price (" + minimumAmount + " DKK)");
        }

        // Set dates
        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        // Set expiry date to 7 days from now
        String expiryDate = LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Create offer with PENDING status
        Offer offer = new Offer(
                listingId,
                buyerId,
                listing.getUserID(),
                listing.getPrice(),
                offerAmount,
                "PENDING",
                createdDate,
                expiryDate
        );

        // Save offer
        Offer savedOffer = offerRepository.save(offer);

        // Send notifications using the notification service
        notificationService.notifyOfferReceived(
                savedOffer.getSellerID(),
                savedOffer.getListingID(),
                savedOffer.getOfferID(),
                listing.getTitle(),
                savedOffer.getOfferAmount()
        );

        // Also notify users who favorited this listing
        notificationService.notifyOfferOnFavoritedListing(
                savedOffer.getListingID(),
                savedOffer.getOfferID(),
                listing.getTitle(),
                savedOffer.getOfferAmount()
        );

        LoggerUtility.logEvent("Offer created: " + savedOffer.getOfferID() +
                " for listing: " + listingId + " by buyer: " + buyerId);

        return savedOffer;
    }
}