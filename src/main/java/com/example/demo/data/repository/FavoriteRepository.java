package com.example.demo.data.repository;

import com.example.demo.domain.entities.Favorite;
import com.example.demo.domain.entities.Listing;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FavoriteRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ListingRepository listingRepository;

    private final RowMapper<Favorite> favoriteRowMapper = new RowMapper<Favorite>() {
        @Override
        public Favorite mapRow(ResultSet rs, int rowNum) throws SQLException {
            Favorite favorite = new Favorite(
                    rs.getString("UserID"),
                    rs.getString("ListingAdID"),
                    rs.getString("CreatedDate")
            );
            favorite.setFavoriteID(rs.getString("FavoriteID"));
            return favorite;
        }
    };

    public FavoriteRepository(DataSource dataSource, ListingRepository listingRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.listingRepository = listingRepository;
        LoggerUtility.logEvent("FavoriteRepository initialized");
    }

    // Add a listing to favorites
    public Favorite save(Favorite favorite) {
        if (favorite.getFavoriteID() == null || favorite.getFavoriteID().isEmpty()) {
            favorite.setFavoriteID(UUID.randomUUID().toString());
        }

        try {
            String sql = "INSERT INTO favorites (FavoriteID, UserID, ListingAdID, CreatedDate) " +
                    "VALUES (?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    favorite.getFavoriteID(),
                    favorite.getUserID(),
                    favorite.getListingAdID(),
                    favorite.getCreatedDate()
            );

            LoggerUtility.logEvent("Listing added to favorites: " + favorite.getListingAdID() + " by user: " + favorite.getUserID());
            return favorite;
        } catch (DuplicateKeyException e) {
            LoggerUtility.logWarning("Listing already in favorites: " + favorite.getListingAdID() + " for user: " + favorite.getUserID());
            throw new RuntimeException("Listing already in favorites");
        }
    }

    // Find if a listing is in user's favorites
    public boolean isFavorite(String userId, String listingAdId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE UserID = ? AND ListingAdID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, listingAdId);
        return count != null && count > 0;
    }

    // Get all favorites for a user
    public List<Favorite> findByUserId(String userId) {
        String sql = "SELECT * FROM favorites WHERE UserID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, favoriteRowMapper, userId);
    }

    // Get all favorite listings for a user
    public List<Listing> findFavoriteListings(String userId) {
        String sql = "SELECT ListingAdID FROM favorites WHERE UserID = ?";
        List<String> listingIds = jdbcTemplate.queryForList(sql, String.class, userId);

        return listingIds.stream()
                .map(listingId -> listingRepository.findById(listingId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(java.util.stream.Collectors.toList());
    }

    // Remove a listing from favorites
    public void delete(String userId, String listingAdId) {
        String sql = "DELETE FROM favorites WHERE UserID = ? AND ListingAdID = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId, listingAdId);

        if (rowsAffected > 0) {
            LoggerUtility.logEvent("Listing removed from favorites: " + listingAdId + " by user: " + userId);
        }
    }

    // Remove all favorites for a listing (e.g., when listing is deleted)
    public void deleteByListingId(String listingAdId) {
        String sql = "DELETE FROM favorites WHERE ListingAdID = ?";
        jdbcTemplate.update(sql, listingAdId);
        LoggerUtility.logEvent("All favorites removed for listing: " + listingAdId);
    }

    public List<String> findUsersByListingId(String listingId) {
        String sql = "SELECT UserID FROM favorites WHERE ListingAdID = ?";
        return jdbcTemplate.queryForList(sql, String.class, listingId);
    }
}