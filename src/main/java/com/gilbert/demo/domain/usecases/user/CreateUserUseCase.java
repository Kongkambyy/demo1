package com.gilbert.demo.domain.usecases.user;

import com.gilbert.demo.data.repository.UserRepository;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.exceptions.user.DuplicateUserException;
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