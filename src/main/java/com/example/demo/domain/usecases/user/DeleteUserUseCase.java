package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.UserNotFoundException;
import com.example.demo.exceptions.user.InvalidCredentialsException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String userId, String passwordConfirmation) {
        LoggerUtility.logEvent("Account deletion initiated for user: " + userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            LoggerUtility.logError("Account deletion failed - user not found: " + userId);
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();

        if (!userRepository.verifyPassword(passwordConfirmation, user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password provided");
        }

        try {
            userRepository.delete(userId);
            LoggerUtility.logEvent("User account successfully deleted: " + userId + " (" + user.getEmail() + ")");

        } catch (Exception e) {
            LoggerUtility.logError("Error during account deletion for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Account deletion failed.", e);
        }
    }
}