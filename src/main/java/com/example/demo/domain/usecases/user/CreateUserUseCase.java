package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.DuplicateUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User Execute (String username, String Alias, String password, String Email, String address, String Number, String UserID) {
        if (userRepository.existsByEmail(Email)) {
            throw new DuplicateUserException("A user with this account name already exists");
        }

        User user = new User(
                UserID,
                Alias,
                username,
                password,
                Email,
                Number,
                address
        );
        return userRepository.save(user);
    }
}
