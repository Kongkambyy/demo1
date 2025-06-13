package com.gilbert.demo.domain.usecases.features.transactions;

import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.domain.entities.Offer;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.data.repository.UserRepository;
import com.gilbert.demo.domain.usecases.Transactions.MakeOfferUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MakeOfferDemoTest {

    @Autowired
    private MakeOfferUseCase makeOfferUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    private User buyer;
    private User seller;
    private Listing expensiveWatch;

    @BeforeEach
    void setupTestData() {
        // Opret en køber
        buyer = new User(
                "Peter Hansen",
                "peter_buyer",
                "password123",
                "peter" + System.currentTimeMillis() + "@email.dk",
                "12345678",
                "Roskilde, Danmark"
        );
        buyer = userRepository.save(buyer);

        // Opret en sælger
        seller = new User(
                "Maria Jensen",
                "maria_seller",
                "password123",
                "maria" + System.currentTimeMillis() + "@email.dk",
                "87654321",
                "København, Danmark"
        );
        seller = userRepository.save(seller);

        // Opret et dyrt ur til salg
        expensiveWatch = new Listing(
                seller.getUserID(),
                "Rolex Submariner",
                "Ægte Rolex ur i perfekt stand",
                50000, // 50.000 DKK
                "01/12/2024",
                "EXCELLENT",
                "ACTIVE"
        );
        expensiveWatch.setBrand("Rolex");
        expensiveWatch.setCategoryID(1); // Sæt til MEN kategori (ID=1)
        expensiveWatch = listingRepository.save(expensiveWatch);
    }

    @Test
    void demoValidOffer() {
        // Arrange - Test data er allerede sat op i @BeforeEach
        int offerAmount = 35000; // 70% af 50.000 DKK

        // Act - Peter laver et tilbud på Marias Rolex
        Offer result = makeOfferUseCase.execute(
                buyer.getUserID(),
                expensiveWatch.getAdID(),
                offerAmount
        );

        // Assert - Tjek at tilbuddet blev oprettet korrekt
        assertNotNull(result, "Tilbuddet skulle være oprettet");
        assertEquals("PENDING", result.getStatus(), "Nye tilbud starter som PENDING");
        assertEquals(35000, result.getOfferAmount(), "Tilbudsbeløbet skal være korrekt");
        assertEquals(50000, result.getOriginalPrice(), "Original pris skal gemmes");
        assertEquals(buyer.getUserID(), result.getBuyerID(), "Køber ID skal være korrekt");
        assertEquals(seller.getUserID(), result.getSellerID(), "Sælger ID skal være korrekt");

        System.out.println("✅ SUCCESS: Peter's tilbud på " + offerAmount + " DKK blev accepteret!");
    }

    @Test
    void demoOfferTooLow() {
        // Arrange
        int lowOffer = 30000; // Under 70% (35.000 DKK)

        // Act & Assert - Dette skulle fejle
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> makeOfferUseCase.execute(buyer.getUserID(), expensiveWatch.getAdID(), lowOffer),
                "Tilbud under 70% skulle være afvist"
        );

        assertTrue(exception.getMessage().contains("70"),
                "Fejlmeddelelsen skulle nævne 70% reglen");

        System.out.println("✅ SUCCESS: For lavt tilbud blev korrekt afvist!");
    }

    @Test
    void demoCantBidOnOwnItem() {
        // Act & Assert - Sælger prøver at byde på sit eget ur
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> makeOfferUseCase.execute(seller.getUserID(), expensiveWatch.getAdID(), 35000),
                "Man kan ikke byde på sine egne ting"
        );

        assertTrue(exception.getMessage().contains("own listing"),
                "Fejlmeddelelsen skulle forklare problemet");

        System.out.println("✅ SUCCESS: Selvbud blev korrekt blokeret!");
    }
}
