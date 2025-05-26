package com.gilbert.demo.data.repository;

import com.gilbert.demo.domain.entities.Message;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class MessageRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Message> messageRowMapper = new RowMapper<Message>() {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Message message = new Message(
                    rs.getString("SenderID"),
                    rs.getString("ReceiverID"),
                    rs.getString("Message"),
                    rs.getString("Date")
            );
            message.setMessageID(rs.getString("MessageID"));
            message.setRead(rs.getBoolean("IsRead"));
            return message;
        }
    };

    public MessageRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("MessageRepository initialiseret");
    }

    public Message save(Message message) {
        if (message.getMessageID() == null || message.getMessageID().isEmpty()) {
            message.setMessageID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO messages (MessageID, SenderID, ReceiverID, Message, Date, IsRead) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                message.getMessageID(),
                message.getSenderID(),
                message.getReceiverID(),
                message.getMessage(),
                message.getDate(),
                message.isRead()
        );

        LoggerUtility.logEvent("Besked sendt fra " + message.getSenderID() + " til " + message.getReceiverID());
        return message;
    }

    public List<Message> findConversation(String userId1, String userId2) {
        String sql = "SELECT * FROM messages " +
                "WHERE (SenderID = ? AND ReceiverID = ?) " +
                "OR (SenderID = ? AND ReceiverID = ?) " +
                "ORDER BY Date DESC";

        return jdbcTemplate.query(sql, messageRowMapper, userId1, userId2, userId2, userId1);
    }

    public List<Message> findByUserId(String userId) {
        String sql = "SELECT * FROM messages " +
                "WHERE SenderID = ? OR ReceiverID = ? " +
                "ORDER BY Date DESC";

        return jdbcTemplate.query(sql, messageRowMapper, userId, userId);
    }

    public List<Message> findLatestMessagesByUserId(String userId) {
        String sql = "SELECT m1.* FROM messages m1 " +
                "INNER JOIN ( " +
                "  SELECT CASE " +
                "    WHEN SenderID = ? THEN ReceiverID " +
                "    ELSE SenderID " +
                "  END AS ConversationPartner, " +
                "  MAX(Date) AS MaxDate " +
                "  FROM messages " +
                "  WHERE SenderID = ? OR ReceiverID = ? " +
                "  GROUP BY ConversationPartner " +
                ") m2 ON ( " +
                "  (m1.SenderID = ? AND m1.ReceiverID = m2.ConversationPartner) OR " +
                "  (m1.ReceiverID = ? AND m1.SenderID = m2.ConversationPartner) " +
                ") AND m1.Date = m2.MaxDate";

        return jdbcTemplate.query(sql, messageRowMapper, userId, userId, userId, userId, userId);
    }

    public int countUnreadMessages(String userId) {
        String sql = "SELECT COUNT(*) FROM messages " +
                "WHERE ReceiverID = ? AND IsRead = false";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public void markAsRead(String messageId) {
        String sql = "UPDATE messages SET IsRead = true WHERE MessageID = ?";
        jdbcTemplate.update(sql, messageId);
        LoggerUtility.logEvent("Besked markeret som læst: " + messageId);
    }

    public void markConversationAsRead(String userId, String otherUserId) {
        String sql = "UPDATE messages SET IsRead = true " +
                "WHERE ReceiverID = ? AND SenderID = ?";

        jdbcTemplate.update(sql, userId, otherUserId);
        LoggerUtility.logEvent("Samtale markeret som læst for bruger: " + userId);
    }
}