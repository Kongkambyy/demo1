package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.DuplicateUserException;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User Execute(String name, String alias, String password, String email, String number, String address) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("A user with this email already exists");
        }

        User user = new User(
                name,
                alias,
                password,
                email,
                number,
                address
        );


        return userRepository.save(user);
    }
}