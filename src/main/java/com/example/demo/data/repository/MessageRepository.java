package com.example.demo.data.repository;

import com.example.demo.domain.entities.Message;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * MessageRepository - JDBC implementering for besked/chat datahåndtering
 *
 * Denne klasse håndterer al database kommunikation for beskeder mellem brugere.
 * Den implementerer funktionalitet for intern kommunikation i markedspladsen.
 *
 * Hovedfunktioner:
 * - Sende beskeder mellem brugere (køber og sælger)
 * - Hente beskedhistorik for samtaler
 * - Markere beskeder som læst/ulæst
 * - Hente alle samtaler for en bruger
 * - Slette beskeder (hvis tilladt)
 * - Håndtere notifikationer for nye beskeder
 *
 * Samtale funktioner:
 * - Gruppere beskeder i samtaler mellem to brugere
 * - Hente seneste besked i hver samtale
 * - Tælle ulæste beskeder
 * - Sortere samtaler efter seneste aktivitet
 *
 * Sikkerhedsovervejelser:
 * - Brugere kan kun se beskeder de er afsender eller modtager af
 * - Beskeder kan ikke redigeres efter afsendelse
 * - Slettede beskeder forbliver i databasen med slettet-markering
 *
 * Relations håndtering:
 * - Hver besked er knyttet til én afsender og én modtager
 * - Beskeder er typisk relateret til en specifik annonce/handel
 */
@Repository
public class MessageRepository {

    private final JdbcTemplate jdbcTemplate;

    // RowMapper til at konvertere database rækker til Message objekter
    private final RowMapper<Message> messageRowMapper = new RowMapper<Message>() {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Message(
                    rs.getString("SenderID"),
                    rs.getString("ReceiverID"),
                    rs.getString("Message"),
                    rs.getString("Date")
            );
        }
    };

    public MessageRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("MessageRepository initialiseret");
    }

    // Sender ny besked
    public Message save(Message message) {
        if (message.getMessageID() == null || message.getMessageID().isEmpty()) {
            message.setMessageID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO messages (MessageID, SenderID, ReceiverID, Message, Date) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                message.getMessageID(),
                message.getSenderID(),
                message.getReceiverID(),
                message.getMessage(),
                message.getDate()
        );

        LoggerUtility.logEvent("Besked sendt fra " + message.getSenderID() + " til " + message.getReceiverID());
        return message;
    }

    // Henter beskeder mellem to brugere
    public List<Message> findConversation(String userId1, String userId2) {
        String sql = "SELECT * FROM messages " +
                "WHERE (SenderID = ? AND ReceiverID = ?) " +
                "OR (SenderID = ? AND ReceiverID = ?) " +
                "ORDER BY Date DESC";

        return jdbcTemplate.query(sql, messageRowMapper, userId1, userId2, userId2, userId1);
    }

    // Henter alle beskeder for en bruger
    public List<Message> findByUserId(String userId) {
        String sql = "SELECT * FROM messages " +
                "WHERE SenderID = ? OR ReceiverID = ? " +
                "ORDER BY Date DESC";

        return jdbcTemplate.query(sql, messageRowMapper, userId, userId);
    }

    // Henter seneste besked i hver samtale for en bruger
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

    // Tæller ulæste beskeder for en bruger
    public int countUnreadMessages(String userId) {
        String sql = "SELECT COUNT(*) FROM messages " +
                "WHERE ReceiverID = ? AND IsRead = false";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // Markerer besked som læst
    public void markAsRead(String messageId) {
        String sql = "UPDATE messages SET IsRead = true WHERE MessageID = ?";
        jdbcTemplate.update(sql, messageId);
        LoggerUtility.logEvent("Besked markeret som læst: " + messageId);
    }

    // Markerer alle beskeder i en samtale som læst
    public void markConversationAsRead(String userId, String otherUserId) {
        String sql = "UPDATE messages SET IsRead = true " +
                "WHERE ReceiverID = ? AND SenderID = ?";

        jdbcTemplate.update(sql, userId, otherUserId);
        LoggerUtility.logEvent("Samtale markeret som læst for bruger: " + userId);
    }
}