package com.gilbert.demo.domain.usecases.listing;

import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.exceptions.listing.ListingNotFoundException;
import com.gilbert.demo.exceptions.user.InsufficientPermissionsException;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class DeleteListingUseCase {

    private final ListingRepository listingRepository;

    public DeleteListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public void execute(String userId, String adId) {
        Optional<Listing> listingOpt = listingRepository.findById(adId);

        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Delete attempt for non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        Listing listing = listingOpt.get();

        if (!listing.getUserID().equals(userId)) {
            LoggerUtility.logError("User " + userId + " attempted to delete listing owned by " + listing.getUserID());
            throw new InsufficientPermissionsException(userId, "Cannot delete listing owned by another user");
        }

        listingRepository.delete(adId);
        LoggerUtility.logEvent("Listing deleted: " + adId + " by user: " + userId);
    }
}