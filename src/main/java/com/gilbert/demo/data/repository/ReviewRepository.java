package com.gilbert.demo.data.repository;

import com.gilbert.demo.domain.entities.Review;
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

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Review> reviewRowMapper = new RowMapper<Review>() {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Review review = new Review(
                    rs.getString("ReviewerID"),
                    rs.getString("ReviewedUserID"),
                    rs.getInt("Rating"),
                    rs.getString("Comment"),
                    rs.getString("CreatedDate")
            );
            review.setReviewID(rs.getString("ReviewID"));
            return review;
        }
    };

    public ReviewRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("ReviewRepository initialized");
    }

    public Review save(Review review) {
        if (review.getReviewID() == null || review.getReviewID().isEmpty()) {
            review.setReviewID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO reviews (ReviewID, ReviewerID, ReviewedUserID, Rating, Comment, CreatedDate) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                review.getReviewID(),
                review.getReviewerID(),
                review.getReviewedUserID(),
                review.getRating(),
                review.getComment(),
                review.getCreatedDate()
        );

        LoggerUtility.logEvent("Review created: " + review.getReviewID());
        return review;
    }

    public Optional<Review> findById(String reviewId) {
        String sql = "SELECT * FROM reviews WHERE ReviewID = ?";

        try {
            Review review = jdbcTemplate.queryForObject(sql, reviewRowMapper, reviewId);
            return Optional.of(review);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Review not found: " + reviewId);
            return Optional.empty();
        }
    }

    public List<Review> findByReviewedUserId(String userId) {
        String sql = "SELECT * FROM reviews WHERE ReviewedUserID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, reviewRowMapper, userId);
    }

    public double getAverageRatingForUser(String userId) {
        String sql = "SELECT AVG(Rating) FROM reviews WHERE ReviewedUserID = ?";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class, userId);
        return avg != null ? avg : 0.0;
    }

    public Review update(Review review) {
        String sql = "UPDATE reviews SET Rating = ?, Comment = ? WHERE ReviewID = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                review.getRating(),
                review.getComment(),
                review.getReviewID()
        );

        if (rowsAffected == 0) {
            LoggerUtility.logError("Update failed - review not found: " + review.getReviewID());
            throw new RuntimeException("Review not found: " + review.getReviewID());
        }

        LoggerUtility.logEvent("Review updated: " + review.getReviewID());
        return review;
    }

    public void delete(String reviewId) {
        String sql = "DELETE FROM reviews WHERE ReviewID = ?";
        int rowsAffected = jdbcTemplate.update(sql, reviewId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Delete failed - review not found: " + reviewId);
            throw new RuntimeException("Review not found: " + reviewId);
        }

        LoggerUtility.logEvent("Review deleted: " + reviewId);
    }
}