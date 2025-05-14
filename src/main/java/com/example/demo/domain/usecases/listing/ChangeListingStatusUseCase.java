package com.example.demo.domain.usecases.listing;

import com.example.demo.data.repository.ListingRepository;
import com.example.demo.domain.entities.Listing;
import com.example.demo.exceptions.listing.ListingNotFoundException;
import com.example.demo.exceptions.user.InsufficientPermissionsException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ChangeListingStatusUseCase {

    private final ListingRepository listingRepository;

    public ChangeListingStatusUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public void execute(String userId, String adId, String newStatus) {
        Optional<Listing> listingOpt = listingRepository.findById(adId);

        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Status change attempt for non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        Listing listing = listingOpt.get();

        if (!listing.getUserID().equals(userId)) {
            LoggerUtility.logError("User " + userId + " attempted to change status of listing owned by " + listing.getUserID());
            throw new InsufficientPermissionsException(userId, "Cannot change status of listing owned by another user");
        }

        listingRepository.updateStatus(adId, newStatus);
        LoggerUtility.logEvent("Listing status changed: " + adId + " to " + newStatus + " by user: " + userId);
    }
}