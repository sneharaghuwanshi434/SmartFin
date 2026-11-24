
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.entity.User;
import com.smartfin.smartfin.exception.InvalidCredentialsException;
import com.smartfin.smartfin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    public User registerUser(String fullName, String email, String password) {

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();

        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password")
                );

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return user;
    }

    public User getUserByEmail(String email) {

        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new InvalidCredentialsException("User not found")
                );
    }
}
