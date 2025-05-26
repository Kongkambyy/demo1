package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserLoginUseCaseTest {

    @Autowired
    private UserLoginUseCase userLoginUseCase;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String testEmail;
    private String testPassword;

    @BeforeEach
    public void setup() {
        testEmail = "test" + System.currentTimeMillis() + "@example.com";
        testPassword = "password123";

        testUser = new User(
                "Test User",
                "testuser",
                testPassword,
                testEmail,
                "12345678",
                "Test Address"
        );

        testUser = userRepository.save(testUser);
    }

    @Test
    public void testSuccessfulLogin() {
        User loggedInUser = userLoginUseCase.execute(testEmail, testPassword);

        assertNotNull(loggedInUser);
        assertEquals(testEmail, loggedInUser.getEmail());
        assertEquals(testUser.getUserID(), loggedInUser.getUserID());
    }

    @Test
    public void testLoginWithWrongPassword() {
        assertThrows(InvalidCredentialsException.class, () -> {
            userLoginUseCase.execute(testEmail, "wrongpassword");
        });
    }
}