package com.gilbert.demo.domain.usecases.user;

import com.gilbert.demo.data.repository.UserRepository;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.exceptions.user.UserNotFoundException;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByIdOrThrow(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            LoggerUtility.logWarning("User not found: " + userId);
            throw new UserNotFoundException(userId);
        }
        return userOpt.get();
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}