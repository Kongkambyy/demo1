package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.data.repository.ListingRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.domain.entities.Listing;
import com.example.demo.exceptions.user.UserNotFoundException;
import com.example.demo.exceptions.user.InsufficientPermissionsException;
import com.example.demo.exceptions.listing.ListingNotFoundException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AdminUserUseCase {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public AdminUserUseCase(UserRepository userRepository, ListingRepository listingRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    public User editUser(String adminUserId, String targetUserId, String name, String alias,
                         String email, String number, String address, String password) {

        verifyAdminPermissions(adminUserId);

        Optional<User> targetUserOpt = userRepository.findById(targetUserId);
        if (targetUserOpt.isEmpty()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to edit non-existent user: " + targetUserId);
            throw new UserNotFoundException(targetUserId);
        }

        User targetUser = targetUserOpt.get();

        User updatedUser = new User(
                alias != null ? alias : targetUser.getAlias(),
                name != null ? name : targetUser.getName(),
                password != null ? password : targetUser.getPassword(),
                email != null ? email : targetUser.getEmail(),
                number != null ? number : targetUser.getNumber(),
                address != null ? address : targetUser.getAddress()
        );

        User savedUser = userRepository.update(updatedUser);
        LoggerUtility.logEvent("Admin " + adminUserId + " updated user: " + targetUserId);

        return savedUser;
    }

    public void deleteUser(String adminUserId, String targetUserId) {
        verifyAdminPermissions(adminUserId);

        if (!userRepository.findById(targetUserId).isPresent()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to delete non-existent user: " + targetUserId);
            throw new UserNotFoundException(targetUserId);
        }

        userRepository.delete(targetUserId);
        LoggerUtility.logEvent("Admin " + adminUserId + " deleted user: " + targetUserId);
    }

    public Listing editListing(String adminUserId, String adId, String title, String description,
                               Integer price, String condition, String status, String brand) {

        verifyAdminPermissions(adminUserId);

        Optional<Listing> listingOpt = listingRepository.findById(adId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to edit non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        Listing listing = listingOpt.get();

        if (title != null) listing.setTitle(title);
        if (description != null) listing.setDescription(description);
        if (price != null) listing.setPrice(price);
        if (condition != null) listing.setCondition(condition);
        if (status != null) listing.setStatus(status);
        if (brand != null) listing.setBrand(brand);

        Listing updatedListing = listingRepository.update(listing);
        LoggerUtility.logEvent("Admin " + adminUserId + " updated listing: " + adId);

        return updatedListing;
    }

    public void deleteListing(String adminUserId, String adId) {
        verifyAdminPermissions(adminUserId);

        if (!listingRepository.findById(adId).isPresent()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to delete non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        listingRepository.delete(adId);
        LoggerUtility.logEvent("Admin " + adminUserId + " deleted listing: " + adId);
    }

    public void approveListing(String adminUserId, String adId) {
        verifyAdminPermissions(adminUserId);

        Optional<Listing> listingOpt = listingRepository.findById(adId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to approve non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        Listing listing = listingOpt.get();

        if (!"PENDING".equals(listing.getStatus())) {
            LoggerUtility.logWarning("Admin " + adminUserId + " attempted to approve non-pending listing: " + adId);
            throw new IllegalStateException("Can only approve PENDING listings");
        }

        listingRepository.updateStatus(adId, "ACTIVE");
        LoggerUtility.logEvent("Admin " + adminUserId + " approved listing: " + adId);
    }

    public void rejectListing(String adminUserId, String adId) {
        verifyAdminPermissions(adminUserId);

        Optional<Listing> listingOpt = listingRepository.findById(adId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to reject non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        Listing listing = listingOpt.get();

        if (!"PENDING".equals(listing.getStatus())) {
            LoggerUtility.logWarning("Admin " + adminUserId + " attempted to reject non-pending listing: " + adId);
            throw new IllegalStateException("Can only reject PENDING listings");
        }

        listingRepository.updateStatus(adId, "REJECTED");
        LoggerUtility.logEvent("Admin " + adminUserId + " rejected listing: " + adId);
    }

    public List<Listing> getPendingListings(String adminUserId) {
        verifyAdminPermissions(adminUserId);

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved pending listings");
        List<Listing> allListings = listingRepository.searchListings(null, null, null, null, null);
        return allListings.stream()
                .filter(listing -> "PENDING".equals(listing.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<User> getAllUsers(String adminUserId) {
        verifyAdminPermissions(adminUserId);

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved all users");
        return userRepository.findAll();
    }

    public List<Listing> getAllListings(String adminUserId) {
        verifyAdminPermissions(adminUserId);

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved all listings");
        return listingRepository.searchListings(null, null, null, null, null);
    }

    public void forceChangeListingStatus(String adminUserId, String adId, String newStatus) {
        verifyAdminPermissions(adminUserId);

        Optional<Listing> listingOpt = listingRepository.findById(adId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to change status of non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        listingRepository.updateStatus(adId, newStatus);
        LoggerUtility.logEvent("Admin " + adminUserId + " force-changed listing " + adId + " status to: " + newStatus);
    }

    private void verifyAdminPermissions(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();

        if (!isAdmin(user)) {
            LoggerUtility.logError("Non-admin user attempted admin action: " + userId);
            throw new InsufficientPermissionsException(userId, "ADMIN");
        }
    }

    private boolean isAdmin(User user) {
        return user.getEmail() != null && user.getEmail().endsWith("@admin.com");
    }
}