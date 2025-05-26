package com.example.demo.domain.usecases.features;

import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.domain.entities.Review;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CreateReviewUseCase {

    private final ReviewRepository reviewRepository;

    public CreateReviewUseCase(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review execute(String reviewerID, String reviewedUserID, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (reviewerID.equals(reviewedUserID)) {
            throw new IllegalArgumentException("Users cannot review themselves");
        }

        String createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Review review = new Review(reviewerID, reviewedUserID, rating, comment, createdDate);
        return reviewRepository.save(review);
    }
}