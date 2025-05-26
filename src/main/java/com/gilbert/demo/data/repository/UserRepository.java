package com.gilbert.demo.data.repository;

import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.exceptions.user.UserNotFoundException;
import com.gilbert.demo.exceptions.user.DuplicateUserException;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Repository
@Transactional
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User(
                    rs.getString("Name"),
                    rs.getString("Alias"),
                    rs.getString("Password"),
                    rs.getString("Email"),
                    rs.getString("Number"),
                    rs.getString("Address")
            );
            user.setUserID(rs.getString("UserID"));
            return user;
        }
    };

    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("UserRepository initialized");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            LoggerUtility.logError("SHA256 algorithm not found: " + e.getMessage());
            throw new RuntimeException("SHA256 algorithm not found", e);
        }
    }

    public boolean verifyPassword(String plainPassword, String storedPassword) {
        String hashedPassword = hashPassword(plainPassword);
        return hashedPassword.equals(storedPassword);
    }

    private boolean isPasswordHashed(String password) {
        return password != null && password.length() >= 40 && !password.contains(" ");
    }

    public User save(User user) {
        if (user.getUserID() == null || user.getUserID().isEmpty()) {
            user.setUserID(UUID.randomUUID().toString());
        }

        String hashedPassword;
        if (!isPasswordHashed(user.getPassword())) {
            hashedPassword = hashPassword(user.getPassword());
        } else {
            hashedPassword = user.getPassword();
        }

        String sql = "INSERT INTO users (UserID, Name, Alias, Password, Email, Number, Address) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    user.getUserID(),
                    user.getName(),
                    user.getAlias(),
                    hashedPassword,
                    user.getEmail(),
                    user.getNumber(),
                    user.getAddress()
            );
            LoggerUtility.logEvent("User created: " + user.getEmail());

            User savedUser = new User(
                    user.getName(),
                    user.getAlias(),
                    hashedPassword,
                    user.getEmail(),
                    user.getNumber(),
                    user.getAddress()
            );
            savedUser.setUserID(user.getUserID());
            return savedUser;
        } catch (DuplicateKeyException e) {
            LoggerUtility.logError("Duplicate user creation attempt: " + user.getEmail());
            throw new DuplicateUserException(user.getEmail());
        } catch (DataAccessException e) {
            LoggerUtility.logError("Database error during user creation: " + e.getMessage());
            throw new RuntimeException("Couldnt register user", e);
        }
    }

    public Optional<User> findById(String userId) {
        String sql = "SELECT * FROM users WHERE UserID = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("User not found with ID: " + userId);
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE Email = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public User update(User user) {
        String passwordToUse;
        if (!isPasswordHashed(user.getPassword())) {
            passwordToUse = hashPassword(user.getPassword());
        } else {
            passwordToUse = user.getPassword();
        }

        String sql = "UPDATE users SET Name = ?, Alias = ?, Password = ?, Email = ?, Number = ?, Address = ? WHERE UserID = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getName(),
                user.getAlias(),
                passwordToUse,
                user.getEmail(),
                user.getNumber(),
                user.getAddress(),
                user.getUserID()
        );

        if (rowsAffected == 0) {
            LoggerUtility.logError("Update failed for: " + user.getUserID());
            throw new UserNotFoundException(user.getUserID());
        }

        LoggerUtility.logEvent("User updated: " + user.getUserID());

        User updatedUser = new User(
                user.getName(),
                user.getAlias(),
                passwordToUse,
                user.getEmail(),
                user.getNumber(),
                user.getAddress()
        );
        return updatedUser;
    }

    public void delete(String userId) {
        try {
            LoggerUtility.logEvent("Starting cascading delete for user: " + userId);

            String deleteNotifications = "DELETE FROM notifications WHERE UserID = ?";
            int notificationsDeleted = jdbcTemplate.update(deleteNotifications, userId);

            String deleteMessages = "DELETE FROM messages WHERE SenderID = ? OR ReceiverID = ?";
            int messagesDeleted = jdbcTemplate.update(deleteMessages, userId, userId);

            String deleteFavorites = "DELETE FROM favorites WHERE UserID = ?";
            int favoritesDeleted = jdbcTemplate.update(deleteFavorites, userId);

            String deleteReviews = "DELETE FROM reviews WHERE ReviewerID = ? OR ReviewedUserID = ?";
            int reviewsDeleted = jdbcTemplate.update(deleteReviews, userId, userId);

            String deleteTransactions = "DELETE FROM transactions WHERE BuyerID = ? OR SellerID = ?";
            int transactionsDeleted = jdbcTemplate.update(deleteTransactions, userId, userId);

            String deleteOffers = "DELETE FROM offers WHERE BuyerID = ? OR SellerID = ?";
            int offersDeleted = jdbcTemplate.update(deleteOffers, userId, userId);

            String deleteFavoritesForUserListings =
                    "DELETE FROM favorites WHERE ListingAdID IN (SELECT AdID FROM listings WHERE UserID = ?)";
            int listingFavoritesDeleted = jdbcTemplate.update(deleteFavoritesForUserListings, userId);

            String deleteListings = "DELETE FROM listings WHERE UserID = ?";
            int listingsDeleted = jdbcTemplate.update(deleteListings, userId);

            String deleteUser = "DELETE FROM users WHERE UserID = ?";
            int rowsAffected = jdbcTemplate.update(deleteUser, userId);

            if (rowsAffected == 0) {
                throw new UserNotFoundException(userId);
            }

            LoggerUtility.logEvent("User and all related data successfully deleted: " + userId);

        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to delete user account due to database error", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user account", e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE Email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public boolean verifyUserCredentials(String email, String plainPassword) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        return verifyPassword(plainPassword, userOpt.get().getPassword());
    }
}
