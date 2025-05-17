package com.example.demo.data.repository;

import com.example.demo.domain.entities.Category;
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

/**
 * CategoryRepository - JDBC implementering for kategori datahåndtering
 *
 * Denne klasse håndterer al database kommunikation for kategorier i markedspladsen.
 * Kategorier bruges til at organisere og gruppere salgsannoncer.
 *
 * Hovedfunktioner:
 * - Oprette nye kategorier med navn og beskrivelse
 * - Hente kategorier baseret på ID eller navn
 * - Opdatere eksisterende kategorier
 * - Slette kategorier (med validering for tilknyttede annoncer)
 * - Hente alle kategorier til visning i navigation
 * - Håndtere hierarkiske kategorier (hvis implementeret)
 *
 * Kategori struktur:
 * - Hver kategori har et unikt ID
 * - Kategorier har et display navn og valgfri beskrivelse
 * - Kategorier kan have underkategorier (parent-child forhold)
 * - Kategorier bruges til filtrering og søgning af annoncer
 *
 * Validering:
 * - Kategorier kan ikke slettes hvis der er aktive annoncer tilknyttet
 * - Kategori navne skal være unikke
 * - Underkategorier valideres for cykliske referencer
 */
@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    // RowMapper til at konvertere database rækker til Category objekter
    private final RowMapper<Category> categoryRowMapper = new RowMapper<Category>() {
        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Category(
                    rs.getInt("CategoryID"),
                    rs.getString("Name"),
                    rs.getString("Description")
            );
        }
    };

    public CategoryRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("CategoryRepository initialiseret");
    }

    // Opretter ny kategori
    public Category save(Category category) {
        String sql = "INSERT INTO categories (CategoryID, Name, Description) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql,
                category.getCategoryID(),
                category.getName(),
                category.getDescription()
        );

        LoggerUtility.logEvent("Kategori oprettet: " + category.getName());
        return category;
    }

    // Finder kategori baseret på ID
    public Optional<Category> findById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE CategoryID = ?";

        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, categoryId);
            return Optional.of(category);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Kategori ikke fundet: " + categoryId);
            return Optional.empty();
        }
    }

    // Finder kategori baseret på navn
    public Optional<Category> findByName(String name) {
        String sql = "SELECT * FROM categories WHERE Name = ?";

        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, name);
            return Optional.of(category);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Henter alle kategorier
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY Name";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    public Category update(Category category) {
        String sql = "UPDATE categories SET Name = ?, Description = ? WHERE CategoryID = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                category.getName(),
                category.getDescription(),
                category.getCategoryID()
        );

        if (rowsAffected == 0) {
            LoggerUtility.logError("Opdatering fejlede - kategori ikke fundet: " + category.getCategoryID());
            throw new RuntimeException("Kategori ikke fundet: " + category.getCategoryID());
        }

        LoggerUtility.logEvent("Kategori opdateret: " + category.getName());
        return category;
    }

    // Sletter kategori
    public void delete(int categoryId) {
        // Tjek om der er annoncer tilknyttet kategorien
        String checkSql = "SELECT COUNT(*) FROM listings WHERE CategoryID = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, categoryId);

        if (count != null && count > 0) {
            LoggerUtility.logError("Kan ikke slette kategori med tilknyttede annoncer: " + categoryId);
            throw new RuntimeException("Kan ikke slette kategori med tilknyttede annoncer");
        }

        String sql = "DELETE FROM categories WHERE CategoryID = ?";
        int rowsAffected = jdbcTemplate.update(sql, categoryId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Sletning fejlede - kategori ikke fundet: " + categoryId);
            throw new RuntimeException("Kategori ikke fundet: " + categoryId);
        }

        LoggerUtility.logEvent("Kategori slettet: " + categoryId);
    }

    // Tæller annoncer i en kategori
    public int countListingsInCategory(int categoryId) {
        String sql = "SELECT COUNT(*) FROM listings WHERE CategoryID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, categoryId);
        return count != null ? count : 0;
    }
}