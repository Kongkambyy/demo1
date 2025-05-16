package com.example.demo.domain.usecases.user;

import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.User;
import com.example.demo.exceptions.user.UserNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String userId) {
        // Tjek om bruger eksisterer
        if (!userRepository.findById(userId).isPresent()) {
            throw new UserNotFoundException("Bruger ikke fundet med ID: " + userId);
        }

        // Slet bruger
        userRepository.delete(userId);
    }
}
