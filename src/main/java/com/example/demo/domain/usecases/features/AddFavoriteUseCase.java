package com.example.demo.domain.usecases.features;

import com.example.demo.data.repository.FavoriteRepository;
import com.example.demo.data.repository.ListingRepository;
import com.example.demo.domain.entities.Favorite;
import com.example.demo.domain.entities.Listing;
import com.example.demo.exceptions.listing.ListingNotFoundException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class AddFavoriteUseCase {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;

    public AddFavoriteUseCase(FavoriteRepository favoriteRepository, ListingRepository listingRepository) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
    }

    public Favorite execute(String userId, String listingAdId) {
        Optional<Listing> listingOpt = listingRepository.findById(listingAdId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Attempt to favorite non existent listing: " + listingAdId);
            throw new ListingNotFoundException(listingAdId);
        }

        if (favoriteRepository.isFavorite(userId, listingAdId)) {
            LoggerUtility.logWarning("Listing already in favorites: " + listingAdId + " for user: " + userId);
            throw new RuntimeException("Listing already in favorites");
        }

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Favorite favorite = new Favorite(userId, listingAdId, createdDate);
        return favoriteRepository.save(favorite);
    }
}