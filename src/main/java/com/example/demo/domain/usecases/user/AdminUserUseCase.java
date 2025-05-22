package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.data.repository.ListingRepository;
import com.example.demo.data.repository.CategoryRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.entities.Category;
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
    private final CategoryRepository categoryRepository;

    public AdminUserUseCase(UserRepository userRepository, ListingRepository listingRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
    }

    // User management methods
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
                name != null ? name : targetUser.getName(),
                alias != null ? alias : targetUser.getAlias(),
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

    // Listing management methods
    public Listing editListing(String adminUserId, String adId, String title, String description,
                               Integer price, String condition, String status, String brand, Integer categoryId) {
        verifyAdminPermissions(adminUserId);

        Optional<Listing> listingOpt = listingRepository.findById(adId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to edit non-existent listing: " + adId);
            throw new ListingNotFoundException(adId);
        }

        // Validate category if specified
        if (categoryId != null && !categoryRepository.categoryExists(categoryId)) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to set invalid category ID: " + categoryId);
            throw new IllegalArgumentException("Invalid category ID: " + categoryId);
        }

        Listing listing = listingOpt.get();

        if (title != null) listing.setTitle(title);
        if (description != null) listing.setDescription(description);
        if (price != null) listing.setPrice(price);
        if (condition != null) listing.setItemCondition(condition);
        if (status != null) listing.setStatus(status);
        if (brand != null) listing.setBrand(brand);
        if (categoryId != null) listing.setCategoryID(categoryId);

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

    // Category management methods - using direct DB queries
    public Category createCategory(String adminUserId, String name, String description, Integer parentId) {
        verifyAdminPermissions(adminUserId);

        // Validate parent category if specified
        if (parentId != null && !categoryRepository.categoryExists(parentId)) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to create category with invalid parent: " + parentId);
            throw new IllegalArgumentException("Invalid parent category ID: " + parentId);
        }

        Category category = new Category(0, name, description);
        category.setParentID(parentId);

        Category savedCategory = categoryRepository.save(category);
        LoggerUtility.logEvent("Admin " + adminUserId + " created category: " + name);

        return savedCategory;
    }

    public Category updateCategory(String adminUserId, int categoryId, String name, String description, Integer parentId) {
        verifyAdminPermissions(adminUserId);

        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to update non-existent category: " + categoryId);
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        // Prevent circular references in category hierarchy
        if (parentId != null) {
            if (parentId == categoryId) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            // Check if the parent ID would create a cycle in the hierarchy
            if (categoryId != 0) {
                List<Integer> descendantIds = categoryRepository.findAllDescendantCategoryIds(categoryId);
                if (descendantIds.contains(parentId)) {
                    throw new IllegalArgumentException("Cannot set a descendant as parent (would create a cycle)");
                }
            }
        }

        Category category = categoryOpt.get();

        if (name != null) category.setName(name);
        if (description != null) category.setDescription(description);
        category.setParentID(parentId);

        Category updatedCategory = categoryRepository.update(category);
        LoggerUtility.logEvent("Admin " + adminUserId + " updated category: " + categoryId);

        return updatedCategory;
    }

    public void deleteCategory(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);

        // Check if category exists
        if (!categoryRepository.categoryExists(categoryId)) {
            LoggerUtility.logError("Admin " + adminUserId + " attempted to delete non-existent category: " + categoryId);
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        try {
            categoryRepository.delete(categoryId);
            LoggerUtility.logEvent("Admin " + adminUserId + " deleted category: " + categoryId);
        } catch (RuntimeException e) {
            LoggerUtility.logError("Admin " + adminUserId + " failed to delete category: " + e.getMessage());
            throw e;
        }
    }

    // Category query methods - direct database access
    public List<Category> getAllCategories(String adminUserId) {
        verifyAdminPermissions(adminUserId);
        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved all categories");
        return categoryRepository.findAll();
    }

    public List<Category> getMainCategories(String adminUserId) {
        verifyAdminPermissions(adminUserId);
        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved main categories");
        return categoryRepository.findMainCategories();
    }

    public List<Category> getSubcategories(String adminUserId, int parentId) {
        verifyAdminPermissions(adminUserId);

        // Validate parent exists
        if (!categoryRepository.categoryExists(parentId)) {
            throw new IllegalArgumentException("Category not found: " + parentId);
        }

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved subcategories for category: " + parentId);
        return categoryRepository.findSubcategories(parentId);
    }

    public boolean hasSubcategories(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);

        // Validate category exists
        if (!categoryRepository.categoryExists(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        List<Category> subcategories = categoryRepository.findSubcategories(categoryId);
        return !subcategories.isEmpty();
    }

    public Optional<Category> getCategoryById(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);
        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved category: " + categoryId);
        return categoryRepository.findById(categoryId);
    }

    public Optional<Category> getParentCategory(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);

        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        Category category = categoryOpt.get();
        Integer parentId = category.getParentID();

        if (parentId == null) {
            return Optional.empty(); // This is a main category with no parent
        }

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved parent for category: " + categoryId);
        return categoryRepository.findById(parentId);
    }

    public int countListingsInCategory(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);

        // Validate category exists
        if (!categoryRepository.categoryExists(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        LoggerUtility.logEvent("Admin " + adminUserId + " counted listings in category: " + categoryId);
        return categoryRepository.countListingsInCategory(categoryId);
    }

    public int countListingsInCategoryHierarchy(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);

        // Validate category exists
        if (!categoryRepository.categoryExists(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        // Get all descendant category IDs (including the category itself)
        List<Integer> categoryIds = categoryRepository.findAllDescendantCategoryIds(categoryId);

        // Count listings in all categories
        int total = 0;
        for (Integer id : categoryIds) {
            total += categoryRepository.countListingsInCategory(id);
        }

        LoggerUtility.logEvent("Admin " + adminUserId + " counted listings in category hierarchy: " + categoryId);
        return total;
    }

    // Listing query methods
    public List<Listing> getPendingListings(String adminUserId) {
        verifyAdminPermissions(adminUserId);

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved pending listings");
        List<Listing> allListings = listingRepository.searchListings(null, null, null, null, null, null);
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
        return listingRepository.searchListings(null, null, null, null, null, null);
    }

    public List<Listing> getListingsByCategory(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);

        // Validate category exists
        if (!categoryRepository.categoryExists(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved listings for category: " + categoryId);
        return listingRepository.searchListings(null, null, null, null, null, categoryId);
    }

    public List<Listing> getListingsByCategoryHierarchy(String adminUserId, int categoryId) {
        verifyAdminPermissions(adminUserId);

        // Validate category exists
        if (!categoryRepository.categoryExists(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }

        // Get all descendant category IDs
        List<Integer> categoryIds = categoryRepository.findAllDescendantCategoryIds(categoryId);

        LoggerUtility.logEvent("Admin " + adminUserId + " retrieved listings for category hierarchy: " + categoryId);
        return listingRepository.searchListingsInCategoryHierarchy(null, null, null, null, null, categoryIds);
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