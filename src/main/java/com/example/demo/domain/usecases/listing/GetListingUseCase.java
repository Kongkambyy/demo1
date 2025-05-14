package com.example.demo.domain.usecases.listing;

import com.example.demo.data.repository.ListingRepository;
import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.listing.ListingNotFoundException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
public class GetListingUseCase {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public GetListingUseCase(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    public Listing execute(String adId, String requestingUserId) {
        Optional<Listing> listingOpt = listingRepository.findById(adId);

        if (listingOpt.isEmpty()) {
            LoggerUtility.logWarning("Listing not found: " + adId);
            throw new ListingNotFoundException(adId);
        }

        Listing listing = listingOpt.get();

        if ("PENDING".equals(listing.getStatus()) || "REJECTED".equals(listing.getStatus())) {
            if (!listing.getUserID().equals(requestingUserId) && !isAdmin(requestingUserId)) {
                throw new ListingNotFoundException(adId);
            }
        }

        return listing;
    }

    public List<Listing> getByUserId(String userId) {
        return listingRepository.findByUserId(userId);
    }

    public List<Listing> getActiveListings() {
        return listingRepository.findActiveListings();
    }

    private boolean isAdmin(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        return user.getEmail() != null && user.getEmail().endsWith("@admin.com");
    }
}