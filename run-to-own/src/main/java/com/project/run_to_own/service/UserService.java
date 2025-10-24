package com.project.run_to_own.service;

import com.project.run_to_own.model.Athlete;
import com.project.run_to_own.model.User;
import com.project.run_to_own.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findOrCreateUser(Athlete athlete) {
        return userRepository.findById(athlete.getId()).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(athlete.getId());
            String name = athlete.getUsername();
            if (name == null || name.isBlank()) {
                name = athlete.getFirstname() + " " + athlete.getLastname();
            }
            newUser.setUsername(name.trim());
            return userRepository.save(newUser);
        });
    }
}
