package com.gilbert.demo.data.repository;

import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.exceptions.listing.ListingNotFoundException;
import com.gilbert.demo.exceptions.listing.ListingNotActiveException;
import com.gilbert.demo.exceptions.listing.ListingAlreadySoldException;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

@Repository
public class ListingRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Listing> listingRowMapper = new RowMapper<Listing>() {
        @Override
        public Listing mapRow(ResultSet rs, int rowNum) throws SQLException {
            Listing listing = new Listing(
                    rs.getString("UserID"),
                    rs.getString("Title"),
                    rs.getString("Description"),
                    rs.getInt("Price"),
                    rs.getString("CreatedDate"),
                    rs.getString("ItemCondition"),
                    rs.getString("Status")
            );
            listing.setAdID(rs.getString("AdID"));
            listing.setBrand(rs.getString("Brand"));
            listing.setCategoryID(rs.getInt("CategoryID"));
            return listing;
        }
    };

    public ListingRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("ListingRepository initialized");
    }

    public Listing save(Listing listing) {
        if (listing.getAdID() == null || listing.getAdID().isEmpty()) {
            listing.setAdID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO listings (AdID, UserID, Title, Description, Price, CreatedDate, ItemCondition, Status, Brand, CategoryID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                listing.getAdID(),
                listing.getUserID(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getCreatedDate(),
                listing.getItemCondition(),
                listing.getStatus(),
                listing.getBrand(),
                listing.getCategoryID()
        );

        LoggerUtility.logEvent("New listing created: " + listing.getAdID());
        return listing;
    }

    public Optional<Listing> findById(String adId) {
        String sql = "SELECT * FROM listings WHERE AdID = ?";

        try {
            Listing listing = jdbcTemplate.queryForObject(sql, listingRowMapper, adId);
            return Optional.of(listing);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Listing not found: " + adId);
            return Optional.empty();
        }
    }

    public List<Listing> findByUserId(String userId) {
        String sql = "SELECT * FROM listings WHERE UserID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, listingRowMapper, userId);
    }

    public List<Listing> findActiveListings() {
        String sql = "SELECT * FROM listings WHERE Status = 'ACTIVE' ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, listingRowMapper);
    }

    public Listing update(Listing listing) {
        String sql = "UPDATE listings SET Title = ?, Description = ?, Price = ?, " +
                "ItemCondition = ?, Status = ?, Brand = ?, CategoryID = ? WHERE AdID = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getItemCondition(),
                listing.getStatus(),
                listing.getBrand(),
                listing.getCategoryID(),
                listing.getAdID()
        );

        if (rowsAffected == 0) {
            LoggerUtility.logError("Update failed - listing not found: " + listing.getAdID());
            throw new ListingNotFoundException("Listing not found: " + listing.getAdID());
        }

        LoggerUtility.logEvent("Listing updated: " + listing.getAdID());
        return listing;
    }

    public void updateStatus(String adId, String status) {
        // Validate current status first
        Optional<Listing> currentListing = findById(adId);
        if (currentListing.isEmpty()) {
            throw new ListingNotFoundException("Listing not found: " + adId);
        }

        Listing listing = currentListing.get();

        if ("SOLD".equals(listing.getStatus())) {
            throw new ListingAlreadySoldException("Listing is already sold: " + adId);
        }

        if (!"ACTIVE".equals(listing.getStatus()) && !"PENDING".equals(status)) {
            throw new ListingNotActiveException("Listing is not active: " + adId);
        }

        String sql = "UPDATE listings SET Status = ? WHERE AdID = ?";
        jdbcTemplate.update(sql, status, adId);

        LoggerUtility.logEvent("Listing status updated: " + adId + " - New status: " + status);
    }

    public void delete(String adId) {
        String sql = "DELETE FROM listings WHERE AdID = ?";

        int rowsAffected = jdbcTemplate.update(sql, adId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Delete failed - listing not found: " + adId);
            throw new ListingNotFoundException("Listing not found: " + adId);
        }

        LoggerUtility.logEvent("Listing deleted: " + adId);
    }

    public List<Listing> searchListings(String keyword, Integer minPrice, Integer maxPrice,
                                        String condition, String brand, Integer categoryId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM listings WHERE Status = 'ACTIVE'");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (Title LIKE ? OR Description LIKE ?)");
            String searchPattern = "%" + keyword + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (minPrice != null) {
            sql.append(" AND Price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND Price <= ?");
            params.add(maxPrice);
        }

        if (condition != null && !condition.isEmpty()) {
            sql.append(" AND ItemCondition = ?");
            params.add(condition);
        }

        if (brand != null && !brand.isEmpty()) {
            sql.append(" AND Brand = ?");
            params.add(brand);
        }

        if (categoryId != null) {
            sql.append(" AND CategoryID = ?");
            params.add(categoryId);
        }

        sql.append(" ORDER BY CreatedDate DESC");

        return jdbcTemplate.query(sql.toString(), listingRowMapper, params.toArray());
    }

    public List<Listing> searchListingsInCategoryHierarchy(String keyword, Integer minPrice, Integer maxPrice,
                                                           String condition, String brand, List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return searchListings(keyword, minPrice, maxPrice, condition, brand, null);
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM listings WHERE Status = 'ACTIVE'");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (Title LIKE ? OR Description LIKE ?)");
            String searchPattern = "%" + keyword + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (minPrice != null) {
            sql.append(" AND Price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND Price <= ?");
            params.add(maxPrice);
        }

        if (condition != null && !condition.isEmpty()) {
            sql.append(" AND ItemCondition = ?");
            params.add(condition);
        }

        if (brand != null && !brand.isEmpty()) {
            sql.append(" AND Brand = ?");
            params.add(brand);
        }

        sql.append(" AND CategoryID IN (");
        for (int i = 0; i < categoryIds.size(); i++) {
            sql.append(i > 0 ? ", ?" : "?");
            params.add(categoryIds.get(i));
        }
        sql.append(")");

        sql.append(" ORDER BY CreatedDate DESC");

        return jdbcTemplate.query(sql.toString(), listingRowMapper, params.toArray());
    }
}