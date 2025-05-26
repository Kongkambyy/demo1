package com.gilbert.demo.data.repository;

import com.gilbert.demo.domain.entities.Notification;
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
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Notification> notificationRowMapper = new RowMapper<Notification>() {
        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notification notification = new Notification(
                    rs.getString("UserID"),
                    rs.getString("SourceID"),
                    rs.getString("SourceType"),
                    rs.getString("Type"),
                    rs.getString("Message"),
                    rs.getString("CreatedDate")
            );
            notification.setNotificationID(rs.getString("NotificationID"));
            notification.setRead(rs.getBoolean("IsRead"));
            return notification;
        }
    };

    public NotificationRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("NotificationRepository initialized");
    }

    public Notification save(Notification notification) {
        if (notification.getNotificationID() == null || notification.getNotificationID().isEmpty()) {
            notification.setNotificationID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO notifications (NotificationID, UserID, SourceID, SourceType, Type, Message, IsRead, CreatedDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                notification.getNotificationID(),
                notification.getUserID(),
                notification.getSourceID(),
                notification.getSourceType(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedDate()
        );

        LoggerUtility.logEvent("Notification created: " + notification.getNotificationID() + " for user: " + notification.getUserID());
        return notification;
    }

    public Optional<Notification> findById(String notificationId) {
        String sql = "SELECT * FROM notifications WHERE NotificationID = ?";

        try {
            Notification notification = jdbcTemplate.queryForObject(sql, notificationRowMapper, notificationId);
            return Optional.of(notification);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Notification not found: " + notificationId);
            return Optional.empty();
        }
    }

    public List<Notification> findByUserId(String userId) {
        String sql = "SELECT * FROM notifications WHERE UserID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, userId);
    }

    public List<Notification> findUnreadByUserId(String userId) {
        String sql = "SELECT * FROM notifications WHERE UserID = ? AND IsRead = FALSE ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, userId);
    }

    public List<Notification> findBySourceId(String sourceId) {
        String sql = "SELECT * FROM notifications WHERE SourceID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, sourceId);
    }

    public int countUnreadNotifications(String userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE UserID = ? AND IsRead = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public void markAsRead(String notificationId) {
        String sql = "UPDATE notifications SET IsRead = TRUE WHERE NotificationID = ?";
        jdbcTemplate.update(sql, notificationId);
        LoggerUtility.logEvent("Notification marked as read: " + notificationId);
    }

    public void markAllAsRead(String userId) {
        String sql = "UPDATE notifications SET IsRead = TRUE WHERE UserID = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        LoggerUtility.logEvent(rowsAffected + " notifications marked as read for user: " + userId);
    }

    public void delete(String notificationId) {
        String sql = "DELETE FROM notifications WHERE NotificationID = ?";
        int rowsAffected = jdbcTemplate.update(sql, notificationId);

        if (rowsAffected == 0) {
            LoggerUtility.logWarning("Notification not found for deletion: " + notificationId);
        } else {
            LoggerUtility.logEvent("Notification deleted: " + notificationId);
        }
    }

    public void deleteAllForUser(String userId) {
        String sql = "DELETE FROM notifications WHERE UserID = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        LoggerUtility.logEvent(rowsAffected + " notifications deleted for user: " + userId);
    }
}