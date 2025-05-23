package com.example.demo.data.repository;

import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.DuplicateUserException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

// Bruges til at rulle DB tilbage igen efter test. På den måde slipper vi for at testen ændre data
@Transactional
class UserRepositoryTest {

// AutoInjekt af nødvendige pakker som JDBC, SpringBoot.
    @Autowired
    private UserRepository userRepository;

    // Laver en bruger med de nødvendige data
    private User createTestUser() {
        return new User(
                "234523435",
                "Test The Tester",
                "TestUser",
                "TopMegaHemmeligKode",
                "test@example.com",
                "12345678"
        );
    }

    // gemmer en bruger i databasen og tester om man kan finde den med id
    @Test
    void testSaveAndFindById() {
        User user = createTestUser();
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getUserID());

        assertTrue(found.isPresent());
        assertEquals(saved.getEmail(), found.get().getEmail());
    }

    // Tester om der bliver kastet en DublicateUserException, hvis man prøver at oprette en bruger med samme main
    @Test
    void testDuplicateEmailThrowsException() {
        User user1 = createTestUser();
        userRepository.save(user1);

        User user2 = createTestUser();
        user2.setEmail(user1.getEmail()); // Samme email

        assertThrows(DuplicateUserException.class, () -> userRepository.save(user2));
    }

    // Tester om man kan opdatere brugers Alias Og deres Adresse
    @Test
    void testUpdate() {
        User user = createTestUser();
        User saved = userRepository.save(user);

        saved.setAlias("NyAlias");
        saved.setAddress("Ny Adresse 123");
        User updated = userRepository.update(saved);

        assertEquals("NyAlias", updated.getAlias());
        assertEquals("Ny Adresse 123", updated.getAddress());
    }

    // Laver en bruger, gemmer den, sletter den og tjekker om brugeren er blevet slettet
    @Test
    void testDelete() {
        User user = createTestUser();
        User saved = userRepository.save(user);
        userRepository.delete(saved.getUserID());

        Optional<User> found = userRepository.findById(saved.getUserID());
        assertTrue(found.isEmpty());
    }

    // Opretter Testbruger, Gemmer Testbrugeren,
    @Test
    void testExistsByEmail() {
        User user = createTestUser();
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail(user.getEmail()));
        assertFalse(userRepository.existsByEmail("test@example.com"));
    }

}
