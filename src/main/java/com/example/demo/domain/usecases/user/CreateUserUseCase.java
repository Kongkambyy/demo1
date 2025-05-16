package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.domain.usecases.util.HashingUseCase;
import com.example.demo.exceptions.user.DuplicateUserException;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final HashingUseCase hashingUseCase;
    private final UserRepository userRepository;

    public CreateUserUseCase(HashingUseCase hashingUseCase, UserRepository userRepository) {
        this.hashingUseCase = hashingUseCase;
        this.userRepository = userRepository;
    }

    public User Execute(String username, String Alias, String password, String Email, String address, String Number, String UserID) {
        if (userRepository.existsByEmail(Email)) {
            throw new DuplicateUserException("A user with this account name already exists");
        }

        String hashedPassword = hashingUseCase.hashPassword(password);

        User user = new User(
                UserID,
                Alias,
                username,
                hashedPassword,
                Email,
                Number,
                address
        );
        return userRepository.save(user);
    }
}