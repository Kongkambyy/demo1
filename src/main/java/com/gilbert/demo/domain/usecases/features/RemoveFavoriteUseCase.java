package com.gilbert.demo.domain.usecases.features;

import com.gilbert.demo.data.repository.FavoriteRepository;
import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

@Service
public class RemoveFavoriteUseCase {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;

    public RemoveFavoriteUseCase(FavoriteRepository favoriteRepository, ListingRepository listingRepository) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
    }

    public void execute(String userId, String listingAdId) {
        if (!listingRepository.findById(listingAdId).isPresent()) {
            LoggerUtility.logWarning("Attempt to remove favorite for non-existent listing: " + listingAdId);
            throw new RuntimeException("Listing not found: " + listingAdId);
        }

        if (!favoriteRepository.isFavorite(userId, listingAdId)) {
            LoggerUtility.logWarning("Listing not in favorites: " + listingAdId + " for user: " + userId);
            throw new RuntimeException("Listing not in favorites");
        }

        favoriteRepository.delete(userId, listingAdId);
        LoggerUtility.logEvent("Favorite removed: " + listingAdId + " for user: " + userId);
    }
}