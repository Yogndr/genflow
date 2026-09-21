package com.genflow.backend.controller;

import com.genflow.backend.dto.LoginRequest;
import com.genflow.backend.dto.LoginResponse;
import com.genflow.backend.dto.RegisterRequest;
import com.genflow.backend.dto.UserResponse;
import com.genflow.backend.entity.User;
import com.genflow.backend.service.JwtService;
import com.genflow.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Services used by this controller
    private final UserService userService;
    private final JwtService jwtService;

    // Spring gives us both services automatically
    public AuthController(
            UserService userService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // 1. Check email and password
        User user = userService.loginUser(request);

        // 2. Generate JWT
        String token = jwtService.generateToken(user);

        // 3. Prepare safe user information
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        // 4. Return JWT + user information
        LoginResponse response = new LoginResponse(
                token,
                userResponse
        );

        return ResponseEntity.ok(response);
    }
}