package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.UserNotFoundException;
import com.example.demo.exceptions.user.DuplicateUserException;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UpdateUserUseCase {

    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(String userId, String name, String alias, String email,
                        String number, String address, String password) {

        Optional<User> existingUserOpt = userRepository.findById(userId);
        if (existingUserOpt.isEmpty()) {
            LoggerUtility.logError("Update attempt for non-existent user: " + userId);
            throw new UserNotFoundException(userId);
        }

        User existingUser = existingUserOpt.get();

        // Check if email is being changed and if it's already in use by another user
        if (email != null && !email.trim().isEmpty() && !email.equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                LoggerUtility.logError("Update attempt with duplicate email: " + email);
                throw new DuplicateUserException("Email already in use: " + email);
            }
        }

        // Create updated user object with new values or keep existing ones
        User updatedUser = new User(
                name != null && !name.trim().isEmpty() ? name.trim() : existingUser.getName(),
                alias != null && !alias.trim().isEmpty() ? alias.trim() : existingUser.getAlias(),
                password != null && !password.trim().isEmpty() ? password : existingUser.getPassword(),
                email != null && !email.trim().isEmpty() ? email.trim() : existingUser.getEmail(),
                number != null && !number.trim().isEmpty() ? number.trim() : existingUser.getNumber(),
                address != null && !address.trim().isEmpty() ? address.trim() : existingUser.getAddress()
        );

        // Set the user ID for the update
        updatedUser.setUserID(userId);

        User savedUser = userRepository.update(updatedUser);
        LoggerUtility.logEvent("User updated successfully: " + userId);

        return savedUser;
    }

    public User executePartialUpdate(String userId, User updates) {
        return execute(
                userId,
                updates.getName(),
                updates.getAlias(),
                updates.getEmail(),
                updates.getNumber(),
                updates.getAddress(),
                updates.getPassword()
        );
    }
}