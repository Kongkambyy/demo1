package com.example.demo.data.repository;

import com.example.demo.domain.entities.Offer;
import com.example.demo.data.util.LoggerUtility;
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

@Repository
public class OfferRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Offer> offerRowMapper = new RowMapper<Offer>() {
        @Override
        public Offer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Offer offer = new Offer(
                    rs.getString("ListingID"),
                    rs.getString("BuyerID"),
                    rs.getString("SellerID"),
                    rs.getInt("OriginalPrice"),
                    rs.getInt("OfferAmount"),
                    rs.getString("Status"),
                    rs.getString("CreatedDate"),
                    rs.getString("ExpiryDate")
            );
            offer.setOfferID(rs.getString("OfferID"));
            return offer;
        }
    };

    public OfferRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("OfferRepository initialized");
    }

    // Create a new offer
    public Offer save(Offer offer) {
        if (offer.getOfferID() == null || offer.getOfferID().isEmpty()) {
            offer.setOfferID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO offers (OfferID, ListingID, BuyerID, SellerID, OriginalPrice, OfferAmount, Status, CreatedDate, ExpiryDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                offer.getOfferID(),
                offer.getListingID(),
                offer.getBuyerID(),
                offer.getSellerID(),
                offer.getOriginalPrice(),
                offer.getOfferAmount(),
                offer.getStatus(),
                offer.getCreatedDate(),
                offer.getExpiryDate()
        );

        LoggerUtility.logEvent("Offer created: " + offer.getOfferID() + " for listing: " + offer.getListingID());
        return offer;
    }

    // Find offer by ID
    public Optional<Offer> findById(String offerId) {
        String sql = "SELECT * FROM offers WHERE OfferID = ?";

        try {
            Offer offer = jdbcTemplate.queryForObject(sql, offerRowMapper, offerId);
            return Optional.of(offer);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Offer not found: " + offerId);
            return Optional.empty();
        }
    }

    // Find offers by buyer ID
    public List<Offer> findByBuyerId(String buyerId) {
        String sql = "SELECT * FROM offers WHERE BuyerID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, offerRowMapper, buyerId);
    }

    // Find offers by seller ID
    public List<Offer> findBySellerId(String sellerId) {
        String sql = "SELECT * FROM offers WHERE SellerID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, offerRowMapper, sellerId);
    }

    // Find offers by listing ID
    public List<Offer> findByListingId(String listingId) {
        String sql = "SELECT * FROM offers WHERE ListingID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, offerRowMapper, listingId);
    }

    // Find pending offers by listing ID
    public List<Offer> findPendingOffersByListingId(String listingId) {
        String sql = "SELECT * FROM offers WHERE ListingID = ? AND Status = 'PENDING' ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, offerRowMapper, listingId);
    }

    // Update offer status
    public Offer updateStatus(String offerId, String newStatus) {
        String sql = "UPDATE offers SET Status = ? WHERE OfferID = ?";

        int rowsAffected = jdbcTemplate.update(sql, newStatus, offerId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Update failed - offer not found: " + offerId);
            throw new RuntimeException("Offer not found: " + offerId);
        }

        Optional<Offer> updatedOffer = findById(offerId);
        LoggerUtility.logEvent("Offer status updated: " + offerId + " to " + newStatus);

        return updatedOffer.orElseThrow(() -> new RuntimeException("Could not find updated offer"));
    }

    // Count active offers for a listing
    public int countPendingOffersForListing(String listingId) {
        String sql = "SELECT COUNT(*) FROM offers WHERE ListingID = ? AND Status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, listingId);
        return count != null ? count : 0;
    }
}