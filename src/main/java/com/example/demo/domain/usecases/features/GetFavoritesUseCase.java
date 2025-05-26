package com.example.demo.domain.usecases.features;

import com.example.demo.data.repository.FavoriteRepository;
import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.entities.Favorite;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetFavoritesUseCase {

    private final FavoriteRepository favoriteRepository;

    public GetFavoritesUseCase(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<Favorite> getFavoritesByUserId(String userId) {
        return favoriteRepository.findByUserId(userId);
    }

    public List<Listing> getFavoriteListings(String userId) {
        return favoriteRepository.findFavoriteListings(userId);
    }

    public boolean isListingFavorite(String userId, String listingAdId) {
        return favoriteRepository.isFavorite(userId, listingAdId);
    }
}