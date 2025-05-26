package com.example.demo.domain.usecases.Transactions;

import com.example.demo.data.repository.TransactionRepository;
import com.example.demo.data.repository.ListingRepository;
import com.example.demo.domain.entities.Transaction;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CompleteTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final ListingRepository listingRepository;

    public CompleteTransactionUseCase(TransactionRepository transactionRepository,
                                      ListingRepository listingRepository) {
        this.transactionRepository = transactionRepository;
        this.listingRepository = listingRepository;
    }

    public Transaction execute(String transactionId, String userId) {

        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            LoggerUtility.logError("Completion attempt for non-existent transaction: " + transactionId);
            throw new RuntimeException("Transaction not found: " + transactionId);
        }

        Transaction transaction = transactionOpt.get();

        if (!transaction.getSellerID().equals(userId) && !transaction.getBuyerID().equals(userId)) {
            LoggerUtility.logError("User " + userId + " attempted to complete transaction they're not part of: " + transactionId);
            throw new RuntimeException("Insufficient permissions to complete this transaction");
        }

        if (!"PENDING".equals(transaction.getStatus())) {
            LoggerUtility.logError("Attempt to complete transaction with status: " + transaction.getStatus());
            throw new IllegalStateException("Only PENDING transactions can be completed");
        }

        String completedDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Transaction completedTransaction = transactionRepository.updateStatus(
                transactionId, "COMPLETED", completedDate);

        listingRepository.updateStatus(transaction.getListingID(), "SOLD");

        LoggerUtility.logEvent("Transaction completed: " + transactionId + " by user: " + userId);

        return completedTransaction;
    }
}