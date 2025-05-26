package com.example.demo.data.repository;

import com.example.demo.domain.entities.Transaction;
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
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> transactionRowMapper = new RowMapper<Transaction>() {
        @Override
        public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            Transaction transaction = new Transaction(
                    rs.getString("ListingID"),
                    rs.getString("BuyerID"),
                    rs.getString("SellerID"),
                    rs.getInt("Amount"),
                    rs.getString("Status"),
                    rs.getString("CreatedDate")
            );
            transaction.setTransactionID(rs.getString("TransactionID"));
            transaction.setCompletedDate(rs.getString("CompletedDate"));
            return transaction;
        }
    };

    public TransactionRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("TransactionRepository initialized");
    }

    public Transaction save(Transaction transaction) {
        if (transaction.getTransactionID() == null || transaction.getTransactionID().isEmpty()) {
            transaction.setTransactionID(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO transactions (TransactionID, ListingID, BuyerID, SellerID, Amount, Status, CreatedDate, CompletedDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                transaction.getTransactionID(),
                transaction.getListingID(),
                transaction.getBuyerID(),
                transaction.getSellerID(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedDate(),
                transaction.getCompletedDate()
        );

        LoggerUtility.logEvent("Transaction created: " + transaction.getTransactionID());
        return transaction;
    }

    public Optional<Transaction> findById(String transactionId) {
        String sql = "SELECT * FROM transactions WHERE TransactionID = ?";

        try {
            Transaction transaction = jdbcTemplate.queryForObject(sql, transactionRowMapper, transactionId);
            return Optional.of(transaction);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Transaction not found: " + transactionId);
            return Optional.empty();
        }
    }

    public List<Transaction> findByBuyerId(String buyerId) {
        String sql = "SELECT * FROM transactions WHERE BuyerID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, transactionRowMapper, buyerId);
    }

    public List<Transaction> findBySellerId(String sellerId) {
        String sql = "SELECT * FROM transactions WHERE SellerID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, transactionRowMapper, sellerId);
    }

    public List<Transaction> findByListingId(String listingId) {
        String sql = "SELECT * FROM transactions WHERE ListingID = ? ORDER BY CreatedDate DESC";
        return jdbcTemplate.query(sql, transactionRowMapper, listingId);
    }

    public Transaction updateStatus(String transactionId, String newStatus, String completedDate) {
        String sql = "UPDATE transactions SET Status = ?, CompletedDate = ? WHERE TransactionID = ?";

        int rowsAffected = jdbcTemplate.update(sql, newStatus, completedDate, transactionId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Update failed - transaction not found: " + transactionId);
            throw new RuntimeException("Transaction not found: " + transactionId);
        }

        Optional<Transaction> updatedTransaction = findById(transactionId);
        LoggerUtility.logEvent("Transaction status updated: " + transactionId + " to " + newStatus);

        return updatedTransaction.orElseThrow(() -> new RuntimeException("Could not find updated transaction"));
    }

    public void delete(String transactionId) {
        String sql = "DELETE FROM transactions WHERE TransactionID = ?";
        int rowsAffected = jdbcTemplate.update(sql, transactionId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Delete failed - transaction not found: " + transactionId);
            throw new RuntimeException("Transaction not found: " + transactionId);
        }

        LoggerUtility.logEvent("Transaction deleted: " + transactionId);
    }
}