package com.example.demo.data.repository;

import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.UserNotFoundException;
import com.example.demo.exceptions.user.DuplicateUserException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserRepository - JDBC implementering for bruger datahåndtering
 *
 * Denne klasse håndterer al database kommunikation relateret til brugere i systemet.
 * Den fungerer som et abstraktionslag mellem domænelogikken og den fysiske database.
 *
 * Hovedfunktioner:
 * - Oprette nye brugere i databasen med unik validering på email
 * - Hente brugeroplysninger baseret på forskellige kriterier (ID, email)
 * - Opdatere eksisterende brugerdata
 * - Slette brugere fra systemet
 * - Validere brugereksistens
 * - Håndtere login-validering med email og password
 *
 * Klassen bruger Spring's JdbcTemplate for at udføre SQL forespørgsler og
 * implementerer RowMapper interfacet for at mappe database rækker til User objekter.
 *
 * Fejlhåndtering:
 * - Kaster UserNotFoundException når en bruger ikke findes
 * - Kaster DuplicateUserException når en bruger med samme email allerede eksisterer
 * - Logger alle vigtige operationer og fejl via LoggerUtility
 *
 * Sikkerhed:
 * - Passwords gemmes aldrig i klartekst (forventes at være hashed før lagring)
 * - Bruger parameteriserede SQL queries for at undgå SQL injection
 */

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    // RowMapper til at konvertere database rækker til User objekter
    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(
                    rs.getString("UserID"),
                    rs.getString("Alias"),
                    rs.getString("Name"),
                    rs.getString("Password"),
                    rs.getString("Email"),
                    rs.getString("Number"),
                    rs.getString("Address")
            );
        }
    };

    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("UserRepository initialiseret");
    }

    // Opretter ny bruger i databasen
    public User save(User user) {
        // Generer UserID hvis det ikke er sat
        if (user.getUserID() == null || user.getUserID().isEmpty()) {
            user.setUserID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO users (UserID, Name, Password, Email, Number, Address) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    user.getUserID(),
                    user.getName(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getNumber(),
                    user.getAddress()
            );
            LoggerUtility.logEvent("Bruger oprettet: " + user.getEmail());
            return user;
        } catch (DuplicateKeyException e) {
            LoggerUtility.logError("Duplikat bruger forsøgt oprettet: " + user.getEmail());
            throw new DuplicateUserException(user.getEmail());
        } catch (DataAccessException e) {
            LoggerUtility.logError("Database fejl ved oprettelse af bruger: " + e.getMessage());
            throw new RuntimeException("Kunne ikke oprette bruger", e);
        }
    }

    // Finder bruger baseret på ID
    public Optional<User> findById(String userId) {
        String sql = "SELECT * FROM users WHERE UserID = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Bruger ikke fundet med ID: " + userId);
            return Optional.empty();
        }
    }

    // Finder bruger baseret på email
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE Email = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Opdaterer eksisterende bruger
    public User update(User user) {
        String sql = "UPDATE users SET Name = ?, Password = ?, Email = ?, Number = ?, Address = ? WHERE UserID = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getName(),
                user.getPassword(),
                user.getEmail(),
                user.getNumber(),
                user.getAddress(),
                user.getUserID()
        );

        if (rowsAffected == 0) {
            LoggerUtility.logError("Opdatering fejlede - bruger ikke fundet: " + user.getUserID());
            throw new UserNotFoundException(user.getUserID());
        }

        LoggerUtility.logEvent("Bruger opdateret: " + user.getUserID());
        return user;
    }

    // Sletter bruger
    public void delete(String userId) {
        String sql = "DELETE FROM users WHERE UserID = ?";

        int rowsAffected = jdbcTemplate.update(sql, userId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Sletning fejlede - bruger ikke fundet: " + userId);
            throw new UserNotFoundException(userId);
        }

        LoggerUtility.logEvent("Bruger slettet: " + userId);
    }

    // Finder alle brugere
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    // Tjekker om bruger eksisterer - HER ER DEN!
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE Email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}