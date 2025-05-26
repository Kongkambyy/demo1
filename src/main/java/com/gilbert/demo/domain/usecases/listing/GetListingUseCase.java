package com.gilbert.demo.domain.usecases.listing;

import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.data.repository.UserRepository;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.exceptions.listing.ListingNotFoundException;
import com.gilbert.demo.data.util.LoggerUtility;
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
            if (!listing.getUserID().equals(requestingUserId)) {
                throw new ListingNotFoundException(adId);
            }
        }

        return listing;
    }

    public List<Listing> getByUserId(String userId) {
        return listingRepository.findByUserId(userId);
    }

}