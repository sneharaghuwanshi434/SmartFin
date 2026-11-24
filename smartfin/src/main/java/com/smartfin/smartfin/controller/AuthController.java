
package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.LoginRequest;
import com.smartfin.smartfin.dto.RegisterRequest;
import com.smartfin.smartfin.entity.User;
import com.smartfin.smartfin.service.JwtService;
import com.smartfin.smartfin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword()
        );

        Map<String, String> response = Map.of(
                "message", "User registered successfully",
                "fullName", user.getFullName(),
                "email", user.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        User user = userService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user);

        Map<String, Object> response = Map.of(
                "message", "Login successful",
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "tokenType", "Bearer",
                "accessToken", token
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userService.getUserByEmail(email);

        Map<String, String> response = Map.of(
                "message", "Authenticated successfully",
                "fullName", user.getFullName(),
                "email", user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
