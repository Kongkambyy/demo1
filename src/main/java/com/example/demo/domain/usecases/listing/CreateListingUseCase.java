package com.example.demo.domain.usecases.listing;

import com.example.demo.data.repository.ListingRepository;
import com.example.demo.domain.entities.Listing;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

        String adId = UUID.randomUUID().toString();
        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Listing listing = new Listing(
                adId,
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