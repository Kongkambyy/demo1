package com.gilbert.demo.domain.usecases.user;

import com.gilbert.demo.data.repository.UserRepository;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.exceptions.user.UserNotFoundException;
import com.gilbert.demo.exceptions.user.DuplicateUserException;
import com.gilbert.demo.data.util.LoggerUtility;
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

        if (email != null && !email.trim().isEmpty() && !email.equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                LoggerUtility.logError("Update attempt with duplicate email: " + email);
                throw new DuplicateUserException("Email already in use: " + email);
            }
        }

        User updatedUser = new User(
                name != null && !name.trim().isEmpty() ? name.trim() : existingUser.getName(),
                alias != null && !alias.trim().isEmpty() ? alias.trim() : existingUser.getAlias(),
                password != null && !password.trim().isEmpty() ? password : existingUser.getPassword(),
                email != null && !email.trim().isEmpty() ? email.trim() : existingUser.getEmail(),
                number != null && !number.trim().isEmpty() ? number.trim() : existingUser.getNumber(),
                address != null && !address.trim().isEmpty() ? address.trim() : existingUser.getAddress()
        );

        updatedUser.setUserID(userId);

        User savedUser = userRepository.update(updatedUser);
        LoggerUtility.logEvent("User updated successfully: " + userId);

        return savedUser;
    }
}