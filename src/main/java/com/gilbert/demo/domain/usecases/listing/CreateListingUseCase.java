package com.gilbert.demo.domain.usecases.listing;

import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CreateListingUseCase {

    private final ListingRepository listingRepository;

    public CreateListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public Listing execute(String userId, String title, String description, int price,
                           String condition, String brand) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition cannot be empty");
        }

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Listing listing = new Listing(
                userId,
                title.trim(),
                description.trim(),
                price,
                createdDate,
                condition.trim(),
                "PENDING"
        );

        if (brand != null && !brand.trim().isEmpty()) {
            listing.setBrand(brand.trim());
        }

        Listing savedListing = listingRepository.save(listing);
        LoggerUtility.logEvent("New listing created: " + savedListing.getAdID() + " by user: " + userId);

        return savedListing;
    }
}