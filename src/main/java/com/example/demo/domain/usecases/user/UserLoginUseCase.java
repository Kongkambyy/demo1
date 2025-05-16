package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.data.util.LoggerUtility;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.InvalidCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserLoginUseCase {

    private final UserRepository userRepository;

    public UserLoginUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            LoggerUtility.logWarning("Login attempt with empty email");
            throw new InvalidCredentialsException("Email cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            LoggerUtility.logWarning("Login attempt with empty password");
            throw new InvalidCredentialsException("Password cannot be empty");
        }

        Optional<User> userOptional = userRepository.findByEmail(email.trim());

        if (userOptional.isEmpty()) {
            LoggerUtility.logWarning("Login attempt for non-existent email: " + email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOptional.get();

        // Use repository's verification method
        if (!userRepository.verifyPassword(password, user.getPassword())) {
            LoggerUtility.logWarning("Login attempt with incorrect password for email: " + email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        LoggerUtility.logEvent("Successful login for email: " + email);
        return user;
    }
}