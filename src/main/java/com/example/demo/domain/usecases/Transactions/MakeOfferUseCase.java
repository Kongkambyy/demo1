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

        if (!userRepository.findById(buyerId).isPresent()) {
            LoggerUtility.logError("Offer creation attempt by non-existent user: " + buyerId);
            throw new UserNotFoundException(buyerId);
        }

        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Offer creation attempt for non-existent listing: " + listingId);
            throw new ListingNotFoundException(listingId);
        }

        Listing listing = listingOpt.get();

        if (!"ACTIVE".equals(listing.getStatus())) {
            LoggerUtility.logError("Offer creation attempt for non-active listing: " + listingId);
            throw new ListingNotActiveException(listingId);
        }

        if (listing.getUserID().equals(buyerId)) {
            LoggerUtility.logError("User attempted to make offer on their own listing: " + buyerId);
            throw new IllegalArgumentException("Cannot make offer on your own listing");
        }

        int minimumAmount = (int) (listing.getPrice() * MINIMUM_OFFER_PERCENTAGE);
        if (offerAmount < minimumAmount) {
            throw new IllegalArgumentException("Offer amount must be at least " +
                    (MINIMUM_OFFER_PERCENTAGE * 100) + "% of the original price (" + minimumAmount + " DKK)");
        }

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String expiryDate = LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

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

        Offer savedOffer = offerRepository.save(offer);

        notificationService.notifyOfferReceived(
                savedOffer.getSellerID(),
                savedOffer.getListingID(),
                savedOffer.getOfferID(),
                listing.getTitle(),
                savedOffer.getOfferAmount()
        );

        notificationService.notifyOfferSubmitted(
                savedOffer.getBuyerID(),
                savedOffer.getOfferID(),
                savedOffer.getListingID(),
                listing.getTitle(),
                savedOffer.getOfferAmount()
        );

        notificationService.notifyOfferOnFavoritedListing(
                savedOffer.getListingID(),
                savedOffer.getOfferID(),
                listing.getTitle(),
                savedOffer.getOfferAmount()
        );

        return savedOffer;
    }
}