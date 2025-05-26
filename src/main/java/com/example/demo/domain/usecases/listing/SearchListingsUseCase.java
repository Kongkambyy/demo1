package com.example.demo.domain.usecases.listing;

import com.example.demo.data.repository.ListingRepository;
import com.example.demo.data.repository.CategoryRepository;
import com.example.demo.domain.entities.Listing;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchListingsUseCase {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;

    public SearchListingsUseCase(ListingRepository listingRepository, CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Listing> execute(String keyword, Integer minPrice, Integer maxPrice,
                                 String condition, String brand, Integer categoryId) {
        validateSearchParameters(minPrice, maxPrice);

        return listingRepository.searchListings(keyword, minPrice, maxPrice, condition, brand, categoryId);
    }

    public List<Listing> searchByCategoryHierarchy(String keyword, Integer minPrice, Integer maxPrice,
                                                   String condition, String brand, List<Integer> categoryIds) {
        validateSearchParameters(minPrice, maxPrice);

        return listingRepository.searchListingsInCategoryHierarchy(keyword, minPrice, maxPrice, condition, brand, categoryIds);
    }

    public List<Listing> searchByKeyword(String keyword) {
        return execute(keyword, null, null, null, null, null);
    }

    public List<Listing> searchByPriceRange(Integer minPrice, Integer maxPrice) {
        return execute(null, minPrice, maxPrice, null, null, null);
    }

    public List<Listing> searchByCondition(String condition) {
        return execute(null, null, null, condition, null, null);
    }

    public List<Listing> searchByBrand(String brand) {
        return execute(null, null, null, null, brand, null);
    }

    public List<Listing> searchByCategory(Integer categoryId) {
        return execute(null, null, null, null, null, categoryId);
    }

    private void validateSearchParameters(Integer minPrice, Integer maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        if (minPrice != null && minPrice < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }
    }
}