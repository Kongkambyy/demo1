package com.gilbert.demo.domain.usecases.listing;

import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.exceptions.listing.ListingNotFoundException;
import com.gilbert.demo.exceptions.user.InsufficientPermissionsException;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UpdateListingUseCase {

    private final ListingRepository listingRepository;

    public UpdateListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public Listing execute(String userId, String adId, String title, String description,
                           Integer price, String ItemCondition, String brand) {

        Optional<Listing> existingListingOpt = listingRepository.findById(adId);

        if (existingListingOpt.isEmpty()) {
            LoggerUtility.logError("Update attempt for non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        Listing existingListing = existingListingOpt.get();

        if (!existingListing.getUserID().equals(userId)) {
            throw new InsufficientPermissionsException(userId, "Cannot edit listing owned by another user");
        }

        if (title != null && !title.trim().isEmpty()) {
            existingListing.setTitle(title.trim());
        }

        if (description != null && !description.trim().isEmpty()) {
            existingListing.setDescription(description.trim());
        }

        if (price != null && price > 0) {
            existingListing.setPrice(price);
        }

        if (ItemCondition != null && !ItemCondition.trim().isEmpty()) {
            existingListing.setItemCondition(ItemCondition.trim());
        }

        if (brand != null) {
            existingListing.setBrand(brand.trim());
        }

        Listing updatedListing = listingRepository.update(existingListing);
        LoggerUtility.logEvent("Listing updated: " + adId + " by user: " + userId);

        return updatedListing;
    }
}