package com.gilbert.demo.domain.usecases.Transactions;

import com.gilbert.demo.data.repository.TransactionRepository;
import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.data.repository.UserRepository;
import com.gilbert.demo.domain.entities.Transaction;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.exceptions.listing.ListingNotFoundException;
import com.gilbert.demo.exceptions.listing.ListingNotActiveException;
import com.gilbert.demo.exceptions.user.UserNotFoundException;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public CreateTransactionUseCase(TransactionRepository transactionRepository,
                                    ListingRepository listingRepository,
                                    UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    public Transaction execute(String buyerId, String listingId) {

        if (!userRepository.findById(buyerId).isPresent()) {
            LoggerUtility.logError("Transaction creation attempt by non-existent user: " + buyerId);
            throw new UserNotFoundException(buyerId);
        }

        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        if (listingOpt.isEmpty()) {
            LoggerUtility.logError("Transaction creation attempt for non-existent listing: " + listingId);
            throw new ListingNotFoundException(listingId);
        }

        Listing listing = listingOpt.get();

        if (!"ACTIVE".equals(listing.getStatus())) {
            LoggerUtility.logError("Transaction creation attempt for non-active listing: " + listingId);
            throw new ListingNotActiveException(listingId);
        }

        if (listing.getUserID().equals(buyerId)) {
            LoggerUtility.logError("User attempted to buy their own listing: " + buyerId);
            throw new IllegalArgumentException("Cannot buy your own listing");
        }

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Transaction transaction = new Transaction(
                listingId,
                buyerId,
                listing.getUserID(),
                listing.getPrice(),
                "COMPLETED",
                createdDate
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        listingRepository.updateStatus(listingId, "SOLD");

        LoggerUtility.logEvent("Transaction created: " + savedTransaction.getTransactionID() +
                " for listing: " + listingId + " by buyer: " + buyerId);

        return savedTransaction;
    }
}