package com.gilbert.demo.domain.usecases.listing;

import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.exceptions.listing.ListingNotFoundException;
import com.gilbert.demo.exceptions.user.InsufficientPermissionsException;
import com.gilbert.demo.data.util.LoggerUtility;
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
            throw new ListingNotFoundException(adId);
        }

        Listing listing = listingOpt.get();

        if (!listing.getUserID().equals(userId)) {
            throw new InsufficientPermissionsException(userId, "Cannot change status of listing owned by another user");
        }

        listingRepository.updateStatus(adId, newStatus);
        LoggerUtility.logEvent("Listing status changed: " + adId + " to " + newStatus + " by user: " + userId);
    }
}