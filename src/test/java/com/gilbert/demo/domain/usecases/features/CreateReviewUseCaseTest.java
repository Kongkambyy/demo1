package com.gilbert.demo.domain.usecases.features;

import com.gilbert.demo.domain.entities.Review;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CreateReviewUseCaseTest {

    @Autowired
    private CreateReviewUseCase createReviewUseCase;

    @Autowired
    private UserRepository userRepository;

    private User reviewer;
    private User reviewedUser;

    @BeforeEach
    void setup() {
        reviewer = new User(
                "Reviewer",
                "reviewer",
                "password123",
                "reviewer" + System.currentTimeMillis() + "@example.com",
                "12345678",
                "Address 1"
        );
        reviewer = userRepository.save(reviewer);

        reviewedUser = new User(
                "Reviewed User",
                "reviewed",
                "password123",
                "reviewed" + System.currentTimeMillis() + "@example.com",
                "87654321",
                "Address 2"
        );
        reviewedUser = userRepository.save(reviewedUser);
    }

    @Test
    void testExecuteValidReview() {
        Review review = createReviewUseCase.execute(
                reviewer.getUserID(),
                reviewedUser.getUserID(),
                5,
                "test!"
        );

        assertNotNull(review);
        assertEquals(5, review.getRating());
        assertEquals("test", review.getComment());
        assertNotNull(review.getCreatedDate());
    }

    @Test
    void testInvalidRating() {
        assertThrows(IllegalArgumentException.class, () -> {
            createReviewUseCase.execute(
                    reviewer.getUserID(),
                    reviewedUser.getUserID(),
                    0,
                    "Test"
            );
        });

        assertThrows(IllegalArgumentException.class, () -> {
            createReviewUseCase.execute(
                    reviewer.getUserID(),
                    reviewedUser.getUserID(),
                    6,
                    "Test"
            );
        });
    }

    @Test
    void testUserCannotReviewThemselves() {
        assertThrows(IllegalArgumentException.class, () -> {
            createReviewUseCase.execute(
                    reviewer.getUserID(),
                    reviewer.getUserID(),
                    5,
                    "Cannot review myself"
            );
        });
    }
}