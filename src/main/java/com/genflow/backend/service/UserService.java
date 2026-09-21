package com.genflow.backend.service;

import com.genflow.backend.dto.RegisterRequest;
import com.genflow.backend.entity.User;
import com.genflow.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.genflow.backend.exception.EmailAlreadyExistsException;
import com.genflow.backend.dto.LoginRequest;
import com.genflow.backend.exception.InvalidCredentialsException;
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
           throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public User loginUser(LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new InvalidCredentialsException("Invalid email or password");
    }

    return user;
}
}