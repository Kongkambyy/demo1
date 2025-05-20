package com.example.demo.data.repository;

import com.example.demo.domain.entities.Listing;
import com.example.demo.exceptions.listing.ListingNotFoundException;
import com.example.demo.exceptions.listing.ListingNotActiveException;
import com.example.demo.exceptions.listing.ListingAlreadySoldException;
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

/**
 * ListingRepository - JDBC implementering for annoncer/salgsopslag datahåndtering
 *
 * Denne klasse håndterer al database kommunikation for salgsannoncer i markedspladsen.
 * Den fungerer som data access layer for alle salgsobjekter i systemet.
 *
 * Hovedfunktioner:
 * - Oprette nye salgsannoncer med alle relaterede detaljer
 * - Hente annoncer baseret på forskellige søgekriterier
 * - Opdatere eksisterende annonce
 * - Slette annoncer når de ikke længere er relevante
 * - Søge og filtrere annoncer efter forskellige parametre
 * - Håndtere annonce (aktiv, solgt, reserveret)
 *
 * Søgefunktionalitet:
 * - Søgning efter pris interval
 * - Filtrering på kategori
 * - Søgning på brand
 * - Filtrering på tilstand (ny, brugt, slidt)
 * - Sortering efter dato eller pris
 *
 * Fejlhåndtering:
 * - ListingNotFoundException: Når en annonce ikke findes
 * - ListingNotActiveException: Når man forsøger at interagere med inaktiv annonce
 * - ListingAlreadySoldException: Når man forsøger at købe/ændre solgt annonce
 *
 * Sikkerhedsovervejelser:
 * - Kun ejeren kan redigere/slette egne annoncer
 * - Statusændringer valideres for at undgå ugyldige tilstande
 */
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
                    rs.getString("Condition"),
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
        LoggerUtility.logEvent("ListingRepository initialiseret");
    }

    public Listing save(Listing listing) {
        if (listing.getAdID() == null || listing.getAdID().isEmpty()) {
            listing.setAdID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO listings (AdID, UserID, Title, Description, Price, CreatedDate, Condition, Status, Brand, CategoryID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                listing.getAdID(),
                listing.getUserID(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getCreatedDate(),
                listing.getCondition(),
                listing.getStatus(),
                listing.getBrand(),
                listing.getCategoryID()
        );

        LoggerUtility.logEvent("Ny annonce oprettet: " + listing.getAdID());
        return listing;
    }

    // Finder annonce baseret på ID
    public Optional<Listing> findById(String adId) {
        String sql = "SELECT * FROM listings WHERE AdID = ?";

        try {
            Listing listing = jdbcTemplate.queryForObject(sql, listingRowMapper, adId);
            return Optional.of(listing);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Annonce ikke fundet: " + adId);
            return Optional.empty();
        }
    }

    // Finder alle annoncer for en bruger
    public List<Listing> findByUserId(String userId) {
        String sql = "SELECT * FROM listings WHERE UserID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, listingRowMapper, userId);
    }

    // Finder aktive annoncer
    public List<Listing> findActiveListings() {
        String sql = "SELECT * FROM listings WHERE Status = 'ACTIVE' ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, listingRowMapper);
    }

    // Opdaterer annonce
    public Listing update(Listing listing) {
        String sql = "UPDATE listings SET Title = ?, Description = ?, Price = ?, " +
                "Condition = ?, Status = ?, Brand = ?, CategoryID = ? WHERE AdID = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getCondition(),
                listing.getStatus(),
                listing.getBrand(),
                listing.getCategoryID(),
                listing.getAdID()
        );


        if (rowsAffected == 0) {
            LoggerUtility.logError("Opdatering fejlede - annonce ikke fundet: " + listing.getAdID());
            throw new ListingNotFoundException("Annonce ikke fundet: " + listing.getAdID());
        }

        LoggerUtility.logEvent("Annonce opdateret: " + listing.getAdID());
        return listing;
    }

    // Opdaterer status på annonce
    public void updateStatus(String adId, String status) {
        // Validér nuværende status først
        Optional<Listing> currentListing = findById(adId);
        if (currentListing.isEmpty()) {
            throw new ListingNotFoundException("Annonce ikke fundet: " + adId);
        }

        Listing listing = currentListing.get();

        // Tjek for ugyldige statusovergange
        if ("SOLD".equals(listing.getStatus())) {
            throw new ListingAlreadySoldException("Annonce er allerede solgt: " + adId);
        }

        if (!"ACTIVE".equals(listing.getStatus())) {
            throw new ListingNotActiveException("Annonce er ikke aktiv: " + adId);
        }

        String sql = "UPDATE listings SET Status = ? WHERE AdID = ?";
        jdbcTemplate.update(sql, status, adId);

        LoggerUtility.logEvent("Annonce status opdateret: " + adId + " - Ny status: " + status);
    }

    // Sletter annonce
    public void delete(String adId) {
        String sql = "DELETE FROM listings WHERE AdID = ?";

        int rowsAffected = jdbcTemplate.update(sql, adId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Sletning fejlede - annonce ikke fundet: " + adId);
            throw new ListingNotFoundException("Annonce ikke fundet: " + adId);
        }

        LoggerUtility.logEvent("Annonce slettet: " + adId);
    }

    // Søger annoncer med filtre
    public List<Listing> searchListings(String keyword, Integer minPrice, Integer maxPrice,
                                        String condition, String brand) {
        StringBuilder sql = new StringBuilder("SELECT * FROM listings WHERE Status = 'ACTIVE'");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (Title LIKE ? OR Description LIKE ?)");
        }
        if (minPrice != null) {
            sql.append(" AND Price >= ?");
        }
        if (maxPrice != null) {
            sql.append(" AND Price <= ?");
        }
        if (condition != null && !condition.isEmpty()) {
            sql.append(" AND Condition = ?");
        }
        if (brand != null && !brand.isEmpty()) {
            sql.append(" AND Brand = ?");
        }

        sql.append(" ORDER BY CreatedDate DESC");

        // Bygger parameter array dynamisk
        List<Object> params = new java.util.ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            String searchPattern = "%" + keyword + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        if (minPrice != null) {
            params.add(minPrice);
        }
        if (maxPrice != null) {
            params.add(maxPrice);
        }
        if (condition != null && !condition.isEmpty()) {
            params.add(condition);
        }
        if (brand != null && !brand.isEmpty()) {
            params.add(brand);
        }

        return jdbcTemplate.query(sql.toString(), listingRowMapper, params.toArray());
    }
}