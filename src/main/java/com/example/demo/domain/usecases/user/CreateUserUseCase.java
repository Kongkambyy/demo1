package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.DuplicateUserException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User Execute(String username, String Alias, String password, String Email, String address, String Number, String UserID) {
        if (userRepository.existsByEmail(Email)) {
            throw new DuplicateUserException("A user with this account name already exists");
        }

        User user = new User(
                UserID,
                Alias,
                username,
                password = oneWayHashSHA256(password),
                Email,
                Number,
                address
        );
        return userRepository.save(user);

    }

    private String oneWayHashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}