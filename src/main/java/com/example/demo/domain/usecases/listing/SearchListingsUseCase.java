package com.example.demo.domain.usecases.listing;

import com.example.demo.data.repository.ListingRepository;
import com.example.demo.domain.entities.Listing;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchListingsUseCase {

    private final ListingRepository listingRepository;

    public SearchListingsUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<Listing> execute(String keyword, Integer minPrice, Integer maxPrice,
                                 String condition, String brand) {

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        if (minPrice != null && minPrice < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }

        LoggerUtility.logEvent("Search performed with params - keyword: " + keyword +
                ", minPrice: " + minPrice + ", maxPrice: " + maxPrice +
                ", condition: " + condition + ", brand: " + brand);

        List<Listing> results = listingRepository.searchListings(keyword, minPrice, maxPrice, condition, brand);

        return results.stream()
                .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Listing> searchByKeyword(String keyword) {
        return execute(keyword, null, null, null, null);
    }

    public List<Listing> searchByPriceRange(Integer minPrice, Integer maxPrice) {
        return execute(null, minPrice, maxPrice, null, null);
    }

    public List<Listing> searchByCondition(String condition) {
        return execute(null, null, null, condition, null);
    }

    public List<Listing> searchByBrand(String brand) {
        return execute(null, null, null, null, brand);
    }
}